package de.mss.autologout.server.rest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.utils.exception.MssException;

public class CheckCounter extends AutoLogoutWebService {

   private static final long serialVersionUID = 6568891347882291391L;


   @Override
   public String getPath() {
      return "/{username}/checkCounter";
   }


   @Override
   protected int handleException(String loggingId, MssException e, HttpServletResponse httpResponse) {
      return 0;
   }


   @Override
   public int get(
         String loggingId,
         Map<String, String> pathParams,
         Request baseRequest,
         HttpServletRequest httpRequest,
         HttpServletResponse httpResponse)
         throws MssException {
      return HttpServletResponse.SC_NOT_FOUND;
   }
}
