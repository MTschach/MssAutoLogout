package de.mss.autologout.param;

import javax.ws.rs.PathParam;

import de.mss.net.webservice.BodyParam;
import de.mss.net.webservice.WebServiceRequest;

public class AddCounterRequest extends WebServiceRequest {

   private static final long serialVersionUID = 2336575L;


   @PathParam(value = "username")
   private String userName = null;


   @BodyParam(value = "user")
   private String user = null;


   @BodyParam(value = "secret")
   private String secret = null;


   @BodyParam(value = "value")
   private Integer value = null;


   public AddCounterRequest() {}


   public void setUserName(String u) {
      this.userName = u;
   }


   public void setUser(String u) {
      this.user = u;
   }


   public void setSecret(String s) {
      this.secret = s;
   }
   
   
   public void setValue(Integer v) {
	   this.value = v;
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
   
   
   public Integer getValue() {
	   return this.value;
   }
}
