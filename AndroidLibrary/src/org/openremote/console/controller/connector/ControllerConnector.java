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
import java.util.List;
import java.util.Map;

import org.openremote.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.console.controller.ControllerConnectionStatus;
import org.openremote.console.controller.auth.Credentials;
import org.openremote.entities.panel.Panel;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceLocator;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.Command;
import org.openremote.entities.controller.CommandResponse;
import org.openremote.entities.controller.CommandSender;
import org.openremote.entities.controller.ControlCommand;
import org.openremote.entities.controller.ControlCommandResponse;
import org.openremote.entities.controller.Device;
import org.openremote.entities.controller.DeviceInfo;

/**
 * This interface defines the contract for controller connectors.
 * Implementations of this interface are used to make the connection to the
 * controller.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * 
 */
public interface ControllerConnector extends CommandSender, ResourceLocator {
  public static final int DEFAULT_TIMEOUT = 5000;
  
  /**
   * Sets the controller URL
   * 
   * @param controllerUrl
   */
  void setControllerUrl(URL controllerUrl);

  /**
   * Gets the controller URL
   * 
   * @return
   */
  URL getControllerUrl();
  
  /**
   * Gets the unique controller identity
   * @return
   */
  String getControllerIdentity();

  /**
   * Sets the {@link org.openremote.console.controller.auth.Credentials} to be used
   * by this connector
   * 
   * @param credentials
   */
  void setCredentials(Credentials credentials);

  /**
   * Set the timeout in milliseconds for this connector
   * @param timeout
   */
  void setTimeout(int timeout);
  
  /**
   * Get the timeout in milliseconds for this connector
   * @return
   */
  int getTimeout();
  
  /**
   * Returns the currently active credentials
   * 
   * @return
   */
  Credentials getCredentials();

  /**
   * Indicates if the connector is currently connected to the controller
   * 
   * @return
   */
  boolean isConnected();

  /**
   * Connects to the controller and maintains the connection; it is the
   * connectors responsibility to notify the caller via the callback onFailure
   * method when the connection closes for some reason
   * 
   * @param callback
   */
  void connect(AsyncControllerCallback<ControllerConnectionStatus> callback);

  /**
   * Disconnects from the controller asynchronously; does nothing if not already connected.
   * When disconnect is completed the onFailure method of the connect callback will be
   * called with a ControllerResponseCode of {@value org.openremote.entities.controller.ControllerResponseCode.DISCONNECTED}. 
   */
  void disconnect();

  /**
   * Returns {@link java.util.List<org.openremote.entities.panel.PanelInfo>} of
   * panels that are recognised by this controller.
   * 
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   */
  void getPanelList(AsyncControllerCallback<List<PanelInfo>> callback);

  /**
   * Returns {@link org.openremote.entities.panel.Panel} definition of the
   * specified panel.
   * 
   * @param panelName
   *          Name of the panel to retrieve
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   */
  void getPanel(String panelName, AsyncControllerCallback<Panel> callback);

  /**
   * Returns {@link Boolean} indicating whether control command send request was
   * successful.
   * 
   * @param command
   *          Control Command to send
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   */
  void sendControlCommand(ControlCommand command, AsyncControllerCallback<ControlCommandResponse> callback);
  
  /**
   * Returns {@link Boolean} indicating whether command send request was
   * successful.
   * 
   * @param command
   *          Command to send
   * @param parameter
   *          Command parameter for use with ${param} dynamic command values
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   */
  void sendCommand(Command command, String parameter, AsyncControllerCallback<CommandResponse> callback);

  /**
   * Returns Map<Integer, String> of sensor IDs and values only for sensors
   * whose values have changed since the last request.
   * 
   * @param sensorIds
   *          List of sensor IDs to monitor
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   */
  void monitorSensors(List<Integer> sensorIds, AsyncControllerCallback<Map<Integer, String>> callback);

  /**
   * Returns Map<Integer, String> of sensor IDs and values. *
   * 
   * @param sensorIds
   *          List of sensor IDs to get values for
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   */
  void getSensorValues(List<Integer> sensorIds, AsyncControllerCallback<Map<Integer, String>> callback);

  /**
   * Logs out of the controller. *
   * 
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   */
  void logout(AsyncControllerCallback<Boolean> callback);
  
  /**
   * Perform controller discovery for the specified period of time using the specified TCP port
   * and call onDiscoveryStarted, if discovery cannot be started then call onStartDiscoveryFailed.
   * When a new controller is discovered call onControllerFound.
   * @param callback
   * @param tcpPort
   * @param searchDuration
   */
  void startDiscovery(AsyncControllerDiscoveryCallback callback, int tcpPort, Integer searchDuration);
  
  /**
   * Stop current controller discovery and call onDiscoveryStopped of callback provided at startDiscovery
   * @param callback
   */
  void stopDiscovery();
  
  /**
   * Determines if controller discovery is currently running
   * @return
   */
  boolean isDiscoveryRunning();

  /**
   * Returns {@link java.util.List<org.openremote.entities.controller.DeviceInfo>} of
   * devices that are recognised by this controller.
   * 
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   */
  void getDeviceList(AsyncControllerCallback<List<DeviceInfo>> callback);

  /**
   * Returns {@link org.openremote.entities.controller.Device} that matches
   * the supplied device name.
   * @param deviceName
   * @param callback
   */
  void getDevice(String deviceName, AsyncControllerCallback<Device> callback);
  
  /**
   * Sets whether the connector should automatically try to re-establish connection
   * when a connection error occurs.
   * @param autoReconnect
   */
  void setAutoReconnect(boolean autoReconnect);
  
  /**
   * Indicates whether the connector will automatically try to re-establish connection
   * when a connection error occurs.
   * @return
   */
  boolean isAutoReconnect();
}
