package de.mss.autologout.param;

import javax.ws.rs.PathParam;

import de.mss.net.webservice.WebServiceRequest;

public class GetCounterRequest extends WebServiceRequest {

   private static final long serialVersionUID = 5837825646349178565L;


   @PathParam(value = "username")
   private String userName = null;


   public GetCounterRequest() {}


   public void setUserName(String u) {
      this.userName = u;
   }


   public String getUserName() {
      return this.userName;
   }
}
