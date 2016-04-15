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

package org.openremote.android.test;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Some generic utility functions for use within the JUnit tests.
 *
 * @author Andrew D. Ball <aball@osintegrators.com>
 */
public class TestUtils
{
  /**
   * Parse a string of XML, causing the test to fail if exceptions are thrown.
   *
   * @param xmlText String containing XML to parse
   *
   * @return the root element of the parsed XML's DOM tree
   */
  public static Element parseXml(String xmlText)
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try
    {
      builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
      Assert.fail("failed to initialize XML parser");
    }

    InputSource in = new InputSource();
    in.setCharacterStream(new StringReader(xmlText));

    Document doc = null;
    try
    {
      doc = builder.parse(in);
    }
    catch (SAXException e)
    {
      Assert.fail("parse error");
    }
    catch (IOException e)
    {
      Assert.fail("IOException while parsing XML");
    }

    return doc.getDocumentElement();
  }
}