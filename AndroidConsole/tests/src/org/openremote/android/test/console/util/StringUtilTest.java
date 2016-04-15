/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2010, OpenRemote Inc.
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

package org.openremote.android.test.console.util;

import junit.framework.TestCase;

import org.openremote.android.console.util.StringUtil;

/**
 * Tests for {@link org.openremote.android.console.util.StringUtil} class.
 *
 * @author <a href="mailto:aball@osintegrators.com">Andrew Ball</a>
 */
public class StringUtilTest extends TestCase
{

  public void testMarkControllerServerURLSelected()
  {
    assertEquals("+http://localhost:8080/controller", StringUtil.markControllerServerURLSelected("http://localhost:8080/controller"));
  }

  public void testIpLongToString()
  {
    assertNull(StringUtil.removeControllerServerURLSelected(null));
    assertEquals("http://localhost:8080/controller", StringUtil.removeControllerServerURLSelected("+http://localhost:8080/controller"));
    assertEquals("http://localhost:8080/controller", StringUtil.removeControllerServerURLSelected("http://localhost:8080/controller"));
    assertEquals("", StringUtil.removeControllerServerURLSelected("+"));
    assertEquals("", StringUtil.removeControllerServerURLSelected(""));
  }

}
