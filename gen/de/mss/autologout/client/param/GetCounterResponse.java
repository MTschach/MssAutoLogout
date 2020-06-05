package de.mss.autologout.client.param;

public class GetCounterResponse extends de.mss.net.webservice.WebServiceResponse {
   private static final long serialVersionUID = 537684144851606579l;




   /**  */
   
   private de.mss.autologout.client.param.CounterValues counterValues = null;
   

   public GetCounterResponse () {
      super();
   }
   

   public de.mss.autologout.client.param.CounterValues getCounterValues () { return this.counterValues; }
   


   public void setCounterValues (de.mss.autologout.client.param.CounterValues v) { this.counterValues = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.counterValues != null)
         sb.append("CounterValues {" + this.counterValues.toString() + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }


   
   
}
