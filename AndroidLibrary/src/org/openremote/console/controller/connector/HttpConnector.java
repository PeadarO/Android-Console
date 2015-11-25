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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.http.Header;
import org.openremote.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.console.controller.ControllerConnectionStatus;
import org.openremote.console.controller.auth.Credentials;
import org.openremote.entities.panel.Panel;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfoDetails;
import org.openremote.entities.panel.ResourceLocator;
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

/**
 * Base class for HTTP connector implementations. This uses the HTTP
 * REST API which is obviously connectionless so we mimic a connection
 * using a heartbeat timer to poll the panel list REST API 
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
abstract class HttpConnector implements ControllerConnector {
  protected enum RestCommand {
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
    GET_RESOURCE_DETAILS(""),
    GET_RESOURCE_DATA(""),
    DISCOVERY(""),
    STOP_DISCOVERY(""),
    GET_DEVICE_LIST("rest/devices/"),
    GET_DEVICE("rest/devices/");
    

    private String url;

    private RestCommand(String url) {
      this.url = url;
    }
  }

  private static final int HEARTBEAT_TIMEOUT = 2000;
  private static final int HEARTBEAT_PERIOD = 10000;
  private boolean connectInProgress;
  private boolean connected;
  private boolean autoReconnect = true;
  protected URL controllerUrl;
  protected Credentials credentials;
  private Timer heartBeatTimer;
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
  public void connect(AsyncControllerCallback<ControllerConnectionStatus> callback) {
    // Check URL is valid
    if (controllerUrl != null && !isConnected() && !connectInProgress) {
      connectInProgress = true;
      doRequest(buildRequestUrl(RestCommand.CONNECT), null, null,
              new ControllerCallback(RestCommand.CONNECT, callback), timeout);
    }
  }

  @Override
  public void disconnect() {
    if (!isConnected()) {
      return;
    }
    
    connected = false;
    heartBeatTimer.cancel();
  }

  @Override
  public void getPanelList(AsyncControllerCallback<List<PanelInfo>> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(RestCommand.GET_PANEL_LIST), null, null, new ControllerCallback(
              RestCommand.GET_PANEL_LIST, callback), timeout);
    }
  }

  @Override
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { panelName }, RestCommand.GET_PANEL_LAYOUT), null, null,
              new ControllerCallback(RestCommand.GET_PANEL_LAYOUT, callback), timeout);
    }
  }
  
  @Override
  public void getDeviceList(AsyncControllerCallback<List<DeviceInfo>> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(RestCommand.GET_DEVICE_LIST), null, null, new ControllerCallback(
              RestCommand.GET_DEVICE_LIST, callback), timeout);
    } 
  }
  
  @Override
  public void getDevice(String deviceName, AsyncControllerCallback<Device> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { deviceName }, RestCommand.GET_DEVICE), null, null,
              new ControllerCallback(RestCommand.GET_DEVICE, callback), timeout);
    }
  }

  @Override
  public void monitorSensors(List<Integer> sensorIds,
          AsyncControllerCallback<Map<Integer, String>> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(
              buildRequestUrl(
                      new String[] {
                          new Integer(sensorIds.hashCode()).toString(),
                          Arrays.toString(sensorIds.toArray()).replace(", ", ",").replace("]", "")
                                  .replace("[", "") }, RestCommand.DO_SENSOR_POLLING), null, null,
              new ControllerCallback(RestCommand.DO_SENSOR_POLLING, callback), 55000);
    }
  }

  @Override
  public void getSensorValues(List<Integer> sensorIds,
          AsyncControllerCallback<Map<Integer, String>> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(
              buildRequestUrl(
                      new String[] { Arrays.toString(sensorIds.toArray()).replace(", ", ",").replace("]", "")
                              .replace("[", "") }, RestCommand.GET_SENSOR_STATUS), null, null,
              new ControllerCallback(RestCommand.GET_SENSOR_STATUS, callback), timeout);
    }
  }

  @Override
  public void sendControlCommand(ControlCommand command,
          AsyncControllerCallback<ControlCommandResponse> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { Integer.toString(command.getSenderId()), command.getData() }, RestCommand.SEND_CONTROL_COMMAND),
             null, null, new ControllerCallback(RestCommand.SEND_CONTROL_COMMAND, callback, command), timeout);
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
            
      // Create JSON content
      String content = parameter != null && !parameter.isEmpty() ? "{\"parameter\": \"" + parameter + "\"}" : null;
      
      doRequest(url, null, content,
              new ControllerCallback(RestCommand.SEND_NAMED_COMMAND, callback, command), timeout);
    }
  }

  @Override
  public void getResourceInfoDetails(String resourceName,
          AsyncControllerCallback<ResourceInfoDetails> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { resourceName }, RestCommand.GET_RESOURCE_DETAILS),
              null, null, new ControllerCallback(RestCommand.GET_RESOURCE_DETAILS, callback, null), timeout);
    }
  }

  @Override
  public void getResourceData(String resourceName,
          AsyncControllerCallback<ResourceDataResponse> callback) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { resourceName }, RestCommand.GET_RESOURCE_DATA), null, null,
              new ControllerCallback(RestCommand.GET_RESOURCE_DATA, callback, resourceName), timeout);
    }
  }  

  @Override
  public void startDiscovery(AsyncControllerDiscoveryCallback callback, int tcpPort, Integer searchDuration) {
    doRequest(null, null, null, new ControllerCallback(RestCommand.DISCOVERY, callback, tcpPort), searchDuration);
  }

  @Override
  public void stopDiscovery() {
    doRequest(null, null, null, new ControllerCallback(RestCommand.STOP_DISCOVERY, null), 0);
  }
  
  @Override
  public void setAutoReconnect(boolean autoReconnect) {
    this.autoReconnect = autoReconnect;
  }

  @Override
  public boolean isAutoReconnect() {
    return autoReconnect;
  }
  

  // ---------------------------------------------------------------------
  // CALLBACK
  // ---------------------------------------------------------------------

  class ControllerCallback {
    RestCommand command;
    AsyncControllerCallback<?> callback;
    Object data;

    protected ControllerCallback(RestCommand command, AsyncControllerCallback<?> callback) {
      this(command, callback, null);
    }

    protected ControllerCallback(RestCommand command, AsyncControllerCallback<?> callback, Object data) {
      this.command = command;
      this.callback = callback;
      this.data = data;
    }
  };

  // ---------------------------------------------------------------------
  // HELPERS
  // ---------------------------------------------------------------------

  protected abstract void doRequest(String url, Map<String, String> headers, String content, final ControllerCallback callback, Integer timeout);
  
  // TODO: Provide better handling of failures
  @SuppressWarnings("unchecked")
  protected void handleResponse(ControllerCallback controllerCallback, int responseCode,
          Header[] headers, byte[] responseData) {
    RestCommand command = controllerCallback.command;
    AsyncControllerCallback<?> callback = controllerCallback.callback;
    Object data = controllerCallback.data;
    String responseStr = null;
    
    if (command != RestCommand.GET_RESOURCE_DATA) {
      try {
        responseStr = responseData != null ? new String(responseData, "UTF-8") : null;
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    
    switch (command) {
    case CONNECT:
    {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      final AsyncControllerCallback<ControllerConnectionStatus> c = (AsyncControllerCallback<ControllerConnectionStatus>) callback;
      connected = true;
      
      // Start heartbeat task
      final TimerTask task = new TimerTask() {
        
        @Override
        public void run() {
          getPanelList(new AsyncControllerCallback<List<PanelInfo>>() {
            @Override
            public void onSuccess(List<PanelInfo> result) {
              if (!isConnected()) {
                connected = true;
                c.onSuccess(new ControllerConnectionStatus(ControllerResponseCode.OK));
              }
            }
            
            @Override
            public void onFailure(ControllerResponseCode error) {
              // Cancel heartbeat if auto-reconnect is disabled
              if (!isAutoReconnect()) {
                heartBeatTimer.cancel();
              }
              
              connected = false;
              
              // Call connect callback onFailure with NO_RESPONSE
              c.onFailure(ControllerResponseCode.NO_RESPONSE);
            }           
          });
          
        }
      };
      
      heartBeatTimer = new Timer();
      heartBeatTimer.schedule(task, HEARTBEAT_PERIOD, HEARTBEAT_PERIOD);
      c.onSuccess(new ControllerConnectionStatus(ControllerResponseCode.OK));
      break;
    }
    case GET_PANEL_LIST:
    {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<List<PanelInfo>> cPL = (AsyncControllerCallback<List<PanelInfo>>) callback;
      PanelInfoList panelInfoList = null;
      try {
        panelInfoList = JacksonProcessor.unMarshall(responseStr,
                PanelInfoList.class);
      } catch (Exception e) {
        processError(callback, responseStr);
        return;
      }
      cPL.onSuccess(panelInfoList.getPanelInfos());
      break;
    }
    case GET_PANEL_LAYOUT:
    {
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
    case GET_DEVICE_LIST:
    {
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
    case GET_DEVICE:
    {
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
    case SEND_CONTROL_COMMAND:
    {
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
    case GET_RESOURCE_DATA:
    {
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
      resourceDataCallback.onSuccess(new ResourceDataResponse((String) data, responseData, ControllerResponseCode.OK));
      break;
    }
    case DO_SENSOR_POLLING:
    {
      if (responseCode != 200 && responseCode != 504) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<Map<Integer, String>> pollingCallback = (AsyncControllerCallback<Map<Integer, String>>) callback;
      if (responseCode == 504) {
        pollingCallback.onSuccess(null);
      } else {
        try {
          SensorStatusList sensorStatusList = JacksonProcessor.unMarshall(responseStr, SensorStatusList.class);
          pollingCallback.onSuccess(getSensorValueMap(sensorStatusList.getStatuses()));
        } catch (Exception e) {
          try {
            // Controller can return 200 response with JSON Controller error that contains 504 timeout
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
    case GET_SENSOR_STATUS:
    {
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
    case LOGOUT:
    {
      AsyncControllerCallback<Boolean> logoutCallback = (AsyncControllerCallback<Boolean>) callback;
      if (responseCode == 200 || responseCode == 401) {
        logoutCallback.onSuccess(true);
      } else {
        processError(callback, responseStr);
      }
      break;
    }
    case GET_RESOURCE_DETAILS:
    {
      if (responseCode != 200) {
        processError(callback, responseStr);
        return;
      }

      AsyncControllerCallback<ResourceInfoDetails> resourceCallback = (AsyncControllerCallback<ResourceInfoDetails>) callback;

      String contentType = null;
      Date modifiedTime = null;

      // Look at headers
      for (Header header : headers) {
        if (header.getName().equalsIgnoreCase("content-type")) {
          contentType = header.getValue();
        } else if (header.getName().equalsIgnoreCase("last-modified")) {
          try {
            modifiedTime = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").parse(header.getValue());
          } catch (Exception e) {
            // TODO: Log exception
          }
        }
      }

      // Set data
      resourceCallback.onSuccess(new ResourceInfoDetails(modifiedTime, contentType));
      break;
    }
    case DISCOVERY:
    {
      AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback)callback;
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
      ControllerError error = JacksonProcessor.unMarshall(responseData,
              ControllerError.class);
      if (error != null) {
        callback.onFailure(error.getResponse());
      }
    } catch (Exception ex) {
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

  protected String buildRequestUrl(RestCommand command) {
    return buildRequestUrl(new String[0], command);
  }

  protected String buildRequestUrl(String[] params, RestCommand command) {
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
    return url;
  }
}