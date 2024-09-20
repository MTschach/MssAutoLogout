package de.mss.autologout.server.rest;

import java.util.HashMap;
import java.util.List;

import de.mss.autologout.client.param.CounterValues;
import de.mss.autologout.client.param.GetAllCounterRequest;
import de.mss.autologout.client.param.GetAllCounterResponse;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.rest.RestMethod;
import de.mss.utils.exception.MssException;

public class GetAllCounter extends AutoLogoutWebService<GetAllCounterRequest, GetAllCounterResponse> {

   private static final long serialVersionUID = 6568891347882291391L;


   public GetAllCounter() {
      super(GetAllCounterRequest::new, GetAllCounterResponse::new);
   }


   @Override
   public RestMethod getMethod() {
      return CallPaths.GET_ALL_COUNTER.getMethod();
   }


   @Override
   public String getPath() {
      return CallPaths.GET_ALL_COUNTER.getPath();
   }


   @Override
   public GetAllCounterResponse handleRequest(String loggingId, GetAllCounterRequest req) throws MssException {
      final List<String> users = this.server.getUsers();
      final GetAllCounterResponse resp = new GetAllCounterResponse();
      resp.setCounterValues(new HashMap<>());
      for (final String user : users) {
         final CounterValues cv = new CounterValues();
         cv.setValues(this.server.loadUserValues(user, 7));
         resp.getCounterValues().put(user, cv);
      }

      return resp;
   }
}
