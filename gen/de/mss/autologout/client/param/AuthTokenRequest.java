package de.mss.autologout.client.param;

public class AuthTokenRequest extends de.mss.net.webservice.WebServiceRequest {
   private static final long serialVersionUID = 58356463495445657l;




   /**  */
   @javax.ws.rs.HeaderParam (value = "authtoken")
   private String authToken = "null";
   

   /**  */
   @javax.ws.rs.PathParam (value = "username")
   private String userName = "null";
   

   public AuthTokenRequest () {
      super();
   }
   

   public String getAuthToken () { return this.authToken; }
   

   public String getUserName () { return this.userName; }
   


   public void setAuthToken (String v) { this.authToken = v; }
   

   public void setUserName (String v) { this.userName = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.authToken != null)
         sb.append("AuthToken {" + this.authToken + "} ");

      if (this.userName != null)
         sb.append("UserName {" + this.userName + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }



   @Override
   public void checkRequiredFields() throws de.mss.utils.exception.MssException {

      if (this.authToken == null)
         throw new de.mss.utils.exception.MssException(de.mss.net.exception.ErrorCodes.ERROR_REQUIRED_FIELD_MISSING, "authToken must not be null");


      if (this.userName == null)
         throw new de.mss.utils.exception.MssException(de.mss.net.exception.ErrorCodes.ERROR_REQUIRED_FIELD_MISSING, "userName must not be null");


      super.checkRequiredFields();
   }





   
   
}
