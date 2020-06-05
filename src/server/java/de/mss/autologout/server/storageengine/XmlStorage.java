package de.mss.autologout.server.storageengine;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.mss.autologout.param.AutoLogoutCounter;
import de.mss.autologout.server.LogoutCounter;
import de.mss.configtools.ConfigFile;
import de.mss.configtools.XmlConfigFile;
import de.mss.utils.DateTimeTools;
import de.mss.utils.exception.ErrorCodes;
import de.mss.utils.exception.MssException;

public class XmlStorage implements StorageEngine {

   private static final long serialVersionUID = 8468247047936897983L;

   private static final String DB_BASE_KEY           = "autologout.";

   public static final String  CFG_KEY_BASE          = "de.mss.autologout";
   private static final String CFG_KEY_DB_FILE       = CFG_KEY_BASE + ".dbfile";
   private static final String DB_KEY_DAILY_MINUTES  = ".minutes.daily";
   private static final String DB_KEY_WEEKLY_MINUTES = ".minutes.weekly";

   private static final SimpleDateFormat DB_DATE_FORMAT        = new SimpleDateFormat("yyyyMMdd");
   private static final SimpleDateFormat DB_DATETIME_FORMAT    = new SimpleDateFormat("yyyyMMddHHmmssSSS");

   private ConfigFile dbFile = null;
   private ConfigFile cfg    = null;


   public XmlStorage(ConfigFile v) {
      this.cfg = v;
      this.dbFile = new XmlConfigFile(v.getValue(CFG_KEY_DB_FILE, "autologout.db"));
   }

   
   @Override
   public AutoLogoutCounter loadUser(String userName) throws MssException {
      LogoutCounter dailyCounter = new LogoutCounter(
            this.cfg.getValue(CFG_KEY_BASE + "." + userName + DB_KEY_DAILY_MINUTES, BigInteger.valueOf(30)).intValue(),
            "tägliche");
      LogoutCounter weeklyCounter = new LogoutCounter(
            this.cfg.getValue(CFG_KEY_BASE + "." + userName + DB_KEY_WEEKLY_MINUTES, BigInteger.valueOf(240)).intValue(),
            "wöchentliche");

      java.util.Date checkDate = new java.util.Date();

      AutoLogoutCounter alc = new AutoLogoutCounter();
      alc.setCounterValues(new java.util.TreeMap<>());

      int minutesDaily = this.dbFile.getValue(getKey(userName, checkDate), BigInteger.ZERO).intValue();
      if (minutesDaily == 0)
         minutesDaily = this.dbFile.getValue(getOldKey(userName, checkDate), BigInteger.ZERO).intValue();
      int minutesWeekly = minutesDaily;
      for (int i = 1; i < 7; i++ ) {
         checkDate = DateTimeTools.addDate(checkDate, -1, Calendar.DAY_OF_MONTH);
         int tmp = this.dbFile.getValue(getKey(userName, checkDate), BigInteger.ZERO).intValue();
         if (tmp == 0)
            tmp = this.dbFile.getValue(getOldKey(userName, checkDate), BigInteger.ZERO).intValue();

         minutesWeekly += tmp;
         alc
               .getCounterValues()
               .put(
                     DB_DATE_FORMAT.format(checkDate),
                     this.dbFile.getValue(getKey(userName, checkDate), BigInteger.ZERO));
      }

      dailyCounter.addMinutes(minutesDaily);
      dailyCounter.setDate(DB_DATE_FORMAT.format(new java.util.Date()));
      weeklyCounter.addMinutes(minutesWeekly);

      alc.setDailycounter(dailyCounter);
      alc.setWeeklyCounter(weeklyCounter);

      return alc;
   }


   @Override
   public void storeUser(String userName, AutoLogoutCounter counters) throws MssException {
      if (counters == null || counters.getDailyCounter() == null || counters.getDailyCounter().getMaxMinutes() <= 0)
         return;

      this.dbFile.insertKeyValue(getKey(userName, new java.util.Date()), "" + counters.getDailyCounter().getCurrentMinutes());
      try {
         this.dbFile.writeConfig(this.cfg.getValue(CFG_KEY_DB_FILE, "autologout.db"));
      }
      catch (IOException e) {
         throw new MssException(ErrorCodes.ERROR_DB_CLOSE_FAILURE, e, "could not write to db file");
      }
   }


   @Override
   public void storeUser(String userName, AutoLogoutCounter counters, String reason) throws MssException {
      this.dbFile
            .insertKeyValue(
                  DB_BASE_KEY + userName + "." + DB_DATETIME_FORMAT.format(new java.util.Date()),
                  counters.getDailyCounter().getCurrentMinutes() + " - " + reason);
      try {
         this.dbFile.writeConfig(this.cfg.getValue(CFG_KEY_DB_FILE, "autologout.db"));
      }
      catch (IOException e) {
         throw new MssException(ErrorCodes.ERROR_DB_CLOSE_FAILURE, e, "could not write to db file");
      }
   }


   private static String getOldKey(String userName, java.util.Date checkDate) {
      return DB_BASE_KEY + userName + ".D" + DB_DATE_FORMAT.format(checkDate);
   }


   private static String getKey(String userName, java.util.Date checkDate) {
      return DB_BASE_KEY + userName + "." + DB_DATE_FORMAT.format(checkDate);
   }
}
