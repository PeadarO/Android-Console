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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Base class for HTTP connector implementations
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

  private boolean connected;
  protected URL controllerUrl;
  protected Credentials credentials;
  private String uuid = UUID.randomUUID().toString();

  
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
  public boolean isConnected() {
    return connected;
  }

  // TODO: Make this a scheduled task to keep checking controller status
  @Override
  public void connect(AsyncControllerCallback<ControllerConnectionStatus> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(RestCommand.CONNECT), null, null,
              new ControllerCallback(RestCommand.CONNECT, callback), timeout);
    }
  }

  @Override
  public void disconnect() {
    // TODO Implement connection heartbeat
    connected = false;
  }

  @Override
  public void getPanelList(AsyncControllerCallback<List<PanelInfo>> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(RestCommand.GET_PANEL_LIST), null, null, new ControllerCallback(
              RestCommand.GET_PANEL_LIST, callback), timeout);
    }
  }

  @Override
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { panelName }, RestCommand.GET_PANEL_LAYOUT), null, null,
              new ControllerCallback(RestCommand.GET_PANEL_LAYOUT, callback), timeout);
    }
  }
  
  @Override
  public void getDeviceList(AsyncControllerCallback<List<DeviceInfo>> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(RestCommand.GET_DEVICE_LIST), null, null, new ControllerCallback(
              RestCommand.GET_DEVICE_LIST, callback), timeout);
    } 
  }
  
  @Override
  public void getDevice(String deviceName, AsyncControllerCallback<Device> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { deviceName }, RestCommand.GET_DEVICE), null, null,
              new ControllerCallback(RestCommand.GET_DEVICE, callback), timeout);
    }
  }

  @Override
  public void monitorSensors(int[] sensorIds,
          AsyncControllerCallback<Map<Integer, String>> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(
              buildRequestUrl(
                      new String[] {
                          uuid,
                          Arrays.toString(sensorIds).replace(", ", ",").replace("]", "")
                                  .replace("[", "") }, RestCommand.DO_SENSOR_POLLING), null, null,
              new ControllerCallback(RestCommand.DO_SENSOR_POLLING, callback), timeout);
    }
  }

  @Override
  public void getSensorValues(int[] sensorIds,
          AsyncControllerCallback<Map<Integer, String>> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(
              buildRequestUrl(
                      new String[] { Arrays.toString(sensorIds).replace(", ", ",").replace("]", "")
                              .replace("[", "") }, RestCommand.GET_SENSOR_STATUS), null, null,
              new ControllerCallback(RestCommand.GET_SENSOR_STATUS, callback), timeout);
    }
  }

  @Override
  public void sendControlCommand(ControlCommand command,
          AsyncControllerCallback<ControlCommandResponse> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { Integer.toString(command.getSenderId()), command.getData() }, RestCommand.SEND_CONTROL_COMMAND),
             null, null, new ControllerCallback(RestCommand.SEND_CONTROL_COMMAND, callback, command), timeout);
    }
  }
  
  @Override
  public void sendCommand(Command command, String parameter,
          AsyncControllerCallback<CommandResponse> callback, int timeout) {

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
  public void getResourceInfoDetails(ResourceLocator resourceLocator, String resourceName,
          AsyncControllerCallback<ResourceInfoDetails> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      Object[] data = new Object[] { resourceLocator, resourceName };
      doRequest(buildRequestUrl(new String[] { resourceName }, RestCommand.GET_RESOURCE_DETAILS),
              null, null, new ControllerCallback(RestCommand.GET_RESOURCE_DETAILS, callback, data), timeout);
    }
  }

  // @Override
  // public void getResources(String[] resourceName, boolean getData,
  // AsyncControllerCallback<ResourceInfo[]> resourceCallback, int timeout) {
  // // TODO Auto-generated method stub
  //
  // }

  @Override
  public void getResourceData(String resourceName,
          AsyncControllerCallback<ResourceDataResponse> callback, int timeout) {
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

    switch (command) {
    case CONNECT:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<ControllerConnectionStatus> c = (AsyncControllerCallback<ControllerConnectionStatus>) callback;
      connected = true;
      c.onSuccess(new ControllerConnectionStatus(ControllerResponseCode.OK));
      break;
    }
    case GET_PANEL_LIST:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<List<PanelInfo>> cPL = (AsyncControllerCallback<List<PanelInfo>>) callback;
      PanelInfoList panelInfoList = null;
      try {
        panelInfoList = JacksonProcessor.unMarshall(new String(responseData, "UTF-8"),
                PanelInfoList.class);
      } catch (Exception e) {
        processError(callback, responseData);
        return;
      }
      cPL.onSuccess(panelInfoList.getPanelInfos());
      break;
    }
    case GET_PANEL_LAYOUT:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<Panel> cPanel = (AsyncControllerCallback<Panel>) callback;
      Panel panel = null;
      try {
        panel = JacksonProcessor.unMarshall(new String(responseData, "UTF-8"), Panel.class);
      } catch (Exception e) {
        processError(callback, responseData);
        return;
      }
      
      cPanel.onSuccess(panel);
      break;
    }
    case GET_DEVICE_LIST:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<List<DeviceInfo>> cDL = (AsyncControllerCallback<List<DeviceInfo>>) callback;
      DeviceInfo[] deviceList = null;
      try {
        deviceList = JacksonProcessor.unMarshall(new String(responseData, "UTF-8"),
                DeviceInfo[].class);
      } catch (Exception e) {
        processError(callback, responseData);
        return;
      }
      cDL.onSuccess(deviceList != null ? Arrays.asList(deviceList) : null);
      break;
    }
    case GET_DEVICE:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<Device> cDevice = (AsyncControllerCallback<Device>) callback;
      Device device = null;
      try {
        device = JacksonProcessor.unMarshall(new String(responseData, "UTF-8"), Device.class);
      } catch (Exception e) {
        processError(callback, responseData);
        return;
      }
      
      cDevice.onSuccess(device);
      break;
    }
    case SEND_CONTROL_COMMAND:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
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
    case DO_SENSOR_POLLING:
    {
      if (responseCode != 200 && responseCode != 504) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<Map<Integer, String>> pollingCallback = (AsyncControllerCallback<Map<Integer, String>>) callback;
      if (responseCode == 504) {
        pollingCallback.onSuccess(null);
      } else {
        SensorStatusList sensorStatusList = null;
        try {
          sensorStatusList = JacksonProcessor.unMarshall(new String(responseData,
                  "UTF-8"), SensorStatusList.class);
        } catch (Exception e) {
          processError(callback, responseData);
          return;
        }
        pollingCallback.onSuccess(getSensorValueMap(sensorStatusList.getStatuses()));
      }
      break;
    }
    case GET_SENSOR_STATUS:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<Map<Integer, String>> statusCallback = (AsyncControllerCallback<Map<Integer, String>>) callback;
      SensorStatusList sensorStatusList = null;
      try {
        sensorStatusList = JacksonProcessor.unMarshall(new String(responseData,
                "UTF-8"), SensorStatusList.class);
      } catch (Exception e) {
        processError(callback, responseData);
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
        processError(callback, responseData);
      }
      break;
    }
    case GET_RESOURCE_DETAILS:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<ResourceInfoDetails> resourceCallback = (AsyncControllerCallback<ResourceInfoDetails>) callback;

      if (data == null || !(data instanceof Object[])) {
        callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
      }

      Object[] objs = (Object[]) data;
      String contentType = null;
      Date modifiedTime = null;
      byte[] contentData = null;

      // Look at headers
      for (Header header : headers) {
        if (header.getName().equalsIgnoreCase("content-type")) {
          contentType = header.getValue();
        } else if (header.getName().equalsIgnoreCase("last-modified")) {
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
    case GET_RESOURCE_DATA:
    {
      if (responseCode != 200) {
        processError(callback, responseData);
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
    case DISCOVERY:
    {
      AsyncControllerDiscoveryCallback discoveryCallback = (AsyncControllerDiscoveryCallback)callback;
      ControllerInfo controllerInfo = null;
      try {
        String responseStr = new String(responseData, "UTF-8");
       
        if (responseStr.indexOf("{") == 0) {
          controllerInfo = JacksonProcessor.unMarshall(responseStr, ControllerInfo.class);
        } else {
          // Assume v1 protocol where just the url is returned
          controllerInfo = new ControllerInfo(responseStr);
        }
      } catch (Exception e) {
        processError(callback, responseData);
        return;
      }
      discoveryCallback.onControllerFound(controllerInfo);
      break;
    }
    default:
      processError(callback, responseData);
    }
  }

  private void processError(AsyncControllerCallback<?> callback, byte[] responseData) {
    // Let's see if it's an error response instead
    try {
      ControllerError error = JacksonProcessor.unMarshall(new String(responseData, "UTF-8"),
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