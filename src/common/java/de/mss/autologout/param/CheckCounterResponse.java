package de.mss.autologout.param;

import de.mss.net.webservice.WebServiceResponse;

public class CheckCounterResponse extends WebServiceResponse {

   private static final long serialVersionUID = -5376841448516046929L;
   private Boolean forceLogout = Boolean.FALSE;
   private String  headline    = null;
   private String  message     = null;
   private String            spokenMessage    = null;

   public CheckCounterResponse() {}

   public CheckCounterResponse(Boolean forceLogout, String headline, String message) {
      setForceLogout(forceLogout);
      setHeadline(headline);
      setMessage(message);
      setSpokenMessage(message);
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


   public void setSpokenMessage(String v) {
      this.spokenMessage = v;
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


   public String getSpokenMessage() {
      return this.spokenMessage;
   }
}
