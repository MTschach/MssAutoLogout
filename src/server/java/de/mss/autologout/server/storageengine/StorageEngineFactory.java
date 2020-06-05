package de.mss.autologout.server.storageengine;

import de.mss.configtools.ConfigFile;

public class StorageEngineFactory {

   private static StorageEngine storageEngine = null;

   private StorageEngineFactory() {}


   public static StorageEngine getStorageEngine(ConfigFile cfg) {
      if (storageEngine == null) {
         storageEngine = new XmlStorage(cfg);
      }

      return storageEngine;
   }
}
