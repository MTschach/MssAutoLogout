package de.mss.autologout.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mss.autologout.client.param.AddUserBody;
import de.mss.autologout.client.param.ChangeUserBody;
import de.mss.autologout.client.param.CheckCounterRequest;
import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.client.param.CounterMaxValues;
import de.mss.autologout.client.param.CounterValues;
import de.mss.autologout.client.param.GetAllCounterRequest;
import de.mss.autologout.client.param.GetAllCounterResponse;
import de.mss.autologout.client.param.GetCounterRequest;
import de.mss.autologout.client.param.GetCounterResponse;
import de.mss.autologout.client.param.ListUsersRequest;
import de.mss.autologout.client.param.ListUsersResponse;
import de.mss.autologout.client.tts.TextToSpeech;
import de.mss.autologout.common.db.param.UserConfig;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.counter.LogoutCounter;
import de.mss.autologout.counter.WorkingTimeChecker;
import de.mss.autologout.db.UserDb;
import de.mss.autologout.db.WorkDb;
import de.mss.autologout.defs.Defs;
import de.mss.autologout.exception.ErrorCodes;
import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.net.webservice.WebServiceResponseChecks;
import de.mss.utils.DateTimeTools;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;
import de.mss.utils.os.OsType;

public class AutoLogoutClient {

   private static final String CMD_OPTION_CONFIG_FILE = "config";
   private static final String CMD_OPTION_AS_DAEMON   = "daemon";

   public static final String  CFG_KEY_BASE           = "de.mss.autologout";
   private static final String CFG_KEY_RUN_INTERVAL   = CFG_KEY_BASE + ".run.interval";
   private static boolean      noUserNameLogged       = false;
   private static Logger       logger                 = null;

   private static Logger getLogger() {
      if (logger == null) {
         logger = LogManager.getLogger(AutoLogoutClient.class);
      }

      return logger;
   }


   public static final void main(String[] args) {
      try {
         final AutoLogoutClient al = new AutoLogoutClient(args);
         al.run();
      }
      catch (final Exception e) {
         getLogger().error("", e);
      }
   }


   private boolean    asDaemon      = false;

   private String     cfgFileName   = "autologout.conf";

   private ConfigFile cfgFile       = null;

   private String     lastUserName  = null;
   private int        checkInterval = 60;

   private boolean    isRunning     = true;


   private AutoLogoutCounter localCounter      = new AutoLogoutCounter();

   private UserDb            userDb            = null;

   private boolean           localCounterUsed  = false;
   private boolean           userDbInitialized = false;
   private boolean           workDbInitialized = false;


   private WorkDb workDb = null;


   private TextToSpeech tts = null;


   public AutoLogoutClient(String[] args) throws ParseException {
      init(args);
   }


   private boolean checkCounter(String loggingId) {
      if (!Tools.isSet(this.lastUserName)) {
         return true;
      }

      this.localCounter.getWeeklyCounter().addSeconds(this.checkInterval);
      this.localCounter.getDailyCounter().addSeconds(this.checkInterval);
      try {
         this.workDb.saveTime(this.lastUserName, this.localCounter.getDailyCounter().getCurrentMinutes(), new Date());
      }
      catch (final MssException e) {
         getLogger().error(Tools.formatLoggingId(loggingId) + "could not save time", e);
      }

      final CheckCounterRequest ccReq = new CheckCounterRequest();
      ccReq.setCheckInterval(this.checkInterval);
      ccReq.setCurrentCounter(this.localCounter.getDailyCounter().getCurrentSeconds());
      ccReq.setUserName(this.lastUserName);
      try {
         final CheckCounterResponse ccResp = MssAutoLogoutClient.getInstance(getConfig()).checkCounter(loggingId, ccReq);
         if (ccResp != null) {
            if (ccResp.getCounterValue() > this.localCounter.getDailyCounter().getCurrentSeconds()) {
               final int diff = ccResp.getCounterValue() - this.localCounter.getDailyCounter().getCurrentSeconds();
               this.localCounter.getDailyCounter().addSeconds(diff);
               this.localCounter.getWeeklyCounter().addSeconds(diff);

               this.workDb.saveTime(this.lastUserName, this.localCounter.getDailyCounter().getCurrentMinutes(), new Date());
            }

            if (checkResponse(loggingId, ccResp)) {
               return true;
            }
         }
      }
      catch (final Exception e) {
         getLogger().error(Tools.formatLoggingId(loggingId) + "could not check counter on server", e);
      }

      final CheckCounterResponse localResponse = new CheckCounterResponse();
      if (!checkWorkingTime(loggingId, localResponse)) {
         if (!checkTimer(loggingId, this.localCounter.getWeeklyCounter(), localResponse)) {
            checkTimer(loggingId, this.localCounter.getDailyCounter(), localResponse);
         }
      }

      return checkResponse(loggingId, localResponse);
   }


   private boolean checkCounterOld(String loggingId) {
      if (!Tools.isSet(this.lastUserName)) {
         return true;
      }

      final CheckCounterRequest request = new CheckCounterRequest();
      request.setUserName(this.lastUserName);
      request.setCheckInterval(Integer.valueOf(this.checkInterval));
      if (this.localCounterUsed) {
         request.setCurrentCounter(this.localCounter.getDailyCounter().getCurrentMinutes());
      }

      final CheckCounterResponse localResponse = new CheckCounterResponse();
      if (!checkWorkingTime(loggingId, localResponse)) {
         if (!checkTimer(loggingId, this.localCounter.getWeeklyCounter(), localResponse)) {
            checkTimer(loggingId, this.localCounter.getDailyCounter(), localResponse);
         }
      }
      this.localCounter.getWeeklyCounter().addSeconds(this.checkInterval);
      this.localCounter.getDailyCounter().addSeconds(this.checkInterval);

      this.localCounterUsed = false;

      CheckCounterResponse response = null;
      try {
         response = MssAutoLogoutClient.getInstance(getConfig()).checkCounter(loggingId, request);
         if (response.getErrorCode() == 0) {
            if (response.getCounterValue() != null) {
               if (this.localCounter.getDailyCounter().getCurrentSeconds() < response.getCounterValue()) {
                  final int diff = response.getCounterValue() - this.localCounter.getDailyCounter().getCurrentSeconds();
                  this.localCounter.getDailyCounter().addSeconds(diff);
                  this.localCounter.getWeeklyCounter().addSeconds(diff);
               }
            }
         } else {
            this.localCounterUsed = true;
         }
      }
      catch (final Exception e) {
         this.localCounterUsed = true;
         getLogger().error(Tools.formatLoggingId(loggingId) + "using local counter", e);
      }

      try {
         this.workDb.saveTime(this.lastUserName, this.localCounter);
      }
      catch (final Exception e) {
         getLogger().error(Tools.formatLoggingId(loggingId) + "could not save local counter", e);
      }

      if (!WebServiceResponseChecks.isResponseOk(response)) {
         if (response == null) {
            response = localResponse;
         }
      }

      if (response == null) {
         return false;
      }

      if (Tools.isSet(response.getMessage())) {
         showInfo(loggingId, response.getHeadLine(), response.getMessage());
         speak(response.getSpokenMessage());
      }

      if ((response.getForceLogout() != null) && (response.getForceLogout().compareTo(Boolean.TRUE) == 0)) {
         logout(loggingId, 10);
      }

      return true;
   }


   private void checkForUser(String loggingId) {
      final String user = getLoggedInUser(loggingId);
      if (!Tools.isSet(user)) {
         this.lastUserName = null;
         if (!noUserNameLogged) {
            getLogger().debug(Tools.formatLoggingId(loggingId) + "no valid username found");
         }
         noUserNameLogged = true;
         return;
      }

      noUserNameLogged = false;
      if (user.equals(this.lastUserName)) {
         return;
      }
      getLogger().debug(Tools.formatLoggingId(loggingId) + "checking for user " + user);

      this.lastUserName = user;
      this.localCounter = new AutoLogoutCounter();

      try {
         loadFromLocalDb(loggingId, user);

         final ListUsersRequest luReq = new ListUsersRequest();
         final ListUsersResponse luResp = MssAutoLogoutClient.getInstance(getConfig()).listUsers(loggingId, luReq);
         if ((luResp != null) && (luResp.getUserlist() != null)) {
            for (final UserConfig u : luResp.getUserlist()) {
               if (this.lastUserName.equals(u.getUsername())) {
                  this.localCounter.getDailyCounter().setMaxMinutes(u.getDailyValue());
                  this.localCounter.getWeeklyCounter().setMaxMinutes(u.getWeeklyValue());
                  this.localCounter.setWorkingTimeChecker(new WorkingTimeChecker(u.getWorkingTimes()));
                  final ChangeUserBody data = new ChangeUserBody();
                  data.setCounterMaxValues(new CounterMaxValues());
                  data.getCounterMaxValues().setDailyMinutes(u.getDailyValue());
                  data.getCounterMaxValues().setWeeklyMinutes(u.getWeeklyValue());
                  data.setWorkingTimes(u.getWorkingTimes());
                  this.userDb.changeUser(this.lastUserName, data);
               }
            }
         }

         final GetCounterRequest request = new GetCounterRequest();
         request.setUserName(user);
         request.setLoggingId(loggingId);
         request.setTimeFrame(7);
         final GetCounterResponse response = MssAutoLogoutClient.getInstance(getConfig()).getCounter(loggingId, request);

         if (WebServiceResponseChecks.isResponseOk(response)) {
            if (response.getCounterValues() != null) {
               for (final Entry<String, BigInteger> e : response.getCounterValues().getValues().entrySet()) {
                  saveToLocalCounter(this.lastUserName, e.getKey(), e.getValue());
               }
            }
         }
      }
      catch (final Exception e) {
         getLogger().error(Tools.formatLoggingId(loggingId) + "using default values", e);
         this.localCounter.setDailycounter(new LogoutCounter(30, "tägliche"));
         this.localCounter.setWeeklyCounter(new LogoutCounter(210, "wöchentliche"));
         this.localCounter.setWorkingTimeChecker(new WorkingTimeChecker());
      }

      getLogger().debug(Tools.formatLoggingId(loggingId) + "localCounter " + this.localCounter.toString());
   }


   private void checkForUserOld(String loggingId) {
      final String user = getLoggedInUser(loggingId);
      if (!Tools.isSet(user)) {
         this.lastUserName = null;
         if (!noUserNameLogged) {
            getLogger().debug(Tools.formatLoggingId(loggingId) + "no valid username found");
         }
         noUserNameLogged = true;
         return;
      }

      noUserNameLogged = false;
      if (user.equals(this.lastUserName)) {
         return;
      }
      getLogger().debug(Tools.formatLoggingId(loggingId) + "checking for user " + user);

      this.lastUserName = user;
      this.localCounter = new AutoLogoutCounter();

      try {
         final ListUsersRequest luReq = new ListUsersRequest();
         final ListUsersResponse luResp = MssAutoLogoutClient.getInstance(getConfig()).listUsers(loggingId, luReq);
         if (!WebServiceResponseChecks.isResponseOk(luResp)) {
            loadFromLocalDb(loggingId, user);
         } else {
            boolean found = false;
            for (final UserConfig u : luResp.getUserlist()) {
               if (this.lastUserName.equals(u.getUsername())) {
                  found = true;
                  this.localCounter.setDailycounter(new LogoutCounter(u.getDailyValue(), "tägliche"));
                  this.localCounter.setWeeklyCounter(new LogoutCounter(u.getWeeklyValue(), "wöchentliche"));
                  this.localCounter.setWorkingTimeChecker(new WorkingTimeChecker(u.getWorkingTimes()));

                  getCurrentValuesForUser(loggingId, u.getUsername());
                  break;
               }
            }
            if (!found) {
               loadFromLocalDb(loggingId, user);
            }
         }

         final GetCounterRequest request = new GetCounterRequest();
         request.setUserName(user);
         request.setLoggingId(loggingId);
         final GetCounterResponse response = MssAutoLogoutClient.getInstance(getConfig()).getCounter(loggingId, request);

         if (WebServiceResponseChecks.isResponseOk(response)) {
            if (response.getCounterValues() != null) {
               this.localCounter.setCounterValues(response.getCounterValues().getValues());
            }
         }
      }
      catch (final Exception e) {
         getLogger().error(Tools.formatLoggingId(loggingId) + "using default values", e);
         this.localCounter.setDailycounter(new LogoutCounter(30, "tägliche"));
         this.localCounter.setWeeklyCounter(new LogoutCounter(210, "wöchentliche"));
         this.localCounter.setWorkingTimeChecker(new WorkingTimeChecker());
      }

      getLogger().debug(Tools.formatLoggingId(loggingId) + "localCounter " + this.localCounter.toString());
   }


   private boolean checkResponse(String loggingId, CheckCounterResponse resp) {
      if (resp == null) {
         return false;
      }

      boolean handled = false;
      if (Tools.isSet(resp.getMessage())) {
         showInfo(loggingId, resp.getHeadLine(), resp.getMessage());
         speak(resp.getSpokenMessage());
         handled = true;
      }

      if ((resp.getForceLogout() != null) && (resp.getForceLogout().compareTo(Boolean.TRUE) == 0)) {
         logout(loggingId, 10);
         handled = true;
      }

      return handled;
   }


   public boolean checkRunning() {
      return this.isRunning;
   }


   private void checkSudoes(String loggingId) {
      if (!Tools.isSet(this.lastUserName)) {
         return;
      }

      final String[] validSudoers = this.cfgFile.getValue("de.mss.autologout.validsudoers", "").split(";");
      for (final String vs : validSudoers) {
         if (this.lastUserName.equals(vs)) {
            getLogger().debug(Tools.formatLoggingId(loggingId) + this.lastUserName + " is a valid sudo");
            return;
         }
      }

      getLogger().debug(Tools.formatLoggingId(loggingId) + this.lastUserName + " is not a valid sudo");

      switch (OsType.getOsType()) {
         case LINUX:
            checkSudoesLinux(loggingId);
            break;
         default:
            break;
      }
   }


   private void checkSudoesLinux(String loggingId) {
      final Map<String, String> params = new HashMap<>();
      params.put("USERNAME", this.lastUserName);
      runCommand(
            loggingId,
            getCommand("checkuser"),
            params);
   }


   private boolean checkTimer(String loggingId, LogoutCounter counter, CheckCounterResponse resp) {
      return (counter == null) || counter.check(loggingId, getLogger(), this.lastUserName, this.checkInterval, resp);
   }


   private boolean checkWorkingTime(String loggingId, CheckCounterResponse resp) {
      if ((this.localCounter == null) || (this.localCounter.getWorkingTimeChecker() == null)) {
         return false;
      }

      return this.localCounter.getWorkingTimeChecker().check(loggingId, getLogger(), this.lastUserName, this.checkInterval, resp);
   }


   private void doAction(String loggingId) {
      syncUserDb(loggingId);
      //      syncWorkDb(loggingId);

      checkForUser(loggingId);

      checkSudoes(loggingId);

      checkCounter(loggingId);
   }


   private String getCheckUserDefaultCommand(OsType osType) {
      switch (osType) {
         case LINUX:
            return "/scripts/checkUser.sh {~USERNAME~}";

         case WINDOWS:
         default:
            return "";
      }
   }


   private String getCommand(String commandFromConfig) {
      final OsType osType = OsType.getOsType();
      String cmd = this.cfgFile.getValue(CFG_KEY_BASE + "." + osType.getName() + "." + commandFromConfig, "").trim();

      if (!Tools.isSet(cmd)) {
         switch (commandFromConfig) {
            case "notify":
               cmd = getNotifyDefaultCommand(osType);
               break;

            case "username":
               cmd = getUsernameDefaultCommand(osType);
               break;

            case "logoff":
               cmd = getLogoffDefaultCommand(osType);
               break;

            case "checkuser":
               cmd = getCheckUserDefaultCommand(osType);
               break;

            default:
               break;
         }
      }

      return cmd;
   }


   public ConfigFile getConfig() {
      return this.cfgFile;
   }


   private void getCurrentValuesForUser(String loggingId, String username) {
      final GetCounterRequest req = new GetCounterRequest();
      req.setLoggingId(loggingId);
      req.setUserName(username);
      req.setTimeFrame(7);

      this.localCounter.getDailyCounter().reset();
      this.localCounter.getWeeklyCounter().reset();

      try {
         final GetCounterResponse resp = MssAutoLogoutClient.getInstance(this.cfgFile).getCounter(loggingId, req);
         if ((resp == null) || (resp.getCounterValues() == null) || (resp.getCounterValues().getValues() == null)) {
            throw new MssException(ErrorCodes.ERROR_LOADING_USER, "could not load user values");
         }

         final String today = Defs.DB_DATE_FORMAT.format(new java.util.Date());
         BigInteger sum = BigInteger.ZERO;
         for (final Entry<String, BigInteger> e : resp.getCounterValues().getValues().entrySet()) {
            if (today.equals(e.getKey())) {
               this.localCounter.getDailyCounter().addMinutes(e.getValue().intValue());
            }
            sum = sum.add(e.getValue());
         }
         this.localCounter.getWeeklyCounter().addMinutes(sum.intValue());
      }
      catch (final MssException e) {
         getLogger().error(Tools.formatLoggingId(loggingId), e);
         getCurrentValuesForUSerFromLocalDb(loggingId, username);
      }
   }


   private void getCurrentValuesForUSerFromLocalDb(String loggingId, String username) {
      final Date d = new Date();
      try {
         int sum = this.workDb.getDailyValue(username, d);
         this.localCounter.getDailyCounter().addMinutes(sum);
         this.localCounter.setCounterValues(new HashMap<>());
         this.localCounter.getCounterValues().put(Defs.DB_DATE_FORMAT.format(d), BigInteger.valueOf(sum));

         for (int i = 1; i <= 7; i++ ) {
            final int val = this.workDb.getDailyValue(username, DateTimeTools.addDate(d, -i, Calendar.DAY_OF_MONTH));
            this.localCounter
                  .getCounterValues()
                  .put(Defs.DB_DATE_FORMAT.format(DateTimeTools.addDate(d, -i, Calendar.DAY_OF_MONTH)), BigInteger.valueOf(sum));
            sum += val;
         }
         this.localCounter.getWeeklyCounter().addMinutes(sum);
      }
      catch (final MssException e) {
         getLogger().error(Tools.formatLoggingId(loggingId), e);
      }
   }


   private String getLoggedInUser(String loggingId) {
      final List<String> output = runCommandAndReturnCmdOutput(loggingId, "username");
      if (output.isEmpty()) {
         getLogger().debug(Tools.formatLoggingId(loggingId) + "no output");
      }
      for (final String l : output) {
         getLogger().debug(Tools.formatLoggingId(loggingId) + "line " + l);
      }

      final String[] validUsers = getConfig().getValue("de.mss.autologout.validusers", "").split(";");

      for (final String vu : validUsers) {
         for (final String l : output) {
            if (vu.equals(l)) {
               return l;
            }
         }
      }

      return null;
   }


   private String getLogoffDefaultCommand(OsType osType) {
      switch (osType) {
         case LINUX:
            //            return "/usr/bin/gnome-session-quit --logout --force --no-prompt";
            return "/scripts/cron-logout.sh {~USERNAME~}";

         case WINDOWS:
            return "cmd.exe /c echo \"Logoff\"";

         default:
            return "";
      }
   }


   private String getNotifyDefaultCommand(OsType osType) {
      switch (osType) {
         case LINUX:
            //            return "/usr/bin/notify-send --urgency=normal -t 5000 \"{~HEADLINE~}\" \"{~MESSAGE~}\"";
            return "/scripts/cron-notify.sh {~HEADLINE~} {~MESSAGE~} {~USERNAME~}";

         case WINDOWS:
            return "cmd.exe /c echo {~MESSAGE~}";

         default:
            return "";
      }
   }


   private String getUsernameDefaultCommand(OsType osType) {
      switch (osType) {
         case LINUX:
            //return "/usr/bin/whoami";
            return "/scripts/getUsername.sh";

         case WINDOWS:
            return "cmd.exe /c echo %username%";

         default:
            return "";
      }
   }


   private void init(String[] args) throws ParseException {
      final Options cmdArgs = new Options();

      final Option confFile = new Option("f", CMD_OPTION_CONFIG_FILE, true, "configuration file");
      confFile.setRequired(false);
      cmdArgs.addOption(confFile);

      final Option asDaemon = new Option("d", CMD_OPTION_AS_DAEMON, false, "run as daemon");
      asDaemon.setRequired(false);
      cmdArgs.addOption(asDaemon);

      final CommandLineParser parser = new DefaultParser();
      final CommandLine cmd = parser.parse(cmdArgs, args);

      if (cmd.hasOption(CMD_OPTION_CONFIG_FILE)) {
         this.cfgFileName = cmd.getOptionValue(CMD_OPTION_CONFIG_FILE);
      }

      if (cmd.hasOption(CMD_OPTION_AS_DAEMON)) {
         this.asDaemon = true;
      }

      this.cfgFile = new XmlConfigFile(this.cfgFileName);

      this.checkInterval = this.cfgFile.getValue(CFG_KEY_RUN_INTERVAL, BigInteger.valueOf(30)).intValue();
      if ((this.checkInterval < 30) && this.asDaemon) {
         this.checkInterval = 30;
      }
      if ((this.checkInterval < 60) && !this.asDaemon) {
         this.checkInterval = 60;
      }

      this.tts = initTts();

      try {
         this.userDb = new UserDb(this.cfgFile.getValue("de.mss.autologout.userdb", "user.sqlite3"));
         this.workDb = new WorkDb(this.cfgFile.getValue("de.mss.autologout.workdb", "work.sqlite3"));
         final String loggingId = UUID.randomUUID().toString();

         syncUserDb(loggingId);
         //         syncWorkDb(loggingId);
      }
      catch (final Exception e) {
         e.printStackTrace();
      }
   }


   private TextToSpeech initTts() {
      try {
         final TextToSpeech t = new TextToSpeech();
         //Print all the available audio effects
         //         t.getAudioEffects().stream().forEach(audioEffect -> {
         //            getLogger().debug("-----Name-----");
         //            getLogger().debug(audioEffect.getName());
         //            getLogger().debug("-----Examples-----");
         //            getLogger().debug(audioEffect.getExampleParameters());
         //            getLogger().debug("-----Help Text------");
         //            getLogger().debug(audioEffect.getHelpText() + "\n\n");
         //         });

         getLogger().debug("Listing voices");

         // Print all the available voices
         t.getAvailableVoices().stream().forEach(voice -> getLogger().debug("Voice: " + voice));

         t.setVoice("bits1-hsmm");

         return t;
      }
      catch (final Exception e) {
         getLogger().error("error creating TTS-Interface", e);
      }
      return null;
   }


   private void loadFromLocalDb(String loggingId, final String user) throws MssException {
      final AutoLogoutCounter alc = this.userDb.loadUser(user);
      this.localCounter.setDailycounter(alc.getDailyCounter());
      this.localCounter.setWeeklyCounter(alc.getWeeklyCounter());
      this.localCounter.setWorkingTimeChecker(alc.getWorkingTimeChecker());
      getCurrentValuesForUSerFromLocalDb(loggingId, user);
   }


   private void logout(String loggingId, long sec) {
      getLogger().debug(Tools.formatLoggingId(loggingId) + "logout for user " + this.lastUserName);

      try {
         Thread.sleep(sec * 1000);
      }
      catch (final InterruptedException e) {
         getLogger().error(e);
      }

      final Map<String, String> params = new HashMap<>();
      params.put("USERNAME", this.lastUserName);

      runCommand(loggingId, "logoff", params);
   }


   public void run() {
      getLogger().log(Level.ALL, "AutoLogout is up and running");
      getLogger().log(Level.ALL, "my All-Message");
      getLogger().info("my Info Message");
      getLogger().warn("my Warn Message");
      getLogger().debug("my Debug Message");
      getLogger().error("my Error Message");
      getLogger().trace("my Trace Message");

      final File f = new File(System.getProperty("user.home") + File.separator + ".disableAutologout");
      if (f.exists()) {
         f.delete();
      }

      if (this.asDaemon) {
         runAsDaemon();
      } else {
         runOnce();
      }
   }


   private void runAsDaemon() {
      final long checkIntervalMillis = this.checkInterval * 1000;

      long nextRun = System.currentTimeMillis() + checkIntervalMillis;

      waitUntil(nextRun);

      //      final AutoLogoutListener all = new AutoLogoutListener(this);
      //      final Thread t = new Thread(all);
      //      t.start();
      String loggingId = UUID.randomUUID().toString();
      while (checkRunning()) {
         try {
            nextRun = System.currentTimeMillis() + checkIntervalMillis;

            loggingId = UUID.randomUUID().toString();

            doAction(loggingId);

            waitUntil(nextRun);
         }
         catch (final Exception e) {
            getLogger().error(Tools.formatLoggingId(loggingId) + e);
         }
      }

      getLogger().log(Level.ALL, "AutoLogout is shutting down");
   }


   private Process runCommand(String loggingId, String commandFromConfig, Map<String, String> params) {
      final String cmd = getCommand(commandFromConfig);

      if (!Tools.isSet(cmd)) {
         return null;
      }

      final String[] cmds = cmd.split(" ");

      if (params != null) {
         for (final Entry<String, String> entry : params.entrySet()) {
            for (int i = 0; i < cmds.length; i++ ) {
               cmds[i] = cmds[i].replaceAll("\\{~" + entry.getKey() + "~\\}", entry.getValue());
            }
         }
      }

      final StringBuilder execCmd = new StringBuilder(cmds[0] + " ");
      for (int i = 1; i < cmds.length; i++ ) {
         execCmd.append("\"" + cmds[i] + "\" ");
      }

      getLogger().debug(Tools.formatLoggingId(loggingId) + "Exec '" + execCmd.toString() + "'");

      try {
         final Process p = Runtime.getRuntime().exec(cmds);
         if (p != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
               final String ret = br.readLine();
               if (Tools.isSet(ret)) {
                  getLogger().error(Tools.formatLoggingId(loggingId) + ret);
               }
            }
         }

         return p;
      }
      catch (final IOException e) {
         getLogger().error(Tools.formatLoggingId(loggingId) + "Exec '" + execCmd.toString() + "' failed", e);
      }
      return null;
   }


   private List<String> runCommandAndReturnCmdOutput(String loggingId, String commandFromConfig) {
      final Process p = runCommand(loggingId, commandFromConfig, null);
      if (p == null) {
         return new ArrayList<>();
      }

      final List<String> ret = new ArrayList<>();
      String line = null;
      try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
         while ((line = br.readLine()) != null) {
            ret.add(line);
         }
      }
      catch (final IOException e) {
         getLogger().error(Tools.formatLoggingId(loggingId) + "Read line failed", e);
      }

      return ret;
   }


   private void runOnce() {
      final String loggingId = UUID.randomUUID().toString();

      try {
         doAction(loggingId);
      }
      catch (final Exception e) {
         getLogger().error(Tools.formatLoggingId(loggingId) + e);
      }
   }


   private void saveToLocalCounter(String username, String key, BigInteger value) throws MssException {
      if (this.localCounter.getCounterValues() == null) {
         this.localCounter.setCounterValues(new HashMap<>());
      }

      for (final Entry<String, BigInteger> lc : this.localCounter.getCounterValues().entrySet()) {
         if (key.equals(lc.getKey()) && (value.compareTo(lc.getValue()) > 0)) {
            lc.setValue(value);
            this.workDb.saveTime(username, key, value.intValue());
            return;
         }
      }
      this.localCounter.getCounterValues().put(key, value);
      this.workDb.saveTime(username, key, value.intValue());
   }


   public void setTts(TextToSpeech t) {
      this.tts = t;
   }


   private void showInfo(String loggingId, String headline, String message) {
      getLogger()
            .debug(
                  Tools.formatLoggingId(loggingId)
                        + "showInfo for user "
                        + this.lastUserName
                        + " headline: '"
                        + headline
                        + "'; message: '"
                        + message
                        + "'");

      final Map<String, String> params = new HashMap<>();
      params.put("HEADLINE", headline);
      params.put("MESSAGE", message);
      params.put("USERNAME", this.lastUserName);
      runCommand(loggingId, "notify", params);
   }


   private void speak(String msg) {
      if ((this.tts == null) || !de.mss.utils.Tools.isSet(msg)) {
         return;
      }

      this.tts.speak(msg, 2.0f, false, true);
   }


   public void stop() {
      this.isRunning = false;
   }


   private boolean syncUserDb(String loggingId) {
      if (this.userDbInitialized) {
         return true;
      }

      try {
         final ListUsersResponse users = MssAutoLogoutClient.getInstance(this.cfgFile).listUsers(loggingId, new ListUsersRequest());
         if ((users == null) || (users.getUserlist() == null)) {
            return false;
         }
         for (final UserConfig uc : users.getUserlist()) {
            if (this.userDb.isUserPresent(uc.getUsername())) {
               final ChangeUserBody data = new ChangeUserBody();
               data.setCounterMaxValues(new CounterMaxValues());
               data.getCounterMaxValues().setDailyMinutes(uc.getDailyValue());
               data.getCounterMaxValues().setWeeklyMinutes(uc.getWeeklyValue());
               data.setWorkingTimes(uc.getWorkingTimes());
               this.userDb.changeUser(uc.getUsername(), data);
            } else {
               final AddUserBody data = new AddUserBody();
               data.setCounterMaxValues(new CounterMaxValues());
               data.getCounterMaxValues().setDailyMinutes(uc.getDailyValue());
               data.getCounterMaxValues().setWeeklyMinutes(uc.getWeeklyValue());
               data.setUserName(uc.getUsername());
               data.setWorkingTimes(uc.getWorkingTimes());
               this.userDb.addUser(data);
            }
         }
      }
      catch (final Exception e) {
         getLogger().error(loggingId, e);
         return false;
      }

      this.userDbInitialized = true;
      return true;
   }


   private boolean syncWorkDb(String loggingId) {
      if (this.workDbInitialized) {
         return true;
      }

      try {
         final GetAllCounterResponse counters = MssAutoLogoutClient.getInstance(this.cfgFile).getAllCounters(loggingId, new GetAllCounterRequest());
         if ((counters == null) || (counters.getCounterValues() == null)) {
            return false;
         }
         for (final Entry<String, CounterValues> cv : counters.getCounterValues().entrySet()) {
            for (final Entry<String, BigInteger> c : cv.getValue().getValues().entrySet()) {
               this.workDb.saveTime(cv.getKey(), c.getKey(), c.getValue().intValue());
            }
         }
      }
      catch (final Exception e) {
         getLogger().error(loggingId, e);
         return false;
      }

      this.workDbInitialized = true;
      return true;
   }


   private void waitUntil(long nextRun) {
      getLogger().debug("waiting until " + new Date(nextRun));
      final long now = System.currentTimeMillis();
      final long waitFor = nextRun - now;
      if (waitFor <= 0) {
         return;
      }

      try {
         Thread.sleep(waitFor);
      }
      catch (final Exception e) {
         getLogger().error("", e);
      }
   }
}
