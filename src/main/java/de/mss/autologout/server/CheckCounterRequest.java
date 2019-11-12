package de.mss.autologout.server;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.mss.net.webservice.WebServiceRequest;

public class CheckCounterRequest extends WebServiceRequest {

   private static final long serialVersionUID = 5837825646349178565L;


   @PathParam(value = "username")
   private String userName = null;


   @QueryParam(value = "checkinterval")
   private Integer checkInterval = null;


   public CheckCounterRequest() {}


   public void setUserName(String u) {
      this.userName = u;
   }


   public void setCheckInterval(Integer i) {
      this.checkInterval = i;
   }


   public String getUserName() {
      return this.userName;
   }


   public Integer getCheckInterval() {
      return this.checkInterval;
   }

}
