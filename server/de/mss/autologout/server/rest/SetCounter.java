package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.SetCounterRequest;
import de.mss.autologout.counter.AutoLogoutCounter;
import de.mss.autologout.defs.Defs;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.DateTimeTools;
import de.mss.utils.exception.MssException;

public class SetCounter extends AutoLogoutAuthTokenWebService<SetCounterRequest> {

   private static final long serialVersionUID = 6568891347882291391L;


   public SetCounter() {
      super(SetCounterRequest::new, WebServiceResponse::new);
   }


   private void addOtherDay(String loggingId, AutoLogoutCounter alc, SetCounterRequest req) throws MssException {
      final String reason = getReason(req);

      final String dateString = Defs.DB_DATE_FORMAT.format(req.getBody().getDate());

      int min = 0;
      if (alc.getCounterValues().get(dateString) != null) {
         min = alc.getCounterValues().get(dateString).intValue();
      }


      this.server.getWorkDb().saveSpecialTime(req.getUserName(), new java.util.Date(), min, reason);
      this.server.getWorkDb().saveTime(req.getUserName(), req.getBody().getValue(), req.getBody().getDate());
   }


   private void addToday(String loggingId, AutoLogoutCounter alc, SetCounterRequest req) throws MssException {
      final int min = alc.getDailyCounter().getCurrentMinutes();
      alc.getDailyCounter().reset();
      alc.getDailyCounter().addMinutes(req.getBody().getValue());
      alc.getWeeklyCounter().addMinutes(req.getBody().getValue() - min);

      final String reason = getReason(req);

      this.server.getWorkDb().saveSpecialTime(req.getUserName(), new java.util.Date(), min, reason);
      this.server.getWorkDb().saveTime(req.getUserName(), alc);
   }


   @Override
   public String getMethod() {
      return RestMethod.POST.getMethod();
   }


   @Override
   public String getPath() {
      return "/{username}/setCounter";
   }


   private String getReason(SetCounterRequest req) {
      String reason = this.authenticatedUser.getName() + " - " + req.getBody().getReason();
      if (reason.length() > 255) {
         reason = reason.substring(0, 252) + "...";
      }
      return reason;
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, SetCounterRequest req) throws MssException {
      final AutoLogoutCounter alc = this.server.loadUser(req.getUserName(), false);


      if (req.getBody().getDate() == null || DateTimeTools.isSameDay(req.getBody().getDate(), new java.util.Date())) {
         addToday(loggingId, alc, req);
      } else {
         addOtherDay(loggingId, alc, req);
      }


      this.server.loadUser(req.getUserName(), true);

      return WebService.getDefaultOkResponse();
   }
}
