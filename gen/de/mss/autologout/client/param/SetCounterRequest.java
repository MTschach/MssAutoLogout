package de.mss.autologout.client.param;

public class SetCounterRequest extends AuthTokenRequest {
   private static final long serialVersionUID = 5837825646349542334l;




   /**  */
   @de.mss.net.webservice.BodyParam (value = "body")
   private de.mss.autologout.client.param.SetCounterBody body = null;
   

   public SetCounterRequest () {
      super();
   }
   

   public de.mss.autologout.client.param.SetCounterBody getBody () { return this.body; }
   


   public void setBody (de.mss.autologout.client.param.SetCounterBody v) { this.body = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.body != null)
         sb.append("Body {" + this.body.toString() + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }



   @Override
   public void checkRequiredFields() throws de.mss.utils.exception.MssException {

      if (this.body == null)
         throw new de.mss.utils.exception.MssException(de.mss.net.exception.ErrorCodes.ERROR_REQUIRED_FIELD_MISSING, "body must not be null");
this.body.checkRequiredFields();


      super.checkRequiredFields();
   }



   
   
}
