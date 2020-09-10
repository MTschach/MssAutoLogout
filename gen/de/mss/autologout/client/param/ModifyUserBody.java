package de.mss.autologout.client.param;

public class ModifyUserBody implements java.io.Serializable {
   private static final long serialVersionUID = 2134236456l;




   /**  */
   
   private Integer dailyMinutes = null;
   

   /**  */
   
   private Integer weeklyMinutes = null;
   

   /**  */
   
   private java.util.List<de.mss.autologout.client.param.UserSpecialValue> specialValues = null;
   

   public ModifyUserBody () {
      // nothing to do here
   }
   

   public Integer getDailyMinutes () { return this.dailyMinutes; }
   

   public Integer getWeeklyMinutes () { return this.weeklyMinutes; }
   

   public java.util.List<de.mss.autologout.client.param.UserSpecialValue> getSpecialValues () { return this.specialValues; }
   


   public void setDailyMinutes (Integer v) { this.dailyMinutes = v; }
   

   public void setWeeklyMinutes (Integer v) { this.weeklyMinutes = v; }
   

   public void setSpecialValues (java.util.List<de.mss.autologout.client.param.UserSpecialValue> v) { this.specialValues = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.dailyMinutes != null)
         sb.append("DailyMinutes {" + this.dailyMinutes.toString() + "} ");

      if (this.weeklyMinutes != null)
         sb.append("WeeklyMinutes {" + this.weeklyMinutes.toString() + "} ");

      if (this.specialValues != null)
         sb.append("SpecialValues {" + writeSpecialValues() + "} ");

      sb.append("] ");
      return sb.toString();
   }








   public String writeSpecialValues() {
      StringBuilder sb = new StringBuilder("size {" + this.specialValues.size() + "} ");
      for(int i=0; i<this.specialValues.size(); i++) {
         if (this.specialValues.get(i) != null) sb.append("[" + i + "] {" + this.specialValues.get(i).toString() + "} ");
      }
      return sb.toString();
   }



   
   
}
