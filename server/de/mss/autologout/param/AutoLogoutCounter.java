package de.mss.autologout.param;

import java.math.BigInteger;
import java.util.Map;

import de.mss.autologout.server.LogoutCounter;
import de.mss.utils.DateTimeTools;

public class AutoLogoutCounter {

   private LogoutCounter           dailyCounter;
   private LogoutCounter           weeklyCounter;
   private Map<String, BigInteger> counterValues;
   private boolean                 lockedForToday = false;
   private String                  reasonForLock  = null;
   private final java.util.Date    currentDate    = new java.util.Date();


   public AutoLogoutCounter() {}


   public AutoLogoutCounter(LogoutCounter dailyCounter, LogoutCounter weeklyCounter) {
      setDailycounter(dailyCounter);
      setWeeklyCounter(weeklyCounter);
   }


   public void setWeeklyCounter(LogoutCounter w) {
      this.weeklyCounter = w;
   }


   public void setDailycounter(LogoutCounter d) {
      this.dailyCounter = d;
   }


   public LogoutCounter getWeeklyCounter() {
      return this.weeklyCounter;
   }


   public LogoutCounter getDailyCounter() {
      return this.dailyCounter;
   }


   public void setCounterValues(Map<String, BigInteger> l) {
      this.counterValues = l;
   }


   public Map<String, BigInteger> getCounterValues() {
      return this.counterValues;
   }


   public void setLockedForToday(boolean v) {
      this.lockedForToday = v;
   }


   public boolean getLockedForToday() {
      return this.lockedForToday;
   }


   public void setReasonForLocked(String v) {
      this.reasonForLock = v;
   }


   public String getReasonForLock() {
      return this.reasonForLock;
   }


   public java.util.Date getCurrentDate() {
      return this.currentDate;
   }


   public boolean isSameDay() {
      return DateTimeTools.isSameDay(this.currentDate, new java.util.Date());
   }
}

