package de.mss.autologout.client.param;

public class SetCounterBody implements java.io.Serializable {
   private static final long serialVersionUID = 537684144851606234l;




   /**  */
   
   private Integer value = null;
   

   /**  */
   
   private String reason = null;
   

   public SetCounterBody () {
      // nothing to do here
   }
   

   public Integer getValue () { return this.value; }
   

   public String getReason () { return this.reason; }
   


   public void setValue (Integer v) { this.value = v; }
   

   public void setReason (String v) { this.reason = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.value != null)
         sb.append("Value {" + this.value.toString() + "} ");

      if (this.reason != null)
         sb.append("Reason {" + this.reason + "} ");

      sb.append("] ");
      return sb.toString();
   }



   public void checkRequiredFields() throws de.mss.utils.exception.MssException {

      if (this.value == null)
         throw new de.mss.utils.exception.MssException(de.mss.net.exception.ErrorCodes.ERROR_REQUIRED_FIELD_MISSING, "value must not be null");


      

   }





   
   
}
