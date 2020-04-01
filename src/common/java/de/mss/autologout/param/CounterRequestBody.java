package de.mss.autologout.param;

public class CounterRequestBody implements java.io.Serializable {

   private static final long serialVersionUID = 2336575L;


   private String user = null;


   private String secret = null;


   private Integer value = null;


   public CounterRequestBody() {}


   public void setUser(String u) {
      this.user = u;
   }


   public void setSecret(String s) {
      this.secret = s;
   }
   
   
   public void setValue(Integer v) {
	   this.value = v;
   }


   public String getUser() {
      return this.user;
   }


   public String getSecret() {
      return this.secret;
   }
   
   
   public Integer getValue() {
	   return this.value;
   }
}
