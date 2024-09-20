package de.mss.autologout.client;

import de.mss.autologout.client.param.AddUserRequest;
import de.mss.autologout.client.param.AddUserResponse;
import de.mss.autologout.client.param.ChangeUserRequest;
import de.mss.autologout.client.param.ChangeUserResponse;
import de.mss.autologout.client.param.CheckCounterRequest;
import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.client.param.DeleteUserRequest;
import de.mss.autologout.client.param.DeleteUserResponse;
import de.mss.autologout.client.param.GetAllCounterRequest;
import de.mss.autologout.client.param.GetAllCounterResponse;
import de.mss.autologout.client.param.GetCounterRequest;
import de.mss.autologout.client.param.GetCounterResponse;
import de.mss.autologout.client.param.ListUsersRequest;
import de.mss.autologout.client.param.ListUsersResponse;
import de.mss.autologout.enumeration.CallPaths;
import de.mss.configtools.ConfigFile;
import de.mss.net.client.ClientBase;
import de.mss.net.webservice.WebServiceJsonCaller;
import de.mss.utils.exception.MssException;

public class MssAutoLogoutClient extends ClientBase {

   private static MssAutoLogoutClient instance    = null;

   private static final String        PATH_PREFIX = "/autologout/v1";
   private static final int           MAX_RETRIES = 3;

   public static MssAutoLogoutClient getInstance(ConfigFile cfg) throws MssException {
      if (instance == null) {
         instance = new MssAutoLogoutClient(cfg);
      }

      return instance;
   }


   private MssAutoLogoutClient(ConfigFile cfg) throws MssException {
      super(cfg, "de.mss.autologout");
   }


   public AddUserResponse addUser(String loggingId, AddUserRequest request) throws MssException {
      return new WebServiceJsonCaller<AddUserRequest, AddUserResponse>()
            .call(
                  loggingId,
                  this.servers,
                  PATH_PREFIX + CallPaths.ADD_USER.getPath(),
                  CallPaths.ADD_USER.getMethod(),
                  request,
                  new AddUserResponse(),
                  MAX_RETRIES);
   }


   public ChangeUserResponse changeUser(String loggingId, ChangeUserRequest request) throws MssException {
      return new WebServiceJsonCaller<ChangeUserRequest, ChangeUserResponse>()
            .call(
                  loggingId,
                  this.servers,
                  PATH_PREFIX + CallPaths.CHANGE_USER.getPath(),
                  CallPaths.CHANGE_USER.getMethod(),
                  request,
                  new ChangeUserResponse(),
                  MAX_RETRIES);
   }


   public CheckCounterResponse checkCounter(String loggingId, CheckCounterRequest request) throws MssException {
      return new WebServiceJsonCaller<CheckCounterRequest, CheckCounterResponse>()
            .call(
                  loggingId,
                  this.servers,
                  PATH_PREFIX + CallPaths.CHECK_COUNTER.getPath(),
                  CallPaths.CHECK_COUNTER.getMethod(),
                  request,
                  new CheckCounterResponse(),
                  MAX_RETRIES);
   }


   public DeleteUserResponse deleteUser(String loggingId, DeleteUserRequest request) throws MssException {
      return new WebServiceJsonCaller<DeleteUserRequest, DeleteUserResponse>()
            .call(
                  loggingId,
                  this.servers,
                  PATH_PREFIX + CallPaths.DELETE_USER.getPath(),
                  CallPaths.DELETE_USER.getMethod(),
                  request,
                  new DeleteUserResponse(),
                  MAX_RETRIES);
   }


   public GetAllCounterResponse getAllCounters(String loggingId, GetAllCounterRequest request) throws MssException {
      return new WebServiceJsonCaller<GetAllCounterRequest, GetAllCounterResponse>()
            .call(
                  loggingId,
                  this.servers,
                  PATH_PREFIX + CallPaths.GET_ALL_COUNTER.getPath(),
                  CallPaths.GET_ALL_COUNTER.getMethod(),
                  request,
                  new GetAllCounterResponse(),
                  MAX_RETRIES);
   }


   public GetCounterResponse getCounter(String loggingId, GetCounterRequest request) throws MssException {
      return new WebServiceJsonCaller<GetCounterRequest, GetCounterResponse>()
            .call(
                  loggingId,
                  this.servers,
                  PATH_PREFIX + CallPaths.GET_COUNTER.getPath(),
                  CallPaths.GET_COUNTER.getMethod(),
                  request,
                  new GetCounterResponse(),
                  MAX_RETRIES);
   }


   public ListUsersResponse listUsers(String loggingId, ListUsersRequest request) throws MssException {
      return new WebServiceJsonCaller<ListUsersRequest, ListUsersResponse>()
            .call(
                  loggingId,
                  this.servers,
                  PATH_PREFIX + CallPaths.GET_USERS.getPath(),
                  CallPaths.GET_USERS.getMethod(),
                  request,
                  new ListUsersResponse(),
                  MAX_RETRIES);
   }

}
