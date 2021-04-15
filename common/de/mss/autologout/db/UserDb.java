package de.mss.autologout.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.mss.autologout.client.param.ModifyUserBody;
import de.mss.autologout.client.param.UserSpecialValue;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.counter.LogoutCounter;
import de.mss.autologout.defs.Defs;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;

public class UserDb implements Serializable {


   private static final long serialVersionUID = 5747704806558656489L;


   private Connection dbCon = null;

   private String     dbUrl = null;

   public UserDb(String url) throws MssException {
      init(url);
   }


   private void addUser(String userName, ModifyUserBody data) throws MssException {
      if (data == null || data.getDailyMinutes() == null || data.getWeeklyMinutes() == null) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, "incomplete data for adding new user");
      }

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt
               .executeUpdate(
                     "insert into CONFIG_USER (USERNAME, DAILY_MINUTES, WEEKLY_MINUTES) values ('"
                           + userName
                           + "', "
                           + data.getDailyMinutes().toString()
                           + ", "
                           + data.getWeeklyMinutes().toString()
                           + ");");
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not add user " + userName);
      }
   }


   public void changeUser(String userName, ModifyUserBody data) throws MssException {
      initDatabase();

      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt.executeQuery("select count(*) as COUNT from CONFIG_USER where USERNAME = '" + userName + "';")
      ) {
         if (res == null || !res.next() || res.getInt("COUNT") == 0) {
            addUser(userName, data);
         } else if (data == null) {
            deleteUser(userName);
            return;
         } else {
            modifyUser(userName, data);
         }

         if (data != null && data.getSpecialValues() != null) {
            saveSpecialValues(userName, data.getSpecialValues());
         }
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not change user " + userName);
      }
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


   private void deleteUser(String userName) throws MssException {
      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate("delete from CONFIG_USER_SPECIAL where USERNAME = '" + userName + "';");
         stmt.executeUpdate("delete from CONFIG_USER where USERNAME = '" + userName + "';");
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not delete user " + userName);
      }
   }


   public List<String> getUsers() throws MssException {
      final List<String> ret = new ArrayList<>();

      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt.executeQuery("select distinct USERNAME from CONFIG_USER order by USERNAME;");
      ) {
         while (res.next()) {
            ret.add(res.getString("USERNAME"));
         }
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_LOADING_USER, e, "Could not load users");
      }

      return ret;
   }


   private void init(String url) throws MssException {
      try {
         Class.forName("org.sqlite.JDBC");
         this.dbUrl = url;

         initConnection();

         initDatabase();
      }
      catch (final ClassNotFoundException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INIT_USERDB, e, "could not init user db");
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
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INIT_USERDB, e, "could not connect to user db");
      }

      Runtime.getRuntime().addShutdownHook(new Thread() {

         @Override
         public void run() {
            closeDbCon();
         }
      });
   }


   private void initDatabase() throws MssException {
      initConnection();

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt
               .executeUpdate(
                     "create table if not exists CONFIG_USER (USERNAME varchar(50) not null primary key, DAILY_MINUTES int not null default 30, WEEKLY_MINUTES int not null default 350);");

         stmt
               .executeUpdate(
                     "create table if not exists CONFIG_USER_SPECIAL (USERNAME varchar(50) not null, DATE varchar(10) not null, MINUTES int null, LOCK boolean null, REASON varchar(100) not null, primary key(USERNAME, DATE));");

         stmt
               .executeUpdate(
                     "insert into CONFIG_USER (USERNAME, DAILY_MINUTES, WEEKLY_MINUTES) select 'demouser', 15, 175 where not exists (select 1 from CONFIG_USER where USERNAME = 'demouser');");
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INIT_USERDB, e, "could not prepare user db");
      }
   }


   public AutoLogoutCounter loadUser(String userName) throws MssException {
      AutoLogoutCounter ret = null;

      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt.executeQuery("select * from CONFIG_USER where USERNAME = '" + userName + "';")
      ) {

         if (res == null || !res.next()) {
            return null;
         }

         ret = new AutoLogoutCounter();
         ret.setDailycounter(new LogoutCounter(res.getInt("DAILY_MINUTES"), "täglich"));
         ret.setWeeklyCounter(new LogoutCounter(res.getInt("WEEKLY_MINUTES"), "wöchentlich"));
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_LOADING_USER, e, "Could not load user " + userName);
      }


      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt
                 .executeQuery(
                       "select * from CONFIG_USER_SPECIAL where USERNAME = '"
                             + userName
                             + "' and DATE = '"
                             + Defs.DB_DATE_FORMAT.format(new java.util.Date())
                             + "';")
      ) {
         if (res != null && res.next()) {
            ret.setLockedForToday(res.getBoolean("LOCK"));
            if (res.wasNull()) {
               ret.setLockedForToday(false);
            }

            ret.setReasonForLocked(res.getString("REASON"));
            final int min = res.getInt("MINUTES");
            if (!res.wasNull()) {
               ret.getDailyCounter().setMaxMinutes(min);
            }
         }
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_LOADING_USER, e, "Could not load user " + userName);
      }

      return ret;
   }


   private void modifyUser(String userName, ModifyUserBody data) throws MssException {
      if (data == null) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, "incomplete data for modify user");
      }

      if (data.getDailyMinutes() == null && data.getWeeklyMinutes() == null) {
         return;
      }

      final StringBuilder sql = new StringBuilder("update CONFIG_USER set ");
      if (data.getDailyMinutes() != null) {
         sql.append("DAILY_MINUTES = " + data.getDailyMinutes().toString());
      }

      if (data.getDailyMinutes() != null && data.getWeeklyMinutes() != null) {
         sql.append(", ");
      }

      if (data.getWeeklyMinutes() != null) {
         sql.append("WEEKLY_MINUTES = " + data.getWeeklyMinutes().toString());
      }
      sql.append(" where USERNAME = '" + userName + "';");

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate(sql.toString());
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not delete user " + userName);
      }
   }


   private void saveSpecialValue(String userName, UserSpecialValue usv) throws MssException {
      if (usv == null || usv.getDate() == null) {
         return;
      }

      final StringBuilder sql = new StringBuilder();

      if (Tools.isTrue(usv.getDelete())) {
         sql
               .append(
                     "delete from CONFIG_USER_SPECIAL where USERNAME = '"
                           + userName
                           + "' and DATE = '"
                           + Defs.DB_DATE_FORMAT.format(usv.getDate())
                           + "';");
      } else {
         sql
               .append(
                     "insert or replace into CONFIG_USER_SPECIAL (USERNAME, DATE, MINUTES, LOCK, REASON) values ('"
                           + userName
                           + "', '"
                           + Defs.DB_DATE_FORMAT.format(usv.getDate())
                           + "', ");
         if (usv.getMinutes() != null) {
            sql.append(usv.getMinutes().toString() + ", ");
         } else {
            sql.append("null, ");
         }

         if (usv.getLock() != null) {
            sql.append("'" + usv.getLock().toString() + "', ");
         } else {
            sql.append("null, ");
         }

         if (usv.getReason() != null && usv.getReason().length() <= 100) {
            sql.append("'" + usv.getReason() + "');");
         } else if (usv.getReason() != null && usv.getReason().length() > 100) {
            sql.append("'" + usv.getReason().substring(0, 100) + "');");
         } else {
            sql.append("null);");
         }
      }

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate(sql.toString());
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not save special value for user " + userName);
      }
   }


   private void saveSpecialValues(String userName, List<UserSpecialValue> specialValues) throws MssException {
      for (final UserSpecialValue usv : specialValues) {
         saveSpecialValue(userName, usv);
      }
   }
}
