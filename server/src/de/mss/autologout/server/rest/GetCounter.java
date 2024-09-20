package de.mss.autologout.server.rest;

import java.math.BigInteger;

import de.mss.autologout.client.param.CounterValues;
import de.mss.autologout.client.param.GetCounterRequest;
import de.mss.autologout.client.param.GetCounterResponse;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.defs.Defs;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.rest.RestMethod;
import de.mss.utils.exception.MssException;

public class GetCounter extends AutoLogoutWebService<GetCounterRequest, GetCounterResponse> {

   private static final long serialVersionUID = 6568891347882291391L;


   public GetCounter() {
      super(GetCounterRequest::new, GetCounterResponse::new);
   }


   private GetCounterResponse get7Days(String loggingId, String userName) throws MssException {
      final AutoLogoutCounter alc = this.server.loadUser(userName, false);

      final GetCounterResponse resp = new GetCounterResponse();

      resp.setCounterValues(new CounterValues());
      resp.getCounterValues().setValues(alc.getCounterValues());
      resp
            .getCounterValues()
            .getValues()
            .put(Defs.DB_DATE_FORMAT.format(new java.util.Date()), BigInteger.valueOf(alc.getDailyCounter().getCurrentMinutes()));

      return resp;
   }


   @Override
   public RestMethod getMethod() {
      return CallPaths.GET_COUNTER.getMethod();
   }


   @Override
   public String getPath() {
      return CallPaths.GET_COUNTER.getPath();
   }


   @Override
   public GetCounterResponse handleRequest(String loggingId, GetCounterRequest req) throws MssException {
      if ((req.getTimeFrame() == null) || (req.getTimeFrame().intValue() == 7)) {
         return get7Days(loggingId, req.getUserName());
      }

      final GetCounterResponse resp = new GetCounterResponse();
      resp.setCounterValues(new CounterValues());
      resp.getCounterValues().setValues(this.server.loadUserValues(req.getUserName(), req.getTimeFrame()));

      return resp;
   }
}
