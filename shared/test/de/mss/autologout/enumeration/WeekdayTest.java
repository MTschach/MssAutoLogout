package de.mss.autologout.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

public class WeekdayTest {

   private void check(Weekday exp, Weekday is) {
      if (exp == null) {
         assertNull(is);
         return;
      }

      assertNotNull(is);
      assertEquals(exp.getApiValue(), is.getApiValue());
      assertEquals(exp.getDbValue(), is.getDbValue());
      assertEquals(Integer.valueOf(exp.getWeekday()), Integer.valueOf(is.getWeekday()));
      assertEquals(exp.toString(), is.toString());
   }


   @Test
   public void testGetByApiValue() {
      check(null, Weekday.getByApiValue(null));
      check(Weekday.MONDAY, Weekday.getByApiValue("monday"));
      try {
         Weekday.getByApiValue("blah", () -> {
            return new Exception();
         });
         fail();
      }
      catch (final Exception e) {
         assertNotNull(e);
      }
   }


   @Test
   public void testGetByDbValue() {
      check(null, Weekday.getByDbValue(null));
      check(Weekday.MONDAY, Weekday.getByDbValue("Mo"));
      try {
         Weekday.getByDbValue("blah", () -> {
            return new Exception();
         });
         fail();
      }
      catch (final Exception e) {
         assertNotNull(e);
      }
   }


   @Test
   public void testGetByWeekday() {
      check(null, Weekday.getByWeekday(123));
      check(Weekday.MONDAY, Weekday.getByWeekday(Calendar.MONDAY));
      try {
         Weekday.getByWeekday(12, () -> {
            return new Exception();
         });
         fail();
      }
      catch (final Exception e) {
         assertNotNull(e);
      }
   }
}
