package de.mss.autologout.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.mss.net.rest.RestMethod;

public class CallPathsTest {

   @Test
   public void test() {
      assertEquals(RestMethod.GET, CallPaths.GET_USERS.getMethod());
      assertEquals("/users", CallPaths.GET_USERS.getPath());
   }
}
