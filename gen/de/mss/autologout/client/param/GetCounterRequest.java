package de.mss.autologout.client.param;

public class GetCounterRequest extends de.mss.net.webservice.WebServiceRequest {
   private static final long serialVersionUID = 58356463491745654l;




   /**  */
   @javax.ws.rs.PathParam (value = "username")
   private String userName = "null";
   

   public GetCounterRequest () {
      super();
   }
   

   public String getUserName () { return this.userName; }
   


   public void setUserName (String v) { this.userName = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.userName != null)
         sb.append("UserName {" + this.userName + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }


   
   
}
