package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.AddToCounterRequest;
import de.mss.autologout.client.param.SetCounterBody;
import de.mss.autologout.client.param.SetCounterRequest;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class AddToCounter extends AutoLogoutAuthTokenWebService<AddToCounterRequest> {

   private static final long serialVersionUID = 6568891347882291391L;


   public AddToCounter() {
      super(AddToCounterRequest::new, WebServiceResponse::new);
   }


   @Override
   public RestMethod getMethod() {
      return CallPaths.ADD_TO_COUNTER.getMethod();
   }


   @Override
   public String getPath() {
      return CallPaths.ADD_TO_COUNTER.getPath();
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, AddToCounterRequest req) throws MssException {
      final int newDailyValue = this.server.getWorkDb().getDailyValue(req.getUserName(), req.getBody().getDate()) + req.getBody().getValue();

      final SetCounterRequest scr = new SetCounterRequest();
      scr.setAuthToken(req.getAuthToken());
      scr.setBody(new SetCounterBody());
      scr.getBody().setDate(req.getBody().getDate());
      scr.getBody().setReason(req.getBody().getReason());
      scr.getBody().setValue(newDailyValue);
      scr.setLanguage(req.getLanguage());
      scr.setLoggingId(loggingId);
      scr.setUserName(req.getUserName());

      return new SetCounter().handleRequest(loggingId, scr);
   }
}
