/* OpenRemote, the Home of the Digital Home.
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.ResponseHandlerInterface;

/**
 * This is a custom implementation of the Loopj AsyncHttpClient to add
 * capability for controller discovery (this re-uses the existing Loopj
 * infrastructure).
 * 
 * Android Asynchronous Http Client Copyright (c) 2011 James Smith
 * <james@loopj.com> http://loopj.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * 
 */
class CustomAsyncHttpClient extends AsyncHttpClient {
  private ExecutorService threadPool;
  private ControllerDiscoveryServer discoveryServer;

  public CustomAsyncHttpClient() {
    threadPool = Executors.newFixedThreadPool(3);
  }

  /**
   * Starts controller discovery
   * 
   * @param tcpPort
   * @param discoveryDuration
   * @param responseHandler
   * @return
   */
  public void startDiscovery(int tcpPort, Integer discoveryDuration,
          ResponseHandlerInterface responseHandler) {
    if (discoveryServer != null)
      return;

    discoveryServer = new ControllerDiscoveryServer(tcpPort, discoveryDuration, responseHandler);
    threadPool.submit(discoveryServer);
  }

  /**
   * Force stop controller discovery if it is running
   * 
   * @param responseHandler
   * @return
   */
  public void stopDiscovery() {
    if (discoveryServer != null) {
      discoveryServer.cancel();
    }
    discoveryServer = null;
  }

  public boolean isDiscoveryRunning() {
    return discoveryServer != null;
  }
}
