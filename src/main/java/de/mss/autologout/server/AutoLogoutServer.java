package de.mss.autologout.server;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mss.autologout.param.AutoLogoutCounter;
import de.mss.autologout.param.CheckCounterResponse;
import de.mss.autologout.param.GetCounterResponse;
import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceServer;
import de.mss.utils.DateTimeTools;
import de.mss.utils.exception.MssException;

public class AutoLogoutServer extends WebServiceServer {

   private static final String            DB_BASE_KEY            = "autologout.";
   private static final String CMD_OPTION_CONFIG_FILE = "config";
   public static final String  CFG_KEY_BASE           = "de.mss.autologout";
   private static final String CFG_KEY_DB_FILE        = CFG_KEY_BASE + ".dbfile";

   private static final String DB_KEY_DAILY_MINUTES   = ".minutes.daily";
   private static final String DB_KEY_WEEKLY_MINUTES  = ".minutes.weekly";

   private static final SimpleDateFormat  DB_DATE_FORMAT         = new SimpleDateFormat("yyyyMMdd");
   private static final SimpleDateFormat  DB_DATETIME_FORMAT     = new SimpleDateFormat("yyyyMMddHHmmssSSS");

   private Map<String, AutoLogoutCounter> counterMap             = new HashMap<>();

   private static Logger logger = null;


   public static Logger getLogger() {
      if (logger == null) {
         logger = LogManager.getLogger(AutoLogoutServer.class);
      }

      return logger;
   }


   private ConfigFile dbFile = null;


   public AutoLogoutServer(ConfigFile c) {
      super(c);
   }


   public ConfigFile getDbFile() {
      return this.dbFile;
   }


   public AutoLogoutServer(ConfigFile c, Integer p) {
      super(c, p);
   }


   @Override
   protected void initApplication() {
      this.dbFile = new XmlConfigFile(getConfigFile().getValue(CFG_KEY_DB_FILE, "autologout.db"));
   }


   @Override
   protected void shutDown() {
      stopServer();
   }


   @Override
   protected Map<String, WebService> getServiceList() {
      Map<String, WebService> ret = loadWebServices(this.getClass().getClassLoader(), "de.mss.autologout.server.rest");

      for (Entry<String, WebService> entry : ret.entrySet()) {
         if (entry.getValue() instanceof AutoLogoutWebService)
            ((AutoLogoutWebService)entry.getValue()).setAutoLogoutServer(this);
      }

      return ret;
   }


   private static String getLocalIp(CommandLine cmd) {
      if (cmd.hasOption("ip"))
         return cmd.getOptionValue("ip");

      return "localhost";
   }


   public void addToCounter(String userName, int checkInterval) {
      if (!this.counterMap.containsKey(userName))
         loadUser(userName);

      LogoutCounter dailyCounter = this.counterMap.get(userName).getDailyCounter();
      LogoutCounter weeklyCounter = this.counterMap.get(userName).getWeeklyCounter();

      if (weeklyCounter != null && weeklyCounter.getMaxMinutes() > 0)
         weeklyCounter.addSeconds(checkInterval);

      if (dailyCounter != null && dailyCounter.getMaxMinutes() > 0) {
         dailyCounter.addSeconds(checkInterval);
         this.dbFile
               .insertKeyValue(DB_BASE_KEY + userName + ".D" + DB_DATE_FORMAT.format(new java.util.Date()), "" + dailyCounter.getCurrentMinutes());
         try {
            this.dbFile.writeConfig(getConfigFile().getValue(CFG_KEY_DB_FILE, "autologout.db"));
         }
         catch (IOException e) {
            getLogger().error("Error while updating db", e);
         }
      }
   }


   public CheckCounterResponse checkCounter(String userName, int checkInterval) {
      CheckCounterResponse ret = null;

      if (!this.counterMap.containsKey(userName))
         loadUser(userName);

      ret = checkCounter(this.counterMap.get(userName).getWeeklyCounter(), checkInterval);
      if (ret != null)
         return ret;

      ret = checkCounter(this.counterMap.get(userName).getDailyCounter(), checkInterval);
      if (ret != null)
         return ret;

      return new CheckCounterResponse(Boolean.FALSE, "", "");
   }


   public GetCounterResponse getCounter(String userName) {
      GetCounterResponse resp = new GetCounterResponse();

      if (!this.counterMap.containsKey(userName))
         loadUser(userName);

      resp.setCounterValues(this.counterMap.get(userName).getCounterValues());

      return resp;
   }


   private CheckCounterResponse checkCounter(LogoutCounter lc, int checkInterval) {
      if (lc == null)
         return null;

      if (lc.isForceLogoff()) {
         return new CheckCounterResponse(Boolean.TRUE, "Info", "Deine " + lc.getName() + " Zeit ist abgelaufen, Du wirst automatisch abgemeldet");
      } else if (lc.isMinutesUntilForceLogoff()) {
         return new CheckCounterResponse(
               Boolean.FALSE,
               "Info",
               "Deine " + lc.getName() + " Zeit ist in " + lc.getMinutesUntilFoceLogoff() + " Minuten abgelaufen");
      } else if (lc.isFirstWarning(checkInterval)) {
         return new CheckCounterResponse(
               Boolean.FALSE,
               "Info",
               "Deine " + lc.getName() + " Zeit l�uft in " + (lc.getMinutesForceLogoff() - lc.getMinutesFirstWarning()) + " Minuten ab");
      } else if (lc.isMinutesReached(checkInterval)) {
         return new CheckCounterResponse(Boolean.FALSE, "Info", "Deine " + lc.getName() + " Zeit ist abgelaufen");
      } else if (lc.isSecondInfo(checkInterval)) {
         return new CheckCounterResponse(
               Boolean.FALSE,
               "Info",
               "Deine " + lc.getName() + " Zeit l�uft in " + lc.getMinutesSecondInfo() + " Minuten ab");
      } else if (lc.isFirstInfo(checkInterval)) {
         return new CheckCounterResponse(
               Boolean.FALSE,
               "Info",
               "Deine " + lc.getName() + " Zeit l�uft in " + lc.getMinutesFirstInfo() + " Minuten ab");
      }

      return null;
   }


   private void loadUser(String userName) {
      if (this.counterMap.containsKey(userName))
         return;

      LogoutCounter dailyCounter = new LogoutCounter(
            getConfigFile().getValue(CFG_KEY_BASE + "." + userName + DB_KEY_DAILY_MINUTES, BigInteger.valueOf(30)).intValue(),
            "tägliche");
      LogoutCounter weeklyCounter = new LogoutCounter(
            getConfigFile().getValue(CFG_KEY_BASE + "." + userName + DB_KEY_WEEKLY_MINUTES, BigInteger.valueOf(240)).intValue(),
            "wöchentliche");

      java.util.Date checkDate = new java.util.Date();

      AutoLogoutCounter alc = new AutoLogoutCounter();
      alc.setCounterValues(new java.util.TreeMap<>());

      int minutesDaily = this.dbFile.getValue(DB_BASE_KEY + userName + ".D" + DB_DATE_FORMAT.format(checkDate), BigInteger.ZERO).intValue();
      int minutesWeekly = minutesDaily;
      for (int i = 1; i <= 7; i++ ) {
         try {
            checkDate = DateTimeTools.addDate(checkDate, -1, Calendar.DAY_OF_MONTH);
         }
         catch (MssException e) {
            getLogger().error("Error while loading user '" + userName + "'", e);
         }
         minutesWeekly += this.dbFile.getValue(DB_BASE_KEY + userName + ".D" + DB_DATE_FORMAT.format(checkDate), BigInteger.ZERO).intValue();
         alc
               .getCounterValues()
               .put(
                     DB_DATE_FORMAT.format(checkDate),
                     this.dbFile.getValue(DB_BASE_KEY + userName + ".D" + DB_DATE_FORMAT.format(checkDate), BigInteger.ZERO));
      }

      dailyCounter.addMinutes(minutesDaily);
      weeklyCounter.addMinutes(minutesWeekly);

      alc.setDailycounter(dailyCounter);
      alc.setWeeklyCounter(weeklyCounter);

      this.counterMap.put(userName, alc);
   }


   private static Integer getPort(CommandLine cmd, Logger log) {
      if (cmd.hasOption("port"))
         try {
            return new Integer(cmd.getOptionValue("port"));
         }
         catch (NumberFormatException nfe) {
            log.error("could not parse port", nfe);
         }

      return Integer.valueOf(8080);
   }


   private static ConfigFile getConfig(CommandLine cmd) {
      String cfgFile = "masterserver.ini";
      if (cmd.hasOption(CMD_OPTION_CONFIG_FILE))
         cfgFile = cmd.getOptionValue(CMD_OPTION_CONFIG_FILE);

      return new XmlConfigFile(cfgFile);
   }


   private static CommandLine getCmdLineOptions(String[] args) throws ParseException {
      Options cmdArgs = new Options();

      Option confFile = new Option("f", CMD_OPTION_CONFIG_FILE, true, "configuration file");
      confFile.setRequired(false);
      cmdArgs.addOption(confFile);

      Option port = new Option("p", "port", true, "local port");
      port.setRequired(false);
      cmdArgs.addOption(port);

      Option ip = new Option("ip", "ip", true, "local server address");
      ip.setRequired(false);
      cmdArgs.addOption(ip);

      CommandLineParser parser = new DefaultParser();
      return parser.parse(cmdArgs, args);
   }


   public static void main(String[] args) throws ParseException {
      CommandLine cmd = getCmdLineOptions(args);

      AutoLogoutServer as = new AutoLogoutServer(getConfig(cmd), getPort(cmd, getLogger()));
      as.run(getLocalIp(cmd));
   }
}
