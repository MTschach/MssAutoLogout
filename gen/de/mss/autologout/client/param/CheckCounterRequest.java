package de.mss.autologout.client.param;

public class CheckCounterRequest extends de.mss.net.webservice.WebServiceRequest {
   private static final long serialVersionUID = 5837825646349178565l;




   /**  */
   @javax.ws.rs.PathParam (value = "username")
   private String userName = null;
   

   /**  */
   @javax.ws.rs.QueryParam (value = "checkInterval")
   private Integer checkInterval = null;
   

   /**  */
   @javax.ws.rs.QueryParam (value = "currentCounter")
   private Integer currentCounter = null;
   

   public CheckCounterRequest () {
      super();
   }
   

   public String getUserName () { return this.userName; }
   

   public Integer getCheckInterval () { return this.checkInterval; }
   

   public Integer getCurrentCounter () { return this.currentCounter; }
   


   public void setUserName (String v) { this.userName = v; }
   

   public void setCheckInterval (Integer v) { this.checkInterval = v; }
   

   public void setCurrentCounter (Integer v) { this.currentCounter = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.userName != null)
         sb.append("UserName {" + this.userName + "} ");

      if (this.checkInterval != null)
         sb.append("CheckInterval {" + this.checkInterval.toString() + "} ");

      if (this.currentCounter != null)
         sb.append("CurrentCounter {" + this.currentCounter.toString() + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }









   
   
}
