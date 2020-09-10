package de.mss.autologout.client.param;

public class DeleteUserRequest extends AuthTokenRequest {
   private static final long serialVersionUID = 23544575454l;




   public DeleteUserRequest () {
      super();
   }
   



   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }



   
   
}
