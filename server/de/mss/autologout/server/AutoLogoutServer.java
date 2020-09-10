package de.mss.autologout.server;

import java.text.SimpleDateFormat;
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
import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceRequest;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.net.webservice.WebServiceServer;
import de.mss.utils.exception.MssException;

public class AutoLogoutServer extends WebServiceServer {

   private static final String CMD_OPTION_CONFIG_FILE = "config";
   public static final String  CFG_KEY_BASE           = "de.mss.autologout";

   private UserDb              userDb                 = null;
   private WorkDb              workDb                 = null;


   public static final SimpleDateFormat DB_DATE_FORMAT     = new SimpleDateFormat("yyyyMMdd");
   public static final SimpleDateFormat DB_DATETIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");


   private final Map<String, AutoLogoutCounter> counterMap = new HashMap<>();

   private static Logger                        logger     = null;


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
      try {
         this.userDb = new UserDb(getConfigFile().getValue("de.mss.autologout.userdb", "user.sqlite3"));
         this.workDb = new WorkDb(getConfigFile().getValue("de.mss.autologout.workdb", "work.sqlite3"));
      }
      catch (final MssException e) {
         e.printStackTrace();
      }
   }


   @Override
   protected void shutDown() {
      stopServer();
   }


   @Override
   protected Map<String, WebService<WebServiceRequest, WebServiceResponse>> getServiceList() {
      final Map<String,
                WebService<WebServiceRequest,
                           WebServiceResponse>> ret = loadWebServices(this.getClass().getClassLoader(), "de.mss.autologout.server.rest", "/v1");

      for (final Entry<String, WebService<WebServiceRequest, WebServiceResponse>> entry : ret.entrySet()) {
         if (entry.getValue() instanceof AutoLogoutWebService) {
            ((AutoLogoutWebService<WebServiceRequest, WebServiceResponse>)entry.getValue()).setAutoLogoutServer(this);
         }
      }

      return ret;
   }


   private static String getLocalIp(CommandLine cmd) {
      if (cmd.hasOption("ip")) {
         return cmd.getOptionValue("ip");
      }

      return "localhost";
   }


   private static Integer getPort(CommandLine cmd, Logger log) {
      if (cmd.hasOption("port")) {
         try {
            return Integer.valueOf(Integer.parseInt(cmd.getOptionValue("port")));
         }
         catch (final NumberFormatException nfe) {
            log.error("could not parse port", nfe);
         }
      }

      return Integer.valueOf(8080);
   }


   private static ConfigFile getConfig(CommandLine cmd) {
      String cfgFile = "masterserver.ini";
      if (cmd.hasOption(CMD_OPTION_CONFIG_FILE)) {
         cfgFile = cmd.getOptionValue(CMD_OPTION_CONFIG_FILE);
      }

      return new XmlConfigFile(cfgFile);
   }


   private static CommandLine getCmdLineOptions(String[] args) throws ParseException {
      final Options cmdArgs = new Options();

      final Option confFile = new Option("f", CMD_OPTION_CONFIG_FILE, true, "configuration file");
      confFile.setRequired(false);
      cmdArgs.addOption(confFile);

      final Option port = new Option("p", "port", true, "local port");
      port.setRequired(false);
      cmdArgs.addOption(port);

      final Option ip = new Option("ip", "ip", true, "local server address");
      ip.setRequired(false);
      cmdArgs.addOption(ip);

      final CommandLineParser parser = new DefaultParser();
      return parser.parse(cmdArgs, args);
   }


   public static void main(String[] args) throws ParseException {
      final CommandLine cmd = getCmdLineOptions(args);

      final AutoLogoutServer as = new AutoLogoutServer(getConfig(cmd), getPort(cmd, getLogger()));
      as.run(getLocalIp(cmd));
   }


   public UserDb getUserDb() {
      return this.userDb;
   }


   public WorkDb getWorkDb() {
      return this.workDb;
   }


   public Map<String, AutoLogoutCounter> getCounterMap() {
      return this.counterMap;
   }


   public AutoLogoutCounter loadUser(String userName, boolean force) throws MssException {
      AutoLogoutCounter alc = this.counterMap.get(userName);

      if (alc != null && !alc.isSameDay()) {
         this.counterMap.remove(userName);
         alc = null;
      }

      if (alc == null || force) {
         alc = this.userDb.loadUser(userName);
         if (alc == null) {
            return null;
         }

         alc = this.workDb.loadUser(userName, alc);

         applyThresholds(alc.getDailyCounter());
         applyThresholds(alc.getWeeklyCounter());
         this.counterMap.put(userName, alc);
      }

      return alc;
   }


   private void applyThresholds(LogoutCounter lc) {
      lc.setMinutesFirstInfo(getConfigFile().getValue(CFG_KEY_BASE + ".minutes_first_info", 10));
      lc.setMinutesSecondInfo(getConfigFile().getValue(CFG_KEY_BASE + ".minutes_second_info", 5));
      lc.setMinutesFirstWarning(getConfigFile().getValue(CFG_KEY_BASE + ".minutes_first_warning", 5));
      lc.setMinutesForceLogoff(getConfigFile().getValue(CFG_KEY_BASE + ".minutes_force_logoff", 10));
   }
}
