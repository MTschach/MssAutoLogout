package de.mss.autologout.server.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import de.mss.autologout.param.GetAllCountersResponse;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.webservice.WebServiceJsonDataBuilder;
import de.mss.utils.exception.MssException;

public class GetAllCounters extends AutoLogoutWebService {

   private static final long serialVersionUID = 6568891347882291391L;


   @Override
   public String getPath() {
      return "/getAllCounters";
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

      try {
         GetAllCountersResponse resp = this.server.getAllCounters();

         httpResponse.getWriter().write(new WebServiceJsonDataBuilder<GetAllCountersResponse>().writeData(resp));
         httpResponse.getWriter().flush();
         httpResponse.getWriter().close();

         return HttpServletResponse.SC_OK;
      }
      catch (IOException e) {
         throw new MssException(
               de.mss.utils.exception.ErrorCodes.ERROR_UNABLE_TO_EXECUTE_REQUEST,
               e,
               "error while processing getAllCounters request");
      }
   }
}
