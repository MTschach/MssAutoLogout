package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.DeleteUserRequest;
import de.mss.autologout.client.param.DeleteUserResponse;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class DeleteUser extends AutoLogoutAuthTokenWebService<DeleteUserRequest> {

   private static final long serialVersionUID = 6568891347882291391L;


   public DeleteUser() {
      super(DeleteUserRequest::new, DeleteUserResponse::new);
   }


   @Override
   public RestMethod getMethod() {
      return CallPaths.DELETE_USER.getMethod();
   }


   @Override
   public String getPath() {
      return CallPaths.DELETE_USER.getPath();
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, DeleteUserRequest req) throws MssException {
      this.server.getUserDb().deleteUser(req.getUserName());

      return WebService.getDefaultOkResponse();
   }
}
