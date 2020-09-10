package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.ModifiyUserRequest;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class ModifyUser extends AutoLogoutAuthTokenWebService<ModifiyUserRequest> {

   public ModifyUser() {
      super(ModifiyUserRequest::new, WebServiceResponse::new);
   }


   private static final long serialVersionUID = 6568891347882291391L;


   @Override
   public String getPath() {
      return "/admin/{username}/modify";
   }


   @Override
   public String getMethod() {
      return RestMethod.POST.getMethod();
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, ModifiyUserRequest req) throws MssException {

      this.server.getUserDb().changeUser(req.getUserName(), req.getBody());

      return WebService.getDefaultOkResponse();
   }
}
