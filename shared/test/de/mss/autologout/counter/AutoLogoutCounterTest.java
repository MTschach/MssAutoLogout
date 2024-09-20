package de.mss.autologout.counter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.mss.utils.DateTimeTools;

public class AutoLogoutCounterTest {

   private Date          testDate;
   private LogoutCounter lc1;
   private LogoutCounter lc2;

   @BeforeEach
   public void setUp() {
      this.testDate = new Date();
      DateTimeTools.initNowForTest(this.testDate);

      this.lc1 = new LogoutCounter(20, "LC1");
      this.lc2 = new LogoutCounter(100, "LC2");
   }


   @Test
   public void testCounterValues() {
      final AutoLogoutCounter alc = new AutoLogoutCounter();
      alc.setCounterValues(new HashMap<>());
      alc.getCounterValues().put("2022-01-02", BigInteger.ZERO);
      alc.getCounterValues().put("2022-02-03", BigInteger.TEN);

      assertEquals("{[LockedForToday] false [CounterValues] {{[2022-02-03] 10 } {[2022-01-02] 0 } } } ", alc.toString());
   }


   @Test
   public void testDisabled() {
      final AutoLogoutCounter alc = new AutoLogoutCounter();
      assertFalse(alc.isDisabled());
      alc.disable();
      assertTrue(alc.isDisabled());
      alc.enable();
      assertFalse(alc.isDisabled());
   }


   @Test
   public void testLocked() {
      final AutoLogoutCounter alc = new AutoLogoutCounter();
      alc.setLockedForToday(true);
      alc.setReasonForLocked("just for Test");
      assertEquals("{[LockedForToday] true [ReasonForLock] just for Test } ", alc.toString());
   }


   @Test
   public void testOk() {
      final AutoLogoutCounter alc = new AutoLogoutCounter();
      assertNull(alc.getCounterValues());
      assertEquals(this.testDate, alc.getCurrentDate());
      assertNull(alc.getDailyCounter());
      assertFalse(alc.getLockedForToday());
      assertNull(alc.getReasonForLock());
      assertNull(alc.getWeeklyCounter());
      assertEquals("{[LockedForToday] false } ", alc.toString());
      assertTrue(alc.isSameDay());
   }


   @Test
   public void testOk2() {
      final AutoLogoutCounter alc = new AutoLogoutCounter(this.lc1, this.lc2);
      assertNull(alc.getCounterValues());
      assertEquals(this.testDate, alc.getCurrentDate());
      assertTrue(alc.getDailyCounter().equals(this.lc1));
      assertFalse(alc.getLockedForToday());
      assertNull(alc.getReasonForLock());
      assertTrue(alc.getWeeklyCounter().equals(this.lc2));
      assertEquals(
            "{[DailyCounter] {[Name] LC1 [MaxMinutes] 20 [CurrentMinutes] 0 [CurrentSeconds] 0}  [WeeklyCounter] {[Name] LC2 [MaxMinutes] 100 [CurrentMinutes] 0 [CurrentSeconds] 0}  [LockedForToday] false } ",
            alc.toString());
   }
}
