package de.mss.autologout.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.mss.autologout.client.param.AddUserBody;
import de.mss.autologout.client.param.ChangeUserBody;
import de.mss.autologout.client.param.CounterMaxValues;
import de.mss.autologout.client.param.SpecialValue;
import de.mss.autologout.client.param.WorkingTime;
import de.mss.autologout.common.db.param.UserConfig;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.counter.LogoutCounter;
import de.mss.autologout.counter.WorkingTimeChecker;
import de.mss.autologout.defs.Defs;
import de.mss.autologout.enumeration.Weekday;
import de.mss.utils.DateTimeTools;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;

public class UserDb implements Serializable {


   private static final long serialVersionUID = 5747704806558656489L;


   private Connection dbCon = null;

   private String     dbUrl = null;

   public UserDb(String url) throws MssException {
      init(url);
   }


   public void addUser(AddUserBody data) throws MssException {
      if (isUserPresent(data.getUserName())) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_USER_ALREADY_EXISTS);
      }

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt
               .executeUpdate(
                     "insert into CONFIG_USER (USERNAME, DAILY_MINUTES, WEEKLY_MINUTES) values ('"
                           + data.getUserName()
                           + "', "
                           + data.getCounterMaxValues().getDailyMinutes().toString()
                           + ", "
                           + data.getCounterMaxValues().getWeeklyMinutes().toString()
                           + ");");

         if (data.getWorkingTimes() != null) {
            for (final WorkingTime wt : data.getWorkingTimes()) {
               if (wt.getWeekday() != null) {
                  stmt
                        .executeUpdate(
                              "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                                    + data.getUserName()
                                    + "', '"
                                    + wt.getWeekday().getDbValue()
                                    + "', '"
                                    + wt.getFrom()
                                    + "', '"
                                    + wt.getUntil()
                                    + "');");
               } else if (wt.getDate() != null) {
                  stmt
                        .executeUpdate(
                              "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                                    + data.getUserName()
                                    + "', '"
                                    + new SimpleDateFormat("yyyy-MM-dd").format(wt.getDate())
                                    + "', '"
                                    + wt.getFrom()
                                    + "', '"
                                    + wt.getUntil()
                                    + "');");
               }
            }
         } else {
            stmt
                  .executeUpdate(
                        "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + data.getUserName()
                              + "', 'Mo', '07:30', '20:00');");
            stmt
                  .executeUpdate(
                        "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + data.getUserName()
                              + "', 'Di', '07:30', '20:00');");
            stmt
                  .executeUpdate(
                        "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + data.getUserName()
                              + "', 'Mi', '07:30', '20:00');");
            stmt
                  .executeUpdate(
                        "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + data.getUserName()
                              + "', 'Do', '07:30', '20:00');");
            stmt
                  .executeUpdate(
                        "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + data.getUserName()
                              + "', 'Fr', '07:30', '20:00');");
            stmt
                  .executeUpdate(
                        "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + data.getUserName()
                              + "', 'Sa', '07:30', '20:00');");
            stmt
                  .executeUpdate(
                        "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + data.getUserName()
                              + "', 'So', '07:30', '20:00');");
         }
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not add user " + data.getUserName());
      }
   }


   public void changeUser(String userName, ChangeUserBody data) throws MssException {
      if (!isUserPresent(userName)) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INVALID_USER, "Could not change user " + userName);
      }

      if (data.getCounterMaxValues() != null) {
         changeUserCounterMaxValues(userName, data.getCounterMaxValues());
      }

      if (data.getSpecialValues() != null) {
         changeUserSpecialValues(userName, data.getSpecialValues());
      }
      if (data.getWorkingTimes() != null) {
         changeWorkingTimes(userName, data.getWorkingTimes());
      }
   }


   private void changeUserCounterMaxValues(String userName, CounterMaxValues values) throws MssException {
      final String sql = "update CONFIG_USER set DAILY_MINUTES = "
            + values.getDailyMinutes()
            + ", WEEKLY_MINUTES = "
            + values.getWeeklyMinutes()
            + " where USERNAME = '"
            + userName
            + "';";

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate(sql);
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not change max values for user " + userName);
      }
   }


   private void changeUserSpecialValue(String userName, SpecialValue sv) throws MssException {
      final StringBuilder sql = new StringBuilder();

      if (Tools.isTrue(sv.getDelete())) {
         sql
               .append(
                     "delete from CONFIG_USER_SPECIAL where USERNAME = '"
                           + userName
                           + "' and DATE = '"
                           + Defs.DB_DATE_FORMAT.format(sv.getDate())
                           + "';");
      } else {
         sql
               .append(
                     "insert or replace into CONFIG_USER_SPECIAL (USERNAME, DATE, MINUTES, LOCK, REASON) values ('"
                           + userName
                           + "', '"
                           + Defs.DB_DATE_FORMAT.format(sv.getDate())
                           + "', ");
         if (sv.getMinutes() != null) {
            sql.append(sv.getMinutes().toString() + ", ");
         } else {
            sql.append("null, ");
         }

         if (sv.getLock() != null) {
            sql.append("'" + sv.getLock().toString() + "', ");
         } else {
            sql.append("null, ");
         }

         if (sv.getReason() == null) {
            sql.append("''");
         } else if (sv.getReason().length() > 100) {
            sql.append("'" + sv.getReason().substring(0, 100) + "'");
         } else {
            sql.append("'" + sv.getReason() + "'");
         }

         sql.append(");");
      }

      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate(sql.toString());
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not save special value for user " + userName);
      }
   }


   private void changeUserSpecialValues(String userName, List<SpecialValue> specialValues) throws MssException {
      for (final SpecialValue sv : specialValues) {
         changeUserSpecialValue(userName, sv);
      }
   }


   private void changeWorkingTimes(String userName, List<WorkingTime> workingTimes) throws MssException {
      final StringBuilder sql = new StringBuilder();
      for (final WorkingTime wt : workingTimes) {
         if (wt.getWeekday() != null) {
            sql
                  .append(
                        "insert or replace into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + userName
                              + "', '"
                              + wt.getWeekday().getDbValue()
                              + "', '"
                              + wt.getFrom()
                              + "', '"
                              + wt.getUntil()
                              + "');");
         } else if (wt.getDate() != null) {
            sql
                  .append(
                        "insert or replace into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) values ('"
                              + userName
                              + "', '"
                              + new SimpleDateFormat("yyyy-MM-dd").format(wt.getDate())
                              + "', '"
                              + wt.getFrom()
                              + "', '"
                              + wt.getUntil()
                              + "');");
         }
      }
      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate(sql.toString());
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_CHANGE_USER, e, "Could not save special value for user " + userName);
      }
   }


   private void closeDbCon() {
      try {
         if ((this.dbCon != null) && !this.dbCon.isClosed()) {
            this.dbCon.close();
         }
      }
      catch (final SQLException e) {
         Tools.doNullLog(e);
      }
   }


   public void deleteUser(String userName) throws MssException {
      if (!isUserPresent(userName)) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INVALID_USER, "Could not delete user " + userName);
      }
      try (Statement stmt = this.dbCon.createStatement()) {
         stmt.executeUpdate("delete from USER_WORKING_TIMES where USERNAME = '" + userName + "';");
         stmt.executeUpdate("delete from CONFIG_USER_SPECIAL where USERNAME = '" + userName + "';");
         stmt.executeUpdate("delete from CONFIG_USER where USERNAME = '" + userName + "';");
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_DELETE_USER, e, "Could not delete user " + userName);
      }
   }


   private List<WorkingTime> fillDefaultWorkingTimes(List<WorkingTime> workingTimes) {
      for (final Weekday w : Weekday.values()) {
         fillDefaultWorkingTimes(workingTimes, w);
      }
      return workingTimes;
   }


   private void fillDefaultWorkingTimes(List<WorkingTime> workingTimes, Weekday weekday) {
      for (final WorkingTime wt : workingTimes) {
         if ((wt.getWeekday() != null) && (wt.getWeekday() == weekday)) {
            return;
         }
      }

      final WorkingTime wt = new WorkingTime();
      wt.setWeekday(weekday);
      wt.setFrom("07:30");
      wt.setUntil("20:00");
      workingTimes.add(wt);
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


   public List<UserConfig> getUsersAndConfig() throws MssException {
      final List<UserConfig> ret = new ArrayList<>();

      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt.executeQuery("select distinct USERNAME, DAILY_MINUTES, WEEKLY_MINUTES from CONFIG_USER order by USERNAME;");
      ) {
         while (res.next()) {
            final UserConfig uc = new UserConfig();
            uc.setUsername(res.getString("USERNAME"));
            uc.setDailyValue(res.getInt("DAILY_MINUTES"));
            uc.setWeeklyValue(res.getInt("WEEKLY_MINUTES"));
            uc.setWorkingTimes(getWorkingTimes(uc.getUsername()));
            ret.add(uc);
         }
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_LOADING_USER, e, "Could not load users");
      }

      return ret;
   }


   private List<WorkingTime> getWorkingTimes(String username) throws MssException {
      final List<WorkingTime> ret = new ArrayList<>();

      final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

      //@formatter:off
      String sql = "select DAY, VALID_FROM, VALID_UNTIL "
                 + "from USER_WORKING_TIMES "
                 + "where USERNAME = '" + username + "' "
                 + "and DAY in ('Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So');"
                 ;
      //@formatter:on

      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt.executeQuery(sql);
      ) {
         while (res.next()) {
            final WorkingTime wt = new WorkingTime();
            wt.setWeekday(Weekday.getByDbValue(res.getString("DAY")));
            wt.setFrom(res.getString("VALID_FROM"));
            wt.setUntil(res.getString("VALID_UNTIL"));
            ret.add(wt);
         }
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_LOADING_USER, e, "Could not load users");
      }

      //@formatter:off
      sql = "select DAY, VALID_FROM, VALID_UNTIL "
                 + "from USER_WORKING_TIMES "
                 + "where USERNAME = '" + username + "' "
                 + "and DAY not in ('Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So') "
                 + "and DAY >= '" + date + "' "
                 + "order by DAY asc;"
                 ;
      //@formatter:on

      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt.executeQuery(sql);
      ) {
         while (res.next()) {
            final WorkingTime wt = new WorkingTime();
            wt.setDate(DateTimeTools.parseString2Date(res.getString("DAY")));
            wt.setFrom(res.getString("VALID_FROM"));
            ret.add(wt);
         }
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_LOADING_USER, e, "Could not load users");
      }

      return fillDefaultWorkingTimes(ret);
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

         stmt
               .executeUpdate(
                     "create table if not exists USER_WORKING_TIMES (USERNAME varchar(50) not null, DAY varchar(10) not null, VALID_FROM varchar(5) not null, VALID_UNTIL varchar(5) not null, primary key (USERNAME, DAY));");

         stmt
               .executeUpdate(
                     "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) select 'demouser', 'Mo', '07:30', '20:00' where not exists (select 1 from USER_WORKING_TIMES where USERNAME = 'demouser' and DAY = 'Mo');");
         stmt
               .executeUpdate(
                     "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) select 'demouser', 'Di', '07:30', '20:00' where not exists (select 1 from USER_WORKING_TIMES where USERNAME = 'demouser' and DAY = 'Di');");
         stmt
               .executeUpdate(
                     "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) select 'demouser', 'Mi', '07:30', '20:00' where not exists (select 1 from USER_WORKING_TIMES where USERNAME = 'demouser' and DAY = 'Mi');");
         stmt
               .executeUpdate(
                     "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) select 'demouser', 'Do', '07:30', '20:00' where not exists (select 1 from USER_WORKING_TIMES where USERNAME = 'demouser' and DAY = 'Do');");
         stmt
               .executeUpdate(
                     "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) select 'demouser', 'Fr', '07:30', '20:00' where not exists (select 1 from USER_WORKING_TIMES where USERNAME = 'demouser' and DAY = 'Fr');");
         stmt
               .executeUpdate(
                     "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) select 'demouser', 'Sa', '07:30', '20:00' where not exists (select 1 from USER_WORKING_TIMES where USERNAME = 'demouser' and DAY = 'Sa');");
         stmt
               .executeUpdate(
                     "insert into USER_WORKING_TIMES (USERNAME, DAY, VALID_FROM, VALID_UNTIL) select 'demouser', 'So', '07:30', '20:00' where not exists (select 1 from USER_WORKING_TIMES where USERNAME = 'demouser' and DAY = 'So');");
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INIT_USERDB, e, "could not prepare user db");
      }
   }


   public boolean isUserPresent(String userName) throws MssException {
      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt.executeQuery("select count(*) as COUNT from CONFIG_USER where USERNAME = '" + userName + "';")
      ) {
         int count = 0;
         if ((res != null) && res.next()) {
            count = res.getInt("COUNT");
         }

         return count == 1;
      }
      catch (final SQLException e) {
         throw new MssException(de.mss.autologout.exception.ErrorCodes.ERROR_INVALID_USER, e, "Could not find user " + userName);
      }
   }


   public AutoLogoutCounter loadUser(String userName) throws MssException {
      AutoLogoutCounter ret = null;

      try (
           Statement stmt = this.dbCon.createStatement();
           ResultSet res = stmt.executeQuery("select * from CONFIG_USER where USERNAME = '" + userName + "';")
      ) {

         if ((res == null) || !res.next()) {
            return null;
         }

         ret = new AutoLogoutCounter();
         ret.setDailycounter(new LogoutCounter(res.getInt("DAILY_MINUTES"), "tägliche"));
         ret.setWeeklyCounter(new LogoutCounter(res.getInt("WEEKLY_MINUTES"), "wöchentliche"));
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
         if ((res != null) && res.next()) {
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

      ret.setWorkingTimeChecker(new WorkingTimeChecker(getWorkingTimes(userName)));

      return ret;
   }
}
