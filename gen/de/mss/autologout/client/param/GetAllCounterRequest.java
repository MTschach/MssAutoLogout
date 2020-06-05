package de.mss.autologout.client.param;

public class GetAllCounterRequest extends de.mss.net.webservice.WebServiceRequest {
   private static final long serialVersionUID = 5837825646349542334l;




   public GetAllCounterRequest () {
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
