package de.mss.autologout.param;

import javax.ws.rs.PathParam;

import de.mss.net.webservice.BodyParam;
import de.mss.net.webservice.WebServiceRequest;

public class AddCounterRequest extends WebServiceRequest {

   private static final long serialVersionUID = 2336575L;


   @PathParam(value = "username")
   private String userName = null;


   @BodyParam(value = "body")
   private AddCounterRequestBody body = null;


   public AddCounterRequest() {}


   public void setUserName(String u) {
      this.userName = u;
   }


   public void setBody(AddCounterRequestBody v) {
      this.body = v;
   }


   public String getUserName() {
      return this.userName;
   }


   public AddCounterRequestBody getBody() {
      return this.body;
   }
}
