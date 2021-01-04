package de.mss.autologout.server.rest;

import java.math.BigInteger;
import java.util.List;
import java.util.TreeMap;

import de.mss.autologout.client.param.CounterValues;
import de.mss.autologout.client.param.GetAllCounterRequest;
import de.mss.autologout.client.param.GetAllCounterResponse;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.defs.Defs;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.rest.RestMethod;
import de.mss.utils.exception.MssException;

public class GetAllCounters extends AutoLogoutWebService<GetAllCounterRequest, GetAllCounterResponse> {

   public GetAllCounters() {
      super(GetAllCounterRequest::new, GetAllCounterResponse::new);
   }


   private static final long serialVersionUID = 6568891347882291391L;


   @Override
   public String getPath() {
      return "/getAllCounters";
   }


   @Override
   public String getMethod() {
      return RestMethod.GET.getMethod();
   }


   @Override
   public GetAllCounterResponse handleRequest(String loggingId, GetAllCounterRequest req) throws MssException {
      final List<String> users = this.server.getUserDb().getUsers();

      final GetAllCounterResponse resp = new GetAllCounterResponse();
      resp.setCounterValues(new TreeMap<>());

      for (final String userName : users) {
         final AutoLogoutCounter alc = this.server.loadUser(userName, false);
         final CounterValues values = new CounterValues();
         values.setValues(alc.getCounterValues());
         values
               .getValues()
               .put(Defs.DB_DATE_FORMAT.format(new java.util.Date()), BigInteger.valueOf(alc.getDailyCounter().getCurrentMinutes()));
         resp.getCounterValues().put(userName, values);
      }

      return resp;
   }
}
