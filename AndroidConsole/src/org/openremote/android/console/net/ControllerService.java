/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2011, OpenRemote Inc.
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

package org.openremote.android.console.net;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.openremote.android.console.exceptions.AppInitializationException;
import org.openremote.android.console.exceptions.ControllerAuthenticationFailureException;
import org.openremote.android.console.exceptions.InvalidDataFromControllerException;
import org.openremote.android.console.exceptions.ORConnectionException;

/**
 * Abstraction of a controller, used to keep code that does communication with a controller
 * in one place as well as to facilitate testing that doesn't require communication over the
 * network with a real controller.
 *
 * Where possible, the responses from the controller are parsed and returned in a suitable
 * data structure.  This should make using JSON instead of XML much simpler to deal with.
 *
 * TODO declare better checked exceptions in throws clauses
 *
 * @author Andrew D. Ball <aball@osintegrators.com>
 */
public interface ControllerService
{
  /**
   * Returns a list of all of the cluster group member's URLs,
   * from the /rest/servers service.
   */
  public List<URL> getServers() throws ControllerAuthenticationFailureException,
      ORConnectionException, AppInitializationException, InvalidDataFromControllerException,
      Exception;

  /**
   * Returns an InputStream containing the contents of a particular
   * panel.
   *
   * TODO add suitable checked exceptions for other known possible problems.
   *
   * Documented error conditions from the controller API:
   *
   * error code 426: panel.xml not found
   * error code 427: invalid panel.xml
   * error code 428: no such panel identity
   *
   * @param panelName name of the panel (not percent encoded)
   *
   * @return InputStream containing panel.xml contents from controller
   */
  public InputStream getPanel(String panelName) throws ControllerAuthenticationFailureException,
      ORConnectionException, AppInitializationException, Exception;

  public InputStream getResource(String resourceName)
      throws ControllerAuthenticationFailureException, ORConnectionException,
             AppInitializationException, Exception;

  /**
   * Sends a command to a control via a controller.  This corresponds to
   * /rest/control/{control_id}/{command_param} in the XML REST API.
   *
   * @param controlId identifier for the control
   * @param command what command to send (e.g. "ON", "OFF", "3", "2.1", "click", "swipe")
   *
   * @throws ControllerAuthenticationFailureException
   * @throws ORConnectionException if failed to connect to any valid controller
   * @throws Exception if something else wrong occurred
   */
  public void sendWriteCommand(int controlId, String command)
      throws ControllerAuthenticationFailureException, ORConnectionException,
             Exception;
  
  /**
   * Identifies the API version of this controller: -
   * V1 = Pre named sensor/command API
   * V2 = Named sensor/command API
   * @return
   */
  public int getApiVersion();
}