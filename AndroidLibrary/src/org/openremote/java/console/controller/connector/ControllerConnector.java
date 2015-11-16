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
import java.util.List;
import java.util.Map;

import org.openremote.entities.panel.PanelCommand;
import org.openremote.entities.panel.PanelCommandResponse;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfo;
import org.openremote.entities.panel.ResourceInfoDetails;
import org.openremote.entities.panel.ResourceLocator;
import org.openremote.entities.panel.version1.Panel;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.java.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.java.console.controller.ControllerConnectionStatus;
import org.openremote.java.console.controller.auth.Credentials;

/**
 * This interface defines the contract for controller connectors.
 * Implementations of this interface are used to make the connection to the
 * controller.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * 
 */
public interface ControllerConnector {
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
   * Sets the {@link org.openremote.java.console.controller.auth.Credentials} to be used
   * by this connector
   * 
   * @param credentials
   */
  void setCredentials(Credentials credentials);

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
   * @param timeout
   *          Timeout in milliseconds for this command
   */
  void connect(AsyncControllerCallback<ControllerConnectionStatus> callback, int timeout);

  /**
   * Disconnects from the controller; does nothing if not already connected
   */
  void disconnect();

  /**
   * Returns {@link java.util.List<org.openremote.entities.panel.PanelInfo>} of
   * panels that are recognised by this controller.
   * 
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   * @param timeout
   *          Timeout in milliseconds for this command
   */
  void getPanelInfo(AsyncControllerCallback<List<PanelInfo>> callback, int timeout);

  /**
   * Returns {@link org.openremote.entities.panel.version1.Panel} definition of the
   * specified panel.
   * 
   * @param panelName
   *          Name of the panel to retrieve
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   * @param timeout
   *          Timeout in milliseconds for this command
   */
  void getPanel(String panelName, AsyncControllerCallback<Panel> callback, int timeout);

  /**
   * Returns {@link Boolean} indicating whether command send request was
   * successful.
   * 
   * @param command
   *          Command to send
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   * @param timeout
   *          Timeout in milliseconds for this command
   */
  void sendCommand(PanelCommand command, AsyncControllerCallback<PanelCommandResponse> callback,
          int timeout);

  /**
   * Returns Map<Integer, String> of sensor IDs and values only for sensors
   * whose values have changed since the last request.
   * 
   * @param sensorIds
   *          Array of sensor IDs to monitor
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   * @param timeout
   *          Timeout in milliseconds for this command
   */
  void monitorSensors(int[] sensorIds, AsyncControllerCallback<Map<Integer, String>> callback,
          int timeout);

  /**
   * Returns Map<Integer, String> of sensor IDs and values. *
   * 
   * @param sensorIds
   *          Array of sensor IDs to get values for
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   * @param timeout
   *          Timeout in milliseconds for this command
   */
  void getSensorValues(int[] sensorIds, AsyncControllerCallback<Map<Integer, String>> callback,
          int timeout);

  /**
   * Logs out of the controller. *
   * 
   * @param callback
   *          {@link AsyncControllerCallback} callback for handling the response
   *          asynchronously
   * @param timeout
   *          Timeout in milliseconds for this command
   */
  void logout(AsyncControllerCallback<Boolean> callback, int timeout);

  /**
   * Get the resource info details for the requested resource
   * 
   * @param resourceLocator
   * @param resourceName
   * @param getData
   * @param resourceCallback
   */
  void getResourceInfoDetails(ResourceLocator resourceLocator, String resourceName,
          AsyncControllerCallback<ResourceInfoDetails> resourceCallback, int timeout);

  // /**
  // * Get the resource info for the requested resources optionally retrieve the
  // data of the resources (otherwise lazy load)
  // * @param resourceName
  // * @param getData
  // * @param resourceCallback
  // * @param timeout Timeout in milliseconds for this command
  // */
  // void getResources(String[] resourceName, boolean getData,
  // AsyncControllerCallback<ResourceInfo[]> resourceCallback, int timeout);
  //
  /**
   * Get the data for the requested resource
   * 
   * @param resourceName
   * @param resourceDataCallback
   * @param timeout
   *          Timeout in milliseconds for this command
   */
  void getResourceData(String resourceName,
          AsyncControllerCallback<ResourceDataResponse> resourceDataCallback, int timeout);
  
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
}
