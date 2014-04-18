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
package org.openremote.java.console.controller.connector;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.openremote.entities.panel.PanelCommand;
import org.openremote.entities.panel.PanelCommandResponse;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceLocator;
import org.openremote.entities.panel.version1.Panel;
import org.openremote.entities.util.JacksonProcessor;
import org.openremote.entitites.controller.AsyncControllerCallback;
import org.openremote.entitites.controller.ControllerError;
import org.openremote.entitites.controller.ControllerResponseCode;
import org.openremote.entitites.controller.PanelInfoList;
import org.openremote.entitites.controller.SensorStatus;
import org.openremote.entitites.controller.SensorStatusList;
import org.openremote.java.console.controller.ControllerConnectionStatus;
import org.openremote.java.console.controller.auth.Credentials;

/**
 * Base class for HTTP connector implementations
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
abstract class HttpConnector implements ControllerConnector {
  protected enum Command {
    GET_PANEL_LIST("rest/panels/"),
    GET_PANEL_LAYOUT("rest/panel/"),
    SEND_COMMAND("rest/control/"),
    GET_SENSOR_STATUS("rest/status/"),
    DO_SENSOR_POLLING("rest/polling/"),
    GET_ROUND_ROBIN_LIST("rest/servers"),
    LOGIN(""),
    LOGOUT(""),
    CONNECT("rest/servers"),
    GET_RESOURCE(""),
    GET_RESOURCE_DATA("");

    private String url;

    private Command(String url) {
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
  public void setCredentials(Credentials credentials) {
    this.credentials = credentials;
    // Handle as a command
    doRequest(buildRequestUrl(Command.LOGIN), new ControllerCallback(Command.LOGIN, null), 3000);
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
      doRequest(buildRequestUrl(Command.CONNECT),
              new ControllerCallback(Command.CONNECT, callback), timeout);
    }
  }

  @Override
  public void disconnect() {
    // TODO Implement connection heartbeat
    connected = false;
  }

  @Override
  public void getPanelInfo(AsyncControllerCallback<List<PanelInfo>> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(Command.GET_PANEL_LIST), new ControllerCallback(
              Command.GET_PANEL_LIST, callback), timeout);
    }
  }

  @Override
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { panelName }, Command.GET_PANEL_LAYOUT),
              new ControllerCallback(Command.GET_PANEL_LAYOUT, callback), timeout);
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
                                  .replace("[", "") }, Command.DO_SENSOR_POLLING),
              new ControllerCallback(Command.DO_SENSOR_POLLING, callback), timeout);
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
                              .replace("[", "") }, Command.GET_SENSOR_STATUS),
              new ControllerCallback(Command.GET_SENSOR_STATUS, callback), timeout);
    }
  }

  @Override
  public void logout(AsyncControllerCallback<Boolean> callback, int timeout) {
    // Check URL is valid
    credentials = null;
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(Command.LOGOUT), new ControllerCallback(Command.LOGOUT, callback),
              timeout);
    }
  }

  @Override
  public void sendCommand(PanelCommand command,
          AsyncControllerCallback<PanelCommandResponse> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      doRequest(buildRequestUrl(new String[] { Integer.toString(command.getSenderId()), command.getData() }, Command.SEND_COMMAND),
              new ControllerCallback(Command.SEND_COMMAND, callback, command), timeout);
    }
  }

  @Override
  public void getResource(ResourceLocator resourceLocator, String resourceName, boolean getData,
          AsyncControllerCallback<ResourceInfo> callback, int timeout) {
    // Check URL is valid
    if (controllerUrl != null) {
      Object[] data = new Object[] { resourceLocator, resourceName, getData };
      doRequest(buildRequestUrl(new String[] { resourceName }, Command.GET_RESOURCE),
              new ControllerCallback(Command.GET_RESOURCE, callback, data), timeout);
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
      doRequest(buildRequestUrl(new String[] { resourceName }, Command.GET_RESOURCE_DATA),
              new ControllerCallback(Command.GET_RESOURCE_DATA, callback, resourceName), timeout);
    }
  }

  // ---------------------------------------------------------------------
  // CALLBACK
  // ---------------------------------------------------------------------

  class ControllerCallback {
    Command command;
    AsyncControllerCallback<?> callback;
    Object data;

    protected ControllerCallback(Command command, AsyncControllerCallback<?> callback) {
      this(command, callback, null);
    }

    protected ControllerCallback(Command command, AsyncControllerCallback<?> callback, Object data) {
      this.command = command;
      this.callback = callback;
      this.data = data;
    }
  };

  // ---------------------------------------------------------------------
  // HELPERS
  // ---------------------------------------------------------------------

  protected abstract void doRequest(String url, final ControllerCallback callback, Integer timeout);

  // TODO: Provide better handling of failures
  @SuppressWarnings("unchecked")
  protected void handleResponse(ControllerCallback controllerCallback, int responseCode,
          Header[] headers, byte[] responseData) {
    Command command = controllerCallback.command;
    AsyncControllerCallback<?> callback = controllerCallback.callback;
    Object data = controllerCallback.data;

    switch (command) {
    case CONNECT:
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<ControllerConnectionStatus> c = (AsyncControllerCallback<ControllerConnectionStatus>) callback;
      connected = true;
      c.onSuccess(new ControllerConnectionStatus(ControllerResponseCode.OK));
      break;
    case GET_PANEL_LIST:
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<List<PanelInfo>> cPL = (AsyncControllerCallback<List<PanelInfo>>) callback;
      PanelInfoList panelInfoList;
      try {
        panelInfoList = JacksonProcessor.unMarshall(new String(responseData, "UTF-8"),
                PanelInfoList.class);
        cPL.onSuccess(panelInfoList.getPanelInfos());
      } catch (Exception e) {
        processError(callback, responseData);
      }
      break;
    case GET_PANEL_LAYOUT:
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<Panel> cPanel = (AsyncControllerCallback<Panel>) callback;
      try {
        Panel panel = JacksonProcessor.unMarshall(new String(responseData, "UTF-8"), Panel.class);
        cPanel.onSuccess(panel);
      } catch (Exception e) {
        processError(callback, responseData);
      }
      break;
    case SEND_COMMAND:
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      if (data == null || !(data instanceof PanelCommand)) {
        callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
      }
      PanelCommand sendCommand = (PanelCommand) data;
      AsyncControllerCallback<PanelCommandResponse> successCallback = (AsyncControllerCallback<PanelCommandResponse>) callback;
      successCallback.onSuccess(new PanelCommandResponse(sendCommand.getSenderId(),
              ControllerResponseCode.OK));
      break;
    case DO_SENSOR_POLLING:
      if (responseCode != 200 && responseCode != 504) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<Map<Integer, String>> pollingCallback = (AsyncControllerCallback<Map<Integer, String>>) callback;
      if (responseCode == 504) {
        pollingCallback.onSuccess(null);
      } else {
        try {
          SensorStatusList sensorStatusList = JacksonProcessor.unMarshall(new String(responseData,
                  "UTF-8"), SensorStatusList.class);
          pollingCallback.onSuccess(getSensorValueMap(sensorStatusList.getStatuses()));
        } catch (Exception e) {
          processError(callback, responseData);
        }
      }
      break;
    case GET_SENSOR_STATUS:
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<Map<Integer, String>> statusCallback = (AsyncControllerCallback<Map<Integer, String>>) callback;
      try {
        SensorStatusList sensorStatusList = JacksonProcessor.unMarshall(new String(responseData,
                "UTF-8"), SensorStatusList.class);
        statusCallback.onSuccess(getSensorValueMap(sensorStatusList.getStatuses()));
      } catch (Exception e) {
        processError(callback, responseData);
      }
      break;
    case LOGOUT:
      AsyncControllerCallback<Boolean> logoutCallback = (AsyncControllerCallback<Boolean>) callback;
      if (responseCode == 200 || responseCode == 401) {
        logoutCallback.onSuccess(true);
      } else {
        processError(callback, responseData);
      }
      break;
    case GET_RESOURCE:
      if (responseCode != 200) {
        processError(callback, responseData);
        return;
      }

      AsyncControllerCallback<ResourceInfo> resourceCallback = (AsyncControllerCallback<ResourceInfo>) callback;

      if (data == null || !(data instanceof Object[])) {
        callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
      }

      Object[] objs = (Object[]) data;
      boolean loadData = (Boolean) objs[2];
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
      if (loadData) {
        contentData = responseData;
      }
      resourceCallback.onSuccess(new ResourceInfo((ResourceLocator) objs[0], (String) objs[1],
              modifiedTime, contentType, contentData));
      break;
    case GET_RESOURCE_DATA:
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

  protected String buildRequestUrl(Command command) {
    return buildRequestUrl(new String[0], command);
  }

  protected String buildRequestUrl(String[] params, Command command) {
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