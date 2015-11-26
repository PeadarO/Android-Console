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
package org.openremote.console.controller.service;

import org.openremote.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.console.controller.connector.AndroidHttpConnector;
import org.openremote.console.controller.connector.ControllerConnector;

/**
 * Controller discovery service for asynchronously discovering controllers
 * within the current local area network.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * 
 */
public class ControllerDiscoveryService {
  public static final int DEFAULT_SEARCH_DURATION = 5000;
  public static final int DEFAULT_TCP_PORT = 2346;
  private static Class<?> connectorClazz = AndroidHttpConnector.class;
  private static ControllerConnector connector;
  // private static final ControllerConnector connector = new
  // AndroidHttpConnector();

  static {
    try {
      connector = (ControllerConnector) connectorClazz.newInstance();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private ControllerDiscoveryService() {

  }

  /**
   * Start Controller Discovery and use the specified callback for reporting
   * progress. Discovery will continue until {@link #stopDiscovery} is called.
   * 
   * @param callback
   */
  public static void startDiscovery(AsyncControllerDiscoveryCallback callback) {
    startDiscovery(callback, DEFAULT_TCP_PORT, null);
  }

  /**
   * Start Controller Discovery and use the specified callback for reporting
   * progress. Discovery will run for the specified number of milliseconds
   * 
   * @param callback
   * @param searchDuration
   */
  public static void startDiscovery(AsyncControllerDiscoveryCallback callback, int searchDuration) {
    startDiscovery(callback, DEFAULT_TCP_PORT, new Integer(searchDuration));
  }

  /**
   * Start Controller Discovery and use the specified callback for reporting
   * progress. Discovery will run for the specified number of milliseconds on
   * the specified TCP port.
   * 
   * @param callback
   * @param searchDuration
   */
  public static void startDiscovery(AsyncControllerDiscoveryCallback callback, int searchDuration,
          int tcpPort) {
    startDiscovery(callback, tcpPort, new Integer(searchDuration));
  }

  private static void startDiscovery(AsyncControllerDiscoveryCallback callback, int tcpPort,
          Integer searchDuration) {
    connector.startDiscovery(callback, tcpPort, searchDuration);
  }

  /**
   * Stop the currently running discovery service. The
   * {@link org.openremote.console.controller.AsyncControllerDiscoveryCallback#onDiscoveryStopped}
   * method of the callback supplied when discovery started will be called.
   */
  public static void stopDiscovery() {
    connector.stopDiscovery();
  }

  /**
   * Check if discovery is currently running.
   * 
   * @return is controller discovery currently running
   */
  public static boolean isDiscoveryRunning() {
    return connector.isDiscoveryRunning();
  }
}
