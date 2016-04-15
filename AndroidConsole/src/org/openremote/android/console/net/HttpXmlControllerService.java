/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2011, OpenRemote Inc.
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

package org.openremote.android.console.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.openremote.android.console.Constants;
import org.openremote.android.console.ControllerDataHelper;
import org.openremote.android.console.ControllerObject;
import org.openremote.android.console.LoginDialog;
import org.openremote.android.console.LoginDialog.OnloginClickListener;
import org.openremote.android.console.exceptions.AppInitializationException;
import org.openremote.android.console.exceptions.ControllerAuthenticationFailureException;
import org.openremote.android.console.exceptions.InvalidDataFromControllerException;
import org.openremote.android.console.exceptions.ORConnectionException;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.ViewHelper;
import org.openremote.android.console.util.SecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * Implementation for the ControllerService interface that uses HTTP (or HTTPS) to
 * communicate with an OpenRemote 2.0 controller, using the XML API.
 *
 * TODO throw AuthenticatedRequiredExceptions where appropriate
 *
 * @author Andrew D. Ball <aball@osintegrators.com>
 */
@Singleton
public class HttpXmlControllerService implements ControllerService
{
  public static final String LOG_CATEGORY = Constants.LOG_CATEGORY + "HttpXmlControllerService";

  private Context ctx;

  @Inject
  public HttpXmlControllerService(Context ctx)
  {
    this.ctx = ctx;
  }

  /**
   * Returns the URL of the current controller, from the AppSettingsModel.
   */
  protected URL getControllerUrl()
  {
    return AppSettingsModel.getSecuredServer(ctx);
  }

  /**
   * Returns a default HttpClient.  Additional methods could return more specialized HttpClient
   * instances, such as one that has longer timeout values.
   */
  protected HttpClient getHttpClient(Integer timeout)
  {
    timeout = timeout == null ? Constants.HTTP_CONNECTION_TIMEOUT : timeout;
    HttpParams params = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(params, timeout);
    HttpConnectionParams.setSoTimeout(params, timeout);
    return new DefaultHttpClient(params);
  }

  /**
   * Constructs an HttpGet object given a URL which specifies that self-signed SSL certificates
   * should be accepted.
   *
   * TODO add HTTP authentication header if needed
   *
   * @param httpClient the HttpClient instance that will be used for the request
   * @param url the URL that will be used for the request
   */
  protected HttpGet getHttpGetRequest(HttpClient httpClient, URL url)
  {
    HttpGet request = new HttpGet(url.toString());
    SecurityUtil.addCredentialToHttpRequest(ctx, request);

    // accept self-signed SSL certificates
    if ("https".equals(url.getProtocol()))
    {
      Scheme sch = new Scheme(url.getProtocol(), new SelfCertificateSSLSocketFactory(),
          url.getPort());
      httpClient.getConnectionManager().getSchemeRegistry().register(sch);
    }

    return request;
  }

  protected HttpPost getHttpPostRequest(HttpClient httpClient, URL url)
  {
    HttpPost request = new HttpPost(url.toString());
    SecurityUtil.addCredentialToHttpRequest(ctx, request);

    // accept self-signed SSL certificates
    if ("https".equals(url.getProtocol()))
    {
      Scheme sch = new Scheme(url.getProtocol(), new SelfCertificateSSLSocketFactory(),
          url.getPort());
      httpClient.getConnectionManager().getSchemeRegistry().register(sch);
    }

    return request;
  }

  protected void dealWithConnectionFailure(URL url, Throwable t) throws ORConnectionException
  {
    String message = "could not make connection to controller for URL " + url;
    Log.e(LOG_CATEGORY, message, t);

    // TODO attempt client-side controller failover:
    //failover shalt occur here
    // If an untried controller was found in the same failover group as the
    // current one, set the current controller URL to that one and do not throw
    // an ORConnectionException.
    //
    // Otherwise, throw an ORConnectionException, indicating that we are giving
    // up on the current controller failover group.
    
    ControllerDataHelper dh = new ControllerDataHelper(ctx);
   // switch controller maybe
     ControllerObject availableGroupMemberURL = ORControllerServerSwitcher.getOneAvailableFromGroupMemberURLs(AppSettingsModel.getCurrentController(ctx),dh);//so i guess the purpose of this would be to get the checkedresult and get one out of them
	 
     //dh.closeConnection();
     
     //if none vailable show dialog for now and finish
     Log.i(LOG_CATEGORY, "availableGroupMemberURL." + availableGroupMemberURL);
     if(availableGroupMemberURL==null){
     	 // ViewHelper.showAlertViewWithTitle(context, "Polling Error", ControllerException.exceptionMessageOfCode(statusCode));
     	 //ViewHelper.showAlertViewWithSetting(ctx, "Switch controller", "Choose a different controller");
     	//  this.notifyAll();

     }
  
     else{
     	ORControllerServerSwitcher.switchControllerWithURL(ctx, availableGroupMemberURL.getUrl());
					//AppSettingsModel.setCurrentServer(context, new URL(availableGroupMemberURL.getControllerName()));
     	
     }

  //  throw new ORConnectionException(message, t);maybe not throw like that
  }

  /**
   * Parse XML from an InputStream and return the resulting DOM tree as a Document.
   *
   * @param data InputStream containing the XML text
   *
   * @returns the DOM tree as a Document
   *
   * @throws IOException if the data could not be read
   * @throws SAXException if a parse error occurred
   *
   * @throws RuntimeException if an XML parser could not be constructed
   *
   * TODO This code probably belongs elsewhere.  It's here to prevent code duplication.
   */
  protected Document parseXml(InputStream data) throws IOException, SAXException,
      AppInitializationException
  {
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    try
    {
      factory = DocumentBuilderFactory.newInstance();
      builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
      Log.e(LOG_CATEGORY, "Can't construct new document builder", e);
      throw new AppInitializationException("XML parsing configuration unrecoverably bad!", e);
    }

    return builder.parse(data);
  }

  /**
   * Returns a list of all of the controller's cluster group member URLs.
   *
   * See {@link ControllerService.getServers}
   */
  public List<URL> getServers() throws ControllerAuthenticationFailureException,
      ORConnectionException, AppInitializationException, InvalidDataFromControllerException,
      Exception
  {
	  
	Log.e(LOG_CATEGORY, "getServers() init");
	  
    final String logPrefix = "getServers(): ";
    Log.e(logPrefix, "receiving failover group member URL ");
    
	  Log.e(LOG_CATEGORY, "getControllerUrl().toString(): "+getControllerUrl().toString());
	  
    URL url = new URL(getControllerUrl().toString() + "/rest/servers");
    HttpClient httpClient = getHttpClient(null);
    HttpGet request = getHttpGetRequest(httpClient, url);

  /*  HttpClient httpClient = new DefaultHttpClient(params);
    String url = AppSettingsModel.getSecuredServer(context);
    HttpGet httpGet = new HttpGet(url + "/rest/servers");*/

    HttpResponse response = null;
    try
    {
      response = httpClient.execute(request);
    }
    catch (HttpHostConnectException e)
    {
      dealWithConnectionFailure(url, e);
      Log.i(LOG_CATEGORY, logPrefix + "retrying request with controller at " + getControllerUrl());
     // getServers(); // this is okay to find failover controllers. When the main controller fails before fetching the failover urls
      //that may be a problem
    }

    int statusCode = response.getStatusLine().getStatusCode();

    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED)
    {
      throw new ControllerAuthenticationFailureException("received HTTP unauthorized (401) " +
          "response for " + url);
    }
    else if (statusCode != HttpURLConnection.HTTP_OK)
    {
      // TODO throw a better exception
      throw new Exception("getServers() request to controller failed with HTTP status code " +
          statusCode);
    }

    Document dom = parseXml(response.getEntity().getContent());

    ArrayList<URL> servers = new ArrayList<URL>();

    NodeList serverNodes = dom.getDocumentElement().getElementsByTagName("server");

    try
    {
      for (int i = 0; i < serverNodes.getLength(); i++)
      {
        servers.add(
            new URL(serverNodes.item(i).getAttributes().getNamedItem("url").getNodeValue()));
      }
    } catch (MalformedURLException e)
    {
      throw new InvalidDataFromControllerException("received URL from controller via " + url, e);
    }

    return servers;
  }

  /**
   * Returns an InputStream containing the contents of a panel.xml file from the controller.
   *
   * See {@link ControllerService.getPanel}
   */
  @Override
  public InputStream getPanel(String panelName) throws ControllerAuthenticationFailureException,
      ORConnectionException, AppInitializationException, Exception
  {
    final String logPrefix = "getPanel(): ";

    String encodedPanelName = URLEncoder.encode(panelName, Constants.UTF8_ENCODING);

    URL url = new URL(getControllerUrl().toString() + "/rest/panel/" + encodedPanelName);
    HttpClient httpClient = getHttpClient(Constants.PANEL_GET_TIMEOUT);
    HttpGet request = getHttpGetRequest(httpClient, url);

    HttpResponse response = null;
    try
    {
      response = httpClient.execute(request);
    }
    catch (HttpHostConnectException e)
    {
      dealWithConnectionFailure(url, e);
      Log.i(LOG_CATEGORY, logPrefix + "retrying request with controller at " + getControllerUrl());
      //getPanel(panelName);
      // TODO Handle these exceptions better
      throw e;
    }

    int statusCode = response.getStatusLine().getStatusCode();

    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED)
    {
      throw new ControllerAuthenticationFailureException("controller authentication required");
    }
    else if (statusCode != HttpURLConnection.HTTP_OK)
    {
      // TODO throw a better exception
      throw new Exception("getPanel() request to controller failed with HTTP status code " +
          statusCode);
    }

    return response.getEntity().getContent();
  }

  /**
   * Returns an InputStream containing the contents of a file (resource) from the controller.
   *
   * See {@link ControllerService.getResource}
   */
  @Override
  public InputStream getResource(String resourceName)
      throws ControllerAuthenticationFailureException, ORConnectionException,
             AppInitializationException, Exception
  {
    final String logPrefix = "getResource(): ";

    String encodedResourceName = URLEncoder.encode(resourceName, Constants.UTF8_ENCODING);

    URL url = new URL(getControllerUrl().toString() + "/resources/" + encodedResourceName);
    HttpClient httpClient = getHttpClient(Constants.RESOURCE_GET_TIMEOUT);
    HttpGet request = getHttpGetRequest(httpClient, url);

    HttpResponse response = null;
    try
    {
      response = httpClient.execute(request);
    }
    catch (HttpHostConnectException e)
    {
      dealWithConnectionFailure(url, e);
      Log.i(LOG_CATEGORY, logPrefix + "retrying request with controller at " + getControllerUrl());
      getResource(resourceName);
    }

    int statusCode = response.getStatusLine().getStatusCode();

    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED)
    {
      throw new ControllerAuthenticationFailureException("controller authentication required");
    }
    else if (statusCode != HttpURLConnection.HTTP_OK)
    {
        return null;
    }

    return response.getEntity().getContent();
  }

  @Override
  public void sendWriteCommand(int controlId, String command)
          throws ControllerAuthenticationFailureException, ORConnectionException, Exception
  {
    final String logPrefix = "sendWriteCommand(): ";

    String encodedCommand = URLEncoder.encode(command, Constants.UTF8_ENCODING);

    URL url = new URL(getControllerUrl().toString() + "/rest/control/" +
        Integer.toString(controlId) + "/" + encodedCommand);
    HttpClient httpClient = getHttpClient(null);
    HttpPost request = getHttpPostRequest(httpClient, url);

    HttpResponse response = null;
    Log.i("HttpXmlControllerService", "sendWriteCommand");
    try
    {
      response = httpClient.execute(request);
    }
    catch (HttpHostConnectException e)
    {
      dealWithConnectionFailure(url, e);
      Log.i(LOG_CATEGORY, logPrefix + "retrying request with controller at " + getControllerUrl());
      sendWriteCommand(controlId, command);//why is this happening on catch?
      return;
      //sendWriteCommand(controlId, command);//why is this happening on catch?
    }
    
    int statusCode = response.getStatusLine().getStatusCode();
    Log.i("HttpXmlControllerService statusCode", "statusCode");
    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED)
    {
   /* 	//should not throw unauthorized.instead show a login screen
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setOnClickListener(loginDialog.new OnloginClickListener() {
          @Override
          public void onClick(View v) {
            super.onClick(v);
            requestPanelList();
          }
        });*/
      throw new ControllerAuthenticationFailureException("controller authentication required");
    }
    else if (statusCode != HttpURLConnection.HTTP_OK)
    {
      
      
 
   /*   
   // TODO throw a better exception, there are several documented error conditions associated
      //      with other HTTP response status codes in the controller REST API documentation
      throw new Exception(logPrefix + "request to controller failed with HTTP status code " +
          statusCode);*/
     
    }
  }

  @Override
  public int getApiVersion() {
    try
    {
      URL url = new URL(getControllerUrl().toString() + "/rest/commands");
      HttpClient httpClient = getHttpClient(Constants.HTTP_CONNECTION_TIMEOUT);
      HttpPost request = getHttpPostRequest(httpClient, url);

      HttpResponse response = null;
      response = httpClient.execute(request);

      return response.getStatusLine().getStatusCode() == 400 ? 2 : 1;
    }
    catch (Exception e)
    {
        return 1;
    }
  }
}