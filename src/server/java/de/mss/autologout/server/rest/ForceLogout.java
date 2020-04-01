package de.mss.autologout.server.rest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import de.mss.autologout.param.ForceLogoutRequest;
import de.mss.net.webservice.WebServiceJsonDataBuilder;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;

public class ForceLogout extends SecretAutoLogoutWebService {

   private static final long serialVersionUID = 6568891347882291391L;


   @Override
   public String getPath() {
      return "/{username}/forceLogout";
   }


   @Override
   protected int handleException(String loggingId, MssException e, HttpServletResponse httpResponse) {
      return 0;
   }


   @Override
   public int post(
         String loggingId,
         Map<String, String> params,
         Request baseRequest,
         HttpServletRequest httpRequest,
         HttpServletResponse httpResponse)
         throws MssException {

      WebServiceJsonDataBuilder<ForceLogoutRequest> in = new WebServiceJsonDataBuilder<>();

      try {
         ForceLogoutRequest req = in.parseData(params, new ForceLogoutRequest());

         if (!Tools.isSet(req.getUserName()))
            throw new MssException(de.mss.utils.exception.ErrorCodes.ERROR_INVALID_PARAM, "no username given");

         if (req.getBody() == null)
        	 throw new MssException(de.mss.utils.exception.ErrorCodes.ERROR_INVALID_PARAM, "no body given");

         if (!Tools.isSet(req.getBody().getUser()))
            throw new MssException(de.mss.utils.exception.ErrorCodes.ERROR_INVALID_PARAM, "no user given");

         checkSecret(req.getBody().getSecret());

         this.server.setForceLogout(req.getUserName(), req.getBody().getUser());

         httpResponse.getWriter().flush();
         httpResponse.getWriter().close();

         return HttpServletResponse.SC_OK;
      }
      catch (MssException e) {
         throw e;
      }
      catch (IllegalAccessException | InvocationTargetException | IOException e) {
         throw new MssException(de.mss.utils.exception.ErrorCodes.ERROR_UNABLE_TO_EXECUTE_REQUEST, e, "error while processing forceLogout request");
      }
   }
}
