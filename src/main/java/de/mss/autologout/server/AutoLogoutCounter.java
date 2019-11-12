package de.mss.autologout.server;

import de.mss.autologout.app.LogoutCounter;

public class AutoLogoutCounter {

   private LogoutCounter dailyCounter;
   private LogoutCounter weeklyCounter;


   public AutoLogoutCounter(LogoutCounter dailyCounter, LogoutCounter weeklyCounter) {
      setDailycounter(dailyCounter);
      setWeeklyCounter(weeklyCounter);
   }


   private void setWeeklyCounter(LogoutCounter w) {
      this.weeklyCounter = w;
   }


   private void setDailycounter(LogoutCounter d) {
      this.dailyCounter = d;
   }


   public LogoutCounter getWeeklyCounter() {
      return this.weeklyCounter;
   }


   public LogoutCounter getDailyCounter() {
      return this.dailyCounter;
   }
}
