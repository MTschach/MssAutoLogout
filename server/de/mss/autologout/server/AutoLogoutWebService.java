package de.mss.autologout.server;

import java.util.function.Supplier;

import de.mss.net.webservice.WebService;
import de.mss.net.webservice.WebServiceRequest;
import de.mss.net.webservice.WebServiceResponse;

public abstract class AutoLogoutWebService<R extends WebServiceRequest, T extends WebServiceResponse> extends WebService<R, T> {


   public AutoLogoutWebService(Supplier<R> reqts, Supplier<T> rts) {
      super(reqts, rts);
   }


   private static final long serialVersionUID = 4706554074682781143L;


   protected AutoLogoutServer server = null;


   public void setAutoLogoutServer(AutoLogoutServer s) {
      this.server = s;
   }
}
