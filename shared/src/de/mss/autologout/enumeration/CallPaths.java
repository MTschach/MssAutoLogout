package de.mss.autologout.enumeration;

import de.mss.net.rest.RestMethod;

public enum CallPaths {

   //@formatter:off
     CHECK_COUNTER               (RestMethod.PATCH       , "/users/{username}/counter")
   , GET_COUNTER                 (RestMethod.GET         , "/users/{username}/counter")
   , GET_USERS                   (RestMethod.GET         , "/users")
   , ADD_USER                    (RestMethod.POST        , "/users")
   , DELETE_USER                 (RestMethod.DELETE      , "/users/{username}")
   , CHANGE_USER                 (RestMethod.PATCH       , "/users/{username}")
   , SET_COUNTER                 (RestMethod.POST        , "/users/{username}/counter")
   , ADD_TO_COUNTER              (RestMethod.PATCH       , "/users/{username}/counter")
   , GET_ALL_COUNTER             (RestMethod.GET         , "/users/counter")
   ;
   //@formatter:on

   private RestMethod method;
   private String     path;

   private CallPaths(RestMethod r, String p) {
      this.method = r;
      this.path = p;
   }


   public RestMethod getMethod() {
      return this.method;
   }


   public String getPath() {
      return this.path;
   }
}
