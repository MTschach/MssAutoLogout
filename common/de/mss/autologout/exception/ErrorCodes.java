package de.mss.autologout.exception;

import java.io.Serializable;

import de.mss.utils.exception.Error;

public class ErrorCodes implements Serializable {

   private static final long serialVersionUID                = 23544375686L;

   private static final int  ERROR_CODE_BASE                 = 6000;
   public static final Error ERROR_AUTH_TOKEN_INVALID        = new Error(ERROR_CODE_BASE + 0, "AuthToken invalid");
   public static final Error ERROR_INVLID_AUTHENTICATED_USER = new Error(ERROR_CODE_BASE + 1, "this user is not authenticated");
   public static final Error ERROR_INIT_USERDB               = new Error(ERROR_CODE_BASE + 2, "could not initialize user db");
   public static final Error ERROR_INIT_WORKDB               = new Error(ERROR_CODE_BASE + 3, "could not initialize working db");
   public static final Error ERROR_LOADING_USER              = new Error(ERROR_CODE_BASE + 4, "could not load user");
   public static final Error ERROR_CHANGE_USER               = new Error(ERROR_CODE_BASE + 5, "could not change user");
   public static final Error ERROR_SAVING_TIME               = new Error(ERROR_CODE_BASE + 6, "could not save user times");


   private ErrorCodes() {}
}
