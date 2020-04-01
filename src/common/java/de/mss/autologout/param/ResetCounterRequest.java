package de.mss.autologout.param;

import javax.ws.rs.PathParam;

import de.mss.net.webservice.BodyParam;
import de.mss.net.webservice.WebServiceRequest;

public class ResetCounterRequest extends WebServiceRequest {

   private static final long serialVersionUID = 5837825646349178565L;


   @PathParam(value = "username")
   private String userName = null;


   @BodyParam(value = "body")
   private CounterRequestBody body = null;
   public ResetCounterRequest() {}


   public void setUserName(String u) {
      this.userName = u;
   }


   public void setBody (CounterRequestBody v) {
      this.body = v;
   }


   public String getUserName() {
      return this.userName;
   }


   public CounterRequestBody getBody () {
      return this.body;
   }
}
