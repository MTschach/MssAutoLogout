package de.mss.autologout.client.param;

public class UserSpecialValue implements java.io.Serializable {
   private static final long serialVersionUID = 2134236436l;




   /**  */
   
   private java.util.Date date = null;
   

   /**  */
   
   private Integer minutes = null;
   

   /**  */
   
   private Boolean lock = null;
   

   /**  */
   
   private String reason = null;
   

   /**  */
   
   private Boolean delete = null;
   

   public UserSpecialValue () {
      // nothing to do here
   }
   

   public java.util.Date getDate () { return this.date; }
   

   public Integer getMinutes () { return this.minutes; }
   

   public Boolean getLock () { return this.lock; }
   

   public String getReason () { return this.reason; }
   

   public Boolean getDelete () { return this.delete; }
   


   public void setDate (java.util.Date v) { this.date = v; }
   

   public void setMinutes (Integer v) { this.minutes = v; }
   

   public void setLock (Boolean v) { this.lock = v; }
   

   public void setReason (String v) { this.reason = v; }
   

   public void setDelete (Boolean v) { this.delete = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.date != null)
         sb.append("Date {" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(this.date) + "} ");

      if (this.minutes != null)
         sb.append("Minutes {" + this.minutes.toString() + "} ");

      if (this.lock != null)
         sb.append("Lock {" + this.lock + "} ");

      if (this.reason != null)
         sb.append("Reason {" + this.reason + "} ");

      if (this.delete != null)
         sb.append("Delete {" + this.delete + "} ");

      sb.append("] ");
      return sb.toString();
   }













   
   
}
