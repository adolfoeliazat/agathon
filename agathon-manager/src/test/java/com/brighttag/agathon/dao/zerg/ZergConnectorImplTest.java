package com.brighttag.agathon.dao.zerg;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.brighttag.agathon.dao.BackingStoreException;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @author codyaray
 * @since 9/09/2013
 */
public class ZergConnectorImplTest extends EasyMockSupport {

  private AsyncHttpClient client;
  private ZergConnector connector;

  @Before
  public void setupMocks() {
    client = createMock(AsyncHttpClient.class);
    connector = new ZergConnectorImpl(client, new Gson(), "/path");
  }

  @Test
  public void getHosts() throws Exception {
    expectZergResponseBody(MANIFEST);
    replayAll();

    assertEquals(HOSTS, connector.getHosts());
  }

  @Test
  public void getHosts_emptyManifest() throws Exception {
    expectZergResponseBody(EMPTY_MANIFEST);
    replayAll();

    assertEquals(ImmutableSet.of(), connector.getHosts());
  }
  @Test(expected = BackingStoreException.class)
  public void getHosts_badManifest() throws Exception {
    expectZergResponseBody(BAD_MANIFEST);
    replayAll();

    connector.getHosts();
  }

  @Test(expected = BackingStoreException.class)
  public void getHosts_timeout() throws Exception {
    expectBackingStoreException(new ExecutionException(new TimeoutException()));
    replayAll();

    connector.getHosts();
  }

  // Causes sporadic failures elsewhere
  @Ignore @Test(expected = BackingStoreException.class)
  public void getHosts_interrupted() throws Exception {
    expectBackingStoreException(new InterruptedException());
    replayAll();

    connector.getHosts();
  }

  @Test(expected = BackingStoreException.class)
  public void getHosts_ioException() throws Exception {
    expectZergResponseException(new IOException());
    replayAll();

    connector.getHosts();
  }

  private void expectZergResponseBody(String json) throws Exception {
    Response response = expectZergResponse();
    expect(response.getResponseBody()).andReturn(json);
  }

  private void expectZergResponseException(Exception exception) throws Exception {
    Response response = expectZergResponse();
    expect(response.getResponseBody()).andThrow(exception);
  }

  private Response expectZergResponse() throws Exception {
    ListenableFuture<Response> future = expectZergCall();
    Response response = createMock(Response.class);
    expect(future.get()).andReturn(response);
    return response;
  }

  private void expectBackingStoreException(Exception exception) throws Exception {
    ListenableFuture<Response> future = expectZergCall();
    expect(future.get()).andThrow(exception);
  }

  @SuppressWarnings("unchecked")
  private ListenableFuture<Response> expectZergCall() throws Exception {
    BoundRequestBuilder requestBuilder = createMock(BoundRequestBuilder.class);
    ListenableFuture<Response> future = createMock(ListenableFuture.class);
    expect(client.prepareGet("/path")).andReturn(requestBuilder);
    expect(requestBuilder.execute()).andReturn(future);
    return future;
  }

  private static final Set<ZergHost> HOSTS = ImmutableSet.of(
      new ZergHost("tagserve01ap1", ImmutableList.of("tagserve"), "us-northeast-1a", "54.0.1.1"),
      new ZergHost("cass01we2", ImmutableList.of("cassandra", "cassandra_myring"), "us-west-2a", "54.1.1.1"),
      new ZergHost("stats01ea1", ImmutableList.of("cassandra", "cassandra_stats"), "us-east-1c", "54.2.1.1"),
      new ZergHost("cass01ea1", ImmutableList.of("cassandra", "cassandra_myring"), "us-east-1a", "54.2.1.2"),
      new ZergHost("cass02ea1", ImmutableList.of("cassandra", "cassandra_myring"), "us-east-1b", "54.2.1.3"));

  private static final String EMPTY_MANIFEST = "{}";

  private static final String BAD_MANIFEST = "{]";

  private static final String MANIFEST = "" +
      "{" +
      // Empty region
      "  \"ap-southeast-1\": {}," +
      // Region without Cassandra instances
      "  \"ap-northeast-1\": {" +
      "    \"tagserve01ap1\": {" +
      "      \"private ip\": \"10.0.1.1\"," +
      "      \"roles\": [" +
      "        \"tagserve\"" +
      "      ]," +
      "      \"public ip\": \"54.0.1.1\"," +
      "      \"id\": \"abc1\"," +
      "      \"zone\": \"us-northeast-1a\"" +
      "    }" +
      "  }," +
      // Region with a single Cassandra instance
      "  \"us-west-2\": {" +
      "    \"cass01we2\": {" +
      "      \"private ip\": \"10.1.1.1\"," +
      "      \"roles\": [" +
      "        \"cassandra\"," +
      "        \"cassandra_myring\"" +
      "      ]," +
      "      \"public ip\": \"54.1.1.1\"," +
      "      \"id\": \"def2\"," +
      "      \"zone\": \"us-west-2a\"" +
      "    }" +
      "  }," +
      // Region with mixed server groups and multiple Cassandra rings
      "  \"us-east-1\": {" +
      "    \"stats01ea1\": {" +
      "      \"private ip\": \"10.2.1.1\"," +
      "      \"roles\": [" +
      "        \"cassandra\"," +
      "        \"cassandra_stats\"" +
      "      ]," +
      "      \"public ip\": \"54.2.1.1\"," +
      "      \"id\": \"ghi3\"," +
      "      \"zone\": \"us-east-1c\"" +
      "    }," +
      "    \"cass01ea1\": {" +
      "      \"private ip\": \"10.2.1.2\"," +
      "      \"roles\": [" +
      "        \"cassandra\"," +
      "        \"cassandra_myring\"" +
      "      ]," +
      "      \"public ip\": \"54.2.1.2\"," +
      "      \"id\": \"jkl4\"," +
      "      \"zone\": \"us-east-1a\"" +
      "    }," +
      "    \"cass02ea1\": {" +
      "      \"private ip\": \"10.2.1.3\"," +
      "      \"roles\": [" +
      "        \"cassandra\"," +
      "        \"cassandra_myring\"" +
      "      ]," +
      "      \"public ip\": \"54.2.1.3\"," +
      "      \"id\": \"mno5\"," +
      "      \"zone\": \"us-east-1b\"" +
      "    }" +
      "  }" +
      "}";
}