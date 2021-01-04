package de.mss.autologout.client.param;

public class GetUserResponse extends de.mss.net.webservice.WebServiceResponse {
   private static final long serialVersionUID = 2355476l;




   /**  */
   
   private Integer dailyCounter = null;
   

   /**  */
   
   private Integer weeklyCounter = null;
   

   /**  */
   
   private de.mss.autologout.client.param.CounterValues counterValues = null;
   

   public GetUserResponse () {
      super();
   }
   

   public Integer getDailyCounter () { return this.dailyCounter; }
   

   public Integer getWeeklyCounter () { return this.weeklyCounter; }
   

   public de.mss.autologout.client.param.CounterValues getCounterValues () { return this.counterValues; }
   


   public void setDailyCounter (Integer v) { this.dailyCounter = v; }
   

   public void setWeeklyCounter (Integer v) { this.weeklyCounter = v; }
   

   public void setCounterValues (de.mss.autologout.client.param.CounterValues v) { this.counterValues = v; }
   


   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(getClass().getName() + "[ ");

      if (this.dailyCounter != null)
         sb.append("DailyCounter {" + this.dailyCounter.toString() + "} ");

      if (this.weeklyCounter != null)
         sb.append("WeeklyCounter {" + this.weeklyCounter.toString() + "} ");

      if (this.counterValues != null)
         sb.append("CounterValues {" + this.counterValues.toString() + "} ");

      sb.append(super.toString());
      sb.append("] ");
      return sb.toString();
   }









   
   
}
