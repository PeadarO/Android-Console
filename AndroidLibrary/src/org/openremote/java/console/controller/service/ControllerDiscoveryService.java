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
package org.openremote.java.console.controller.service;

import org.openremote.java.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.java.console.controller.connector.AndroidHttpConnector;
import org.openremote.java.console.controller.connector.ControllerConnector;

/**
 * Controller discovery service for asynchronously discovering controllers within
 * the current local area network.
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
public class ControllerDiscoveryService {
  public static final int DEFAULT_SEARCH_DURATION = 5000;
  public static final int DEFAULT_TCP_PORT = 2346;
  private static final ControllerConnector connector = new AndroidHttpConnector();
  
  private ControllerDiscoveryService() {
    
  }

  public static void startDiscovery(AsyncControllerDiscoveryCallback callback) {
    startDiscovery(callback, DEFAULT_TCP_PORT, null);
  }
  
  public static void startDiscovery(AsyncControllerDiscoveryCallback callback, int tcpPort, Integer searchDuration) {
    connector.startDiscovery(callback, tcpPort, searchDuration);
  }
  
  public static void stopDiscovery() {
    connector.stopDiscovery();
  }
  
  public static boolean isDiscoveryRunning() {
    return connector.isDiscoveryRunning();
  }
}
