/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2014, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.console.controller.connector;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.openremote.console.controller.auth.Credentials;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;

/**
 * Test controller connector that does things synchronously for ease of testing
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class SingleThreadHttpConnector extends HttpConnector {
  private final RequestConfig config = RequestConfig.custom().setSocketTimeout(getTimeout())
          .setConnectionRequestTimeout(getTimeout()).setConnectTimeout(getTimeout()).build();
  private final CredentialsProvider creds = new BasicCredentialsProvider();
  private final HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(creds)
          .setDefaultRequestConfig(config).build();

  @Override
  protected void doRequest(URI uri, Map<String, String> headers, String content,
          final ControllerCallback callback, Integer timeout) {
    boolean doHead = false;
    boolean doGet = false;

    if (callback.command == RestCommand.GET_RESOURCE_DETAILS) {
      doHead = true;
    }

    if (callback.command == RestCommand.GET_XML) {
      doGet = true;
    }

    HttpUriRequest http;

    if (doHead) {
      HttpHead httpHead = new HttpHead(uri);
      httpHead.setConfig(RequestConfig.custom().setSocketTimeout(timeout)
              .setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).build());
      http = httpHead;
    } else if(doGet) {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(RequestConfig.custom().setSocketTimeout(timeout)
            .setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).build());
        if (headers != null) {
            for (Entry<String, String> header : headers.entrySet()) {
                httpGet.addHeader(header.getKey(), header.getValue());
            }
        }
        http = httpGet;
    } else {
      HttpPost httpPost = new HttpPost(uri);
      httpPost.addHeader("Accept", "application/json");
      httpPost.setConfig(RequestConfig.custom().setSocketTimeout(timeout)
              .setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).build());
      if (headers != null) {
        for (Entry<String, String> header : headers.entrySet()) {
          httpPost.addHeader(header.getKey(), header.getValue());
        }
      }
      if (content != null) {
        try {
          httpPost.setEntity(new StringEntity(content));
        } catch (UnsupportedEncodingException e) {
          callback.callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
          return;
        }
      }
      http = httpPost;
    }

    HttpResponse response = null;
    byte[] responseData = null;

    try {
      response = client.execute(http);

      if (response.getEntity() != null) {
        InputStream is = response.getEntity().getContent();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = is.read(buffer, 0, buffer.length)) != -1) {
          baos.write(buffer, 0, read);
        }
        baos.flush();
        is.close();
        responseData = baos.toByteArray();
      }

      // java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
      // String responseStr = s.hasNext() ? s.next() : "";

      if (callback.command == RestCommand.LOGOUT) {
        creds.clear();
      }
    } catch (Exception e) {
      if (callback.command == RestCommand.DO_SENSOR_POLLING && e instanceof SocketTimeoutException) {
          if (callback.callback != null) {
              callback.callback.onSuccess(null);
          }
      } else {
          if (callback.callback != null) {
              callback.callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
          }
      }
      return;
    }

    Header[] responseHeaders = response.getAllHeaders();
    Map<String,String> responseHeaderMap = new HashMap<String, String>();
    for (Header header : responseHeaders) {
        responseHeaderMap.put(header.getName(), header.getValue());
    }
    handleResponse(callback, response.getStatusLine().getStatusCode(), responseHeaderMap, responseData);
  }

  @Override
  public void setCredentials(Credentials credentials) {
    creds.clear();
    if (credentials != null) {
      creds.setCredentials(new AuthScope("localhost", 8080), new UsernamePasswordCredentials(
              credentials.getUsername(), credentials.getPassword()));
    }
  }

  @Override
  public void logout(AsyncControllerCallback<Boolean> callback) {
    creds.clear();
  }

  @Override
  public boolean isDiscoveryRunning() {
    // TODO Auto-generated method stub
    return false;
  }
}