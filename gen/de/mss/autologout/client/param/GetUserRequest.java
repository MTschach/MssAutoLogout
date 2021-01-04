package de.mss.autologout.client.param;

public class GetUserRequest extends de.mss.net.webservice.WebServiceRequest {
   private static final long serialVersionUID = 23544575678l;




   /**  */
   @javax.ws.rs.PathParam (value = "username")
   private String userName = null;
   

   public GetUserRequest () {
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



   @Override
   public void checkRequiredFields() throws de.mss.utils.exception.MssException {

      if (this.userName == null)
         throw new de.mss.utils.exception.MssException(de.mss.net.exception.ErrorCodes.ERROR_REQUIRED_FIELD_MISSING, "userName must not be null");


      super.checkRequiredFields();
   }



   
   
}
