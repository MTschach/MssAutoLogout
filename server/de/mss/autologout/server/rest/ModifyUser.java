package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.ModifiyUserRequest;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class ModifyUser extends AutoLogoutAuthTokenWebService<ModifiyUserRequest> {

   private static final long serialVersionUID = 6568891347882291391L;


   public ModifyUser() {
      super(ModifiyUserRequest::new, WebServiceResponse::new);
   }


   @Override
   public String getMethod() {
      return RestMethod.POST.getMethod();
   }


   @Override
   public String getPath() {
      return "/admin/{username}/modify";
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, ModifiyUserRequest req) throws MssException {

      this.server.getUserDb().changeUser(req.getUserName(), req.getBody());
      this.server.loadUser(req.getUserName(), true);

      return WebService.getDefaultOkResponse();
   }
}
