package de.mss.autologout.server;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.TreeMap;

import de.mss.autologout.param.AutoLogoutCounter;
import de.mss.utils.DateTimeTools;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;

public class WorkDb implements Serializable {


   private static final long serialVersionUID = 5747704806558656489L;


   private Connection dbCon = null;
   private String     dbUrl = null;

   public WorkDb(String url) throws MssException {
      init(url);
   }


   private void init(String url) throws MssException {
      this.dbUrl = url;
      try {
         Class.forName("org.sqlite.JDBC");

         initConnection();

         initDatabase();
      }
      catch (final ClassNotFoundException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INIT_WORKDB, e, "could not init work db");
      }
   }


   private void initConnection() throws MssException {
      if (this.dbCon != null) {
         return;
      }

      try {
         this.dbCon = DriverManager.getConnection("jdbc:sqlite:" + this.dbUrl);
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INIT_WORKDB, e, "could not connect to work db");
      }

      Runtime.getRuntime().addShutdownHook(new Thread() {

         @Override
         public void run() {
            closeDbCon();
         }
      });
   }


   private void closeDbCon() {
      try {
         if (this.dbCon != null && !this.dbCon.isClosed()) {
            this.dbCon.close();
         }
      }
      catch (final SQLException e) {
         Tools.doNullLog(e);
      }
   }


   private void initDatabase() throws MssException {
      initConnection();

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt
               .executeUpdate(
                     "create table if not exists USER_TIMES (USERNAME varchar(50) not null, DATE varchar(30) not null, MINUTES int not null default 0, REASON varchar(255) null, primary key (USERNAME, DATE));");
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INIT_WORKDB, e, "could not prepare work db");
      }
   }


   public AutoLogoutCounter loadUser(String userName, AutoLogoutCounter alc) throws MssException {
      if (alc == null) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_LOADING_USER, "could not access Counters");
      }

      java.util.Date checkDate = new java.util.Date();
      alc.setCounterValues(new TreeMap<>());
      alc.getDailyCounter().addMinutes(getDailyValue(userName, checkDate));

      for (int i = 1; i < 7; i++ ) {
         checkDate = DateTimeTools.addDate(checkDate, -1, Calendar.DAY_OF_MONTH);
         final int minutes = getDailyValue(userName, checkDate);
         alc.getWeeklyCounter().addMinutes(minutes);
         alc.getCounterValues().put(AutoLogoutServer.DB_DATE_FORMAT.format(checkDate), BigInteger.valueOf(minutes));
      }

      return alc;
   }


   private int getDailyValue(String userName, java.util.Date date) throws MssException {
      try (
           PreparedStatement stmt = this.dbCon
                 .prepareStatement(
                       "select MINUTES from USER_TIMES where USERNAME = '"
                             + userName
                             + "' and DATE = '"
                             + AutoLogoutServer.DB_DATE_FORMAT.format(date)
                             + "'");
           ResultSet res = stmt.executeQuery();
      ) {
         if (res.next()) {
            return res.getInt("MINUTES");
         }
      }
      catch (final SQLException e) {
         throw new MssException(e);
      }

      return 0;
   }


   public void saveTime(String userName, AutoLogoutCounter counters) throws MssException {
      if (counters == null || counters.getDailyCounter() == null || counters.getDailyCounter().getMaxMinutes() <= 0) {
         return;
      }

      try (
           PreparedStatement stmt = this.dbCon
                 .prepareStatement(
                       "insert or replace into USER_TIMES (USERNAME, DATE, MINUTES) values ('"
                             + userName
                             + "', '"
                             + AutoLogoutServer.DB_DATE_FORMAT.format(new java.util.Date())
                             + "', "
                             + counters.getDailyCounter().getCurrentMinutes()
                             + ");")
      ) {
         stmt.executeUpdate();
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_SAVING_TIME, e, "could not update USER_TIMES");
      }
   }


   public void saveSpecialTime(String userName, java.util.Date date, Integer minutes, String reason) throws MssException {
      String sql;
      if (minutes == null && reason == null) {
         sql = "delete from USER_TIMES where USERNAME = '" + userName + "' and DATE = '" + AutoLogoutServer.DB_DATETIME_FORMAT.format(date) + "';";
      } else {
         sql = "insert or replace into USER_TIMES (USERNAME, DATE, MINUTES, REASON) values ('"
               + userName
               + "', '"
               + AutoLogoutServer.DB_DATETIME_FORMAT.format(date)
               + "', "
               + (minutes != null
                     ? minutes.toString()
                     : "null")
               + ", '"
               + (reason != null
                     ? reason
                     : "null")
               + "');";
      }

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate(sql.toString());
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_SAVING_TIME, e, "Could not save special time for user " + userName);
      }
   }
}
