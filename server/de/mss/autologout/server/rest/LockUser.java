package de.mss.autologout.server.rest;

import java.util.ArrayList;

import de.mss.autologout.client.param.LockUserRequest;
import de.mss.autologout.client.param.LockUserResponse;
import de.mss.autologout.client.param.ModifyUserBody;
import de.mss.autologout.client.param.UserSpecialValue;
import de.mss.autologout.server.AutoLogoutAuthTokenWebService;
import de.mss.net.rest.RestMethod;
import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class LockUser extends AutoLogoutAuthTokenWebService<LockUserRequest> {

   private static final long serialVersionUID = 6568891347882291391L;


   public LockUser() {
      super(LockUserRequest::new, LockUserResponse::new);
   }


   @Override
   public String getMethod() {
      return RestMethod.POST.getMethod();
   }


   @Override
   public String getPath() {
      return "/{username}/lock";
   }


   @Override
   public WebServiceResponse handleRequest(String loggingId, LockUserRequest req) throws MssException {
      java.util.Date date = req.getBody().getDate();
      if (date == null) {
         date = new java.util.Date();
      }

      String reason = this.authenticatedUser.getName() + " - " + req.getBody().getReason();
      if (reason.length() > 255) {
         reason = reason.substring(0, 252) + "...";
      }

      final UserSpecialValue usv = new UserSpecialValue();
      usv.setDate(date);
      usv.setLock(req.getBody().getLock());
      usv.setReason(reason);
      usv.setDelete(!req.getBody().getLock());

      final ModifyUserBody mub = new ModifyUserBody();
      mub.setSpecialValues(new ArrayList<>());
      mub.getSpecialValues().add(usv);
      this.server.getUserDb().changeUser(req.getUserName(), mub);

      this.server.loadUser(req.getUserName(), true);

      return WebService.getDefaultOkResponse();
   }
}
