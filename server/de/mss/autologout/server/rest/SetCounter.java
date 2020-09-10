package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.SetCounterRequest;
import de.mss.autologout.param.AutoLogoutCounter;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class SetCounter extends AutoLogoutAuthTokenWebService<SetCounterRequest> {

   public SetCounter() {
      super(SetCounterRequest::new, WebServiceResponse::new);
   }


   private static final long serialVersionUID = 6568891347882291391L;


   @Override
   public String getPath() {
      return "/{username}/setCounter";
   }


   @Override
   public String getMethod() {
      return RestMethod.POST.getMethod();
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, SetCounterRequest req) throws MssException {
      final AutoLogoutCounter alc = this.server.loadUser(req.getUserName(), false);

      final int min = alc.getDailyCounter().getCurrentMinutes();
      alc.getDailyCounter().reset();
      alc.getDailyCounter().addMinutes(req.getBody().getValue());
      alc.getWeeklyCounter().addMinutes(req.getBody().getValue() - min);

      String reason = this.authenticatedUser.getName() + " - " + req.getBody().getReason();
      if (reason.length() > 255) {
         reason = reason.substring(0, 252) + "...";
      }

      this.server.getWorkDb().saveSpecialTime(req.getUserName(), new java.util.Date(), min, reason);
      this.server.getWorkDb().saveTime(req.getUserName(), alc);

      this.server.loadUser(req.getUserName(), true);

      return WebService.getDefaultOkResponse();
   }
}
