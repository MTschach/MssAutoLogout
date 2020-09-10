package de.mss.autologout.client.param;

public class ModifiyUserRequest extends AuthTokenRequest {
   private static final long serialVersionUID = 23544575678l;




   /**  */
   @de.mss.net.webservice.BodyParam (value = "body")
   private de.mss.autologout.client.param.ModifyUserBody body = null;
   

   public ModifiyUserRequest () {
      super();
   }
   

   public de.mss.autologout.client.param.ModifyUserBody getBody () { return this.body; }
   


   public void setBody (de.mss.autologout.client.param.ModifyUserBody v) { this.body = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.body != null)
         sb.append("Body {" + this.body.toString() + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }





   
   
}
