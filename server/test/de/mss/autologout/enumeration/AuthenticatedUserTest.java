package de.mss.autologout.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.mss.utils.exception.MssException;

public class AuthenticatedUserTest {

   private void check(AuthenticatedUser exp, AuthenticatedUser is) {
      assertEquals(exp.getName(), is.getName());
      assertEquals(exp.getHash(), is.getHash());

   }


   @Test
   public void testGetByHash() throws MssException {
      check(AuthenticatedUser.MICHAEL, AuthenticatedUser.getByHash("a35b23840caa1925297e62125a4086e2343aab1de961a8b51ec28fd2061f6339"));
      check(AuthenticatedUser.CORINA, AuthenticatedUser.getByHash("a35b23840caa1925297e62125a4086e2343aab1de961a8b51ec28fd2061f63389"));
      try {
         AuthenticatedUser.getByHash("");
         fail();
      }
      catch (final MssException e) {
         assertEquals(de.mss.autologout.exception.ErrorCodes.ERROR_INVLID_AUTHENTICATED_USER, e.getError());
      }
   }


   @Test
   public void testGetByName() throws MssException {
      check(AuthenticatedUser.MICHAEL, AuthenticatedUser.getByName("michael"));
      check(AuthenticatedUser.CORINA, AuthenticatedUser.getByName("corina"));
      try {
         AuthenticatedUser.getByName("");
         fail();
      }
      catch (final MssException e) {
         assertEquals(de.mss.autologout.exception.ErrorCodes.ERROR_INVLID_AUTHENTICATED_USER, e.getError());
      }
   }
}
