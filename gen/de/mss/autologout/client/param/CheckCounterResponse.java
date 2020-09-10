package de.mss.autologout.client.param;

public class CheckCounterResponse extends de.mss.net.webservice.WebServiceResponse {
   private static final long serialVersionUID = 5376841448516046929l;




   /**  */
   
   private Boolean forceLogout = Boolean.FALSE;
   

   /**  */
   
   private String headLine = "null";
   

   /**  */
   
   private String message = "null";
   

   /**  */
   
   private String spokenMessage = "null";
   

   public CheckCounterResponse () {
      super();
   }
   

   public Boolean getForceLogout () { return this.forceLogout; }
   

   public String getHeadLine () { return this.headLine; }
   

   public String getMessage () { return this.message; }
   

   public String getSpokenMessage () { return this.spokenMessage; }
   


   public void setForceLogout (Boolean v) { this.forceLogout = v; }
   

   public void setHeadLine (String v) { this.headLine = v; }
   

   public void setMessage (String v) { this.message = v; }
   

   public void setSpokenMessage (String v) { this.spokenMessage = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.forceLogout != null)
         sb.append("ForceLogout {" + this.forceLogout + "} ");

      if (this.headLine != null)
         sb.append("HeadLine {" + this.headLine + "} ");

      if (this.message != null)
         sb.append("Message {" + this.message + "} ");

      if (this.spokenMessage != null)
         sb.append("SpokenMessage {" + this.spokenMessage + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }











   
   
}
