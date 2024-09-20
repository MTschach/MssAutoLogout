package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.CheckCounterRequest;
import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.rest.RestMethod;
import de.mss.utils.Tools;
import de.mss.utils.exception.MssException;

public class CheckCounter extends AutoLogoutWebService<CheckCounterRequest, CheckCounterResponse> {

   private static final long serialVersionUID = 6568891347882291391L;


   public CheckCounter() {
      super(CheckCounterRequest::new, CheckCounterResponse::new);
   }


   @Override
   public RestMethod getMethod() {
      return CallPaths.CHECK_COUNTER.getMethod();
   }


   @Override
   public String getPath() {
      return CallPaths.CHECK_COUNTER.getPath();
   }


   @Override
   public CheckCounterResponse handleRequest(String loggingId, CheckCounterRequest req) throws MssException {
      final AutoLogoutCounter alc = this.server.loadUser(req.getUserName(), false);

      final CheckCounterResponse resp = new CheckCounterResponse();
      resp.setForceLogout(Boolean.FALSE);
      resp.setHeadLine(null);
      resp.setMessage(null);
      resp.setSpokenMessage(null);

      if ((alc == null) || alc.isDisabled()) {
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
         if (alc.getDailyCounter().getCurrentSeconds() < req.getCurrentCounter()) {
            final int diff = Math.abs(req.getCurrentCounter() - alc.getDailyCounter().getCurrentSeconds());
            getLogger()
                  .debug(
                        Tools.formatLoggingId(loggingId)
                              + "correcting counter from "
                              + alc.getDailyCounter().getCurrentSeconds()
                              + " to "
                              + req.getCurrentCounter()
                              + " - adding "
                              + diff);
            alc.getDailyCounter().addSeconds(diff);
            alc.getWeeklyCounter().addSeconds(diff);
         }
      }

      if (!alc.getWorkingTimeChecker().check(loggingId, getLogger(), req.getUserName(), req.getCheckInterval(), resp)) {
         if (!alc.getWeeklyCounter().check(loggingId, getLogger(), req.getUserName(), req.getCheckInterval(), resp)) {
            alc.getDailyCounter().check(loggingId, getLogger(), req.getUserName(), req.getCheckInterval(), resp);
         }
      }

      this.server.getWorkDb().saveTime(req.getUserName(), alc);

      resp.setCounterValue(alc.getDailyCounter().getCurrentSeconds());
      return resp;
   }
}
