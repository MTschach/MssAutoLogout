package de.mss.autologout.client.param;

public class SetCounterRequest extends de.mss.net.webservice.WebServiceRequest {
   private static final long serialVersionUID = 5837825646349542334l;




   /**  */
   @javax.ws.rs.PathParam (value = "username")
   private String userName = "null";
   

   /**  */
   @de.mss.net.webservice.BodyParam (value = "body")
   private de.mss.autologout.client.param.SetCounterBody body = null;
   

   public SetCounterRequest () {
      super();
   }
   

   public String getUserName () { return this.userName; }
   

   public de.mss.autologout.client.param.SetCounterBody getBody () { return this.body; }
   


   public void setUserName (String v) { this.userName = v; }
   

   public void setBody (de.mss.autologout.client.param.SetCounterBody v) { this.body = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.userName != null)
         sb.append("UserName {" + this.userName + "} ");

      if (this.body != null)
         sb.append("Body {" + this.body.toString() + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }




   
   
}
