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

package org.openremote.android.console.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts IP address in dot separated domain format to numeric long and vice versa,
 * eg. long 1344492298 is equivalent to string 80.35.83.10 .
 * 
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author handy 2010-04-27
 *
 */
public class IpUtil
{

  public static long ipStringToLong(String ip)
  {
    long[] ipSections = new long[4];

    int position1 = ip.indexOf(".");
    int position2 = ip.indexOf(".", position1 + 1);
    int position3 = ip.indexOf(".", position2 + 1);

    ipSections[0] = Long.parseLong(ip.substring(0, position1));
    ipSections[1] = Long.parseLong(ip.substring(position1 + 1, position2));
    ipSections[2] = Long.parseLong(ip.substring(position2 + 1, position3));
    ipSections[3] = Long.parseLong(ip.substring(position3 + 1));

    return (ipSections[0] << 24) + (ipSections[1] << 16) + (ipSections[2] << 8) + ipSections[3];
  }

  public static String ipLongToString(long ip)
  {
    long section1 = ip >> 24;
    long section2 = ((ip >> 16) - (section1 << 8));
    long section3 = ((ip >> 8) - (section1 << 16) - (section2 << 8));
    long section4 = (ip - (section1 << 24) - (section2 << 16) - (section3 << 8));

    return section1+"."+section2+"."+section3+"."+section4;
  }

  public static String splitIpFromURL(String url)
  {
    String regex = "^http://(\\d*.\\d*.\\d*.\\d*):?\\d*\\/.+$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(url);

    if (matcher.find())
    {
      return matcher.group(1);
    }

    return null;
	}
}
