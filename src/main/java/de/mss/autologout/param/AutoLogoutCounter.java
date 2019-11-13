package de.mss.autologout.param;

import java.math.BigInteger;
import java.util.Map;

import de.mss.autologout.server.LogoutCounter;

public class AutoLogoutCounter {

   private LogoutCounter dailyCounter;
   private LogoutCounter weeklyCounter;
   private Map<String, BigInteger> counterValues;


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
}
