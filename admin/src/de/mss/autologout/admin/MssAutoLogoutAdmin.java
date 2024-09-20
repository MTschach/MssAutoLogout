package de.mss.autologout.admin;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.mss.autologout.client.MssAutoLogoutClient;
import de.mss.autologout.client.param.AddUserBody;
import de.mss.autologout.client.param.AddUserRequest;
import de.mss.autologout.client.param.AddUserResponse;
import de.mss.autologout.client.param.ChangeUserBody;
import de.mss.autologout.client.param.ChangeUserRequest;
import de.mss.autologout.client.param.ChangeUserResponse;
import de.mss.autologout.client.param.CounterMaxValues;
import de.mss.autologout.client.param.GetCounterRequest;
import de.mss.autologout.client.param.GetCounterResponse;
import de.mss.autologout.client.param.ListUsersRequest;
import de.mss.autologout.client.param.ListUsersResponse;
import de.mss.autologout.common.db.param.UserConfig;
import de.mss.configtools.ConfigFile;
import de.mss.configtools.IniConfigFile;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class MssAutoLogoutAdmin extends JFrame implements ActionListener, MouseListener, WindowListener {

   private static final long   serialVersionUID       = 1L;

   private static final String CMD_OPTION_CONFIG_FILE = "config";

   private static final Insets margins                = new Insets(5, 5, 5, 5);

   public static void main(String[] args) throws ParseException, MssException {
      new MssAutoLogoutAdmin(args);
   }

   private final JComboBox<String> comboUsers    = new JComboBox<>();
   private final JTextField        dailyMinutes  = new JTextField(5);
   private final JTextField        weeklyMinutes = new JTextField(5);
   private final JList<String>     counterValues = new JList<>();
   private final JTextArea         logView       = new JTextArea(80, 15);

   private Map<String, UserConfig> userConfigs   = new HashMap<>();

   private ConfigFile              cfg;


   private String configFileName = "autologoutadmin.ini";


   public MssAutoLogoutAdmin(String[] args) throws ParseException, MssException {
      final CommandLine cmd = getCmdLineOptions(args);
      loadConfigFile(cmd);

      this.setVisible(false);
      this.setTitle("Mss AutoLogout Admin GUI V 0.0.1");

      this.comboUsers.addActionListener(this);
      this.comboUsers.setActionCommand("selectUser");

      initLayout(this.getContentPane());

      loadUsers();

      this.addWindowListener(this);
      this.addMouseListener(this);
      this.setPreferredSize(new Dimension(800, 600));
      this.setSize(getPreferredSize());
      this.setVisible(true);
      log("Application stated");
   }


   @Override
   public void actionPerformed(ActionEvent ae) {
      final String cmd = ae.getActionCommand();

      try {
         switch (cmd) {
            case "selectUser":
               selectUser();
               break;

            case "clearLog":
               clearLog();
               break;

            case "newUser":
               newUser();
               break;

            case "saveUser":
               saveUser();
               break;

            default:
               break;
         }
      }
      catch (final MssException e) {
         log(e);
      }

   }


   private boolean checkResponse(String msg, WebServiceResponse resp) {
      if (resp == null) {
         log(msg + " failed");
         return false;
      }

      log(resp.toString());

      if (resp.getErrorCode() != 0) {
         log(msg + " failed", new MssException(resp.getErrorCode(), resp.getErrorText()));
         return false;
      }

      log(msg + " successfull");
      return true;
   }


   private void clearLog() {
      log("clearLogs");
      this.logView.setText("");
   }


   private void createNewUser(String loggingId, String user) throws MssException {
      final UserConfig uc = new UserConfig();
      uc.setUsername(user);
      uc.setDailyValue(Integer.valueOf(this.dailyMinutes.getText()));
      uc.setWeeklyValue(Integer.valueOf(this.weeklyMinutes.getText()));

      final AddUserRequest req = new AddUserRequest();
      req.setAuthToken(this.cfg.getValue("authtoken", ""));
      req.setLoggingId(loggingId);
      req.setBody(new AddUserBody());
      req.getBody().setCounterMaxValues(new CounterMaxValues());
      req.getBody().getCounterMaxValues().setDailyMinutes(uc.getDailyValue());
      req.getBody().getCounterMaxValues().setWeeklyMinutes(uc.getWeeklyValue());
      req.getBody().setUserName(user);

      final AddUserResponse resp = MssAutoLogoutClient.getInstance(cfg).addUser(loggingId, req);
      checkResponse(loggingId + " adding user", resp);

      loadUsers();
   }


   private void exitProgram() {
      log("exiting program");
      this.dispose();
   }


   private CommandLine getCmdLineOptions(String[] args) throws ParseException {
      final Options cmdArgs = new Options();

      final Option confFile = new Option("f", CMD_OPTION_CONFIG_FILE, true, "configuration file");
      confFile.setRequired(false);
      cmdArgs.addOption(confFile);

      final CommandLineParser parser = new DefaultParser();
      return parser.parse(cmdArgs, args);
   }


   private String getLoggingId() {
      return UUID.randomUUID().toString();
   }


   private void initLayout(Container contentPane) {
      contentPane.setLayout(new GridBagLayout());

      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = margins;
      // Toolbar
      gbc.gridx = 0;
      gbc.gridy = 0;
      contentPane.add(initToolbar(), gbc);

      gbc.gridy = 1;
      gbc.gridheight = 5;
      contentPane.add(initMainPanel(), gbc);

      gbc.gridy = 6;
      gbc.gridheight = 3;
      contentPane.add(initLogPanel(), gbc);
   }


   private JPanel initLogPanel() {
      final JPanel panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = margins;

      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridheight = 3;
      gbc.gridwidth = 5;

      final JScrollPane spane = new JScrollPane(this.logView);
      spane.setPreferredSize(new Dimension(600, 100));
      panel.add(spane, gbc);

      gbc.gridx = 5;
      gbc.gridy = 0;
      gbc.gridheight = 1;
      gbc.gridwidth = 1;
      final JButton btnClear = new JButton("Leeren");
      btnClear.setActionCommand("clearLog");
      btnClear.addActionListener(this);
      panel.add(btnClear, gbc);

      return panel;
   }


   private JPanel initMainPanel() {
      final JPanel panel = new JPanel();

      panel.setLayout(new GridBagLayout());

      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = margins;

      gbc.gridx = 0;
      gbc.gridy = 0;
      panel.add(new JLabel("tägliche Zeit"), gbc);
      gbc.gridx = 1;
      panel.add(this.dailyMinutes, gbc);
      gbc.gridx = 2;
      panel.add(new JLabel("Minuten"), gbc);

      gbc.gridx = 0;
      gbc.gridy = 1;
      panel.add(new JLabel("wöchentliche Zeit"), gbc);
      gbc.gridx = 1;
      panel.add(this.weeklyMinutes, gbc);
      gbc.gridx = 2;
      panel.add(new JLabel("Minuten"), gbc);

      gbc.gridx = 3;
      gbc.gridy = 0;
      panel.add(new JLabel("   "), gbc);

      gbc.gridx = 4;
      gbc.gridy = 0;
      gbc.gridheight = 3;
      gbc.gridwidth = 6;
      final JScrollPane spane = new JScrollPane(this.counterValues);
      spane.setPreferredSize(new Dimension(150, 150));
      panel.add(spane, gbc);

      return panel;
   }


   private JPanel initToolbar() {
      final JPanel panel = new JPanel();

      panel.setLayout(new GridBagLayout());

      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = margins;

      gbc.gridx = 0;
      gbc.gridy = 0;
      panel.add(this.comboUsers, gbc);
      gbc.gridx = 1;
      panel.add(new JLabel("   "), gbc);

      gbc.gridx = 2;
      final JButton newBtn = new JButton("Neu");
      newBtn.addActionListener(this);
      newBtn.setActionCommand("newUser");
      panel.add(newBtn, gbc);

      gbc.gridx = 3;
      final JButton saveBtn = new JButton("Speichern");
      saveBtn.addActionListener(this);
      saveBtn.setActionCommand("saveUser");
      panel.add(saveBtn, gbc);

      gbc.gridx = 4;
      final JButton resetBtn = new JButton("Zurücksetzen");
      resetBtn.addActionListener(this);
      resetBtn.setActionCommand("resetUser");
      panel.add(resetBtn, gbc);

      gbc.gridx = 5;
      final JButton deleteBtn = new JButton("Löschen");
      deleteBtn.addActionListener(this);
      deleteBtn.setActionCommand("deleteUser");
      panel.add(deleteBtn, gbc);

      return panel;
   }


   private void loadConfigFile(CommandLine cmd) {
      if (cmd.hasOption(CMD_OPTION_CONFIG_FILE)) {
         this.configFileName = cmd.getOptionValue(CMD_OPTION_CONFIG_FILE);
      }

      this.cfg = new IniConfigFile(this.configFileName);
   }


   private void loadUsers() throws MssException {
      final ListUsersResponse resp = MssAutoLogoutClient.getInstance(this.cfg).listUsers(getLoggingId(), new ListUsersRequest());

      checkResponse("loading users", resp);

      this.comboUsers.removeAllItems();
      this.comboUsers.addItem("Bitte wählen...");
      this.userConfigs = new HashMap<>();
      for (final UserConfig u : resp.getUserlist()) {
         this.userConfigs.put(u.getUsername(), u);
         this.comboUsers.addItem(u.getUsername());
      }
   }


   private void log(Exception e) {
      for (final StackTraceElement t : e.getStackTrace()) {
         log(t.toString());
      }
   }


   private void log(String msg) {
      final String log = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()) + " " + msg + "\n";
      this.logView.append(log);
   }


   private void log(String msg, Exception e) {
      log(msg);
      log(e);
   }


   @Override
   public void mouseClicked(MouseEvent arg0) {
      // TODO Auto-generated method stub

   }


   @Override
   public void mouseEntered(MouseEvent arg0) {
      // TODO Auto-generated method stub

   }


   @Override
   public void mouseExited(MouseEvent arg0) {
      // TODO Auto-generated method stub

   }


   @Override
   public void mousePressed(MouseEvent arg0) {
      // TODO Auto-generated method stub

   }


   @Override
   public void mouseReleased(MouseEvent arg0) {
      // TODO Auto-generated method stub

   }


   private void newUser() {
      String newUser = null;
      do {
         newUser = showNewUserDialog();
      }
      while ((newUser == null) || (this.userConfigs.get(newUser) != null));

      this.counterValues.removeAll();
      this.comboUsers.addItem(newUser);
      this.comboUsers.setSelectedIndex(this.comboUsers.getItemCount() - 1);

      this.dailyMinutes.setText("30");
      this.weeklyMinutes.setText("210");
      final UserConfig uc = new UserConfig();
      uc.setUsername(newUser);
      uc.setDailyValue(30);
      uc.setWeeklyValue(210);
   }


   private void parseArgs(String[] args) {
      // TODO Auto-generated method stub

   }


   private void saveUser() throws MssException {
      log("saving user");
      final String loggingId = getLoggingId();
      final String user = (String)this.comboUsers.getSelectedItem();
      final UserConfig uc = this.userConfigs.get(user);

      if (uc == null) {
         createNewUser(loggingId, user);
         return;
      }


      try {
         uc.setDailyValue(Integer.valueOf(this.dailyMinutes.getText()));
         uc.setWeeklyValue(Integer.valueOf(this.weeklyMinutes.getText()));
      }
      catch (final NumberFormatException nfe) {
         log("could not parse values", nfe);
         selectUser();
         return;
      }

      final ChangeUserRequest req = new ChangeUserRequest();
      req.setAuthToken(this.cfg.getValue("authtoken", ""));
      req.setBody(new ChangeUserBody());
      req.getBody().setCounterMaxValues(new CounterMaxValues());
      req.getBody().getCounterMaxValues().setDailyMinutes(uc.getDailyValue());
      req.getBody().getCounterMaxValues().setWeeklyMinutes(uc.getWeeklyValue());
      req.setUserName(user);
      req.setLoggingId(loggingId);

      final ChangeUserResponse resp = MssAutoLogoutClient.getInstance(this.cfg).changeUser(loggingId, req);
      checkResponse("saving user", resp);
   }


   private void selectUser() throws MssException {
      final String user = (String)this.comboUsers.getSelectedItem();
      final UserConfig uc = this.userConfigs.get(user);
      this.counterValues.removeAll();
      if (uc == null) {
         this.dailyMinutes.setText("");
         this.weeklyMinutes.setText("");
         return;
      }

      this.dailyMinutes.setText("" + uc.getDailyValue());
      this.weeklyMinutes.setText("" + uc.getWeeklyValue());

      final GetCounterRequest req = new GetCounterRequest();
      req.setUserName(user);
      req.setTimeFrame(7);
      final GetCounterResponse resp = MssAutoLogoutClient.getInstance(this.cfg).getCounter(getLoggingId(), req);
      checkResponse("loading counter", resp);

      for (final Entry<String, BigInteger> v : resp.getCounterValues().getValues().entrySet()) {
         this.counterValues.add(new JLabel(v.getKey() + " - " + v.getValue().toString()));
      }
   }


   private String showNewUserDialog() {

      final JDialog dlg = new JDialog();
      dlg.setVisible(false);
      dlg.setTitle("new user");
      dlg.setPreferredSize(new Dimension(200, 100));
      dlg.setSize(200, 100);
      dlg.getContentPane().setLayout(new GridBagLayout());
      final JTextField tf = new JTextField(10);
      dlg.getContentPane().add(tf);

      final JButton ok = new JButton("ok");
      ok.setActionCommand("newUserOk");
      ok.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0) {
            if (tf.getText().length() > 0) {
               dlg.dispose();
            }
         }

      });
      dlg.getContentPane().add(ok);

      dlg.setModal(true);
      dlg.setVisible(true);

      return tf.getText();
   }


   @Override
   public void windowActivated(WindowEvent arg0) {
      // TODO Auto-generated method stub

   }


   @Override
   public void windowClosed(WindowEvent arg0) {}


   @Override
   public void windowClosing(WindowEvent arg0) {
      exitProgram();
   }


   @Override
   public void windowDeactivated(WindowEvent arg0) {}


   @Override
   public void windowDeiconified(WindowEvent arg0) {
      // TODO Auto-generated method stub

   }


   @Override
   public void windowIconified(WindowEvent arg0) {
      // TODO Auto-generated method stub

   }


   @Override
   public void windowOpened(WindowEvent arg0) {
      // TODO Auto-generated method stub

   }
}
