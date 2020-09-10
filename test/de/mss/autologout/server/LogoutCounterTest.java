package de.mss.autologout.server;

import org.junit.Test;

import de.mss.autologout.server.LogoutCounter;
import junit.framework.TestCase;

public class LogoutCounterTest extends TestCase {

   private LogoutCounter classUnderTest = null;

   @Override
   public void setUp() throws Exception {
      super.setUp();
      this.classUnderTest = new LogoutCounter(30, "täglich");
   }


   @Override
   public void tearDown() throws Exception {
      super.tearDown();
   }


   @Test
   public void testInit() {
      assertEquals("Max Minutes", Integer.valueOf(30), Integer.valueOf(this.classUnderTest.getMaxMinutes()));
      assertEquals("Minutes First Info", Integer.valueOf(10), Integer.valueOf(this.classUnderTest.getMinutesFirstInfo()));
      assertEquals("Minutes Second Info", Integer.valueOf(5), Integer.valueOf(this.classUnderTest.getMinutesSecondInfo()));
      assertEquals("Minutes First Warning", Integer.valueOf(5), Integer.valueOf(this.classUnderTest.getMinutesFirstWarning()));
      assertEquals("Minutes Force Logout", Integer.valueOf(10), Integer.valueOf(this.classUnderTest.getMinutesForceLogoff()));
      assertEquals("Current Minutes", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getCurrentMinutes()));
      assertEquals("Current Seconds", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getCurrentSeconds()));
      assertEquals("Name", "täglich", this.classUnderTest.getName());
   }


   @Test
   public void testAddMinutes() {
      assertEquals("Current Minutes", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getCurrentMinutes()));
      assertEquals("Current Seconds", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getCurrentSeconds()));

      this.classUnderTest.addMinutes(3);
      assertEquals("Current Minutes", Integer.valueOf(3), Integer.valueOf(this.classUnderTest.getCurrentMinutes()));
      assertEquals("Current Seconds", Integer.valueOf(180), Integer.valueOf(this.classUnderTest.getCurrentSeconds()));
   }


   @Test
   public void testAddSeconds() {
      assertEquals("Current Minutes", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getCurrentMinutes()));
      assertEquals("Current Seconds", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getCurrentSeconds()));

      this.classUnderTest.addSeconds(90);
      assertEquals("Current Minutes", Integer.valueOf(1), Integer.valueOf(this.classUnderTest.getCurrentMinutes()));
      assertEquals("Current Seconds", Integer.valueOf(90), Integer.valueOf(this.classUnderTest.getCurrentSeconds()));
   }


   @Test
   public void testFirstInfo() {
      assertFalse("First Info", this.classUnderTest.isFirstInfo(30));
      this.classUnderTest.addMinutes(19);
      assertFalse("First Info", this.classUnderTest.isFirstInfo(30));
      this.classUnderTest.addSeconds(59);
      assertFalse("First Info", this.classUnderTest.isFirstInfo(30));
      this.classUnderTest.addSeconds(20);
      assertTrue("First Info", this.classUnderTest.isFirstInfo(30));
      this.classUnderTest.addSeconds(20);
      assertFalse("First Info", this.classUnderTest.isFirstInfo(30));
   }


   @Test
   public void testSecondInfo() {
      assertFalse("Second Info", this.classUnderTest.isSecondInfo(30));
      this.classUnderTest.addMinutes(24);
      assertFalse("Second Info", this.classUnderTest.isSecondInfo(30));
      this.classUnderTest.addSeconds(59);
      assertFalse("Second Info", this.classUnderTest.isSecondInfo(30));
      this.classUnderTest.addSeconds(20);
      assertTrue("Second Info", this.classUnderTest.isSecondInfo(30));
      this.classUnderTest.addSeconds(20);
      assertFalse("Second Info", this.classUnderTest.isSecondInfo(30));
   }


   @Test
   public void testInfo() {
      assertFalse("Second Info", this.classUnderTest.isMinutesReached(30));
      this.classUnderTest.addMinutes(29);
      assertFalse("Second Info", this.classUnderTest.isMinutesReached(30));
      this.classUnderTest.addSeconds(59);
      assertFalse("Second Info", this.classUnderTest.isMinutesReached(30));
      this.classUnderTest.addSeconds(20);
      assertTrue("Second Info", this.classUnderTest.isMinutesReached(30));
      this.classUnderTest.addSeconds(20);
      assertFalse("Second Info", this.classUnderTest.isMinutesReached(30));
   }


   @Test
   public void testFirstWarning() {
      assertFalse("First Warning", this.classUnderTest.isFirstWarning(30));
      this.classUnderTest.addMinutes(34);
      assertFalse("First Warning", this.classUnderTest.isFirstWarning(30));
      this.classUnderTest.addSeconds(59);
      assertFalse("First Warning", this.classUnderTest.isFirstWarning(30));
      this.classUnderTest.addSeconds(20);
      assertTrue("First Warning", this.classUnderTest.isFirstWarning(30));
      this.classUnderTest.addSeconds(20);
      assertFalse("First Warning", this.classUnderTest.isFirstWarning(30));
   }


   @Test
   public void testForceLogoff() {
      assertFalse("Force Logoff", this.classUnderTest.isForceLogoff());
      this.classUnderTest.addMinutes(39);
      assertFalse("Force Logoff", this.classUnderTest.isForceLogoff());
      this.classUnderTest.addSeconds(59);
      assertFalse("Force Logoff", this.classUnderTest.isForceLogoff());
      this.classUnderTest.addSeconds(20);
      assertTrue("Force Logoff", this.classUnderTest.isForceLogoff());
      this.classUnderTest.addSeconds(20);
      assertTrue("Force Logoff", this.classUnderTest.isForceLogoff());
   }


   @Test
   public void testIsMinutesUntilForceLogoff() {
      assertFalse("Is Minutes until Force Logoff", this.classUnderTest.isMinutesUntilForceLogoff());
      this.classUnderTest.addMinutes(34);
      assertFalse("Is Minutes until Force Logoff", this.classUnderTest.isMinutesUntilForceLogoff());
      this.classUnderTest.addMinutes(1);
      assertFalse("Is Minutes until Force Logoff", this.classUnderTest.isMinutesUntilForceLogoff());
      this.classUnderTest.addMinutes(1);
      assertTrue("Is Minutes until Force Logoff", this.classUnderTest.isMinutesUntilForceLogoff());
      this.classUnderTest.addMinutes(1);
      assertTrue("Is Minutes until Force Logoff", this.classUnderTest.isMinutesUntilForceLogoff());
      this.classUnderTest.addMinutes(3);
      assertFalse("Is Minutes until Force Logoff", this.classUnderTest.isMinutesUntilForceLogoff());
   }


   @Test
   public void testMinutesUntilForceLogoff() {
      assertEquals("Minutes until Force Logoff", Integer.valueOf(40), Integer.valueOf(this.classUnderTest.getMinutesUntilFoceLogoff()));
      this.classUnderTest.addMinutes(39);
      assertEquals("Minutes until Force Logoff", Integer.valueOf(1), Integer.valueOf(this.classUnderTest.getMinutesUntilFoceLogoff()));
      this.classUnderTest.addMinutes(1);
      assertEquals("Minutes until Force Logoff", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getMinutesUntilFoceLogoff()));
      this.classUnderTest.addMinutes(1);
      assertEquals("Minutes until Force Logoff", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getMinutesUntilFoceLogoff()));
   }


   @Test
   public void testCustomCounter() {
      this.classUnderTest.setMinutesFirstInfo(2);
      this.classUnderTest.setMinutesSecondInfo(1);
      this.classUnderTest.setMinutesFirstWarning(10);
      this.classUnderTest.setMinutesForceLogoff(15);
      assertEquals("Minutes First Info", Integer.valueOf(2), Integer.valueOf(this.classUnderTest.getMinutesFirstInfo()));
      assertEquals("Minutes Second Info", Integer.valueOf(1), Integer.valueOf(this.classUnderTest.getMinutesSecondInfo()));
      assertEquals("Minutes First Warning", Integer.valueOf(10), Integer.valueOf(this.classUnderTest.getMinutesFirstWarning()));
      assertEquals("Minutes Force Logout", Integer.valueOf(15), Integer.valueOf(this.classUnderTest.getMinutesForceLogoff()));
      assertEquals("Current Minutes", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getCurrentMinutes()));
      assertEquals("Current Seconds", Integer.valueOf(0), Integer.valueOf(this.classUnderTest.getCurrentSeconds()));
      assertEquals("Name", "täglich", this.classUnderTest.getName());
   }


   @Test
   public void testDisabledCounter() {
      this.classUnderTest.setMaxMinutes(0);
      assertFalse("ForceLogoff", this.classUnderTest.isForceLogoff());
      this.classUnderTest.addMinutes(123);
      assertFalse("LimitReached", this.classUnderTest.isFirstInfo(30));
   }
}
