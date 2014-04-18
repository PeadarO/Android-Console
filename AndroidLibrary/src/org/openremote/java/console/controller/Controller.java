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
package org.openremote.java.console.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openremote.entities.panel.CommandSender;
import org.openremote.entities.panel.CommandWidget;
import org.openremote.entities.panel.PanelCommand;
import org.openremote.entities.panel.PanelCommandResponse;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceInfo;
import org.openremote.entities.panel.ResourceLocator;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.version1.*;
import org.openremote.entitites.controller.AsyncControllerCallback;
import org.openremote.entitites.controller.ControllerResponseCode;
import org.openremote.java.console.controller.auth.Credentials;
import org.openremote.java.console.controller.connector.*;

/**
 * This is the main Controller class used for interacting with a controller; the
 * Controller is used to get panel information and to register/unregister panels
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class Controller {
  class MonitorHandler implements AsyncControllerCallback<Map<Integer, String>> {
    private List<SensoryWidget> monitoredWidgets;
    private int[] sensorIds;
    private boolean cancelled = false;
    
    MonitorHandler(List<SensoryWidget> monitoredWidgets, int[] sensorIds) {
      this.monitoredWidgets = monitoredWidgets;
      this.sensorIds = sensorIds;
    }

    void start() {
      if (connector != null) {
        // Use timeout > 50s so that controller returns timeout response
        connector.monitorSensors(sensorIds, this, 55000);
      }
    }

    void cancel() {
      cancelled = true;
    }

    @Override
    public void onFailure(ControllerResponseCode error) {
      if (cancelled) {
        return;
      }
      // TODO Need to do something about failed polling (notify the console
      // app?)
      start();
    }

    @Override
    public void onSuccess(Map<Integer, String> result) {
      if (cancelled) {
        return;
      }

      if (result != null) {
        // Call on sensor changed for each changed sensor
        for (Entry<Integer, String> entry : result.entrySet()) {
          for (SensoryWidget widget : monitoredWidgets) {
            for (SensorLink link : widget.getSensorLinks()) {
              if (entry.getKey().equals(link.getRef())) {
                widget.onSensorValueChanged(link.getRef(), entry.getValue());
              }
            }
          }
        }
      }

      start();
    }
  };

  class CommandHandler implements CommandSender {
    private boolean cancelled = false;

    void cancel() {
      cancelled = true;
    }

    @Override
    public void sendCommand(PanelCommand command,
            final AsyncControllerCallback<PanelCommandResponse> callback) {
      if (connector != null) {
        Controller.this.connector.sendCommand(command,
                new AsyncControllerCallback<PanelCommandResponse>() {

                  @Override
                  public void onFailure(ControllerResponseCode error) {
                    if (cancelled) {
                      return;
                    }
                    callback.onFailure(error);
                  }

                  @Override
                  public void onSuccess(PanelCommandResponse result) {
                    if (cancelled) {
                      return;
                    }
                    callback.onSuccess(result);
                  }

                }, Controller.this.timeout);
      }
    }
  };

  class LocatorHandler implements ResourceLocator {
    private boolean cancelled = false;

    public void cancel() {
      cancelled = true;
    }

    @Override
    public void getResource(String resourceName, boolean getData,
            final AsyncControllerCallback<ResourceInfo> resourceCallback) {
      if (connector != null) {
        if (loadResourceData) {
          getData = true;
        }

        connector.getResource(this, resourceName, getData,
                new AsyncControllerCallback<ResourceInfo>() {

                  @Override
                  public void onFailure(ControllerResponseCode error) {
                    if (cancelled) {
                      return;
                    }
                    resourceCallback.onFailure(error);
                  }

                  @Override
                  public void onSuccess(ResourceInfo result) {
                    if (cancelled) {
                      return;
                    }
                    resourceCallback.onSuccess(result);
                  }

                }, Controller.this.timeout);
      }
    }

    @Override
    public void getResourceData(String resourceName,
            AsyncControllerCallback<ResourceDataResponse> resourceDataCallback) {
      if (connector != null) {
        connector.getResourceData(resourceName, resourceDataCallback, Controller.this.timeout);
      }
    }
  }

  public static final int DEFAULT_TIMEOUT = 5000;
  private int timeout = DEFAULT_TIMEOUT;
  private Map<Panel, MonitorHandler> registeredMonitors = new HashMap<Panel, MonitorHandler>();
  private Map<Panel, LocatorHandler> registeredLocators = new HashMap<Panel, LocatorHandler>();
  private Map<Panel, CommandHandler> registeredSenders = new HashMap<Panel, CommandHandler>();
  // TODO: Inject the appropriate connector
  private ControllerConnector connector = new AndroidHttpConnector();
  private String name;
  private int version;
  private boolean loadResourceData;
  private AsyncControllerCallback<ControllerConnectionStatus> connectCallback;

  /**
   * Create a controller from the specified {@link org.openremote.java.console.controller.ControllerInfo}
   * @param controllerInfo
   * @throws ConnectionException
   */
  public Controller(ControllerInfo controllerInfo) throws ConnectionException {
    this(controllerInfo, null);
  }

  /**
   * Create a controller from the specified {@link org.openremote.java.console.controller.ControllerInfo}
   * using the specified {@link org.openremote.java.console.controller.auth.Credentials} for the connection
   * @param controllerInfo
   * @param credentials
   * @throws ConnectionException
   */
  public Controller(ControllerInfo controllerInfo, Credentials credentials)
          throws ConnectionException {
    this(controllerInfo != null ? controllerInfo.getUrl() : null, credentials);
  }

  /**
   * Create a controller from the specified string URL
   * @param url
   * @throws ConnectionException
   */
  public Controller(URL url) throws ConnectionException {
    this(url, null);
  }

  /**
   * Create a controller from the specified string URL using the specified
   * {@link org.openremote.java.console.controller.auth.Credentials} for the connection
   * @param url
   * @param credentials
   * @throws ConnectionException
   */
  public Controller(URL url, Credentials credentials) throws ConnectionException {
    setControllerUrl(url);
    setCredentials(credentials);
  }

  /**
   * Set the timeout for the connection to the controller; default value is {@value #DEFAULT_TIMEOUT}.
   * Useful to change this if link/hardware is slow.
   * @param timeout
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /**
   * Get the current timeout for the connection to the controller
   * @return
   */
  public int getTimeout() {
    return this.timeout;
  }

  /**
   * Set the {@link org.openremote.java.console.controller.auth.Credentials} to be used for this
   * connection
   * @param credentials
   */
  public void setCredentials(Credentials credentials) {
    connector.setCredentials(credentials);
  }

  /**
   * Get the {@link org.openremote.java.console.controller.auth.Credentials} used by this controller
   * @return
   */
  public Credentials getCredentials() {
    return connector.getCredentials();
  }

  /**
   * Get the {@link org.openremote.java.console.controller.ControllerInfo} for this controller
   * @return
   */
  public ControllerInfo getControllerInfo() {
    return new ControllerInfo(name, version, connector.getControllerUrl());
  }

  /**
   * Registers the panel with this controller (it is the caller's responsibility
   * to ensure that the panel belongs to this controller; otherwise callbacks
   * will fail or return unexpected results). Registering means that you will be
   * able to send commands to the controller and you will receive notification
   * of sensor value changes and be able to resolve resources
   * 
   * @param panel
   */
  public void registerPanel(final Panel panel) {
    if (panel == null || registeredLocators.containsKey(panel)) {
      return;
    }

    final CommandHandler sender = new CommandHandler();
    final LocatorHandler locator = new LocatorHandler();

    final List<Widget> widgets = panel.getWidgets();
    List<SensoryWidget> monitoredWidgets = new ArrayList<SensoryWidget>();
    List<Integer> monitorIds = new ArrayList<Integer>();

    // Get sensory widgets first
    for (Widget widget : widgets) {
      if (widget instanceof SensoryWidget) {
        monitoredWidgets.add((SensoryWidget) widget);
        for (SensorLink link : ((SensoryWidget) widget).getSensorLinks()) {
          if (!monitorIds.contains(link.getRef())) {
            monitorIds.add(link.getRef());
          }
        }
      }
    }

    int[] sensorIds = new int[monitorIds.size()];
    for (int i = 0; i < sensorIds.length; i++) {
      sensorIds[i] = monitorIds.get(i).intValue();
    }

    // Store panel as registered
    final MonitorHandler monitor = new MonitorHandler(monitoredWidgets, sensorIds);
    registeredMonitors.put(panel, monitor);
    registeredLocators.put(panel, locator);
    registeredSenders.put(panel, sender);

    // Get initial values for all sensors
    connector.getSensorValues(sensorIds, new AsyncControllerCallback<Map<Integer, String>>() {
      @Override
      public void onFailure(ControllerResponseCode error) {
        // If there's a problem getting initial sensor values then should we
        // unregister the panel
        if (registeredMonitors.containsKey(panel)) {
          // Still registered
          monitor.start();
          connectHandlers(widgets, locator, sender);
        }
      }

      @Override
      public void onSuccess(Map<Integer, String> result) {
        // Process responses
        if (result != null) {
          // Call on sensor changed for each changed sensor
          List<SensoryWidget> sensoryWidgets = panel.getWidgets(SensoryWidget.class);
          for (Entry<Integer, String> entry : result.entrySet()) {
            for (SensoryWidget widget : sensoryWidgets) {
              for (SensorLink link : widget.getSensorLinks()) {
                if (entry.getKey().equals(link.getRef())) {
                  widget.onSensorValueChanged(link.getRef(), entry.getValue());
                }
              }
            }
          }
        }

        // Start monitoring all sensor links and link handlers
        if (registeredMonitors.containsKey(panel)) {
          // Still registered
          connectHandlers(widgets, locator, sender);
          monitor.start();
        }
      }

    }, timeout);
  }

  private void connectHandlers(List<Widget> widgets, ResourceLocator locator, CommandSender sender) {
    for (Widget widget : widgets) {
      widget.setResourceLocator(locator);
      if (widget instanceof CommandWidget) {
        CommandWidget commandWidget = (CommandWidget) widget;
        commandWidget.setCommandSender(sender);
      }
    }
  }

  /**
   * Unregisters the panel from the controller; no more sensor change
   * notifications will be received
   * 
   * @param panel
   */
  public void unregisterPanel(Panel panel) {
    // Remove panel
    if (registeredLocators.containsKey(panel)) {
      MonitorHandler monitor = registeredMonitors.get(panel);
      LocatorHandler locator = registeredLocators.get(panel);
      CommandHandler sender = registeredSenders.get(panel);
      locator.cancel();
      sender.cancel();
      monitor.cancel();

      // Remove resource locator and command sender
      List<Widget> widgets = panel.getWidgets();

      for (Widget widget : widgets) {
        widget.setResourceLocator(null);
        if (widget instanceof CommandWidget) {
          CommandWidget commandWidget = (CommandWidget) widget;
          commandWidget.setCommandSender(null);
        }
      }

      registeredMonitors.remove(panel);
      registeredLocators.remove(panel);
      registeredSenders.remove(panel);
    }
  }

  /**
   * Set the force load resource data flag; determines if resource data should be automatically fetched
   * when a resource is resolved into a {@link org.openremote.entities.panel.ResourceInfo} object.
   * If using caching then this should be false and then {@link org.openremote.entities.panel.ResourceInfo#getModifiedTime()}
   * can be compared to the cached resource modified time
   * @param loadResourceData
   */
  public void setLoadResourceData(boolean loadResourceData) {
    this.loadResourceData = loadResourceData;
  }

  /**
   * Gets the current for the force load resource data flag
   * @return
   */
  public boolean getLoadResourceData() {
    return this.loadResourceData;
  }

  // ------------------------------------------------------------------------------
  // Connector Wrapper Methods
  // ------------------------------------------------------------------------------

  /**
   * Set the URL of the controller; will cause a disconnect and re-connect if controller
   * is already connected. Previous connection callback will be called on connection complete
   * @param url
   */
  public void setControllerUrl(URL url) {
    URL currentUrl = connector.getControllerUrl();
    if (currentUrl != null && currentUrl.equals(url)) {
      return;
    }
    boolean isConnected = isConnected();
    disconnect();
    connector.setControllerUrl(url);
    if (isConnected && connectCallback != null) {
      connect(connectCallback);
    }
  }

  /**
   * Gets the URL of the controller
   * @return
   */
  public URL getControllerUrl() {
    return connector.getControllerUrl();
  }

  /**
   * Connects to this controller and calls the {@link AsyncControllerCallback<ControllerConnectionStatus>}
   * callback to be called when finished. Using the specified timeout for the connection attempt.
   * Any already registered panels will resume monitoring when the connection is re-established.
   * @param callback
   * @param timeout
   */
  public void connect(final AsyncControllerCallback<ControllerConnectionStatus> callback, int timeout) {
    if (connector == null) {
      callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
      return;
    }
    
    connectCallback = callback;
    
    connector.connect(new AsyncControllerCallback<ControllerConnectionStatus>() {

      @Override
      public void onSuccess(ControllerConnectionStatus result) {
        // start any existing sensor monitors for already registered panels
        for (CommandHandler sender : registeredSenders.values()) {
          sender.cancelled = false;
        }
        
        for (LocatorHandler locator : registeredLocators.values()) {
          locator.cancelled = false;
        }
        
        for (MonitorHandler monitor : registeredMonitors.values()) {
          monitor.cancelled = false;
          monitor.start();
        }
        callback.onSuccess(result);
      }

      @Override
      public void onFailure(ControllerResponseCode error) {
        callback.onFailure(error);
      }
    }, timeout);
  }

  /**
   * Connects to this controller and calls the {@link AsyncControllerCallback<ControllerConnectionStatus>}
   * callback to be called when finished. Using the default controller timeout for the connection attempt.
   * Any already registered panels will resume monitoring when the connection is re-established.
   * @param callback
   */
  public void connect(AsyncControllerCallback<ControllerConnectionStatus> callback) {
    connect(callback, timeout);
  }

  /**
   * Disconnect from the controller; this will keep any registered panels but you will no longer
   * receive change notifications, be able to resolve resources and/or send commands
   */
  public void disconnect() {
    for (MonitorHandler monitor : registeredMonitors.values()) {
      monitor.cancel();
    }
    for (CommandHandler sender : registeredSenders.values()) {
      sender.cancel();
    }
    for (LocatorHandler locator : registeredLocators.values()) {
      locator.cancel();
    }

    connector.disconnect();
  }

  /**
   * Indicates if the controller is currently connected
   * @return
   */
  public boolean isConnected() {
    return connector.isConnected();
  }

  /**
   * Get {@link List<PanelInfo>} asynchronously from this controller
   * @param callback
   * @param timeout
   */
  public void getPanelInfo(AsyncControllerCallback<List<PanelInfo>> callback, int timeout) {
    connector.getPanelInfo(callback, timeout);
  }

  /**
   * Get {@link List<PanelInfo>} asynchronously from this controller
   * @param callback
   */
  public void getPanelInfo(AsyncControllerCallback<List<PanelInfo>> callback) {
    getPanelInfo(callback, timeout);
  }

  /**
   * Get the specified {@link Panel} from the controller
   * @param panelName
   * @param callback
   * @param timeout
   */
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback, int timeout) {
    connector.getPanel(panelName, callback, timeout);
  }

  /**
   * Get the specified {@link Panel} from the controller
   * @param panelName
   * @param callback
   */
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback) {
    getPanel(panelName, callback, timeout);
  }

  /**
   * Logout from the controller (i.e. remove current credentials)
   * @param callback
   * @param timeout
   */
  public void logout(AsyncControllerCallback<Boolean> callback, int timeout) {
    connector.logout(callback, timeout);
  }

  /**
   * Logout from the controller (i.e. remove current credentials)
   * @param callback
   */
  public void logout(AsyncControllerCallback<Boolean> callback) {
    logout(callback, timeout);
  }
}
