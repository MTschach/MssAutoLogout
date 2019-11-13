package de.mss.autologout.server.rest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import de.mss.autologout.param.CheckCounterRequest;
import de.mss.autologout.param.CheckCounterResponse;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.webservice.WebServiceJsonDataBuilder;
import de.mss.utils.Tools;
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
         Map<String, String> params,
         Request baseRequest,
         HttpServletRequest httpRequest,
         HttpServletResponse httpResponse)
         throws MssException {

      WebServiceJsonDataBuilder<CheckCounterRequest> in = new WebServiceJsonDataBuilder<>();

      try {
         CheckCounterRequest req = in.parseData(params, new CheckCounterRequest());

         if (!Tools.isSet(req.getUserName()))
            throw new MssException(de.mss.utils.exception.ErrorCodes.ERROR_INVALID_PARAM, "no username given");

         if (req.getCheckInterval() == null)
            throw new MssException(de.mss.utils.exception.ErrorCodes.ERROR_INVALID_PARAM, "no check interval  given");

         if (req.getCheckInterval() == null)
            req.setCheckInterval(Integer.valueOf(30));

         this.server.addToCounter(req.getUserName(), req.getCheckInterval().intValue());
         CheckCounterResponse resp = this.server.checkCounter(req.getUserName(), req.getCheckInterval().intValue());

         httpResponse.getWriter().write(new WebServiceJsonDataBuilder<CheckCounterResponse>().writeData(resp));
         httpResponse.getWriter().flush();
         httpResponse.getWriter().close();

         return HttpServletResponse.SC_OK;
      }
      catch (MssException e) {
         throw e;
      }
      catch (IllegalAccessException | InvocationTargetException | IOException e) {
         throw new MssException(de.mss.utils.exception.ErrorCodes.ERROR_UNABLE_TO_EXECUTE_REQUEST, e, "error while processing checkCounter request");
      }
   }
}
