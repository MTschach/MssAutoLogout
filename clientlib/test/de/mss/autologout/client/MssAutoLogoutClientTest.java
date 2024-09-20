package de.mss.autologout.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.easymock.EasyMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.mss.autologout.client.param.AddUserRequest;
import de.mss.autologout.client.param.AddUserResponse;
import de.mss.autologout.client.param.ChangeUserRequest;
import de.mss.autologout.client.param.ChangeUserResponse;
import de.mss.autologout.client.param.CheckCounterRequest;
import de.mss.autologout.client.param.CheckCounterResponse;
import de.mss.autologout.client.param.DeleteUserRequest;
import de.mss.autologout.client.param.DeleteUserResponse;
import de.mss.autologout.client.param.GetCounterRequest;
import de.mss.autologout.client.param.GetCounterResponse;
import de.mss.autologout.client.param.ListUsersRequest;
import de.mss.autologout.client.param.ListUsersResponse;
import de.mss.configtools.ConfigFile;
import de.mss.net.rest.HttpClientFactory;
import de.mss.net.serializer.JsonSerializerFactory;
import de.mss.net.webservice.WebServiceResponse;
import de.mss.utils.exception.MssException;

public class MssAutoLogoutClientTest {

   private CloseableHttpClient   httpClientMock;
   private CloseableHttpResponse httpResponseMock;
   private HttpEntity            httpEntityMock;
   private StatusLine            statusLineMock;
   private ConfigFile            cfgMock;


   private void checkResponse(WebServiceResponse resp) {
      assertNotNull(resp);
      assertEquals(Integer.valueOf(0), resp.getErrorCode());
      assertNull(resp.getErrorText());
   }


   private String getLoggingId() {
      return new Throwable().getStackTrace()[1].getMethodName() + "_" + UUID.randomUUID().toString();
   }


   private void replay() {
      EasyMock.replay(this.httpClientMock);
      EasyMock.replay(this.httpResponseMock);
      EasyMock.replay(this.httpEntityMock);
      EasyMock.replay(this.statusLineMock);
      EasyMock.replay(this.cfgMock);
   }


   @BeforeEach
   public void setUp() {
      this.httpClientMock = EasyMock.createNiceMock(CloseableHttpClient.class);
      this.httpResponseMock = EasyMock.createNiceMock(CloseableHttpResponse.class);
      this.httpEntityMock = EasyMock.createNiceMock(HttpEntity.class);
      this.statusLineMock = EasyMock.createNiceMock(StatusLine.class);

      this.cfgMock = EasyMock.createNiceMock(ConfigFile.class);

      setUpCfgMock();

      HttpClientFactory.initializeHttpClientFactory(this.httpClientMock);
   }


   private void setUpCfgMock() {
      EasyMock.expect(this.cfgMock.getValue(EasyMock.eq("mss_autologout.numberOfServers"), EasyMock.eq(1))).andReturn(1).anyTimes();
      EasyMock.expect(this.cfgMock.getValue(EasyMock.eq("mss_autologout.protocoll1"), EasyMock.eq("http"))).andReturn("http").anyTimes();
      EasyMock.expect(this.cfgMock.getValue(EasyMock.eq("mss_autologout.url1"), EasyMock.eq("localhost"))).andReturn("localhost").anyTimes();
      EasyMock.expect(this.cfgMock.getValue(EasyMock.eq("mss_autologout.port1"), EasyMock.eq(8080))).andReturn(38080).anyTimes();
      EasyMock.expect(this.cfgMock.getValue(EasyMock.eq("mss_autologout.path1"), EasyMock.eq(""))).andReturn("").anyTimes();
   }


   @SuppressWarnings("resource")
   private void setUpHttpClient(WebServiceResponse resp) throws ClientProtocolException, IOException {

      final ByteArrayInputStream bais = new ByteArrayInputStream(JsonSerializerFactory.getInstance().writeValueAsBytes(resp));

      EasyMock.expect(this.httpClientMock.execute(EasyMock.anyObject())).andReturn(this.httpResponseMock);

      EasyMock.expect(this.httpResponseMock.getStatusLine()).andReturn(this.statusLineMock).anyTimes();
      EasyMock.expect(this.httpResponseMock.getEntity()).andReturn(this.httpEntityMock).anyTimes();

      EasyMock.expect(this.httpEntityMock.getContent()).andReturn(bais).anyTimes();

      EasyMock.expect(this.statusLineMock.getStatusCode()).andReturn(200).anyTimes();
   }


   @AfterEach
   public void tearDown() {
      verify();

      HttpClientFactory.initializeHttpClientFactory(null);
   }


   @Test
   public void testAddUser() throws MssException, ClientProtocolException, IOException {
      final String loggingId = getLoggingId();

      setUpHttpClient(new AddUserResponse());

      replay();

      final AddUserResponse resp = MssAutoLogoutClient.getInstance(this.cfgMock).addUser(loggingId, new AddUserRequest());

      checkResponse(resp);
   }


   @Test
   public void testChangeUser() throws MssException, ClientProtocolException, IOException {
      final String loggingId = getLoggingId();

      setUpHttpClient(new ChangeUserResponse());
      final ChangeUserRequest req = new ChangeUserRequest();
      req.setUserName("user");

      replay();

      final ChangeUserResponse resp = MssAutoLogoutClient.getInstance(this.cfgMock).changeUser(loggingId, req);

      checkResponse(resp);
   }


   @Test
   public void testCheckCounter() throws MssException, ClientProtocolException, IOException {
      final String loggingId = getLoggingId();

      setUpHttpClient(new CheckCounterResponse());
      final CheckCounterRequest req = new CheckCounterRequest();
      req.setUserName("user");

      replay();

      final CheckCounterResponse resp = MssAutoLogoutClient.getInstance(this.cfgMock).checkCounter(loggingId, req);

      checkResponse(resp);
   }


   @Test
   public void testDeleteUser() throws MssException, ClientProtocolException, IOException {
      final String loggingId = getLoggingId();

      setUpHttpClient(new DeleteUserResponse());
      final DeleteUserRequest req = new DeleteUserRequest();
      req.setUserName("user");

      replay();

      final DeleteUserResponse resp = MssAutoLogoutClient.getInstance(this.cfgMock).deleteUser(loggingId, req);

      checkResponse(resp);
   }


   @Test
   public void testGetCounter() throws MssException, ClientProtocolException, IOException {
      final String loggingId = getLoggingId();

      setUpHttpClient(new GetCounterResponse());
      final GetCounterRequest req = new GetCounterRequest();
      req.setUserName("user");

      replay();

      final GetCounterResponse resp = MssAutoLogoutClient.getInstance(this.cfgMock).getCounter(loggingId, req);

      checkResponse(resp);
   }


   @Test
   public void testListUsers() throws MssException, ClientProtocolException, IOException {
      final String loggingId = getLoggingId();

      setUpHttpClient(new ListUsersResponse());
      final ListUsersRequest req = new ListUsersRequest();

      replay();

      final ListUsersResponse resp = MssAutoLogoutClient.getInstance(this.cfgMock).listUsers(loggingId, req);

      checkResponse(resp);
   }


   private void verify() {
      EasyMock.verify(this.httpClientMock);
      EasyMock.verify(this.httpResponseMock);
      EasyMock.verify(this.httpEntityMock);
      EasyMock.verify(this.statusLineMock);
      EasyMock.verify(this.cfgMock);
   }
}
