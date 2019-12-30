package de.mss.autologout.server;

import javax.servlet.http.HttpServletResponse;

import de.mss.net.webservice.WebService;
import de.mss.utils.exception.MssException;

public class AutoLogoutWebService extends WebService {

   protected AutoLogoutServer server = null;

   @Override
   public String getPath() {
      return null;
   }

   @Override
   protected int handleException(String loggingId, MssException e, HttpServletResponse httpResponse) {
      return 0;
   }


   public void setAutoLogoutServer(AutoLogoutServer s) {
      this.server = s;
   }
}
