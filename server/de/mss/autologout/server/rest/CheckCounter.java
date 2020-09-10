package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.CheckCounterRequest;
import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.param.AutoLogoutCounter;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.autologout.server.LogoutCounter;
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

      if (!checkCounter(req.getUserName(), alc.getWeeklyCounter(), req.getCheckInterval(), resp)) {
         checkCounter(req.getUserName(), alc.getDailyCounter(), req.getCheckInterval(), resp);
      }

      alc.getDailyCounter().addSeconds(req.getCheckInterval());
      alc.getWeeklyCounter().addSeconds(req.getCheckInterval());

      this.server.getWorkDb().saveTime(req.getUserName(), alc);

      return resp;
   }


   private boolean checkCounter(String userName, LogoutCounter lc, Integer checkInterval, CheckCounterResponse resp) {
      if (lc.isForceLogoff()) {
         resp.setForceLogout(Boolean.TRUE);
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + ". Deine " + lc.getName() + " ist abgelaufen. Du wirst automatisch abgemeldet.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen. Du wirst automatisch abgemeldet.");
         return true;
      }

      if (lc.isMinutesUntilForceLogoff()) {
         resp.setHeadLine("Info");
         resp
               .setMessage(
                     "Hallo "
                           + userName
                           + "! Deine "
                           + lc.getName()
                           + " Zeit ist seit "
                           + lc.getMinutesOvertime()
                           + " Minuten abgelaufen. Du wirst in "
                           + lc.getMinutesUntilFoceLogoff()
                           + "Minuten abgemeldet.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen.");
         return true;
      }

      if (lc.isFirstWarning(checkInterval)) {
         resp.setHeadLine("Info");
         resp
               .setMessage(
                     "Hallo "
                           + userName
                           + "! Deine "
                           + lc.getName()
                           + " Zeit läuft in "
                           + (lc.getMinutesForceLogoff() - lc.getMinutesFirstWarning())
                           + " Minuten ab");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen.");
         return true;
      }

      if (lc.isMinutesReached(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit ist abgelaufen.");
         return true;
      }

      if (lc.isSecondInfo(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + "! Deine " + lc.getName() + " Zeit läuft in " + lc.getMinutesSecondInfo() + " Minuten ab.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit läuft bald ab.");
         return true;
      }

      if (lc.isFirstInfo(checkInterval)) {
         resp.setHeadLine("Info");
         resp.setMessage("Hallo " + userName + "! Deine " + lc.getName() + " Zeit läuft in " + lc.getMinutesFirstInfo() + " Minuten ab.");
         resp.setSpokenMessage("Hallo " + userName + ". Deine Zeit läuft bald ab.");
         return true;
      }

      return false;
   }
}
