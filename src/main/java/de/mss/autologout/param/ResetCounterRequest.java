package de.mss.autologout.param;

import javax.ws.rs.PathParam;

import de.mss.net.webservice.BodyParam;
import de.mss.net.webservice.WebServiceRequest;

public class ResetCounterRequest extends WebServiceRequest {

   private static final long serialVersionUID = 5837825646349178565L;


   @PathParam(value = "username")
   private String userName = null;


   @BodyParam(value = "user")
   private String user = null;


   @BodyParam(value = "secret")
   private String secret = null;


   public ResetCounterRequest() {}


   public void setUserName(String u) {
      this.userName = u;
   }


   public void setUser(String u) {
      this.user = u;
   }


   public void setSecret(String s) {
      this.secret = s;
   }


   public String getUserName() {
      return this.userName;
   }


   public String getUser() {
      return this.user;
   }


   public String getSecret() {
      return this.secret;
   }
}
