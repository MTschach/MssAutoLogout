package de.mss.autologout.counter;

import de.mss.autologout.client.param.CheckCounterResponse;

public class LogoutCounter {

   private int    maxMinutes          = 0;
   private int    minutesFirstInfo    = 10;
   private int    minutesSecondInfo   = 5;
   private int    minutesFirstWarning = 5;
   private int    minutesForceLogoff  = 10;

   private String name                = "";
   private String date                = "";

   private int    maxSeconds          = 0;
   private int    currentSeconds      = 0;
   private int    secondsFirstInfo    = 0;
   private int    secondsSecondInfo   = 0;
   private int    secondsFirstWarning = 0;
   private int    secondsForceLogoff  = 0;


   public LogoutCounter(int minutes, String n) {
      this.name = n;
      setMaxMinutes(minutes);
      reset();
   }


   public String getName() {
      return this.name;
   }


   public int getMaxMinutes() {
      return this.maxMinutes;
   }


   public int getMinutesFirstInfo() {
      return this.minutesFirstInfo;
   }


   public int getMinutesSecondInfo() {
      return this.minutesSecondInfo;
   }


   public int getMinutesFirstWarning() {
      return this.minutesFirstWarning;
   }


   public int getMinutesForceLogoff() {
      return this.minutesForceLogoff;
   }


   public int getMinutesOvertime() {
      return getCurrentMinutes() - getMaxMinutes();
   }


   public String getDate() {
      return this.date;
   }


   public void setMaxMinutes(int minutes) {
      this.maxMinutes = minutes;
      calculateTimers();
   }


   public void setMinutesFirstInfo(int minutes) {
      this.minutesFirstInfo = minutes;
      calculateTimers();
   }


   public void setMinutesSecondInfo(int minutes) {
      this.minutesSecondInfo = minutes;
      calculateTimers();
   }


   public void setMinutesFirstWarning(int minutes) {
      this.minutesFirstWarning = minutes;
      calculateTimers();
   }


   public void setMinutesForceLogoff(int minutes) {
      this.minutesForceLogoff = minutes;
      calculateTimers();
   }


   public void setDate(String d) {
      this.date = d;
   }


   private void calculateTimers() {
      this.maxSeconds = this.maxMinutes * 60;

      this.secondsFirstInfo = this.maxSeconds - this.minutesFirstInfo * 60;
      this.secondsSecondInfo = this.maxSeconds - this.minutesSecondInfo * 60;
      this.secondsFirstWarning = this.maxSeconds + this.minutesFirstWarning * 60;
      this.secondsForceLogoff = this.maxSeconds + this.minutesForceLogoff * 60;
   }


   public void reset() {
      this.currentSeconds = 0;
   }


   public void addSeconds(int seconds) {
      this.currentSeconds += seconds;
   }


   public void addMinutes(int minutes) {
      this.currentSeconds += minutes * 60;
   }


   public int getCurrentSeconds() {
      return this.currentSeconds;
   }


   public int getCurrentMinutes() {
      return this.currentSeconds / 60;
   }


   public boolean isFirstInfo(int checkInterval) {
      return limitReached(this.secondsFirstInfo, checkInterval);
   }


   public boolean isSecondInfo(int checkInterval) {
      return limitReached(this.secondsSecondInfo, checkInterval);
   }


   public boolean isMinutesReached(int checkInterval) {
      return limitReached(this.maxSeconds, checkInterval);
   }


   public boolean isFirstWarning(int checkInterval) {
      return limitReached(this.secondsFirstWarning, checkInterval);
   }


   public boolean isMinutesUntilForceLogoff() {
      if (this.currentSeconds <= this.secondsFirstWarning) {
         return false;
      }

      return this.currentSeconds < this.secondsForceLogoff;
   }


   public int getMinutesUntilFoceLogoff() {
      final int left = this.secondsForceLogoff - this.currentSeconds;
      if (left <= 0) {
         return 0;
      }

      return left / 60;
   }


   public boolean isForceLogoff() {
      return this.maxMinutes > 0 && this.currentSeconds >= this.secondsForceLogoff;
   }


   private boolean limitReached(int limit, int checkInterval) {
      return this.maxMinutes > 0 && this.currentSeconds >= limit && this.currentSeconds < limit + checkInterval;
   }


   public boolean check(String userName, int checkInterval, CheckCounterResponse resp) {
      if (resp == null) {
         return false;
      }

      if (isForceLogoff()) {
         resp.setForceLogout(Boolean.TRUE);
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + ". Deine " + getName() + " ist abgelaufen. Du wirst automatisch abgemeldet.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen. Du wirst automatisch abgemeldet.");
         return true;
      }

      if (isMinutesUntilForceLogoff()) {
         resp.setHeadLine("Info");
         resp
               .setMessage(
                     "Hallo "
                           + userName
                           + "! Deine "
                           + getName()
                           + " Zeit ist seit "
                           + getMinutesOvertime()
                           + " Minuten abgelaufen. Du wirst in "
                           + getMinutesUntilFoceLogoff()
                           + "Minuten abgemeldet.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen.");
         return true;
      }

      if (isFirstWarning(checkInterval)) {
         resp.setHeadLine("Info");
         resp
               .setMessage(
                     "Hallo "
                           + userName
                           + "! Deine "
                           + getName()
                           + " Zeit läuft in "
                           + (getMinutesForceLogoff() - getMinutesFirstWarning())
                           + " Minuten ab");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen.");
         return true;
      }

      if (isMinutesReached(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen.");
         return true;
      }

      if (isSecondInfo(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + "! Deine " + getName() + " Zeit läuft in " + getMinutesSecondInfo() + " Minuten ab.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit läuft bald ab.");
         return true;
      }

      if (isFirstInfo(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + "! Deine " + getName() + " Zeit läuft in " + getMinutesFirstInfo() + " Minuten ab.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit läuft bald ab.");
         return true;
      }

      return false;
   }
}
