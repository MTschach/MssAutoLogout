package de.mss.autologout.counter;

import org.apache.logging.log4j.Logger;

import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.utils.Tools;

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


   public void addMinutes(int minutes) {
      this.currentSeconds += minutes * 60;
   }


   public void addSeconds(int seconds) {
      this.currentSeconds += seconds;
   }


   private void calculateTimers() {
      this.maxSeconds = this.maxMinutes * 60;

      this.secondsFirstInfo = this.maxSeconds - (this.minutesFirstInfo * 60);
      this.secondsSecondInfo = this.maxSeconds - (this.minutesSecondInfo * 60);
      this.secondsFirstWarning = this.maxSeconds + (this.minutesFirstWarning * 60);
      this.secondsForceLogoff = this.maxSeconds + (this.minutesForceLogoff * 60);
   }


   public boolean check(String loggingId, Logger logger, String lastUserName, int checkInterval, CheckCounterResponse resp) {
      if (resp == null) {
         return false;
      }

      if (this.maxMinutes <= 0) {
         return false;
      }

      logger.debug(Tools.formatLoggingId(loggingId) + "checking " + this.name + ":" + this.currentSeconds + " < " + this.maxSeconds + " ?");

      if (isForceLogoff()) {
         resp.setForceLogout(Boolean.TRUE);
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + lastUserName + ". Deine " + getName() + " Zeit ist abgelaufen. Du wirst automatisch abgemeldet.");
         resp.setSpokenMessage("Hallo " + lastUserName + ". Deine Zeit ist abgelaufen. Du wirst automatisch abgemeldet.");
         return true;
      }

      if (isMinutesUntilForceLogoff()) {
         resp.setHeadLine("Info");
         resp
               .setMessage(
                     "Hallo "
                           + lastUserName
                           + ". Deine "
                           + getName()
                           + " Zeit ist seit "
                           + getMinutesOvertime()
                           + " Minuten abgelaufen. Du wirst in "
                           + getMinutesUntilFoceLogoff()
                           + " Minuten abgemeldet.");
         resp.setSpokenMessage("Hallo " + lastUserName + ". Deine Zeit ist abgelaufen.");
         return true;
      }

      if (isFirstWarning(checkInterval)) {
         resp.setHeadLine("Info");
         resp
               .setMessage(
                     "Hallo "
                           + lastUserName
                           + ". Deine "
                           + getName()
                           + " Zeit läuft in "
                           + (getMinutesForceLogoff() - getMinutesFirstWarning())
                           + " Minuten ab.");
         resp.setSpokenMessage("Hallo " + lastUserName + ". Deine Zeit läuft bald ab.");
         return true;
      }

      if (isMinutesReached(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + lastUserName + ". Deine Zeit ist abgelaufen.");
         resp.setSpokenMessage("Hallo " + lastUserName + ". Deine Zeit ist abgelaufen.");
         return true;
      }

      if (isSecondInfo(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + lastUserName + ". Deine " + getName() + " Zeit läuft in " + getMinutesSecondInfo() + " Minuten ab.");
         resp.setSpokenMessage("Hallo " + lastUserName + ". Deine Zeit läuft bald ab.");
         return true;
      }

      if (isFirstInfo(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + lastUserName + ". Deine " + getName() + " Zeit läuft in " + getMinutesFirstInfo() + " Minuten ab.");
         resp.setSpokenMessage("Hallo " + lastUserName + ". Deine Zeit läuft bald ab.");
         return true;
      }

      return false;
   }


   @Override
   public boolean equals(Object o) {
      if (o == null) {
         return false;
      }
      if (!(o instanceof LogoutCounter)) {
         return false;
      }

      final LogoutCounter other = (LogoutCounter)o;

      return this.name.equals(other.getName()) && (this.maxMinutes == other.getMaxMinutes());
   }


   public int getCurrentMinutes() {
      return getCurrentSeconds() / 60;
   }


   public int getCurrentSeconds() {
      return this.currentSeconds < 0 ? 0 : this.currentSeconds;
   }


   public String getDate() {
      return this.date;
   }


   public int getMaxMinutes() {
      return this.maxMinutes;
   }


   public int getMinutesFirstInfo() {
      return this.minutesFirstInfo;
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


   public int getMinutesSecondInfo() {
      return this.minutesSecondInfo;
   }


   public int getMinutesUntilFoceLogoff() {
      final int left = this.secondsForceLogoff - getCurrentSeconds();
      if (left <= 0) {
         return 0;
      }

      return left / 60;
   }


   public String getName() {
      return this.name;
   }


   public boolean isFirstInfo(int checkInterval) {
      return limitReached(this.secondsFirstInfo, checkInterval);
   }


   public boolean isFirstWarning(int checkInterval) {
      return limitReached(this.secondsFirstWarning, checkInterval);
   }


   public boolean isForceLogoff() {
      return (this.maxMinutes > 0) && (this.currentSeconds >= this.secondsForceLogoff);
   }


   public boolean isMinutesReached(int checkInterval) {
      return limitReached(this.maxSeconds, checkInterval);
   }


   public boolean isMinutesUntilForceLogoff() {
      if (this.currentSeconds <= this.secondsFirstWarning) {
         return false;
      }

      return this.currentSeconds < this.secondsForceLogoff;
   }


   public boolean isSecondInfo(int checkInterval) {
      return limitReached(this.secondsSecondInfo, checkInterval);
   }


   public boolean limitReached(int limit, int checkInterval) {
      return (this.maxMinutes > 0) && (getCurrentSeconds() >= limit) && (getCurrentSeconds() < (limit + checkInterval));
   }


   public void reset() {
      this.currentSeconds = 0;
   }


   public void setDate(String d) {
      this.date = d;
   }


   public void setMaxMinutes(int minutes) {
      this.maxMinutes = minutes;
      calculateTimers();
   }


   public void setMinutesFirstInfo(int minutes) {
      this.minutesFirstInfo = minutes;
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


   public void setMinutesSecondInfo(int minutes) {
      this.minutesSecondInfo = minutes;
      calculateTimers();
   }


   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("{[Name] " + this.name + " ");
      sb.append("[MaxMinutes] " + this.maxMinutes + " ");
      sb.append("[CurrentMinutes] " + this.getCurrentMinutes() + " ");
      sb.append("[CurrentSeconds] " + this.getCurrentSeconds() + "} ");
      return sb.toString();
   }
}
