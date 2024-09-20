package de.mss.autologout.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.defs.Defs;
import de.mss.utils.DateTimeTools;
import de.mss.utils.exception.MssException;

public class WorkDbTest {

   private static final String USER_NAME = "demouser";
   private WorkDb              workDb;

   private UserDb              userDb;

   private Connection          dbCon     = null;

   private void check(Map<String, BigInteger> values, Integer[] checkValues, int dayOffset) {
      assertNotNull(values);
      assertEquals(Integer.valueOf(checkValues.length), Integer.valueOf(values.size()));

      for (int i = 0; i < checkValues.length; i++ ) {
         final String checkDate = Defs.DB_DATE_FORMAT.format(DateTimeTools.addDate(dayOffset - i, Calendar.DAY_OF_YEAR));
         assertEquals(BigInteger.valueOf(checkValues[i]), values.get(checkDate));
      }
   }


   private void generateDefaultWorkValues() throws ClassNotFoundException, SQLException {
      openConnection();

      Date date = DateTimeTools.getYesterdayDate();
      for (int i = 0; i < 10; i++ ) {
         final String sql = "insert into USER_TIMES (USERNAME, DATE, MINUTES) values ('"
               + USER_NAME
               + "', '"
               + Defs.DB_DATE_FORMAT.format(date)
               + "', 10);";
         try (Statement stmt = this.dbCon.createStatement()) {
            stmt.executeUpdate(sql);
         }
         date = DateTimeTools.addDate(date, -1, Calendar.DAY_OF_YEAR);
      }
   }


   private void openConnection() throws ClassNotFoundException, SQLException {
      if (this.dbCon != null) {
         return;
      }

      Class.forName("org.sqlite.JDBC");
      this.dbCon = DriverManager.getConnection("jdbc:sqlite:workdb-test.sqlite3");
   }


   @BeforeEach
   public void setUp() throws MssException {
      File f = new File("userdb-test.sqlite3");
      if (f.exists()) {
         f.delete();
      }

      this.userDb = new UserDb("userdb-test.sqlite3");

      f = new File("workdb-test.sqlite3");
      if (f.exists()) {
         f.delete();
      }

      this.workDb = new WorkDb("workdb-test.sqlite3");
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
   public void testLoadUser() throws MssException, ClassNotFoundException, SQLException {
      try {
         this.workDb.loadUser(USER_NAME, null);
         fail();
      }
      catch (final MssException e) {
         assertEquals(de.mss.autologout.exception.ErrorCodes.ERROR_LOADING_USER, e.getError());
      }
      generateDefaultWorkValues();
      final AutoLogoutCounter alc = this.userDb.loadUser(USER_NAME);
      final AutoLogoutCounter alcOut = this.workDb.loadUser(USER_NAME, alc);

      assertEquals(Integer.valueOf(0), alcOut.getDailyCounter().getCurrentMinutes());
      assertEquals(Integer.valueOf(60), alcOut.getWeeklyCounter().getCurrentMinutes());
      assertNotNull(alcOut.getCounterValues());
      assertEquals(Integer.valueOf(6), Integer.valueOf(alcOut.getCounterValues().size()));
      check(alcOut.getCounterValues(), new Integer[] {10, 10, 10, 10, 10, 10}, -1);
   }


   @Test
   public void testLoadUserValues() throws ClassNotFoundException, SQLException, MssException {
      generateDefaultWorkValues();
      final Map<String, BigInteger> values = this.workDb.loadUserValues(USER_NAME, 14);

      check(values, new Integer[] {0, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 0, 0, 0}, 0);
   }


   @Test
   public void testSaveSpecialValues() throws MssException {
      this.workDb.saveSpecialTime(USER_NAME, DateTimeTools.getYesterdayDate(), null, null);
      this.workDb.saveSpecialTime(USER_NAME, DateTimeTools.getYesterdayDate(), 10, "Test");
   }


   @Test
   public void testSaveTime() throws ClassNotFoundException, SQLException, MssException {
      generateDefaultWorkValues();
      this.workDb.saveTime(USER_NAME, 20, DateTimeTools.getYesterdayDate());

      Map<String, BigInteger> values = this.workDb.loadUserValues(USER_NAME, 4);
      check(values, new Integer[] {0, 20, 10, 10}, 0);

      this.workDb.saveTime(USER_NAME, null);
      values = this.workDb.loadUserValues(USER_NAME, 4);
      check(values, new Integer[] {0, 20, 10, 10}, 0);

      AutoLogoutCounter alc = this.userDb.loadUser(USER_NAME);
      alc.setDailycounter(null);
      this.workDb.saveTime(USER_NAME, alc);
      values = this.workDb.loadUserValues(USER_NAME, 4);
      check(values, new Integer[] {0, 20, 10, 10}, 0);

      alc = this.userDb.loadUser(USER_NAME);
      alc.getDailyCounter().setMaxMinutes(0);
      this.workDb.saveTime(USER_NAME, alc);
      values = this.workDb.loadUserValues(USER_NAME, 4);
      check(values, new Integer[] {0, 20, 10, 10}, 0);

      alc = this.userDb.loadUser(USER_NAME);
      alc.getDailyCounter().setMaxMinutes(20);
      alc.getDailyCounter().addMinutes(12);
      this.workDb.saveTime(USER_NAME, alc);
      values = this.workDb.loadUserValues(USER_NAME, 4);
      check(values, new Integer[] {12, 20, 10, 10}, 0);
   }
}
