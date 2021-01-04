package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.CounterValues;
import de.mss.autologout.client.param.GetUserRequest;
import de.mss.autologout.client.param.GetUserResponse;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.rest.RestMethod;
import de.mss.utils.exception.MssException;

public class GetUser extends AutoLogoutWebService<GetUserRequest, GetUserResponse> {

   public GetUser() {
      super(GetUserRequest::new, GetUserResponse::new);
   }


   private static final long serialVersionUID = 6568891347882291391L;


   @Override
   public String getPath() {
      return "/admin/{username}";
   }


   @Override
   public String getMethod() {
      return RestMethod.GET.getMethod();
   }


   @Override
   public GetUserResponse handleRequest(String loggingId, GetUserRequest req) throws MssException {

      final AutoLogoutCounter alc = this.server.getUserDb().loadUser(req.getUserName());

      final GetUserResponse resp = new GetUserResponse();
      if (alc != null) {
         this.server.getWorkDb().loadUser(req.getUserName(), alc);
         resp.setDailyCounter(alc.getDailyCounter().getMaxMinutes());
         resp.setWeeklyCounter(alc.getWeeklyCounter().getMaxMinutes());
         resp.setCounterValues(new CounterValues());
         resp.getCounterValues().setValues(alc.getCounterValues());
      } else {
         resp.setDailyCounter(30);
         resp.setWeeklyCounter(210);
      }

      return resp;
   }
}
