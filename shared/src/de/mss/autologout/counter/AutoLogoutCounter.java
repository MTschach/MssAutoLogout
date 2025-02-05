package de.mss.autologout.counter;

import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;

import de.mss.utils.DateTimeTools;

public class AutoLogoutCounter {

   private LogoutCounter           dailyCounter;
   private LogoutCounter           weeklyCounter;
   private Map<String, BigInteger> counterValues;
   private boolean                 lockedForToday = false;
   private String                  reasonForLock  = null;
   private final java.util.Date    currentDate    = DateTimeTools.now();
   private boolean                 disabled       = false;
   private WorkingTimeChecker      workingTimeChecker;

   public AutoLogoutCounter() {
      setDailycounter(null);
      setWeeklyCounter(null);
   }


   public AutoLogoutCounter(LogoutCounter dailyCounter, LogoutCounter weeklyCounter) {
      setDailycounter(dailyCounter);
      setWeeklyCounter(weeklyCounter);
   }


   public void disable() {
      this.disabled = true;
   }


   public void enable() {
      this.disabled = false;
   }


   public Map<String, BigInteger> getCounterValues() {
      return this.counterValues;
   }


   public java.util.Date getCurrentDate() {
      return this.currentDate;
   }


   public LogoutCounter getDailyCounter() {
      return this.dailyCounter;
   }


   public boolean getLockedForToday() {
      return this.lockedForToday;
   }


   public String getReasonForLock() {
      return this.reasonForLock;
   }


   public LogoutCounter getWeeklyCounter() {
      return this.weeklyCounter;
   }


   public WorkingTimeChecker getWorkingTimeChecker() {
      return this.workingTimeChecker;
   }


   public boolean isDisabled() {
      return this.disabled;
   }


   public boolean isSameDay() {
      return DateTimeTools.isSameDay(this.currentDate, new java.util.Date());
   }


   public void setCounterValues(Map<String, BigInteger> l) {
      this.counterValues = l;
   }


   public void setDailycounter(LogoutCounter d) {
      this.dailyCounter = d != null ? d : new LogoutCounter(30, "täglich");
   }


   public void setLockedForToday(boolean v) {
      this.lockedForToday = v;
   }


   public void setReasonForLocked(String v) {
      this.reasonForLock = v;
   }


   public void setWeeklyCounter(LogoutCounter w) {
      this.weeklyCounter = w != null ? w : new LogoutCounter(210, "wöchentlich");
   }


   public void setWorkingTimeChecker(WorkingTimeChecker wtc) {
      this.workingTimeChecker = wtc;
   }


   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder("{");
      if (this.dailyCounter != null) {
         sb.append("[DailyCounter] " + this.dailyCounter.toString() + " ");
      }
      if (this.weeklyCounter != null) {
         sb.append("[WeeklyCounter] " + this.weeklyCounter.toString() + " ");
      }
      sb.append("[LockedForToday] " + this.lockedForToday + " ");
      if (this.counterValues != null) {
         sb.append("[CounterValues] {");
         for (final Entry<String, BigInteger> entry : this.counterValues.entrySet()) {
            sb.append("{[" + entry.getKey() + "] " + entry.getValue() + " } ");
         }
         sb.append("} ");
      }
      if (this.reasonForLock != null) {
         sb.append("[ReasonForLock] " + this.reasonForLock + " ");
      }
      if (this.workingTimeChecker != null) {
         sb.append("[WorkingTime] " + this.workingTimeChecker.toString() + " ");
      }
      sb.append("} ");
      return sb.toString();
   }
}

