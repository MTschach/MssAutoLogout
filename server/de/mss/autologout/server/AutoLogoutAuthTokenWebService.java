package de.mss.autologout.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

import de.mss.autologout.client.param.AuthTokenRequest;
import de.mss.autologout.enumeration.AuthenticatedUser;
import de.mss.autologout.exception.ErrorCodes;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public abstract class AutoLogoutAuthTokenWebService<R extends AuthTokenRequest> extends AutoLogoutWebService<R, WebServiceResponse> {

   public AutoLogoutAuthTokenWebService(Supplier<R> reqts, Supplier<WebServiceResponse> rts) {
      super(reqts, rts);
   }


   private static final long   serialVersionUID  = -7693493695527247025L;

   protected AuthenticatedUser authenticatedUser = null;


   @Override
   protected void beforeAction(String loggingId, R req) throws MssException {
      checkAuthToken(req.getAuthToken());
   }


   protected void checkAuthToken(String authToken) throws MssException {
      if (authToken == null) {
         throw new MssException(ErrorCodes.ERROR_AUTH_TOKEN_INVALID, "no auth token");
      }

      String hashedToken = null;
      try {
         hashedToken = getHash("SHA-256", authToken);
      }
      catch (final NoSuchAlgorithmException e) {
         throw new MssException(ErrorCodes.ERROR_AUTH_TOKEN_INVALID, e, "could not check auth token");
      }

      this.authenticatedUser = AuthenticatedUser.getByHash(hashedToken);
   }


   private static String getHash(String algo, String token) throws NoSuchAlgorithmException {
      final MessageDigest md = MessageDigest.getInstance(algo);
      md.update(token.getBytes());

      final StringBuilder hash = new StringBuilder();
      final byte[] hashData = md.digest();
      for (final byte b : hashData) {
         final String h = Integer.toHexString(b & 0xff);
         if (h.length() == 1) {
            hash.append("0");
         }
         hash.append(h);
      }

      return hash.toString();
   }
}
