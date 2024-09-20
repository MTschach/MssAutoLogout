package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.AddUserRequest;
import de.mss.autologout.client.param.AddUserResponse;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class AddUser extends AutoLogoutAuthTokenWebService<AddUserRequest> {

   private static final long serialVersionUID = 6568891347882291391L;


   public AddUser() {
      super(AddUserRequest::new, AddUserResponse::new);
   }


   @Override
   public RestMethod getMethod() {
      return CallPaths.ADD_USER.getMethod();
   }


   @Override
   public String getPath() {
      return CallPaths.ADD_USER.getPath();
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, AddUserRequest req) throws MssException {
      this.server.getUserDb().addUser(req.getBody());

      return WebService.getDefaultOkResponse();
   }
}
