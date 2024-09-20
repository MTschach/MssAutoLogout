package de.mss.autologout.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.mss.autologout.client.param.AddUserBody;
import de.mss.autologout.client.param.ChangeUserBody;
import de.mss.autologout.client.param.CounterMaxValues;
import de.mss.autologout.client.param.SpecialValue;
import de.mss.autologout.client.param.UserSpecialValue;
import de.mss.autologout.common.db.param.UserConfig;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.utils.DateTimeTools;
import de.mss.utils.exception.MssException;

public class UserDbTest {

   private UserDb     userDb;
   private Connection dbCon = null;


   private void checkAutoLogoutCounter(AutoLogoutCounter alc, int daily, int weekly) {
      assertNotNull(alc);
      assertFalse(alc.getLockedForToday());
      assertNull(alc.getReasonForLock());
      assertNotNull(alc.getDailyCounter());
      assertNotNull(alc.getWeeklyCounter());
      assertEquals(Integer.valueOf(daily), Integer.valueOf(alc.getDailyCounter().getMaxMinutes()));
      assertEquals(Integer.valueOf(weekly), Integer.valueOf(alc.getWeeklyCounter().getMaxMinutes()));
   }


   private void execute(String sql) throws ClassNotFoundException, SQLException {
      openConnection();
      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate(sql);
      }
   }


   private CounterMaxValues getCounterMaxValues(int d, int w) {
      final CounterMaxValues ret = new CounterMaxValues();
      ret.setDailyMinutes(d);
      ret.setWeeklyMinutes(w);
      return ret;
   }


   private UserSpecialValue getUserSpecialValue(int dayDiff, Boolean locked, Integer minutes, String reason, Boolean delete) {
      final UserSpecialValue ret = new UserSpecialValue();
      ret.setDate(DateTimeTools.addDate(0, Calendar.DAY_OF_YEAR));
      ret.setDelete(delete);
      ret.setLock(locked);
      ret.setMinutes(minutes);
      ret.setReason(reason);
      return ret;
   }


   private void openConnection() throws ClassNotFoundException, SQLException {
      if (this.dbCon != null) {
         return;
      }

      Class.forName("org.sqlite.JDBC");
      this.dbCon = DriverManager.getConnection("jdbc:sqlite:userdb-test.sqlite3");
   }


   @BeforeEach
   public void setUp() throws MssException {
      final File f = new File("userdb-test.sqlite3");
      if (f.exists()) {
         f.delete();
      }

      this.userDb = new UserDb("userdb-test.sqlite3");
   }


   @AfterEach
   public void tearDown() throws SQLException {
      this.userDb = null;
      if (this.dbCon != null) {
         if (!this.dbCon.isClosed()) {
            this.dbCon.close();
         }

         this.dbCon = null;
      }
   }


   @Test
   public void testAddUser() throws MssException {
      final AddUserBody data = new AddUserBody();
      data.setUserName("demouser");
      data.setCounterMaxValues(getCounterMaxValues(20, 140));
      try {
         this.userDb.addUser(data);
      }
      catch (final MssException e) {
         assertEquals(de.mss.autologout.exception.ErrorCodes.ERROR_USER_ALREADY_EXISTS, e.getError());
      }
      data.setUserName("newUser");
      this.userDb.addUser(data);
      final List<UserConfig> uc = this.userDb.getUsersAndConfig();
      assertEquals(Integer.valueOf(2), uc.size());
      assertEquals("demouser", uc.get(0).getUsername());
      assertEquals(Integer.valueOf(15), uc.get(0).getDailyValue());
      assertEquals(Integer.valueOf(175), uc.get(0).getWeeklyValue());
      assertEquals("newUser", uc.get(1).getUsername());
      assertEquals(Integer.valueOf(20), uc.get(1).getDailyValue());
      assertEquals(Integer.valueOf(140), uc.get(1).getWeeklyValue());
   }


   @Test
   public void testChangeUser() throws MssException {
      try {
         this.userDb.changeUser("nonExistingUser", null);
      }
      catch (final MssException e) {
         assertEquals(de.mss.autologout.exception.ErrorCodes.ERROR_INVALID_USER, e.getError());
      }

      final ChangeUserBody data = new ChangeUserBody();
      data.setCounterMaxValues(getCounterMaxValues(10, 70));

      this.userDb.changeUser("demouser", data);
      List<UserConfig> uc = this.userDb.getUsersAndConfig();
      assertEquals(Integer.valueOf(1), uc.size());
      assertEquals("demouser", uc.get(0).getUsername());
      assertEquals(Integer.valueOf(10), uc.get(0).getDailyValue());
      assertEquals(Integer.valueOf(70), uc.get(0).getWeeklyValue());
      AutoLogoutCounter alc = this.userDb.loadUser("demouser");
      assertNull(alc.getCounterValues());

      data.setCounterMaxValues(null);
      data.setSpecialValues(new ArrayList<>());
      SpecialValue sv = new SpecialValue();
      sv.setDate(DateTimeTools.now());
      sv.setMinutes(30);
      data.getSpecialValues().add(sv);
      sv = new SpecialValue();
      sv.setDate(DateTimeTools.getYesterdayDate());
      sv.setDelete(true);
      data.getSpecialValues().add(sv);
      sv = new SpecialValue();
      sv.setDate(DateTimeTools.addDate(-2, Calendar.DAY_OF_YEAR));
      sv.setMinutes(null);
      sv.setLock(true);
      sv.setReason("a simple reason");
      data.getSpecialValues().add(sv);
      sv = new SpecialValue();
      sv.setDate(DateTimeTools.addDate(-3, Calendar.DAY_OF_YEAR));
      sv.setMinutes(null);
      sv.setLock(true);
      sv
            .setReason(
                  "a very good reason which is longer than fitting into the database 3454364675676876tezurzer5rqw4rycbhjnur897hw56wge5fdrxcfgvbnij8678554w45bvrgfd");
      data.getSpecialValues().add(sv);

      this.userDb.changeUser("demouser", data);
      uc = this.userDb.getUsersAndConfig();
      assertEquals(Integer.valueOf(1), uc.size());
      assertEquals("demouser", uc.get(0).getUsername());
      assertEquals(Integer.valueOf(10), uc.get(0).getDailyValue());
      assertEquals(Integer.valueOf(70), uc.get(0).getWeeklyValue());
      alc = this.userDb.loadUser("demouser");
      assertEquals(Integer.valueOf(30), alc.getDailyCounter().getMaxMinutes());
   }


   @Test
   public void testDeleteUser() throws MssException {
      try {
         this.userDb.deleteUser("nonExistingUser");
      }
      catch (final MssException e) {
         assertEquals(de.mss.autologout.exception.ErrorCodes.ERROR_INVALID_USER, e.getError());
      }

      this.userDb.deleteUser("demouser");
      final List<UserConfig> uc = this.userDb.getUsersAndConfig();
      assertEquals(Integer.valueOf(0), uc.size());
   }


   @Test
   public void testDeleteUserNook() throws ClassNotFoundException, SQLException {
      execute("drop table CONFIG_USER_SPECIAL");
      try {
         this.userDb.deleteUser("demouser");
      }
      catch (final MssException e) {
         assertEquals(de.mss.autologout.exception.ErrorCodes.ERROR_DELETE_USER, e.getError());
      }
   }


   @Test
   public void testGetUsers() throws MssException {
      final List<String> users = this.userDb.getUsers();

      assertNotNull(users);
      assertEquals(Integer.valueOf(1), Integer.valueOf(users.size()));
      assertEquals("demouser", users.get(0));
   }


   @Test
   public void testGetUsersAndConfig() throws MssException {
      final List<UserConfig> uc = this.userDb.getUsersAndConfig();

      assertNotNull(uc);
      assertEquals(Integer.valueOf(1), Integer.valueOf(uc.size()));
      assertEquals("demouser", uc.get(0).getUsername());
      assertEquals(Integer.valueOf(15), uc.get(0).getDailyValue());
      assertEquals(Integer.valueOf(175), uc.get(0).getWeeklyValue());
   }


   @Test
   public void testIsUserPresent() throws MssException, ClassNotFoundException, SQLException {
      assertFalse(this.userDb.isUserPresent("testUser"));
      assertTrue(this.userDb.isUserPresent("demouser"));

      execute("drop table CONFIG_USER");

      try {
         this.userDb.isUserPresent("demouser");
      }
      catch (final MssException e) {
         assertEquals(de.mss.autologout.exception.ErrorCodes.ERROR_INVALID_USER, e.getError());
      }
   }


   @Test
   public void testLoadUser() throws MssException {
      assertNull(this.userDb.loadUser("noUser"));

      final AutoLogoutCounter alc = this.userDb.loadUser("demouser");
      checkAutoLogoutCounter(alc, 15, 175);
   }
}
