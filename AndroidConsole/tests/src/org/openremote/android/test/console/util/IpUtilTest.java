/*
 * OpenRemote, the Home of the Digital Home.
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

package org.openremote.android.test.console.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openremote.android.console.util.IpUtil;

/**
 * Tests for {@link org.openremote.android.console.util.IpUtil} class.
 * 
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author handy 2010-04-28
 *
 */
public class IpUtilTest extends TestCase
{

  public void testIpStringToLong()
  {
    long ipLong = IpUtil.ipStringToLong("80.35.83.10");
    Assert.assertEquals(1344492298, ipLong);
  }

  public void testIpLongToString()
  {
    String ipString = IpUtil.ipLongToString(1344492298);
    Assert.assertEquals("80.35.83.10", ipString);
  }

  public void testSplitIpFromURL()
  {
    String ip1 = IpUtil.splitIpFromURL("http://192.168.100.34:8080/controller");
    Assert.assertEquals("192.168.100.34", ip1);
    String ip2 = IpUtil.splitIpFromURL("http://192.168.100.34/controller");
    Assert.assertEquals("192.168.100.34", ip2);
    String ip3 = IpUtil.splitIpFromURL("http://192.168.100.34/");
    Assert.assertEquals(null, ip3);
    String ip4 = IpUtil.splitIpFromURL("http://192.168.100.34/");
    Assert.assertEquals(null, ip4);
  }
}
