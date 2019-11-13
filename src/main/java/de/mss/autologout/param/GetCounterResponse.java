package de.mss.autologout.param;

import java.math.BigInteger;
import java.util.Map;

import de.mss.net.webservice.WebServiceResponse;

public class GetCounterResponse extends WebServiceResponse {

   private static final long serialVersionUID = -5376841448516046929L;

   private Map<String, BigInteger> counterValues;

   public GetCounterResponse() {}


   public void setCounterValues(Map<String, BigInteger> l) {
      this.counterValues = l;
   }


   public Map<String, BigInteger> getCounterValues() {
      return this.counterValues;
   }
}
