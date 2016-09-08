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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openremote.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.console.controller.Controller;
import org.openremote.console.controller.ControllerConnectionStatus;
import org.openremote.console.controller.auth.Credentials;
import org.openremote.entities.panel.Panel;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfoDetails;
import org.openremote.entities.util.JacksonProcessor;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.Command;
import org.openremote.entities.controller.CommandResponse;
import org.openremote.entities.controller.ControlCommand;
import org.openremote.entities.controller.ControlCommandResponse;
import org.openremote.entities.controller.ControllerError;
import org.openremote.entities.controller.ControllerInfo;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.entities.controller.Device;
import org.openremote.entities.controller.DeviceInfo;
import org.openremote.entities.controller.PanelInfoList;
import org.openremote.entities.controller.SensorStatus;
import org.openremote.entities.controller.SensorStatusList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Base class for HTTP connector implementations. This uses the HTTP REST API
 * which is obviously connectionless so we mimic a connection using a heartbeat
 * timer to poll the panel list REST API
 *
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public abstract class HttpConnector implements ControllerConnector {

    private static final Logger LOG = Logger.getLogger(HttpConnector.class.getName());

    public enum RestCommand {
    GET_PANEL_LIST("rest/panels/"),
    GET_PANEL_LAYOUT("rest/panel/"),
    SEND_CONTROL_COMMAND("rest/control/"),
    SEND_NAMED_COMMAND("rest/devices/"),
    GET_SENSOR_STATUS("rest/status/"),
    DO_SENSOR_POLLING("rest/polling/"),
    GET_ROUND_ROBIN_LIST("rest/servers"),
    LOGIN(""),
    LOGOUT(""),
    CONNECT("rest/servers"),
    DISCONNECT(""),
    GET_RESOURCE_DETAILS(""),
    GET_RESOURCE_DATA(""),
    DISCOVERY(""),
    STOP_DISCOVERY(""),
    GET_DEVICE_LIST("rest/devices/"),
    GET_DEVICE("rest/devices/"),
    GET_XML("resources/controller.xml");

    private String url;

    private RestCommand(String url) {
      this.url = url;
    }
  }

  private boolean connectInProgress;
  protected boolean connected;
  protected URL controllerUrl;
  protected Credentials credentials;
  private int timeout;

  protected HttpConnector() {
  }

  @Override
  public void setControllerUrl(URL controllerUrl) {
    this.controllerUrl = controllerUrl;
  }

  @Override
  public URL getControllerUrl() {
    return controllerUrl;
  }

  @Override
  public String getControllerIdentity() {
    // TODO: Implement controller identity population
    return "";
  }

  @Override
  public Credentials getCredentials() {
    return this.credentials;
  }

  @Override
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  @Override
  public int getTimeout() {
    return timeout;
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  @Override
  public void connect(final AsyncControllerCallback<ControllerConnectionStatus> callback) {
    // Check URL is valid
    if (controllerUrl != null && !isConnected() && !connectInProgress) {
      connectInProgress = true;
      doRequest(buildRequestUri(RestCommand.CONNECT), null, null, new ControllerCallback(
              RestCommand.CONNECT, new AsyncControllerCallback<ControllerConnectionStatus>() {

                @Override
                public void onFailure(ControllerResponseCode error) {
                  connected = false;
                  callback.onFailure(error);
                }

                @Override
                public void onSuccess(ControllerConnectionStatus result) {
                  connected = true;
                  connectInProgress = false;
                  callback.onSuccess(result);
                }

              }), timeout);
    }
  }

  @Override
  public void disconnect() {
    if (!isConnected()) {
      return;
    }

    doRequest(null, null, null, new ControllerCallback(RestCommand.DISCONNECT, null), timeout);
  }

  @Override
  public void getPanelList(AsyncControllerCallback<List<PanelInfo>> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUri(RestCommand.GET_PANEL_LIST), null, null, new ControllerCallback(
              RestCommand.GET_PANEL_LIST, callback), timeout);
    }
  }

  @Override
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUri(new String[] { panelName }, RestCommand.GET_PANEL_LAYOUT), null,
              null, new ControllerCallback(RestCommand.GET_PANEL_LAYOUT, callback), timeout);
    }
  }

  @Override
  public void getDeviceList(AsyncControllerCallback<List<DeviceInfo>> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUri(RestCommand.GET_DEVICE_LIST), null, null, new ControllerCallback(
              RestCommand.GET_DEVICE_LIST, callback), timeout);
    }
  }

  @Override
  public void getDevice(String deviceName, AsyncControllerCallback<Device> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUri(new String[] { deviceName }, RestCommand.GET_DEVICE), null, null,
              new ControllerCallback(RestCommand.GET_DEVICE, callback), timeout);
    }
  }

  @Override
  public void getWidgetsCommandInfo(AsyncControllerCallback<List<Controller.WidgetCommandInfo>> callback) {
    if (controllerUrl != null) {
      doRequest(buildRequestUri(RestCommand.GET_XML), null, null,
              new ControllerCallback(RestCommand.GET_XML, callback), timeout);
    }
  };

  @Override
  public void monitorSensors(String uuid, List<Integer> sensorIds,
          AsyncControllerCallback<Map<Integer, String>> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(
              buildRequestUri(
                      new String[] {
                          uuid,
                          Arrays.toString(sensorIds.toArray()).replace(", ", ",").replace("]", "")
                                  .replace("[", "")
                      },
                      RestCommand.DO_SENSOR_POLLING
              ),
              null,
              null,
              new ControllerCallback(RestCommand.DO_SENSOR_POLLING, callback),
              55000
      );
    }
  }

  @Override
  public void getSensorValues(List<Integer> sensorIds,
          AsyncControllerCallback<Map<Integer, String>> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(
              buildRequestUri(new String[] { Arrays.toString(sensorIds.toArray())
                      .replace(", ", ",").replace("]", "").replace("[", "") },
                      RestCommand.GET_SENSOR_STATUS), null, null, new ControllerCallback(
                      RestCommand.GET_SENSOR_STATUS, callback), timeout);
    }
  }

  @Override
  public void sendControlCommand(ControlCommand command,
          AsyncControllerCallback<ControlCommandResponse> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(
              buildRequestUri(
                      new String[] { Integer.toString(command.getSenderId()), command.getData() },
                      RestCommand.SEND_CONTROL_COMMAND), null, null, new ControllerCallback(
                      RestCommand.SEND_CONTROL_COMMAND, callback, command), timeout);
    }
  }

  @Override
  public void sendCommand(Command command, String parameter,
          AsyncControllerCallback<CommandResponse> callback) {

    if (controllerUrl != null) {
      // Build URL
      String url = controllerUrl.toString();
      url = url.endsWith("/") ? url : url + "/";
      url += RestCommand.SEND_NAMED_COMMAND.url;
      url += command.getDevice().getName() + "/";
      url += "commands?name=";
      url += command.getName();

      URI uri = null;
      try {
        URL urlObject = new URL(url);
        uri = new URI(urlObject.getProtocol(), urlObject.getUserInfo(), urlObject.getHost(),
                urlObject.getPort(), urlObject.getPath(), urlObject.getQuery(), urlObject.getRef());
      } catch (Exception e) {
      }

      // Create JSON content
      String content = parameter != null && !parameter.isEmpty() ? "{\"parameter\": \"" + parameter
              + "\"}" : null;

      doRequest(uri, null, content, new ControllerCallback(RestCommand.SEND_NAMED_COMMAND,
              callback, command), timeout);
    }
  }

  @Override
  public void getResourceInfoDetails(String resourceName,
          AsyncControllerCallback<ResourceInfoDetails> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUri(new String[] { resourceName }, RestCommand.GET_RESOURCE_DETAILS),
              null, null, new ControllerCallback(RestCommand.GET_RESOURCE_DETAILS, callback, null),
              timeout);
    }
  }

  @Override
  public void getResourceData(String resourceName,
          AsyncControllerCallback<ResourceDataResponse> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUri(new String[] { resourceName }, RestCommand.GET_RESOURCE_DATA),
              null, null, new ControllerCallback(RestCommand.GET_RESOURCE_DATA, callback,
                      resourceName), timeout);
    }
  }

  @Override
  public void startDiscovery(AsyncControllerDiscoveryCallback callback, int tcpPort,
          Integer searchDuration) {
    doRequest(null, null, null, new ControllerCallback(RestCommand.DISCOVERY, callback, tcpPort),
            searchDuration);
  }

  @Override
  public void stopDiscovery() {
    doRequest(null, null, null, new ControllerCallback(RestCommand.STOP_DISCOVERY, null), 0);
  }

  // @Override
  // public void setAutoReconnect(boolean autoReconnect) {
  // this.autoReconnect = autoReconnect;
  // }
  //
  // @Override
  // public boolean isAutoReconnect() {
  // return autoReconnect;
  // }

  // ---------------------------------------------------------------------
  // CALLBACK
  // ---------------------------------------------------------------------

  static public class ControllerCallback {
    RestCommand command;
    AsyncControllerCallback<?> callback;
    Object data;

    public ControllerCallback(RestCommand command, AsyncControllerCallback<?> callback) {
      this(command, callback, null);
    }

      public ControllerCallback(RestCommand command, AsyncControllerCallback<?> callback,
            Object data) {
      this.command = command;
      this.callback = callback;
      this.data = data;
    }

    public RestCommand getCommand() {
      return command;
    }

    public AsyncControllerCallback<?> getWrappedCallback() {
      return callback;
    }

    public Object getData() {
      return data;
    }

    public void setData(Object data) {
      this.data = data;
    }
  };

  // ---------------------------------------------------------------------
  // HELPERS
  // ---------------------------------------------------------------------

  private int getElementRefId(Element includeElement) {
    return Integer.parseInt(includeElement.getAttribute("ref"));
  }

  private List<Element> getChildElements(Node node, String name) {
    List<Element> elements = new ArrayList<Element>();
    for (int i=0; i<node.getChildNodes().getLength(); i++) {
      Node childNode = node.getChildNodes().item(i);
      if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equalsIgnoreCase(name)) {
        elements.add((Element)childNode);
      }
    }

    return elements;
  }

  private Element getFirstElement(Node node) {
    NodeList nodes = node.getChildNodes();
    if (nodes != null) {
      for (int i=0; i< nodes.getLength(); i++) {
        Node childNode = nodes.item(i);
        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
          return (Element)childNode;
        }
      }
    }
    return null;
  }

  protected abstract void doRequest(URI uri, Map<String, String> headers, String content,
          final ControllerCallback callback, Integer timeout);

  // TODO: Provide better handling of failures
  @SuppressWarnings("unchecked")
  protected void handleResponse(ControllerCallback controllerCallback, int responseCode,
                                Map<String, String> headers, byte[] responseData) {
    RestCommand command = controllerCallback.command;
    AsyncControllerCallback<?> callback = controllerCallback.callback;
    Object data = controllerCallback.data;
    String responseStr = null;

    if (command != RestCommand.GET_RESOURCE_DATA && command != RestCommand.GET_XML) {
      try {
        responseStr = responseData != null ? new String(responseData, "UTF-8") : null;
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    switch (command) {
    case GET_XML:
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      List<Controller.WidgetCommandInfo> widgetCommands = new ArrayList<Controller.WidgetCommandInfo>();
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

      try {
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(responseData)));
        NodeList components = doc.getElementsByTagName("components").item(0).getChildNodes();
        for (int i=0; i<components.getLength(); i++) {
          Node node = components.item(i);
          if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
          }

          Element component = (Element)components.item(i);
          String componentType = component.getNodeName();
          NodeList childNodes = component.getChildNodes();
          Controller.WidgetCommandInfo widgetInfo = null;

          if ("switch".equalsIgnoreCase(componentType)) {
            widgetInfo = new Controller.WidgetCommandInfo(Boolean.class);
            widgetInfo.setWidgetType("switch");
            widgetInfo.setCommandId1(getElementRefId(getFirstElement(component.getElementsByTagName("on").item(0))));
            widgetInfo.setCommandId2(getElementRefId(getFirstElement(component.getElementsByTagName("off").item(0))));
            widgetInfo.setSensorId(getElementRefId(getChildElements((Node)component, "include").get(0)));
          } else if ("slider".equalsIgnoreCase(componentType)) {
            widgetInfo = new Controller.WidgetCommandInfo(Integer.class);
            widgetInfo.setWidgetType("slider");
            widgetInfo.setCommandId1(getElementRefId(getFirstElement(component.getElementsByTagName("setValue").item(0))));
            widgetInfo.setSensorId(getElementRefId(getChildElements((Node)component, "include").get(0)));
          } else if ("button".equalsIgnoreCase(componentType)) {
            widgetInfo = new Controller.WidgetCommandInfo(null);
            widgetInfo.setWidgetType("button");
            widgetInfo.setCommandId1(getElementRefId((Element)component.getElementsByTagName("include").item(0)));
          }

          if (widgetInfo != null) {
            widgetCommands.add(widgetInfo);
          }
        }
      } catch (Exception e) {
        processError(callback, "");
        return;
      }

      AsyncControllerCallback<List<Controller.WidgetCommandInfo>> getXml = (AsyncControllerCallback<List<Controller.WidgetCommandInfo>>) callback;
      getXml.onSuccess(widgetCommands);
      break;
    case CONNECT: {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      final AsyncControllerCallback<ControllerConnectionStatus> c = (AsyncControllerCallback<ControllerConnectionStatus>) callback;
      connected = true;
      c.onSuccess(new ControllerConnectionStatus(ControllerResponseCode.OK));
      break;
    }
    case GET_PANEL_LIST: {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<List<PanelInfo>> cPL = (AsyncControllerCallback<List<PanelInfo>>) callback;
      PanelInfoList panelInfoList = null;
      try {
        panelInfoList = JacksonProcessor.unMarshall(responseStr, PanelInfoList.class);
      } catch (Exception e) {
        processError(callback, responseStr);
        return;
      }
      cPL.onSuccess(panelInfoList.getPanelInfos());
      break;
    }
    case GET_PANEL_LAYOUT: {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<Panel> cPanel = (AsyncControllerCallback<Panel>) callback;
      Panel panel = null;
      try {
        panel = JacksonProcessor.unMarshall(responseStr, Panel.class);
      } catch (Exception e) {
        processError(callback, responseStr);
        return;
      }

      cPanel.onSuccess(panel);
      break;
    }
    case GET_DEVICE_LIST: {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<List<DeviceInfo>> cDL = (AsyncControllerCallback<List<DeviceInfo>>) callback;
      DeviceInfo[] deviceList = null;
      try {
        deviceList = JacksonProcessor.unMarshall(responseStr, DeviceInfo[].class);
      } catch (Exception e) {
        processError(callback, responseStr);
        return;
      }
      cDL.onSuccess(deviceList != null ? Arrays.asList(deviceList) : null);
      break;
    }
    case GET_DEVICE: {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<Device> cDevice = (AsyncControllerCallback<Device>) callback;
      Device device = null;
      try {
        device = JacksonProcessor.unMarshall(responseStr, Device.class);
      } catch (Exception e) {
        processError(callback, responseStr);
        return;
      }

      cDevice.onSuccess(device);
      break;
    }
    case SEND_CONTROL_COMMAND: {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      if (data == null || !(data instanceof ControlCommand)) {
        callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
      }
      ControlCommand sendCommand = (ControlCommand) data;
      AsyncControllerCallback<ControlCommandResponse> successCallback = (AsyncControllerCallback<ControlCommandResponse>) callback;
      successCallback.onSuccess(new ControlCommandResponse(sendCommand.getSenderId(),
              ControllerResponseCode.OK));
      break;
    }
    case SEND_NAMED_COMMAND: {
        if (responseCode != 204) {
            processError(callback, responseStr);
            return;
        }
        AsyncControllerCallback<CommandResponse> commandCallBack = (AsyncControllerCallback<CommandResponse>) callback;
        commandCallBack.onSuccess(new CommandResponse(ControllerResponseCode.NO_CONTENT));
        break;
    }
    case GET_RESOURCE_DATA: {
      if (responseCode != 200) {
        try {
          processError(callback, new String(responseData, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return;
      }

      if (data == null || !(data instanceof String)) {
        callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
      }

      AsyncControllerCallback<ResourceDataResponse> resourceDataCallback = (AsyncControllerCallback<ResourceDataResponse>) callback;
      resourceDataCallback.onSuccess(new ResourceDataResponse((String) data, responseData,
              ControllerResponseCode.OK));
      break;
    }
    case DO_SENSOR_POLLING: {
      if (responseCode != 200 && responseCode != 504) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<Map<Integer, String>> pollingCallback = (AsyncControllerCallback<Map<Integer, String>>) callback;
      if (responseCode == 504) {
        pollingCallback.onSuccess(null);
      } else {
        try {
          SensorStatusList sensorStatusList = JacksonProcessor.unMarshall(responseStr,
                  SensorStatusList.class);
          pollingCallback.onSuccess(getSensorValueMap(sensorStatusList.getStatuses()));
        } catch (Exception e) {
          try {
            // Controller can return 200 response with JSON Controller error
            // that contains 504 timeout
            ControllerError error = JacksonProcessor.unMarshall(responseStr, ControllerError.class);
            if (error.getResponse() == ControllerResponseCode.TIME_OUT) {
              pollingCallback.onSuccess(null);
            } else {
              processError(callback, responseStr);
            }
          } catch (Exception ex) {
            processError(callback, responseStr);
          }
        }
      }
      break;
    }
    case GET_SENSOR_STATUS: {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<Map<Integer, String>> statusCallback = (AsyncControllerCallback<Map<Integer, String>>) callback;
      SensorStatusList sensorStatusList = null;
      try {
        sensorStatusList = JacksonProcessor.unMarshall(responseStr, SensorStatusList.class);
      } catch (Exception e) {
        processError(callback, responseStr);
        return;
      }
      statusCallback.onSuccess(getSensorValueMap(sensorStatusList.getStatuses()));
      break;
    }
    case LOGOUT: {
      AsyncControllerCallback<Boolean> logoutCallback = (AsyncControllerCallback<Boolean>) callback;
      if (responseCode == 200 || responseCode == 401) {
        logoutCallback.onSuccess(true);
      } else {
        processError(callback, responseStr);
      }
      break;
    }
    case GET_RESOURCE_DETAILS: {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<ResourceInfoDetails> resourceCallback = (AsyncControllerCallback<ResourceInfoDetails>) callback;

      String contentType = null;
      Date modifiedTime = null;

      // Look at headers
      for (Map.Entry<String, String> header : headers.entrySet()) {
        if (header.getKey().equalsIgnoreCase("content-type")) {
          contentType = header.getValue();
        } else if (header.getKey().equalsIgnoreCase("last-modified")) {
          try {
            modifiedTime = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").parse(header
                    .getValue());
          } catch (Exception e) {
            // TODO: Log exception
          }
        }
      }

      // Set data
      resourceCallback.onSuccess(new ResourceInfoDetails(modifiedTime, contentType));
      break;
    }
    case DISCOVERY: {
      AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback) callback;
      ControllerInfo controllerInfo = null;
      try {
        if (responseStr.indexOf("{") == 0) {
          controllerInfo = JacksonProcessor.unMarshall(responseStr, ControllerInfo.class);
        } else {
          // Assume v1 protocol where just the url is returned
          controllerInfo = new ControllerInfo(responseStr);
        }
      } catch (Exception e) {
        processError(callback, responseStr);
        return;
      }
      discoveryCallback.onControllerFound(controllerInfo);
      break;
    }
    default:
      processError(callback, responseStr);
    }
  }

  private void processError(AsyncControllerCallback<?> callback, String responseData) {
    // Let's see if it's an error response instead
    try {
      ControllerError error = JacksonProcessor.unMarshall(responseData, ControllerError.class);
      if (error != null) {
        callback.onFailure(error.getResponse());
      }
    } catch (Exception ex) {
      LOG.log(Level.INFO, "Unknown response processing error", ex);
      callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
    }
  }

  private Map<Integer, String> getSensorValueMap(List<SensorStatus> statuses) {
    if (statuses == null) {
      return null;
    }

    Map<Integer, String> valueMap = new HashMap<Integer, String>();
    for (SensorStatus status : statuses) {
      valueMap.put(status.getSensorId(), status.getValue());
    }
    return valueMap;
  }

  protected URI buildRequestUri(RestCommand command) {
    return buildRequestUri(new String[0], command);
  }

  protected URI buildRequestUri(String[] params, RestCommand command) {
    String url = controllerUrl.toString();
    int paramCounter = 0;
    url = url.endsWith("/") ? url : url + "/";
    String methodUrl = command.url;

    if (methodUrl != null) {
      url += methodUrl;
    }

    for (String param : params) {
      url += param;
      paramCounter++;
      if (paramCounter < params.length) {
        url = url.endsWith("/") ? url : url + "/";
      }
    }

    URI uri = null;
    try {
      URL urlObject = new URL(url);
      uri = new URI(urlObject.getProtocol(), urlObject.getUserInfo(), urlObject.getHost(),
              urlObject.getPort(), urlObject.getPath(), urlObject.getQuery(), urlObject.getRef());
    } catch (Exception e) {
    }

    return uri;
  }
}