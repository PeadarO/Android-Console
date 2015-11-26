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
import java.util.List;
import java.util.Map;

import org.openremote.console.controller.auth.Credentials;
import org.openremote.console.controller.connector.AndroidHttpConnector;
import org.openremote.console.controller.connector.ControllerConnector;
import org.openremote.entities.panel.*;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerInfo;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.entities.controller.Device;
import org.openremote.entities.controller.DeviceInfo;

/**
 * This is the main Controller class used for interacting with a controller; the
 * Controller is used to get panel and device information and to
 * register/unregister panels and devices
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 */
public class Controller {
  // TODO: Inject the appropriate connector
  private ControllerConnector connector;
  // private ControllerConnector connector = new SingleThreadHttpConnector();
  private String name;
  private String version;
  private ControllerInfo controllerInfo;
  private List<DeviceRegistrationHandle> registeredDevices = new ArrayList<DeviceRegistrationHandle>();
  private List<PanelRegistrationHandle> registeredPanels = new ArrayList<PanelRegistrationHandle>();
  private static Class<?> connectorClazz = AndroidHttpConnector.class;

  /**
   * Create a controller from the specified string URL
   * 
   * @param url
   * @throws ConnectionException
   */
  public Controller(String url) {
    this(url, null);
  }

  /**
   * Create a controller from the specified string URL using the specified
   * {@link org.openremote.console.controller.auth.Credentials} for the
   * connection
   * 
   * @param url
   * @param credentials
   */
  public Controller(String url, Credentials credentials) {
    this(new ControllerInfo(url), credentials);
  }

  /**
   * Create a controller from the specified
   * {@link org.openremote.entities.controller.ControllerInfo}
   * 
   * @param controllerInfo
   * @throws ConnectionException
   */
  public Controller(ControllerInfo controllerInfo) {
    this(controllerInfo, null);
  }

  /**
   * Create a controller from the specified
   * {@link org.openremote.entities.controller.ControllerInfo} using the
   * specified {@link org.openremote.console.controller.auth.Credentials} for
   * the connection
   * 
   * @param controllerInfo
   * @param credentials
   * @throws ConnectionException
   */
  public Controller(ControllerInfo controllerInfo, Credentials credentials) {
    try {
      connector = (ControllerConnector) connectorClazz.newInstance();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    setControllerInfo(controllerInfo);
    setCredentials(credentials);
    connector.setTimeout(getTimeout());
  }

  public static void setConnectorType(Class<?> connectorClass) {
    if (ControllerConnector.class.isAssignableFrom(connectorClass)) {
      connectorClazz = connectorClass;
    }
  }

  /**
   * Set the timeout for the connection to the controller; default value is
   * {@value org.openremote.console.controller.connector.ControllerConnector#DEFAULT_TIMEOUT}
   * . Useful to change this if link/hardware is slow.
   * 
   * @param timeout
   */
  public void setTimeout(int timeout) {
    connector.setTimeout(timeout);
  }

  /**
   * Get the current timeout for the connection to the controller
   * 
   * @return connection timeout in milliseconds
   */
  public int getTimeout() {
    return connector.getTimeout();
  }

  /**
   * Set the {@link org.openremote.console.controller.auth.Credentials} to be
   * used for this connection
   * 
   * @param credentials
   */
  public void setCredentials(Credentials credentials) {
    connector.setCredentials(credentials);
  }

  /**
   * Get the {@link org.openremote.console.controller.auth.Credentials} used by
   * this controller
   * 
   * @return {@link Credentials} used by this controller
   */
  public Credentials getCredentials() {
    return connector.getCredentials();
  }

  // /**
  // * Sets whether or not the connection to the controller will be
  // automatically re-established
  // * when a connection error occurs.
  // * @param autoReconnect
  // */
  // public void setAutoReconnect(boolean autoReconnect) {
  // connector.setAutoReconnect(autoReconnect);
  // }
  //
  // /**
  // * Indicates whether the connection to the controller will be automatically
  // re-established
  // * when a connection error occurs.
  // * @return is autoreconnect enabled
  // */
  // public boolean isAutoReconnect() {
  // return connector.isAutoReconnect();
  // }

  /**
   * Get the {@link org.openremote.entities.controller.ControllerInfo} for this
   * controller
   * 
   * @return {@link ControllerInfo} for this controller
   */
  public ControllerInfo getControllerInfo() {
    return new ControllerInfo(connector.getControllerUrl().toString(), name, version,
            connector.getControllerIdentity());
  }

  /**
   * Registers the panel asynchronously with this controller (it is the caller's
   * responsibility to ensure that the panel being registered is loaded into
   * this controller and is up to date. If a panel is out of date or isn't
   * loaded into this controller then unpredictable behaviour will occur.
   * 
   * Registering means that you will be able to interact with widgets and
   * receive property change notifications from widgets when sensor values
   * change and you will be able to retrieve widget resources from the
   * controller.
   * 
   * @param panel
   */
  public synchronized PanelRegistrationHandle registerPanel(final Panel panel,
          AsyncRegistrationCallback callback) {
    if (panel == null) {
      callback.onFailure(ControllerResponseCode.PANEL_NULL);
      return null;
    }

    // Check if panel is already registered
    for (PanelRegistrationHandle registeredPanel : registeredPanels) {
      if (registeredPanel.getPanel() == panel) {
        callback.onFailure(ControllerResponseCode.ALREADY_REGISTERED);
        return registeredPanel;
      }
    }

    // Create handle
    PanelRegistrationHandle registration = new PanelRegistrationHandle(panel, callback);
    registeredPanels.add(registration);
    registration.setIsRegistered(true);

    // Connect up command sender and resource locator
    List<ResourceConsumer> consumers = panel.getResourceConsumers();
    List<Widget> widgets = panel.getWidgets();

    if (consumers != null) {
      for (ResourceConsumer consumer : consumers) {
        List<ResourceInfo> resources = consumer.getResources();
        if (resources != null) {
          for (ResourceInfo resource : resources) {
            resource.setResourceLocator(connector);
          }
        }
      }
    }
    if (widgets != null) {
      for (Widget widget : widgets) {
        if (widget instanceof CommandWidget) {
          CommandWidget commandWidget = (CommandWidget) widget;
          commandWidget.setCommandSender(connector);
        }
      }
    }

    doRegistration(registration, true);
    return registration;
  }

  /**
   * Registers the device with this controller (it is the caller's
   * responsibility to ensure that the device belongs to this controller;
   * otherwise callbacks will fail or return unexpected results). Registering
   * means that you will be able to send commands to the device and you will
   * receive notification of sensor value changes.
   * 
   * @param device
   */
  public DeviceRegistrationHandle registerDevice(Device device, AsyncRegistrationCallback callback) {

    if (device == null) {
      callback.onFailure(ControllerResponseCode.DEVICE_NULL);
      return null;
    }

    // Check if device is already registered
    for (DeviceRegistrationHandle registeredDevice : registeredDevices) {
      if (registeredDevice.getDevice() == device) {
        callback.onFailure(ControllerResponseCode.ALREADY_REGISTERED);
        return registeredDevice;
      }
    }

    // Create handle
    DeviceRegistrationHandle registration = new DeviceRegistrationHandle(device, callback);
    registeredDevices.add(registration);
    registration.setIsRegistered(true);

    // Connect up command sender
    registration.getDevice().setCommandSender(connector);

    doRegistration(registration, true);
    return registration;
  }

  private void doRegistration(final RegistrationHandle registration, final boolean firstRun) {
    final List<Integer> sensorIds = registration.getSensorIds();
    final AsyncControllerCallback<Map<Integer, String>> monitorCallback = new AsyncControllerCallback<Map<Integer, String>>() {
      @Override
      public void onFailure(ControllerResponseCode error) {
        if (firstRun) {
          // Pass error back to registration callback
          registration.getCallback().onFailure(error);
        }
      }

      @Override
      public void onSuccess(Map<Integer, String> result) {
        // Pass through to registration handle
        registration.onSensorsChanged(result);
        if (firstRun) {
          registration.getCallback().onSuccess();
        }

        if (isConnected() && registration.isRegistered()) {
          monitorSensors(registration);
        }
      }
    };

    // Get initial sensor values
    if (sensorIds != null && sensorIds.size() > 0) {
      connector.getSensorValues(sensorIds, monitorCallback);
    } else {
      if (firstRun) {
        // Just call registration onSuccess callback
        registration.getCallback().onSuccess();
      }
    }
  }

  private void monitorSensors(final RegistrationHandle registration) {
    final List<Integer> sensorIds = registration.getSensorIds();
    final AsyncControllerCallback<Map<Integer, String>> monitorCallback = new AsyncControllerCallback<Map<Integer, String>>() {
      @Override
      public void onFailure(ControllerResponseCode error) {
        // Pass error back to registration callback
        registration.getCallback().onFailure(error);
      }

      @Override
      public void onSuccess(Map<Integer, String> result) {
        if (isConnected() && registration.isRegistered()) {
          if (result != null) {
            // Pass through to registration handle
            registration.onSensorsChanged(result);
          }
          monitorSensors(registration);
        }
      }
    };

    connector.monitorSensors(registration.uuid, sensorIds, monitorCallback);
  }

  /**
   * Unregisters a panel from the controller; no more sensor change
   * notifications will be received and it will not be possible to send commands
   * to the panel widgets or resolve resources.
   * 
   * @param registrationHandle
   */
  public void unregisterPanel(PanelRegistrationHandle registrationHandle) {
    if (registrationHandle == null || !registeredPanels.contains(registrationHandle)
            || registrationHandle.getPanel() == null) {
      return;
    }

    registeredPanels.remove(registrationHandle);
    registrationHandle.setIsRegistered(false);
    Panel panel = registrationHandle.getPanel();

    // Disconnect command sender and resource locator
    List<ResourceConsumer> consumers = panel.getResourceConsumers();
    List<Widget> widgets = panel.getWidgets();

    if (consumers != null) {
      for (ResourceConsumer consumer : consumers) {
        List<ResourceInfo> resources = consumer.getResources();
        if (resources != null) {
          for (ResourceInfo resource : resources) {
            resource.setResourceLocator(null);
          }
        }
      }
    }
    if (widgets != null) {
      for (Widget widget : widgets) {
        if (widget instanceof CommandWidget) {
          CommandWidget commandWidget = (CommandWidget) widget;
          commandWidget.setCommandSender(null);
        }
      }
    }

    registrationHandle.getCallback().onFailure(ControllerResponseCode.UNREGISTERED);
  }

  /**
   * Unregisters a device from the controller; no more sensor change
   * notifications will be received and it will no longer be possible to send
   * commands to this device
   * 
   * @param registrationHandle
   */
  public void unregisterDevice(DeviceRegistrationHandle registrationHandle) {
    if (registrationHandle == null || !registeredDevices.contains(registrationHandle)
            || registrationHandle.getDevice() == null) {
      return;
    }

    registeredDevices.remove(registrationHandle);
    registrationHandle.setIsRegistered(false);

    // Disconnect command sender and resource locator
    registrationHandle.getDevice().setCommandSender(null);

    registrationHandle.getCallback().onFailure(ControllerResponseCode.UNREGISTERED);
  }

  // ------------------------------------------------------------------------------
  // Connector Wrapper Methods
  // ------------------------------------------------------------------------------

  /**
   * Set the Controller Info; will cause a disconnect and re-connect if
   * controller is already connected. Previous connection callback will be
   * called on connection complete
   * 
   * @param url
   */
  private void setControllerInfo(ControllerInfo controllerInfo) {
    if (!isConnected()) {
      this.controllerInfo = controllerInfo;
    }
  }

  /**
   * Gets the URL of the controller
   * 
   * @return {@link URL} of controller
   */
  public URL getControllerUrl() {
    return connector.getControllerUrl();
  }

  /**
   * Connects to this controller and calls the
   * {@link org.openremote.entities.controller.AsyncControllerCallback} with
   * response type of
   * {@link org.openremote.console.controller.ControllerConnectionStatus}
   * callback to be called when finished. Using the specified timeout for the
   * connection attempt. Any already registered panels will resume monitoring
   * when the connection is re-established.
   * 
   * @param callback
   */
  public void connect(final AsyncControllerCallback<ControllerConnectionStatus> callback) {
    if (connector == null) {
      callback.onFailure(ControllerResponseCode.UNKNOWN_ERROR);
      return;
    }

    // Set connector URL
    if (controllerInfo == null || controllerInfo.getUrl() == null
            || controllerInfo.getUrl().isEmpty()) {
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
        // Restart any existing sensor monitors
        for (PanelRegistrationHandle panelReg : registeredPanels) {
          doRegistration(panelReg, false);
        }
        for (DeviceRegistrationHandle deviceReg : registeredDevices) {
          doRegistration(deviceReg, false);
        }
        callback.onSuccess(result);
      }

      @Override
      public void onFailure(ControllerResponseCode error) {
        callback.onFailure(error);
      }
    });
  }

  /**
   * Disconnect from the controller; this will preserve any registrations but
   * their onFailure callbacks will be called to indicate a DISCONNECT has
   * occurred.
   */
  public void disconnect() {
    connector.disconnect();
  }

  /**
   * Indicates if this controller is currently connected
   * 
   * @return is connected.
   */
  public boolean isConnected() {
    return connector.isConnected();
  }

  /**
   * Get list of {@link org.openremote.entities.panel.PanelInfo} names
   * asynchronously from this controller
   * 
   * @param callback
   */
  public void getPanelList(AsyncControllerCallback<List<PanelInfo>> callback) {
    connector.getPanelList(callback);
  }

  /**
   * Get the specified {@link org.openremote.entities.panel.Panel} from this
   * controller
   * 
   * @param panelName
   * @param callback
   */
  public void getPanel(String panelName, AsyncControllerCallback<Panel> callback) {
    connector.getPanel(panelName, callback);
  }

  /**
   * Get list of {@link org.openremote.entities.controller.Device} names
   * asynchronously from this controller
   * 
   * @param callback
   */
  public void getDeviceList(AsyncControllerCallback<List<DeviceInfo>> callback) {
    connector.getDeviceList(callback);
  }

  /**
   * Get the specified {@link org.openremote.entities.controller.Device} from
   * this controller
   * 
   * @param deviceName
   * @param callback
   */
  public void getDevice(String deviceName, AsyncControllerCallback<Device> callback) {
    connector.getDevice(deviceName, callback);
  }

  /**
   * Logout from the controller (i.e. remove current credentials)
   * 
   * @param callback
   */
  public void logout(AsyncControllerCallback<Boolean> callback) {
    connector.logout(callback);
  }
}
