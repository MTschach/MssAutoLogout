package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.CheckCounterRequest;
import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.rest.RestMethod;
import de.mss.utils.exception.MssException;

public class CheckCounter extends AutoLogoutWebService<CheckCounterRequest, CheckCounterResponse> {

   public CheckCounter() {
      super(CheckCounterRequest::new, CheckCounterResponse::new);
   }


   private static final long serialVersionUID = 6568891347882291391L;


   @Override
   public String getPath() {
      return "/{username}/checkCounter";
   }


   @Override
   public String getMethod() {
      return RestMethod.GET.getMethod();
   }


   @Override
   public CheckCounterResponse handleRequest(String loggingId, CheckCounterRequest req) throws MssException {
      final AutoLogoutCounter alc = this.server.loadUser(req.getUserName(), false);

      final CheckCounterResponse resp = new CheckCounterResponse();
      resp.setForceLogout(Boolean.FALSE);
      resp.setHeadLine(null);
      resp.setMessage(null);
      resp.setSpokenMessage(null);

      if (alc == null) {
         return resp;
      }

      if (alc.getLockedForToday()) {
         resp.setForceLogout(Boolean.TRUE);
         resp.setHeadLine("Login gesperrt");
         resp.setMessage("Hallo " + req.getUserName() + ". Du darfst dich heute nicht einloggen. " + alc.getReasonForLock());
         resp.setSpokenMessage("Login heute gesperrt");
         return resp;
      }

      if (req.getCurrentCounter() != null) {
         if (alc.getDailyCounter().getCurrentMinutes() < req.getCurrentCounter()) {
            final int diff = req.getCheckInterval() - alc.getDailyCounter().getCurrentMinutes();
            alc.getDailyCounter().addMinutes(diff);
            alc.getWeeklyCounter().addMinutes(diff);
         }
      }

      if (!alc.getWeeklyCounter().check(req.getUserName(), req.getCheckInterval(), resp)) {
         alc.getDailyCounter().check(req.getUserName(), req.getCheckInterval(), resp);
      }

      alc.getDailyCounter().addSeconds(req.getCheckInterval());
      alc.getWeeklyCounter().addSeconds(req.getCheckInterval());

      this.server.getWorkDb().saveTime(req.getUserName(), alc);

      return resp;
   }
}
