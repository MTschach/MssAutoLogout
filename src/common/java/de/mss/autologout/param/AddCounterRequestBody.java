package de.mss.autologout.param;

public class AddCounterRequestBody extends CounterRequestBody {

   private static final long serialVersionUID = 2336575L;


   private Integer value = null;


   public AddCounterRequestBody() {}


   public void setValue(Integer v) {
      this.value = v;
   }
   
   
   public Integer getValue() {
      return this.value;
   }
}
