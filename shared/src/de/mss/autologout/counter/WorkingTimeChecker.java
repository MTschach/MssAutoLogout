package de.mss.autologout.counter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.logging.log4j.Logger;

import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.client.param.WorkingTime;
import de.mss.autologout.enumeration.Weekday;
import de.mss.utils.DateTimeTools;
import de.mss.utils.Tools;

public class WorkingTimeChecker {

   private final int         firstInfo  = 10 * 60;
   private final int         secondInfo = 5 * 60;
   private List<WorkingTime> workingTimes;

   public WorkingTimeChecker() {
      initWorkingTimes();
   }


   public WorkingTimeChecker(List<WorkingTime> wt) {
      this.workingTimes = wt;
      if (this.workingTimes == null) {
         initWorkingTimes();
      }

      for (final Weekday w : Weekday.values()) {
         check(w);
      }
   }


   public boolean check(String loggingId, Logger logger, String userName, int checkInterval, CheckCounterResponse resp) {
      final Date now = DateTimeTools.now();
      final GregorianCalendar gc = new GregorianCalendar();
      gc.setTime(now);

      final WorkingTime wt = getCurrentWorkingTime(gc);

      if (wt == null) {
         return false;
      }

      final int workingFrom = getTime(wt.getFrom(), (7 * 3600) + (30 * 60));
      final int workingUntil = getTime(wt.getUntil(), 20 * 3600);
      final int currentTime = (gc.get(Calendar.HOUR_OF_DAY) * 3600) + (gc.get(Calendar.MINUTE) * 60);

      logger.debug(Tools.formatLoggingId(loggingId) + " working " + workingFrom + " < " + currentTime + " < " + workingUntil + "?");

      if ((currentTime < workingFrom) || (currentTime > workingUntil)) {
         resp.setForceLogout(true);
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + ". Deine Arbeitszeit ist vorbei. Du wirst automatisch abgemeldet.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Arbeitszeit ist vorbei. Du wirst automatisch abgemeldet.");
         return true;
      }

      int t1 = workingUntil - this.firstInfo;
      int t2 = (workingUntil - this.firstInfo) + checkInterval;
      if ((currentTime >= t1) && (currentTime < t2)) {
         resp.setForceLogout(false);
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + ". Deine Arbeitszeit läuft gleich ab.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Arbeitszeit läuft gleich ab.");
         return true;
      }


      t1 = workingUntil - this.secondInfo;
      t2 = (workingUntil - this.secondInfo) + checkInterval;
      if ((currentTime >= t1) && (currentTime < t2)) {
         resp.setForceLogout(false);
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + ". Deine Arbeitszeit läuft gleich ab.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Arbeitszeit läuft gleich ab.");
         return true;
      }


      return false;
   }


   private void check(Weekday w) {
      for (final WorkingTime wt : this.workingTimes) {
         if (wt.getWeekday() == w) {
            return;
         }
      }

      this.workingTimes.add(initWorkingTime(w));
   }


   private WorkingTime getCurrentWorkingTime(GregorianCalendar gc) {
      WorkingTime ret = null;

      final int weekday = gc.get(Calendar.DAY_OF_WEEK);
      for (final WorkingTime wt : this.workingTimes) {
         if ((wt.getWeekday() != null) && (wt.getWeekday().getWeekday() == weekday)) {
            ret = wt;
         }
      }

      for (final WorkingTime wt : this.workingTimes) {
         if ((wt.getDate() != null) && DateTimeTools.isSameDay(wt.getDate(), gc.getTime())) {
            ret = wt;
         }
      }
      return ret;
   }


   private int getTime(String timeString, int defaultValue) {
      final String[] s = timeString.split(":");

      try {
         return (Integer.parseInt(s[0], 10) * 3600) + (Integer.parseInt(s[1], 10) * 60);
      }
      catch (final Exception e) {
         return defaultValue;
      }
   }


   public List<WorkingTime> getWorkingTimes() {
      return this.workingTimes;
   }


   private WorkingTime initWorkingTime(Weekday w) {
      final WorkingTime wt = new WorkingTime();
      wt.setFrom("10:00");
      wt.setUntil("18:30");
      wt.setWeekday(w);
      return wt;
   }


   private void initWorkingTimes() {
      this.workingTimes = new ArrayList<>();
      for (final Weekday w : Weekday.values()) {
         this.workingTimes.add(initWorkingTime(w));
      }
   }


   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();
      for (final WorkingTime wt : this.workingTimes) {
         sb.append(wt.toString());
      }
      return sb.toString();
   }
}
