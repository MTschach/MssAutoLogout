package de.mss.autologout.client.param;

public class GetAllCounterResponse extends de.mss.net.webservice.WebServiceResponse {
   private static final long serialVersionUID = 53768414485163464l;




   /**  */
   
   private java.util.Map<String,de.mss.autologout.client.param.CounterValues> counterValues = null;
   

   public GetAllCounterResponse () {
      super();
   }
   

   public java.util.Map<String,de.mss.autologout.client.param.CounterValues> getCounterValues () { return this.counterValues; }
   


   public void setCounterValues (java.util.Map<String,de.mss.autologout.client.param.CounterValues> v) { this.counterValues = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.counterValues != null)
         sb.append("CounterValues {" + writeCounterValues() + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }




   public String writeCounterValues() {
      StringBuilder sb = new StringBuilder("size {" + this.counterValues.size() + "} ");
      for(java.util.Map.Entry<String,de.mss.autologout.client.param.CounterValues> e : this.counterValues.entrySet()) {
         if (e.getValue() != null) sb.append("[" + e.getKey() + "] {" + e.getValue().toString() + "} ");
      }
      return sb.toString();
   }

   
   
}
