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
package org.openremote.android.console.bindings;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The button can send command to controller and navigate to, has default image and pressed image.
 * If it has set repeat, it would repeat send command.
 * Use "ORButton" as its name is because android has "Button" component.
 */
@SuppressWarnings("serial")
public class ORButton extends Control {

   private String name;
   private boolean hasControlCommand;
   private boolean repeat;
   
   /** The button's normal image. */
   private Image defaultImage;
   
   /** The button's pressed image. */
   private Image pressedImage;
   
   /** Navigate to. */
   private Navigate navigate;
   
   private int version = 1;
   private String pressCommandName;
   private String longPressCommandName;
   private String repeatCommandName;
   private String releaseCommandName;
   private Integer longPressDelay;
   private Integer repeatInterval;
         
   /**
    * Instantiates a new button by parse the button node.
    * 
    * @param node the node
    */
   public ORButton(Node node) {
      NamedNodeMap nodeMap = node.getAttributes();
      this.setComponentId(Integer.valueOf(nodeMap.getNamedItem(ID).getNodeValue()));
      this.name = nodeMap.getNamedItem(NAME).getNodeValue();
      if (nodeMap.getNamedItem("hasControlCommand") != null) {
         this.hasControlCommand = Boolean.valueOf(nodeMap.getNamedItem("hasControlCommand").getNodeValue());
      }
      if (nodeMap.getNamedItem("repeat") != null) {
         this.repeat = Boolean.valueOf(nodeMap.getNamedItem("repeat").getNodeValue());
      }
      
      NodeList childNodes = node.getChildNodes();
      int nodeNum = childNodes.getLength();
      for (int i = 0; i < nodeNum; i++) {
         if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
            Node elementNode = childNodes.item(i);
            if (DEFAULT.equals(elementNode.getNodeName())) {
               this.defaultImage = createImage(elementNode);
            } else if (PRESSED.equals(elementNode.getNodeName())) {
               this.pressedImage = createImage(elementNode);
            } else if (NAVIGATE.equals(elementNode.getNodeName())) {
               this.navigate = new Navigate(elementNode);
            } else if (CONFIG.equals(elementNode.getNodeName())) {
              // New style button
              NamedNodeMap btnConfig = elementNode.getAttributes();
              version = Integer.valueOf(btnConfig.getNamedItem("version").getNodeValue());
              
              if (getVersion() == 2) {
                Node pressCommandNode = btnConfig.getNamedItem("pressCommand");
                Node longPressCommandNode = btnConfig.getNamedItem("longPressCommand");
                Node repeatCommandNode = btnConfig.getNamedItem("repeatCommand");
                Node releaseCommandNode = btnConfig.getNamedItem("releaseCommand");
                Node repeatIntervalNode = btnConfig.getNamedItem("repeatInterval");
                Node longPressDelayNode = btnConfig.getNamedItem("longPressDelay");
                
                if (pressCommandNode != null) {
                  pressCommandName = pressCommandNode.getNodeValue();
                }
                
                if (releaseCommandNode != null) {
                  releaseCommandName = releaseCommandNode.getNodeValue();
                }
                
                if (longPressCommandNode != null && longPressDelayNode != null) {
                  longPressCommandName = longPressCommandNode.getNodeValue();
                  longPressDelay = Integer.valueOf(longPressDelayNode.getNodeValue());
                }
                
                if (repeatCommandNode != null && repeatIntervalNode != null) {
                  repeatCommandName = repeatCommandNode.getNodeValue();
                  repeatInterval = Integer.valueOf(repeatIntervalNode.getNodeValue());
                }
              }
           }
         }
      }
   }
   
   public String getName() {
      return name;
   }
   
   public boolean isHasControlCommand() {
      return hasControlCommand;
   }
   
   public boolean isRepeat() {
      return repeat;
   }

   public Image getDefaultImage() {
      return defaultImage;
   }

   public Image getPressedImage() {
      return pressedImage;
   }

   public Navigate getNavigate() {
      return navigate;
   }
   
  public int getVersion() {
    return version;
  }

  public String getPressCommandName() {
    return pressCommandName;
  }

  public String getLongPressCommandName() {
    return longPressCommandName;
  }

  public String getRepeatCommandName() {
    return repeatCommandName;
  }

  public String getReleaseCommandName() {
    return releaseCommandName;
  }

  public Integer getLongPressDelay() {
    return longPressDelay;
  }

  public Integer getRepeatInterval() {
    return repeatInterval;
  }

  /**
    * Creates the image by parse image node.
    * 
    * @param elementNode the element node
    * 
    * @return the image
    */
   private Image createImage(Node elementNode) {
      for (Node imageNode = elementNode.getFirstChild(); imageNode != null; imageNode = imageNode.getNextSibling()) {
         if (imageNode.getNodeType() == Node.ELEMENT_NODE && "image".equals(imageNode.getNodeName())) {
            return new Image(imageNode);
         }
      }
      return null;
   }
}
