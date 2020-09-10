package de.mss.autologout.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.mss.configtools.ConfigFile;

public class AutoLogoutListener implements Runnable {

   private AutoLogoutClient autoLogout = null;


   public AutoLogoutListener(AutoLogoutClient a) {
      this.autoLogout = a;
   }


   @Override
   public void run() {
      ConfigFile cfgFile = this.autoLogout.getConfig();
      BigInteger port = cfgFile.getValue(AutoLogoutClient.CFG_KEY_BASE + ".listen.port", BigInteger.valueOf(31002));
      String bindIp = cfgFile.getValue(AutoLogoutClient.CFG_KEY_BASE + ".listen.ip", "localhost");

      try (ServerSocket serverSocket = new ServerSocket(port.intValue(), 100, InetAddress.getByName(bindIp))) {
         while (this.autoLogout.checkRunning()) {
            handleSocket(serverSocket.accept());
         }
      }
      catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }


   private void handleSocket(Socket socket) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
         String command = null;
         if ((command = reader.readLine()) != null) {
            System.out.println(command);

            switch (command.toLowerCase()) {
               case "quit":
                  this.autoLogout.stop();
                  break;

               default:
                  break;
            }
         }
      }
      catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
