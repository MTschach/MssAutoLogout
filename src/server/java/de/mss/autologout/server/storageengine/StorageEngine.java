package de.mss.autologout.server.storageengine;

import java.io.Serializable;

import de.mss.autologout.param.AutoLogoutCounter;
import de.mss.utils.exception.MssException;

public interface StorageEngine extends Serializable {


   public AutoLogoutCounter loadUser(String userName) throws MssException;


   public void storeUser(String userName, AutoLogoutCounter counters) throws MssException;


   public void storeUser(String userName, AutoLogoutCounter counters, String reason) throws MssException;
}
