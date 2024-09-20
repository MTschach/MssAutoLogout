package de.mss.autologout.enumeration;

import java.util.Calendar;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Weekday {

   //@formatter:off
     MONDAY          ("monday"         , "Mo"      , Calendar.MONDAY)
   , TUESDAY         ("tuesday"        , "Di"      , Calendar.TUESDAY)
   , WEDNESDAY       ("wednesday"      , "Mi"      , Calendar.WEDNESDAY)
   , THURSDAY        ("thursday"       , "Do"      , Calendar.THURSDAY)
   , FRIDAY          ("friday"         , "Fr"      , Calendar.FRIDAY)
   , SATURDAY        ("saturday"       , "Sa"      , Calendar.SATURDAY)
   , SUNDAY          ("sunday"         , "So"      , Calendar.SUNDAY)
   ;
   //@formatter:on

   @JsonCreator
   public static Weekday getByApiValue(String d) {
      return getByApiValue(d, null);
   }


   public static <T extends Exception> Weekday getByApiValue(String d, Supplier<T> throwException) throws T {
      for (final Weekday w : Weekday.values()) {
         if (w.getApiValue().equals(d)) {
            return w;
         }
      }

      if (throwException != null) {
         throw throwException.get();
      }

      return null;
   }


   public static Weekday getByDbValue(String d) {
      return getByDbValue(d, null);
   }


   public static <T extends Exception> Weekday getByDbValue(String d, Supplier<T> throwException) throws T {
      for (final Weekday w : Weekday.values()) {
         if (w.getDbValue().equals(d)) {
            return w;
         }
      }

      if (throwException != null) {
         throw throwException.get();
      }

      return null;
   }


   public static Weekday getByWeekday(int d) {
      return getByWeekday(d, null);
   }


   public static <T extends Exception> Weekday getByWeekday(int d, Supplier<T> throwException) throws T {
      for (final Weekday w : Weekday.values()) {
         if (w.getWeekday() == d) {
            return w;
         }
      }

      if (throwException != null) {
         throw throwException.get();
      }

      return null;
   }


   private String apiValue;


   private String dbValue;


   private int weekday;

   private Weekday(String a, String d, int w) {
      this.apiValue = a;
      this.dbValue = d;
      this.weekday = w;
   }


   @JsonValue
   public String getApiValue() {
      return this.apiValue;
   }


   public String getDbValue() {
      return this.dbValue;
   }


   public int getWeekday() {
      return this.weekday;
   }


   @Override
   public String toString() {
      return this.apiValue;
   }

}
