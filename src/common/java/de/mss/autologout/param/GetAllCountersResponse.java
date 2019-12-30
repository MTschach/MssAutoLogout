package de.mss.autologout.param;

import java.math.BigInteger;
import java.util.Map;

import de.mss.net.webservice.WebServiceResponse;

public class GetAllCountersResponse extends WebServiceResponse {

   private static final long serialVersionUID = -5376841448516046929L;

   private Map<String, Map<String, BigInteger>> counterValues;

   public GetAllCountersResponse() {}


   public void setCounterValues(Map<String, Map<String, BigInteger>> l) {
      this.counterValues = l;
   }


   public Map<String, Map<String, BigInteger>> getCounterValues() {
      return this.counterValues;
   }
}
