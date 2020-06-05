package de.mss.autologout.server;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.client.param.CounterValues;
import de.mss.autologout.client.param.GetAllCounterResponse;
import de.mss.autologout.client.param.GetCounterResponse;
import de.mss.autologout.client.param.SetCounterBody;
import de.mss.autologout.param.AutoLogoutCounter;
import de.mss.autologout.server.storageengine.StorageEngineFactory;
import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceServer;
import de.mss.utils.exception.MssException;

public class AutoLogoutServer extends WebServiceServer {

   private static final String CMD_OPTION_CONFIG_FILE = "config";
   public static final String  CFG_KEY_BASE           = "de.mss.autologout";


   private static final SimpleDateFormat  DB_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

   private Map<String, AutoLogoutCounter> counterMap             = new HashMap<>();

   private static Logger logger = null;


   public static Logger getLogger() {
      if (logger == null) {
         logger = LogManager.getLogger(AutoLogoutServer.class);
      }

      return logger;
   }


   public AutoLogoutServer(ConfigFile c) {
      super(c);
   }


   public AutoLogoutServer(ConfigFile c, Integer p) {
      super(c, p);
      WebServiceServer.setLogger(getLogger());
   }


   @Override
   protected void initApplication() {
      // nothing to do here
   }


   @Override
   protected void shutDown() {
      stopServer();
   }


   @Override
   protected Map<String, WebService> getServiceList() {
      Map<String, WebService> ret = loadWebServices(this.getClass().getClassLoader(), "de.mss.autologout.server.rest", "/v1");

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
         try {
            StorageEngineFactory.getStorageEngine(getConfigFile()).storeUser(userName, this.counterMap.get(userName));
         }
         catch (MssException e) {
            getLogger().error(e);
         }
      }
   }


   public CheckCounterResponse checkCounter(String userName, int checkInterval) {
      CheckCounterResponse ret = null;
      
      if (checkLocked(userName)) {
         return getResponse(Boolean.TRUE, "Login gesperrt", "Hallo " + userName + ". Du darfst dich heute nicht einloggen.", null);
      }

      if (!this.counterMap.containsKey(userName))
         loadUser(userName);

      String today = DB_DATE_FORMAT.format(new java.util.Date());
      if (!today.equals(this.counterMap.get(userName).getDailyCounter().getDate())) {
         this.counterMap.remove(userName);
         loadUser(userName);
      }

      ret = checkCounter(this.counterMap.get(userName).getWeeklyCounter(), userName, checkInterval);
      if (ret != null)
         return ret;

      ret = checkCounter(this.counterMap.get(userName).getDailyCounter(), userName, checkInterval);
      if (ret != null)
         return ret;

      return getResponse(Boolean.FALSE, "", "", null);
   }


   private boolean checkLocked(String userName) {
      String date = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
      return de.mss.utils.Tools.isSet(getConfigFile().getValue(CFG_KEY_BASE + "." + userName + ".lock." + date, ""));
   }


   public GetCounterResponse getCounter(String userName) {
      GetCounterResponse resp = new GetCounterResponse();

      if (!this.counterMap.containsKey(userName))
         loadUser(userName);
      
      resp.setCounterValues(getCounterValues(userName));

      return resp;
   }


   public GetAllCounterResponse getAllCounters() {
      GetAllCounterResponse resp = new GetAllCounterResponse();

      List<String> keys = new ArrayList<>();

      resp.setCounterValues(new HashMap<>());
      for (String key : getConfigFile().getKeys()) {
         String[] k = key.split(".");
         if (k.length == 2)
            keys.add(k[2]);
      }

      for (String userName : keys) {
         loadUser(userName);

         resp.getCounterValues().put(userName, getCounterValues(userName));
      }

      return resp;
   }
   
   
   private CounterValues getCounterValues(String userName) {
      CounterValues ret = new CounterValues();
      ret.setValues(this.counterMap.get(userName).getCounterValues());
       
      ret
            .getValues()
            .put(
                  DB_DATE_FORMAT.format(new java.util.Date()),
                  BigInteger.valueOf(this.counterMap.get(userName).getDailyCounter().getCurrentMinutes()));
       
       BigInteger sum = BigInteger.ZERO;
      for (Entry<String, BigInteger> entry : ret.getValues().entrySet())
          sum = sum.add(entry.getValue());
       
      ret.getValues().put("total", sum);
   
      return ret;
   }


   private static CheckCounterResponse checkCounter(LogoutCounter lc, String userName, int checkInterval) {
      if (lc == null)
         return null;

      if (lc.isForceLogoff()) {
         return getResponse(
               Boolean.TRUE,
               "Info",
               "Hallo " + userName + "! Deine " + lc.getName() + " Zeit ist abgelaufen, Du wirst automatisch abgemeldet",
               null);
      } else if (lc.isMinutesUntilForceLogoff()) {
         return getResponse(
               Boolean.FALSE,
               "Info",
               "Hallo " + userName + "! Deine " + lc.getName() + " Zeit ist seit " + lc.getMinutesOvertime() + " Minuten abgelaufen. Du wirst in "
                     + lc.getMinutesUntilFoceLogoff()
                     + "Minuten abgemeldet.",
               getMessageReached(userName, ""));
      } else if (lc.isFirstWarning(checkInterval)) {
         return getResponse(
               Boolean.FALSE,
               "Info",
               "Hallo "
                     + userName
                     + "! Deine "
                     + lc.getName()
                     + " Zeit läuft in "
                     + (lc.getMinutesForceLogoff() - lc.getMinutesFirstWarning())
                     + " Minuten ab",
               null);
      } else if (lc.isMinutesReached(checkInterval)) {
         return getResponse(Boolean.FALSE, "Info", getMessageReached(userName, lc.getName()), null);
      } else if (lc.isSecondInfo(checkInterval)) {
         return getResponse(
               Boolean.FALSE,
               "Info",
               getMessageInfo(userName, lc.getName(), lc.getMinutesSecondInfo()),
               null);
      } else if (lc.isFirstInfo(checkInterval)) {
         return getResponse(
               Boolean.FALSE,
               "Info",
               getMessageInfo(userName, lc.getName(), lc.getMinutesFirstInfo()),
               null);
      }

      return null;
   }


   private static String getMessageInfo(String userName, String name, int minutesLeft) {
      return "Hallo " + userName + "! Deine " + name + " Zeit läuft in " + minutesLeft + " Minuten ab.";
   }


   private static String getMessageReached(String userName, String name) {
      return "Hallo " + userName + "! Deine Zeit " + name + " ist abgelaufen.";
   }


   private void loadUser(String userName) {
      if (this.counterMap.containsKey(userName))
         return;

      try {
         this.counterMap.put(userName, StorageEngineFactory.getStorageEngine(getConfigFile()).loadUser(userName));
      }
      catch (MssException e) {
         getLogger().error(e);
      }
   }


   private static CheckCounterResponse getResponse(Boolean forceLogout, String headLine, String message, String spokenMessage) {
      CheckCounterResponse resp = new CheckCounterResponse();
      resp.setForceLogout(forceLogout);
      resp.setHeadLine(headLine);
      resp.setMessage(message);
      resp.setSpokenMessage(spokenMessage);

      return resp;
   }


   private static Integer getPort(CommandLine cmd, Logger log) {
      if (cmd.hasOption("port"))
         try {
            return Integer.valueOf(Integer.parseInt(cmd.getOptionValue("port")));
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


   public void setCounter(String userName, SetCounterBody setCounterBody) {
      loadUser(userName);

      AutoLogoutCounter counters = this.counterMap.get(userName);

      try {
         StorageEngineFactory.getStorageEngine(getConfigFile()).storeUser(userName, counters, setCounterBody.getReason());
      }
      catch (MssException e) {
         getLogger().error(e);
      }

      if (setCounterBody.getValue().startsWith("+")) {
         int addValue = Integer.parseInt(setCounterBody.getValue());
         counters.getDailyCounter().addMinutes(addValue);
         counters.getWeeklyCounter().addMinutes(addValue);
      } else if (setCounterBody.getValue().startsWith("-")) {
         int addValue = Integer.parseInt(setCounterBody.getValue());
         counters.getDailyCounter().addMinutes(addValue);
         counters.getWeeklyCounter().addMinutes(addValue);
      } else {
         int value = Integer.parseInt(setCounterBody.getValue());
         int addValue = value - counters.getDailyCounter().getCurrentMinutes();
         counters.getDailyCounter().addMinutes(addValue);
         counters.getWeeklyCounter().addMinutes(addValue);
      }

      try {
         StorageEngineFactory.getStorageEngine(getConfigFile()).storeUser(userName, counters);
      }
      catch (MssException e) {
         getLogger().error(e);
      }
   }
}
