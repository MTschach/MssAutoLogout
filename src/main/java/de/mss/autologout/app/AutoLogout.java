package de.mss.autologout.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.utils.DateTimeTools;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;
import de.mss.utils.os.OsType;

public class AutoLogout {

   private static final String CMD_OPTION_CONFIG_FILE = "config";

   public static final String  CFG_KEY_BASE           = "de.mss.autologout";
   private static final String CFG_KEY_RUN_INTERVAL   = CFG_KEY_BASE + ".run.interval";
   private static final String CFG_KEY_DB_FILE        = CFG_KEY_BASE + ".dbfile";

   private static final String DB_KEY_DAILY_MINUTES   = ".minutes.daily";
   private static final String DB_KEY_WEEKLY_MINUTES  = ".minutes.weekly";

   private String              cfgFileName            = "autologout.conf";
   private ConfigFile          cfgFile                = null;

   private ConfigFile          dbFile                 = null;

   private String              lastUserName           = null;

   private int                 checkInterval          = 60;

   private LogoutCounter       dailyCounter           = null;
   private LogoutCounter       weeklyCounter          = null;

   private boolean             isRunning              = true;


   private static Logger logger = null;


   private static Logger getLogger() {
      if (logger == null) {
         logger = LogManager.getLogger(AutoLogout.class);
      }
      
      return logger;
   }


   public AutoLogout(String[] args) throws ParseException {
      init(args);
   }


   private void init(String[] args) throws ParseException {
      Options cmdArgs = new Options();

      Option confFile = new Option("f", CMD_OPTION_CONFIG_FILE, true, "configuration file");
      confFile.setRequired(false);
      cmdArgs.addOption(confFile);

      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(cmdArgs, args);

      if (cmd.hasOption(CMD_OPTION_CONFIG_FILE)) {
         this.cfgFileName = cmd.getOptionValue(CMD_OPTION_CONFIG_FILE);
      }

      this.cfgFile = new XmlConfigFile(this.cfgFileName);

      this.dbFile = new XmlConfigFile(this.cfgFile.getValue(CFG_KEY_DB_FILE, "autologout.db"));
      this.checkInterval = this.cfgFile.getValue(CFG_KEY_RUN_INTERVAL, BigInteger.valueOf(30)).intValue();
      if (this.checkInterval < 30)
         this.checkInterval = 30;
   }
   

   public ConfigFile getConfig() {
      return this.cfgFile;
   }


   public void stop() {
      this.isRunning = false;
   }


   public void run() throws MssException, IOException {
      long checkIntervalMillis = this.checkInterval * 1000;

      getLogger().log(Level.ALL, "AutoLogout is up and running");
      
      AutoLogoutListener all = new AutoLogoutListener(this);
      Thread t = new Thread(all);
      t.start();

      while (checkRunning()) {
         long nextRun = System.currentTimeMillis() + checkIntervalMillis;

         checkForUser();

         checkCounter();

         addToCounter();

         waitUntil(nextRun);
      }
      
      getLogger().log(Level.ALL, "AutoLogout is shutting down");
   }


   private boolean checkCounter() {
      if (checkCounter(this.weeklyCounter))
         return true;

      return checkCounter(this.dailyCounter);
   }


   private boolean checkCounter(LogoutCounter lc) {
      if (lc == null)
         return false;

      if (lc.isForceLogoff()) {
         showInfoAndLogout("Deine " + lc.getName() + " Zeit ist abgelaufen, Du wirst automatisch abgemeldet", true);
         return true;
      }
      else if (lc.isMinutesUntilForceLogoff()) {
         showInfoAndLogout("Deine " + lc.getName() + " Zeit ist in " + lc.getMinutesUntilFoceLogoff() + " Minuten abgelaufen", false);
         return true;
      }
      else if (lc.isFirstWarning(this.checkInterval)) {
         showInfoAndLogout(
               "Deine " + lc.getName() + " Zeit l�uft in " + (lc.getMinutesForceLogoff() - lc.getMinutesFirstWarning()) + " Minuten ab",
               false);
         return true;
      }
      else if (lc.isMinutesReached(this.checkInterval)) {
         showInfoAndLogout("Deine " + lc.getName() + " Zeit ist abgelaufen", false);
         return true;
      }
      else if (lc.isSecondInfo(this.checkInterval)) {
         showInfoAndLogout("Deine " + lc.getName() + " Zeit l�uft in " + lc.getMinutesSecondInfo() + " Minuten ab", false);
         return true;
      }
      else if (lc.isFirstInfo(this.checkInterval)) {
         showInfoAndLogout("Deine " + lc.getName() + " Zeit l�uft in " + lc.getMinutesFirstInfo() + " Minuten ab", false);
         return true;
      }


      return false;
   }


   private void showInfoAndLogout(String info, boolean logout) {
	  getLogger().debug("showInfoAndLogout for user " + this.lastUserName + " Info: '" + info + "'; logout: " + logout);
	   
      Map<String, String> params = new HashMap<>();
      params.put("MESSAGE", info);
      runCommand("notify", params);
      if (logout)
         runCommand("logoff", null);
   }


   private void addToCounter() throws IOException {
      if (this.dailyCounter != null && this.dailyCounter.getMaxMinutes() > 0) {
         this.dailyCounter.addSeconds(this.checkInterval);
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
         this.dbFile.insertKeyValue("autologout." + this.lastUserName + ".D" + sdf.format(new java.util.Date()), "" + this.dailyCounter.getCurrentMinutes());
         this.dbFile.writeConfig(this.cfgFile.getValue(CFG_KEY_DB_FILE, "autologout.db"));
      }
      if (this.weeklyCounter != null && this.weeklyCounter.getMaxMinutes() > 0) {
         this.weeklyCounter.addSeconds(this.checkInterval);
      }

   }


   private void checkForUser() throws MssException {
      String user = getLoggedInUser();
      if (!Tools.isSet(user)) {
         this.lastUserName = null;
         this.dailyCounter = null;
         this.weeklyCounter = null;
         return;
      }

      if (user.equals(this.lastUserName))
         return;

      this.lastUserName = user;
      this.dailyCounter = new LogoutCounter(
            this.cfgFile.getValue(CFG_KEY_BASE + "." + user + DB_KEY_DAILY_MINUTES, BigInteger.valueOf(30)).intValue(),
            "tägliche");
      this.weeklyCounter = new LogoutCounter(
            this.cfgFile.getValue(CFG_KEY_BASE + "." + user + DB_KEY_WEEKLY_MINUTES, BigInteger.valueOf(240)).intValue(),
            "wöchentliche");

      java.util.Date checkDate = new java.util.Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

      int minutesDaily = this.dbFile.getValue("autologout." + user + ".D" + sdf.format(checkDate), BigInteger.ZERO).intValue();
      int minutesWeekly = minutesDaily;
      for (int i = 1; i <= 7; i++ ) {
         checkDate = DateTimeTools.addDate(checkDate, -1, Calendar.DAY_OF_MONTH);
         minutesWeekly += this.dbFile.getValue("autologout." + user + ".D" + sdf.format(checkDate), BigInteger.ZERO).intValue();
      }

      this.dailyCounter.addMinutes(minutesDaily);
      this.weeklyCounter.addMinutes(minutesWeekly);
   }


   private String getLoggedInUser() {
      String userName = runCommandAndReturnCmdOutput("username");
      
      if (userName != null && this.cfgFile.getValue(CFG_KEY_BASE + "." + userName + DB_KEY_DAILY_MINUTES, BigInteger.valueOf(30)).intValue() <= 0)
    	  return null;
      
      return userName;
   }


   private String runCommandAndReturnCmdOutput(String commandFromConfig) {
      Process p = runCommand(commandFromConfig, null);
      if (p == null)
         return null;

      String line = null;
      try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
         line = br.readLine();
      }
      catch (IOException e) {
         getLogger().error("Read line failed", e);
      }

       return line;
   }


   private Process runCommand(String commandFromConfig, Map<String, String> params) {
      OsType osType = OsType.getOsType();
      String cmd = this.cfgFile.getValue(CFG_KEY_BASE + "." + osType.getName() + "." + commandFromConfig, "").trim();
      if (!Tools.isSet(cmd))
         return null;

      if (params != null) {
         for (Entry<String, String> entry : params.entrySet())
            cmd = cmd.replaceAll("\\{~" + entry.getKey() + "~\\}", entry.getValue());
      }
      
      System.out.println("Exec '" + cmd + "'");

      try {
         Process p = Runtime.getRuntime().exec(cmd);
         if (p != null)
         try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
              System.out.println(br.readLine());
          }

         return p;
      }
      catch (IOException e) {
         getLogger().error("Exec '" + cmd + "' failed", e);
      }
      return null;
   }


   public boolean checkRunning() {
      return this.isRunning;
   }
   
   
   private void waitUntil(long nextRun) {
      long now = System.currentTimeMillis();
      long waitFor = nextRun - now;
      if (waitFor <= 0)
         return;

      try {
         Thread.sleep(waitFor);
      }
      catch (Exception e) {
         getLogger().error("", e);
      }
   }

   
   public static final void main(String[] args) {
      try {
         AutoLogout al = new AutoLogout(args);
         al.run();
      }
      catch (Exception e) {
         getLogger().error("", e);
      }
   }
}
