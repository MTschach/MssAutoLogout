package de.mss.autologout.server;

import de.mss.net.webservice.WebServiceResponse;

public class CheckCounterResponse extends WebServiceResponse {

   private static final long serialVersionUID = -5376841448516046929L;
   private Boolean forceLogout = Boolean.FALSE;
   private String  headline    = null;
   private String  message     = null;


   public CheckCounterResponse(Boolean forceLogout, String headline, String message) {
      setForceLogout(forceLogout);
      setHeadline(headline);
      setMessage(message);
   }


   private void setForceLogout(Boolean f) {
      this.forceLogout = f;
   }


   private void setHeadline(String h) {
      this.headline = h;
   }


   private void setMessage(String m) {
      this.message = m;
   }


   public Boolean getForceLogout() {
      return this.forceLogout;
   }


   public String getHeadline() {
      return this.headline;
   }


   public String getMessage() {
      return this.message;
   }
}
