package de.mss.autologout.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.HashMap;
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

import de.mss.autologout.client.param.CheckCounterRequest;
import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.client.param.GetUserRequest;
import de.mss.autologout.client.param.GetUserResponse;
import de.mss.autologout.client.param.ModifyUserBody;
import de.mss.autologout.client.tts.TextToSpeech;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.counter.LogoutCounter;
import de.mss.autologout.db.UserDb;
import de.mss.autologout.db.WorkDb;
import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.net.rest.RestMethod;
import de.mss.net.rest.RestServer;
import de.mss.net.webservice.WebServiceJsonCaller;
import de.mss.net.webservice.WebServiceResponseChecks;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;
import de.mss.utils.os.OsType;

public class AutoLogoutClient {

   private static final String CMD_OPTION_CONFIG_FILE = "config";

   public static final String  CFG_KEY_BASE           = "de.mss.autologout";
   private static final String CFG_KEY_RUN_INTERVAL   = CFG_KEY_BASE + ".run.interval";
   private static final String CFG_KEY_SERVER         = CFG_KEY_BASE + ".url";

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

   private String                                                          cfgFileName   = "autologout.conf";

   private ConfigFile                                                      cfgFile       = null;

   private String                                                          lastUserName  = null;
   private int                                                             checkInterval = 60;

   private boolean                                                         isRunning     = true;

   private RestServer[]                                                    servers       = null;

   private WebServiceJsonCaller<CheckCounterRequest, CheckCounterResponse> caller;
   private AutoLogoutCounter                                               localCounter  = new AutoLogoutCounter();

   private UserDb                                                          userDb        = null;


   private WorkDb workDb = null;


   private TextToSpeech tts = null;


   public AutoLogoutClient(String[] args) throws ParseException, MssException {
      init(args);
   }


   private boolean checkCounter(String loggingId) {
      if (this.lastUserName == null) {
         return true;
      }

      final File f = new File(System.getProperty("user.home") + File.separator + ".disableAutologout");
      if (f.exists()) {
         return true;
      }

      final CheckCounterRequest request = new CheckCounterRequest();
      request.setUserName(this.lastUserName);
      request.setCheckInterval(Integer.valueOf(this.checkInterval));
      request.setCurrentCounter(this.localCounter.getDailyCounter().getCurrentMinutes());

      CheckCounterResponse response = null;
      try {
         response = this.caller
               .call(loggingId, this.servers, "v1/{username}/checkCounter", RestMethod.GET, request, new CheckCounterResponse(), 3);
      }
      catch (final MssException e) {
         getLogger().error("using local counter", e);
      }

      if (!WebServiceResponseChecks.isResponseOk(response)) {
         if (response == null) {
            response = new CheckCounterResponse();
         }
         if (!this.localCounter.getWeeklyCounter().check(this.lastUserName, this.checkInterval, response)) {
            this.localCounter.getDailyCounter().check(this.lastUserName, this.checkInterval, response);
         }
      }

      if (response == null) {
         return false;
      }

      if (Tools.isSet(response.getMessage())) {
         showInfo(response.getHeadLine(), response.getMessage());
         speak(response.getSpokenMessage());
      }

      if (response.getForceLogout() != null && response.getForceLogout().compareTo(Boolean.TRUE) == 0) {
         logout(10);
      }

      return true;
   }


   private void checkForUser(String loggingId) {
      final String user = getLoggedInUser();
      if (!Tools.isSet(user)) {
         this.lastUserName = null;
         return;
      }

      if (user.equals(this.lastUserName)) {
         return;
      }

      this.lastUserName = user;
      this.localCounter = new AutoLogoutCounter();

      final WebServiceJsonCaller<GetUserRequest, GetUserResponse> getUserCall = new WebServiceJsonCaller<>();
      final GetUserRequest request = new GetUserRequest();
      request.setUserName(user);
      try {
         final GetUserResponse response = getUserCall
               .call(loggingId, this.servers, "v1/admin/{username}", RestMethod.GET, request, new GetUserResponse(), 3);

         if (!WebServiceResponseChecks.isResponseOk(response)) {
            this.localCounter.setDailycounter(new LogoutCounter(30, "täglich"));
            this.localCounter.setWeeklyCounter(new LogoutCounter(210, "wöchentlich"));
         } else {
            this.localCounter.setDailycounter(new LogoutCounter(response.getDailyCounter(), "täglich"));
            this.localCounter.setWeeklyCounter(new LogoutCounter(response.getWeeklyCounter(), "wöchentlich"));
            if (response.getCounterValues() != null) {
               this.localCounter.setCounterValues(response.getCounterValues().getValues());
            }
         }
      }
      catch (final MssException e) {
         getLogger().error("using default values", e);
         this.localCounter.setDailycounter(new LogoutCounter(30, "täglich"));
         this.localCounter.setWeeklyCounter(new LogoutCounter(210, "wöchentlich"));
      }

      final ModifyUserBody data = new ModifyUserBody();
      data.setDailyMinutes(this.localCounter.getDailyCounter().getMaxMinutes());
      data.setWeeklyMinutes(this.localCounter.getWeeklyCounter().getMaxMinutes());
      try {
         this.userDb.changeUser(user, data);
         if (this.localCounter.getCounterValues() != null) {
            for (final Entry<String, BigInteger> entry : this.localCounter.getCounterValues().entrySet()) {
               this.workDb.saveTime(user, entry.getKey(), entry.getValue().intValue());
            }
         }
      }
      catch (final Exception e) {
         getLogger().error("Error while initializing local counter", e);
      }

   }


   public boolean checkRunning() {
      return this.isRunning;
   }


   public ConfigFile getConfig() {
      return this.cfgFile;
   }


   private String getLoggedInUser() {
      return runCommandAndReturnCmdOutput("username");
   }


   private void init(String[] args) throws ParseException, MssException {
      final Options cmdArgs = new Options();

      final Option confFile = new Option("f", CMD_OPTION_CONFIG_FILE, true, "configuration file");
      confFile.setRequired(false);
      cmdArgs.addOption(confFile);

      final CommandLineParser parser = new DefaultParser();
      final CommandLine cmd = parser.parse(cmdArgs, args);

      if (cmd.hasOption(CMD_OPTION_CONFIG_FILE)) {
         this.cfgFileName = cmd.getOptionValue(CMD_OPTION_CONFIG_FILE);
      }

      this.cfgFile = new XmlConfigFile(this.cfgFileName);

      this.checkInterval = this.cfgFile.getValue(CFG_KEY_RUN_INTERVAL, BigInteger.valueOf(30)).intValue();
      if (this.checkInterval < 30) {
         this.checkInterval = 30;
      }

      this.servers = new RestServer[] {new RestServer(this.cfgFile.getValue(CFG_KEY_SERVER, "http://localhost:21080"))};
      this.caller = new WebServiceJsonCaller<>();

      this.tts = initTts();

      try {
         this.userDb = new UserDb(this.cfgFile.getValue("de.mss.autologout.userdb", "user.sqlite3"));
         this.workDb = new WorkDb(this.cfgFile.getValue("de.mss.autologout.workdb", "work.sqlite3"));
      }
      catch (final MssException e) {
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


   private void logout(long sec) {
      getLogger().debug("logout for user " + this.lastUserName);

      try {
         Thread.sleep(sec * 1000);
      }
      catch (final InterruptedException e) {
         getLogger().error(e);
      }

      runCommand("logoff", null);
   }


   public void run() {
      final long checkIntervalMillis = this.checkInterval * 1000;

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

      final AutoLogoutListener all = new AutoLogoutListener(this);
      final Thread t = new Thread(all);
      t.start();

      while (checkRunning()) {
         final long nextRun = System.currentTimeMillis() + checkIntervalMillis;

         final String loggingId = UUID.randomUUID().toString();

         checkForUser(loggingId);

         checkCounter(loggingId);

         waitUntil(nextRun);
      }

      getLogger().log(Level.ALL, "AutoLogout is shutting down");
   }


   private Process runCommand(String commandFromConfig, Map<String, String> params) {
      final OsType osType = OsType.getOsType();
      String cmd = this.cfgFile.getValue(CFG_KEY_BASE + "." + osType.getName() + "." + commandFromConfig, "").trim();
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

         for (final Entry<String, String> entry : params.entrySet()) {
            cmd = cmd.replaceAll("\\{~" + entry.getKey() + "~\\}", entry.getValue());
         }
      }

      getLogger().debug("Exec '" + cmd + "'");

      try {
         final Process p = Runtime.getRuntime().exec(cmds);
         if (p != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
               getLogger().error(br.readLine());
            }
         }

         return p;
      }
      catch (final IOException e) {
         getLogger().error("Exec '" + cmd + "' failed", e);
      }
      return null;
   }


   private String runCommandAndReturnCmdOutput(String commandFromConfig) {
      final Process p = runCommand(commandFromConfig, null);
      if (p == null) {
         return null;
      }

      String line = null;
      try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
         line = br.readLine();
      }
      catch (final IOException e) {
         getLogger().error("Read line failed", e);
      }

      return line;
   }


   public void setTts(TextToSpeech t) {
      this.tts = t;
   }


   private void showInfo(String headline, String message) {
      getLogger().debug("showInfo for user " + this.lastUserName + " headline: '" + headline + "'; message: '" + message + "'");

      final Map<String, String> params = new HashMap<>();
      params.put("HEADLINE", headline);
      params.put("MESSAGE", message);
      runCommand("notify", params);
   }


   private void speak(String msg) {
      if (this.tts == null || !de.mss.utils.Tools.isSet(msg)) {
         return;
      }

      this.tts.speak(msg, 2.0f, false, true);
   }


   public void stop() {
      this.isRunning = false;
   }


   private void waitUntil(long nextRun) {
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
