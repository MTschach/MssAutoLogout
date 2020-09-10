package de.mss.autologout.enumeration;

import de.mss.autologout.exception.ErrorCodes;
import de.mss.utils.exception.MssException;

public enum AuthenticatedUser {

   //@formatter:off
   MICHAEL     ("michael"           , "a35b23840caa1925297e62125a4086e2343aab1de961a8b51ec28fd2061f6339"); // "slkgjhsdfltkertkcrztlaxzcsntarmzrxrtnlzrmxugh");
   //@formatter:on

   private String name;
   private String hash;


   private AuthenticatedUser(String n, String h) {
      this.name = n;
      this.hash = h;
   }


   public String getName() {
      return this.name;
   }


   public String getHash() {
      return this.hash;
   }


   public static AuthenticatedUser getByName(String n) throws MssException {
      for (final AuthenticatedUser u : AuthenticatedUser.values()) {
         if (u.getName().equals(n)) {
            return u;
         }
      }

      throw new MssException(ErrorCodes.ERROR_INVLID_AUTHENTICATED_USER, "the user " + n + " could not be authenticated");
   }


   public static AuthenticatedUser getByHash(String h) throws MssException {
      for (final AuthenticatedUser u : AuthenticatedUser.values()) {
         if (u.getHash().equals(h)) {
            return u;
         }
      }

      throw new MssException(ErrorCodes.ERROR_INVLID_AUTHENTICATED_USER, "the hash " + h + " is not known");
   }
}
