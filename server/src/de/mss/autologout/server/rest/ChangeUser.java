package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.ChangeUserRequest;
import de.mss.autologout.client.param.ChangeUserResponse;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class ChangeUser extends AutoLogoutAuthTokenWebService<ChangeUserRequest> {

   private static final long serialVersionUID = 6568891347882291391L;


   public ChangeUser() {
      super(ChangeUserRequest::new, ChangeUserResponse::new);
   }


   @Override
   public RestMethod getMethod() {
      return CallPaths.CHANGE_USER.getMethod();
   }


   @Override
   public String getPath() {
      return CallPaths.CHANGE_USER.getPath();
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, ChangeUserRequest req) throws MssException {
      this.server.getUserDb().changeUser(req.getUserName(), req.getBody());

      return WebService.getDefaultOkResponse();
   }
}
