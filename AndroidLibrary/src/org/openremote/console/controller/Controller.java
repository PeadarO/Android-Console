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
package org.openremote.console.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openremote.console.controller.auth.Credentials;
import org.openremote.console.controller.connector.ControllerConnector;
import org.openremote.console.controller.connector.SingleThreadHttpConnector;
import org.openremote.entities.panel.*;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.CommandSender;
import org.openremote.entities.controller.ControllerInfo;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.entities.controller.Device;
import org.openremote.entities.controller.DeviceInfo;

/**
 * This is the main Controller class used for interacting with a controller; the
 * Controller is used to get panel information and to register/unregister panels
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class Controller {
  public static final int DEFAULT_TIMEOUT = 5000;
  private int timeout = DEFAULT_TIMEOUT;
  private Map<Panel, ControllerSensorMonitor> panelMonitors = new HashMap<Panel, ControllerSensorMonitor>();
  private Map<Panel, ControllerResourceLocator> resourceLocators = new HashMap<Panel, ControllerResourceLocator>();
  private Map<Panel, ControllerCommandSender> panelSenders = new HashMap<Panel, ControllerCommandSender>();
  private Map<Device, ControllerSensorMonitor> deviceMonitors = new HashMap<Device, ControllerSensorMonitor>();
  private Map<Device, ControllerCommandSender> deviceSenders = new HashMap<Device, ControllerCommandSender>();
  // TODO: Inject the appropriate connector
  private ControllerConnector connector = new SingleThreadHttpConnector();
  private String name;
  private String version;
  private AsyncControllerCallback<ControllerConnectionStatus> connectCallback;
  private ControllerInfo controllerInfo;

  /**
   * Create a controller from the specified string URL
   * @param url
   * @throws ConnectionException
   */
  public Controller(String url) {
    this(url, null);
  }

  /**
   * Create a controller from the specified string URL using the specified
   * {@link org.openremote.console.controller.auth.Credentials} for the connection
   * @param url
   * @param credentials
   */
  public Controller(String url, Credentials credentials) {
    this(new ControllerInfo(url), credentials);
  }

  /**
   * Create a controller from the specified {@link org.openremote.entities.controller.ControllerInfo}
   * @param controllerInfo
   * @throws ConnectionException
   */
  public Controller(ControllerInfo controllerInfo) {
    this(controllerInfo, null);
  }
  
  /**
   * Create a controller from the specified {@link org.openremote.entities.controller.ControllerInfo}
   * using the specified {@link org.openremote.console.controller.auth.Credentials} for the connection
   * @param controllerInfo
   * @param credentials
   * @throws ConnectionException
   */
  public Controller(ControllerInfo controllerInfo, Credentials credentials) {
    setControllerInfo(controllerInfo);
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
   * Set the {@link org.openremote.console.controller.auth.Credentials} to be used for this
   * connection
   * @param credentials
   */
  public void setCredentials(Credentials credentials) {
    connector.setCredentials(credentials);
  }

  /**
   * Get the {@link org.openremote.console.controller.auth.Credentials} used by this controller
   * @return
   */
  public Credentials getCredentials() {
    return connector.getCredentials();
  }

  /**
   * Get the {@link org.openremote.entities.controller.ControllerInfo} for this controller
   * @return
   */
  public ControllerInfo getControllerInfo() {
    return new ControllerInfo(connector.getControllerUrl().toString(), name, version, connector.getControllerIdentity());
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
  public synchronized void registerPanel(final Panel panel) {
    if (panel == null || resourceLocators.containsKey(panel)) {
      return;
    }

    final ControllerCommandSender sender = new ControllerCommandSender();
    final ControllerResourceLocator locator = new ControllerResourceLocator();
    sender.setConnector(connector);
    locator.setConnector(connector);
    
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
    final ControllerSensorMonitor monitor = new ControllerSensorMonitor(monitoredWidgets, sensorIds);
    monitor.setConnector(connector);
    panelMonitors.put(panel, monitor);
    resourceLocators.put(panel, locator);
    panelSenders.put(panel, sender);

    // Get initial values for all sensors
    if (sensorIds.length > 0) {
      connector.getSensorValues(sensorIds, new AsyncControllerCallback<Map<Integer, String>>() {
        @Override
        public void onFailure(ControllerResponseCode error) {
          // If there's a problem getting initial sensor values then should we
          // unregister the panel
          if (panelMonitors.containsKey(panel)) {
            unregisterPanel(panel);
          }
        }
  
        @Override
        public void onSuccess(Map<Integer, String> result) {
          // Start monitoring all sensor links and link handlers
          if (panelMonitors.containsKey(panel)) {
            // Still registered
            connectHandlers(panel, locator, sender);
            monitor.start();
          }
          
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
        }
  
      }, timeout);
    } else {
      // Start monitoring all sensor links and link handlers
      if (panelMonitors.containsKey(panel)) {
        // Still registered
        connectHandlers(panel, locator, sender);
        monitor.start();
      } 
    }
  }
  
  /**
   * Registers the device with this controller (it is the caller's responsibility
   * to ensure that the device belongs to this controller; otherwise callbacks
   * will fail or return unexpected results). Registering means that you will be
   * able to send commands to the device and you will receive notification
   * of sensor value changes.
   * 
   * @param device
   */
  public void registerDevice(Device device) {
//    if (device == null || registeredLocators.containsKey(panel)) {
//      return;
//    }
  }

  private void connectHandlers(Panel panel, ResourceLocator locator, CommandSender sender) {
    List<ResourceConsumer> consumers = panel.getResourceConsumers();
    
    for (ResourceConsumer consumer : consumers) {
      List<ResourceInfo> resources = consumer.getResources();
      if (resources != null) {
        for (ResourceInfo resource : resources) {
          resource.setResourceLocator(locator);
        }
      }
      if (consumer instanceof CommandWidget) {
        CommandWidget commandWidget = (CommandWidget) consumer;
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
  public synchronized void unregisterPanel(Panel panel) {
    // Remove panel
    if (resourceLocators.containsKey(panel)) {
      ControllerSensorMonitor monitor = panelMonitors.get(panel);
      ControllerResourceLocator locator = resourceLocators.get(panel);
      ControllerCommandSender sender = panelSenders.get(panel);
      locator.disable();
      sender.disable();
      monitor.disable();

      // Remove resource locator and command sender
      List<ResourceConsumer> consumers = panel.getResourceConsumers();
      
      for (ResourceConsumer consumer : consumers) {
        List<ResourceInfo> resources = consumer.getResources();
        for (ResourceInfo resource : resources) {
          resource.setResourceLocator(null);
        }
        if (consumer instanceof CommandWidget) {
          CommandWidget commandWidget = (CommandWidget) consumer;
          commandWidget.setCommandSender(null);
        }
      }

      panelMonitors.remove(panel);
      resourceLocators.remove(panel);
      panelSenders.remove(panel);
    }
  }
  
  /**
   * Unregisters a device from the controller; no more sensor
   * change notifications will be received and it will no longer
   * be possible to send commands to this device
   * @param device
   */
  public synchronized void unregisterDevice(Device device) {
    
  }


  // ------------------------------------------------------------------------------
  // Connector Wrapper Methods
  // ------------------------------------------------------------------------------

  /**
   * Set the Controller Info; will cause a disconnect and re-connect if controller
   * is already connected. Previous connection callback will be called on connection complete
   * @param url
   */
  public void setControllerInfo(ControllerInfo controllerInfo) {
    boolean isConnected = isConnected();
    disconnect();
    this.controllerInfo = controllerInfo;

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
   * Connects to this controller and calls the {@link org.openremote.entities.controller.AsyncControllerCallback<org.openremote.java.console.controller.ControllerConnectionStatus>}
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
    
    // Set connector URL
    if (controllerInfo == null || controllerInfo.getUrl() == null || controllerInfo.getUrl().isEmpty()) {
      callback.onFailure(ControllerResponseCode.INVALID_URL);
      return;
    }
    
    try {
      connector.setControllerUrl(new URL(controllerInfo.getUrl()));
    } catch (MalformedURLException e) {
      callback.onFailure(ControllerResponseCode.INVALID_URL);
      return;
    }
    
    connector.connect(new AsyncControllerCallback<ControllerConnectionStatus>() {

      @Override
      public void onSuccess(ControllerConnectionStatus result) {
        // start any existing sensor monitors for already registered panels
        for (ControllerCommandSender sender : panelSenders.values()) {
          sender.enable();
        }
        
        for (ControllerResourceLocator locator : resourceLocators.values()) {
          locator.enable();
        }
        
        for (ControllerSensorMonitor monitor : panelMonitors.values()) {
          monitor.enable();
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
   * Connects to this controller and calls the {@link org.openremote.entities.controller.AsyncControllerCallback<org.openremote.java.console.controller.ControllerConnectionStatus>}
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
    for (ControllerSensorMonitor monitor : panelMonitors.values()) {
      monitor.disable();
    }
    for (ControllerCommandSender sender : panelSenders.values()) {
      sender.disable();
    }
    for (ControllerResourceLocator locator : resourceLocators.values()) {
      locator.disable();
    }

    connector.disconnect();
  }

  /**
   * Indicates if this controller is currently connected
   * @return
   */
  public boolean isConnected() {
    return connector.isConnected();
  }

  /**
   * Get list of {@link org.openremote.entities.panel.PanelInfo} names asynchronously from this controller
   * @param callback
   * @param timeout
   */
  public void getPanelList(AsyncControllerCallback<List<PanelInfo>> callback, int timeout) {
    connector.getPanelList(callback, timeout);
  }

  /**
   * Get list of {@link org.openremote.entities.panel.PanelInfo} names asynchronously from this controller
   * @param callback
   */
  public void getPanelList(AsyncControllerCallback<List<PanelInfo>> callback) {
    getPanelList(callback, timeout);
  }

  /**
   * Get the specified {@link org.openremote.entities.panel.Panel} from this controller
   * @param panelName
   * @param callback
   * @param timeout
   */
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback, int timeout) {
    connector.getPanel(panelName, callback, timeout);
  }

  /**
   * Get the specified {@link org.openremote.entities.panel.Panel} from this controller
   * @param panelName
   * @param callback
   */
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback) {
    getPanel(panelName, callback, timeout);
  }

  /**
   * Get list of {@link org.openremote.entities.controller.Device} names asynchronously from this controller
   * @param callback
   */
  public void getDeviceList(AsyncControllerCallback<List<DeviceInfo>> callback) {
    getDeviceList(callback,timeout);
  }
  
  /**
   * Get list of {@link org.openremote.entities.controller.Device} names asynchronously from this controller
   * @param callback
   * @param timeout
   */
  public void getDeviceList(AsyncControllerCallback<List<DeviceInfo>> callback, int timeout) {
    connector.getDeviceList(callback,timeout);
  }
  
  /**
   * Get the specified {@link org.openremote.entities.controller.Device} from this controller
   * @param deviceName
   * @param callback
   */
  public void getDevice(String deviceName, AsyncControllerCallback<Device> callback) {
    getDevice(deviceName, callback, timeout);
  }

  /**
   * Get the specified {@link org.openremote.entities.controller.Device} from this controller
   * @param deviceName
   * @param callback
   * @param timeout
   */
  public void getDevice(String deviceName, AsyncControllerCallback<Device> callback, int timeout) {
    
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
