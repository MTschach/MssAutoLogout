package de.mss.autologout.server.rest;

import de.mss.autologout.client.param.ListUsersRequest;
import de.mss.autologout.client.param.ListUsersResponse;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.autologout.server.AutoLogoutWebService;
import de.mss.net.rest.RestMethod;
import de.mss.utils.exception.MssException;

public class ListUsers extends AutoLogoutWebService<ListUsersRequest, ListUsersResponse> {

   private static final long serialVersionUID = 6568891347882291391L;


   public ListUsers() {
      super(ListUsersRequest::new, ListUsersResponse::new);
   }


   @Override
   public RestMethod getMethod() {
      return CallPaths.GET_USERS.getMethod();
   }


   @Override
   public String getPath() {
      return CallPaths.GET_USERS.getPath();
   }


   @Override
   public ListUsersResponse handleRequest(String loggingId, ListUsersRequest req) throws MssException {
      final ListUsersResponse resp = new ListUsersResponse();
      resp.setUserlist(this.server.getUsersAndConfig());
      return resp;
   }
}
