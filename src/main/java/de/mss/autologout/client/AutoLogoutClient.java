package de.mss.autologout.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
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

import de.mss.autologout.param.CheckCounterRequest;
import de.mss.autologout.param.CheckCounterResponse;
import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.net.rest.RestMethod;
import de.mss.net.rest.RestServer;
import de.mss.net.webservice.WebServiceJsonCaller;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;
import de.mss.utils.os.OsType;

public class AutoLogoutClient {

   private static final String CMD_OPTION_CONFIG_FILE = "config";

   public static final String  CFG_KEY_BASE           = "de.mss.autologout";
   private static final String CFG_KEY_RUN_INTERVAL   = CFG_KEY_BASE + ".run.interval";
   private static final String CFG_KEY_SERVER         = CFG_KEY_BASE + ".url";

   private String              cfgFileName            = "autologout.conf";
   private ConfigFile          cfgFile                = null;

   private String              lastUserName           = null;

   private int                 checkInterval          = 60;

   private boolean             isRunning              = true;
   
   private RestServer[] servers = null;
   private WebServiceJsonCaller<CheckCounterRequest, CheckCounterResponse> caller;


   private static Logger logger = null;


   private static Logger getLogger() {
      if (logger == null) {
         logger = LogManager.getLogger(AutoLogoutClient.class);
      }
      
      return logger;
   }


   public AutoLogoutClient(String[] args) throws ParseException, MssException {
      init(args);
   }


   private void init(String[] args) throws ParseException, MssException {
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

      this.checkInterval = this.cfgFile.getValue(CFG_KEY_RUN_INTERVAL, BigInteger.valueOf(30)).intValue();
      if (this.checkInterval < 30)
         this.checkInterval = 30;
      
      this.servers = new RestServer[] { new RestServer(this.cfgFile.getValue(CFG_KEY_SERVER, "http://localhost:21080"))};
      this.caller = new WebServiceJsonCaller<>();
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
      getLogger().log(Level.ALL, "my All-Message");
      getLogger().info("my Info Message");
      getLogger().warn("my Warn Message");
      getLogger().debug("my Debug Message");
      getLogger().error("my Error Message");
      getLogger().trace("my Trace Message");
      
      AutoLogoutListener all = new AutoLogoutListener(this);
      Thread t = new Thread(all);
      t.start();

      while (checkRunning()) {
         long nextRun = System.currentTimeMillis() + checkIntervalMillis;

         checkForUser();

         checkCounter();

         waitUntil(nextRun);
      }
      
      getLogger().log(Level.ALL, "AutoLogout is shutting down");
   }


   private boolean checkCounter() throws MssException {
      if (this.lastUserName == null)
         return true;
      
      CheckCounterRequest request = new CheckCounterRequest();
      request.setUserName(this.lastUserName);
      request.setCheckInterval(Integer.valueOf(this.checkInterval));
      
      CheckCounterResponse response = this.caller
            .call("", this.servers, "v1/{username}/checkCounter", RestMethod.GET, request, new CheckCounterResponse(), 3);
      
      if (response == null)
         return false;
      
      if (Tools.isSet(response.getMessage()))
         showInfo(response.getHeadline(), response.getMessage());
      
      if (response.getForceLogout() != null && response.getForceLogout().compareTo(Boolean.TRUE) == 0)
         logout();
     
      return true;
   }


   private void logout() {
      getLogger().debug("logout for user " + this.lastUserName);

      runCommand("logoff", null);
   }


   private void showInfo(String headline, String message) {
      getLogger().debug("showInfo for user " + this.lastUserName + " headline: '" + headline + "'; message: '" + message + "'");
 
      Map<String, String> params = new HashMap<>();
      params.put("HEADLINE", headline);
      params.put("MESSAGE", message);
      runCommand("notify", params);
   }


   private void checkForUser() throws MssException {
      String user = getLoggedInUser();
      if (!Tools.isSet(user)) {
         this.lastUserName = null;
         return;
      }

      if (user.equals(this.lastUserName))
         return;

      this.lastUserName = user;
   }


   private String getLoggedInUser() {
      return runCommandAndReturnCmdOutput("username");
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

      String[] cmds = cmd.split(" ");

      if (params != null) {
         for (Entry<String, String> entry : params.entrySet())
            for (int i = 0; i < cmds.length; i++ )
               cmds[i] = cmds[i].replaceAll("\\{~" + entry.getKey() + "~\\}", entry.getValue());

         for (Entry<String, String> entry : params.entrySet())
            cmd = cmd.replaceAll("\\{~" + entry.getKey() + "~\\}", entry.getValue());
      }
      
      getLogger().debug("Exec '" + cmd + "'");

      try {
         Process p = Runtime.getRuntime().exec(cmds);
         if (p != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
               getLogger().error(br.readLine());
            }
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
         AutoLogoutClient al = new AutoLogoutClient(args);
         al.run();
      }
      catch (Exception e) {
         getLogger().error("", e);
      }
   }
}
