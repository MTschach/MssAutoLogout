package de.mss.autologout.counter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import de.mss.autologout.client.param.CheckCounterResponse;

public class LogoutCounterTest {

   private static Logger       logger         = null;

   private static final String COUNTER_NAME   = "TestCounter";

   private static final String USER_NAME      = "TestUser";
   private static final int    CHECK_INTERVAL = 30;

   private static Logger getLogger() {
      if (logger == null) {
         logger = LogManager.getLogger(LogoutCounterTest.class);
      }

      return logger;
   }


   private void check(int exp, int is) {
      assertEquals(Integer.valueOf(exp), Integer.valueOf(is));
   }


   @Test
   public void test() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      assertEquals(COUNTER_NAME, lc.getName());
      assertEquals("", lc.getDate());
      check(20, lc.getMaxMinutes());
      check(0, lc.getCurrentMinutes());
      check(0, lc.getCurrentSeconds());
      check(10, lc.getMinutesFirstInfo());
      check(5, lc.getMinutesFirstWarning());
      check(10, lc.getMinutesForceLogoff());
      check(-20, lc.getMinutesOvertime());
      check(5, lc.getMinutesSecondInfo());
      check(30, lc.getMinutesUntilFoceLogoff());
   }


   @Test
   public void testCheck() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      final CheckCounterResponse resp = new CheckCounterResponse();

      assertFalse(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, null));

      lc.setMaxMinutes(0);
      assertFalse(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, resp));

      lc.setMaxMinutes(20);
      assertFalse(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, resp));
   }


   @Test
   public void testCheckFirstInfo() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      final CheckCounterResponse resp = new CheckCounterResponse();

      lc.addMinutes(10);
      assertTrue(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, resp));
      assertFalse(resp.getForceLogout());
      assertEquals("Info", resp.getHeadLine());
      assertEquals(
            "Hallo " + USER_NAME + ". Deine " + COUNTER_NAME + " Zeit läuft in 10 Minuten ab.",
            resp.getMessage());
      assertEquals("Hallo " + USER_NAME + ". Deine Zeit läuft bald ab.", resp.getSpokenMessage());
   }


   @Test
   public void testCheckFirstWarn() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      final CheckCounterResponse resp = new CheckCounterResponse();

      lc.addMinutes(25);
      assertTrue(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, resp));
      assertFalse(resp.getForceLogout());
      assertEquals("Info", resp.getHeadLine());
      assertEquals(
            "Hallo " + USER_NAME + ". Deine " + COUNTER_NAME + " Zeit läuft in 5 Minuten ab.",
            resp.getMessage());
      assertEquals("Hallo " + USER_NAME + ". Deine Zeit läuft bald ab.", resp.getSpokenMessage());
   }


   @Test
   public void testCheckForceLogout() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      final CheckCounterResponse resp = new CheckCounterResponse();

      lc.addMinutes(60);
      assertTrue(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, resp));
      assertTrue(resp.getForceLogout());
      assertEquals("Info", resp.getHeadLine());
      assertEquals("Hallo " + USER_NAME + ". Deine " + COUNTER_NAME + " ist abgelaufen. Du wirst automatisch abgemeldet.", resp.getMessage());
      assertEquals("Hallo " + USER_NAME + ". Deine Zeit ist abgelaufen. Du wirst automatisch abgemeldet.", resp.getSpokenMessage());
   }


   @Test
   public void testCheckMinutesReached() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      final CheckCounterResponse resp = new CheckCounterResponse();

      lc.addMinutes(20);
      assertTrue(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, resp));
      assertFalse(resp.getForceLogout());
      assertEquals("Info", resp.getHeadLine());
      assertEquals(
            "Hallo " + USER_NAME + ". Deine Zeit ist abgelaufen.",
            resp.getMessage());
      assertEquals("Hallo " + USER_NAME + ". Deine Zeit ist abgelaufen.", resp.getSpokenMessage());
   }


   @Test
   public void testCheckSecondInfo() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      final CheckCounterResponse resp = new CheckCounterResponse();

      lc.addMinutes(15);
      assertTrue(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, resp));
      assertFalse(resp.getForceLogout());
      assertEquals("Info", resp.getHeadLine());
      assertEquals(
            "Hallo " + USER_NAME + ". Deine " + COUNTER_NAME + " Zeit läuft in 5 Minuten ab.",
            resp.getMessage());
      assertEquals("Hallo " + USER_NAME + ". Deine Zeit läuft bald ab.", resp.getSpokenMessage());
   }


   @Test
   public void testCheckWarnLogout() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      final CheckCounterResponse resp = new CheckCounterResponse();

      lc.addMinutes(28);
      assertTrue(lc.check("loggingId", getLogger(), USER_NAME, CHECK_INTERVAL, resp));
      assertFalse(resp.getForceLogout());
      assertEquals("Info", resp.getHeadLine());
      assertEquals(
            "Hallo " + USER_NAME + ". Deine TestCounter Zeit ist seit 8 Minuten abgelaufen. Du wirst in 2 Minuten abgemeldet.",
            resp.getMessage());
      assertEquals("Hallo " + USER_NAME + ". Deine Zeit ist abgelaufen.", resp.getSpokenMessage());
   }


   @Test
   public void testEquals() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);

      assertFalse(lc.equals(null));
      assertFalse(lc.equals(""));
      assertFalse(lc.equals(new LogoutCounter(20, USER_NAME)));
      assertFalse(lc.equals(new LogoutCounter(10, COUNTER_NAME)));
      assertTrue(lc.equals(new LogoutCounter(20, COUNTER_NAME)));
   }


   @Test
   public void testGetMinutesUntilForceLogoff() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);

      check(30, lc.getMinutesUntilFoceLogoff());
      lc.addSeconds(30 * 60);
      check(0, lc.getMinutesUntilFoceLogoff());
      lc.addSeconds(60);
      check(0, lc.getMinutesUntilFoceLogoff());
   }


   @Test
   public void testIsForceLogoff() {
      final LogoutCounter lc = new LogoutCounter(0, COUNTER_NAME);

      assertFalse(lc.isForceLogoff());
      lc.setMaxMinutes(20);
      assertFalse(lc.isForceLogoff());
      lc.addMinutes(50);
      assertTrue(lc.isForceLogoff());
   }


   @Test
   public void testIsMinutesUntilLogoff() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);

      assertFalse(lc.isMinutesUntilForceLogoff());
      lc.addMinutes(26);
      assertTrue(lc.isMinutesUntilForceLogoff());
      lc.addMinutes(10);
      assertFalse(lc.isMinutesUntilForceLogoff());
   }


   @Test
   public void testLimitReached() {
      final LogoutCounter lc = new LogoutCounter(0, COUNTER_NAME);

      assertFalse(lc.limitReached(600, CHECK_INTERVAL));
      lc.setMaxMinutes(20);
      assertFalse(lc.limitReached(600, CHECK_INTERVAL));
      lc.addMinutes(10);
      assertTrue(lc.limitReached(600, CHECK_INTERVAL));
      lc.addMinutes(1);
      assertFalse(lc.limitReached(600, CHECK_INTERVAL));
   }


   @Test
   public void testSetter() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      lc.setMaxMinutes(10);
      lc.setMinutesFirstInfo(5);
      lc.setMinutesFirstWarning(2);
      lc.setMinutesForceLogoff(4);
      lc.setMinutesSecondInfo(3);
      lc.setDate("date");

      assertEquals("date", lc.getDate());
      check(10, lc.getMaxMinutes());
      check(5, lc.getMinutesFirstInfo());
      check(2, lc.getMinutesFirstWarning());
      check(4, lc.getMinutesForceLogoff());
      check(-10, lc.getMinutesOvertime());
      check(3, lc.getMinutesSecondInfo());
      check(14, lc.getMinutesUntilFoceLogoff());
   }


   @Test
   public void testToString() {
      final LogoutCounter lc = new LogoutCounter(20, COUNTER_NAME);
      assertEquals("{[Name] " + COUNTER_NAME + " [MaxMinutes] 20 [CurrentMinutes] 0 [CurrentSeconds] 0} ", lc.toString());
   }
}
