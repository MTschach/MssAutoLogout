package de.mss.autologout.client.param;

public class CounterValues implements java.io.Serializable {
   private static final long serialVersionUID = 537684144851606234l;




   /**  */
   
   private java.util.Map<String,java.math.BigInteger> values = null;
   

   public CounterValues () {
      // nothing to do here
   }
   

   public java.util.Map<String,java.math.BigInteger> getValues () { return this.values; }
   


   public void setValues (java.util.Map<String,java.math.BigInteger> v) { this.values = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.values != null)
         sb.append("Values {" + writeValues() + "} ");

      sb.append("] ");
      return sb.toString();
   }




   public String writeValues() {
      StringBuilder sb = new StringBuilder("size {" + this.values.size() + "} ");
      for(java.util.Map.Entry<String,java.math.BigInteger> e : this.values.entrySet()) {
         if (e.getValue() != null) sb.append("[" + e.getKey() + "] {" + e.getValue().toString() + "} ");
      }
      return sb.toString();
   }

   
   
}
