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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for storing data about slider.
 * 
 * @author handy 2010-05-12
 *
 */
@SuppressWarnings("serial")
public class Slider extends SensorComponent {
   
   /** Indicates whether the orientation of slider is vertical or horizontal. True means vertical, otherwise horizontal. It's optional. */
   boolean isVertical;
   
   /** 
    * Indicates whether the slider is passive or active. True means the slider can only render the real-time status, <br />
    * otherwise slider also can control the device in your home or some other places.<br />
    * <b>NOTE:</b> The slider also support one-way mode, that means if the <b>active</b> slider dones't have sonsor for polling,<br />
    * the active slider degenerates into one-way slider. So, one-way slider just can control deveice in your home or some other places.<br />
    * 
    * It's optional.
    */
   boolean isPassive;
   
   /** Minimal value of slider can slide to. */
   int minValue;
   
   /** Maximal value of slider can slide to. */
   int maxValue;
   
   /** Thumb image for slider. It's optional. */
   Image thumbImage;
   
   /** Image standed for the effect of slider slides to the minimal value. */
   Image minImage;
   
   /** Image standed for the effect of slider slides to the maximal value. */
   Image maxImage;
   
   /** Image which is between the left or bottom end and thumb of slider. */
   Image minTrackImage;
   
   /** Image which is between the right or top end and thumb of slider. */
   Image maxTrackImage;
   
   public Slider(Node node) {
      if (node == null) {
         throw new NullPointerException("The node is null");
      }
      parseAttributes(node);
      parseChildren(node);
   }

   /**
    * Parse the attributes of sliderNode 
    */
   private void parseAttributes(Node sliderNode) {
      // id
      Node idAttributeNode = sliderNode.getAttributes().getNamedItem(ID);
      String id = (idAttributeNode != null) ? idAttributeNode.getNodeValue() : null;
      setComponentId(Integer.valueOf((id != null && !"".equalsIgnoreCase(id)) ? id  : "0"));
      
      // thumbImage
      Node thumbImageAttributeNode = sliderNode.getAttributes().getNamedItem(THUMB_IMAGE);
      if (thumbImageAttributeNode != null) {
         String thumbImageName = thumbImageAttributeNode.getNodeValue();
         thumbImage = (thumbImageName != null && !"".equals(thumbImageName)) ? new Image(thumbImageName) : null;
      }
      
      // vertical
      Node verticalAttributeNode = sliderNode.getAttributes().getNamedItem(VERTICAL);
      if (verticalAttributeNode != null) {
         String isVerticalStrVal = verticalAttributeNode.getNodeValue();
         isVertical = (isVerticalStrVal != null && !"".equals(isVerticalStrVal)) ? isVerticalStrVal.equalsIgnoreCase("true") ? true : false : false ;
      }
      
      // passive
      Node passiveAttributeNode = sliderNode.getAttributes().getNamedItem(PASSIVE);
      if (passiveAttributeNode != null) {
         String isPassiveStrVal = passiveAttributeNode.getNodeValue();
         isPassive = (isPassiveStrVal != null && !"".equals(isPassiveStrVal)) ? isPassiveStrVal.equalsIgnoreCase("true") ? true : false : false ;
      }
   }
   
   /**
    * Parse the children nodes from sliderNode 
    */
   private void parseChildren(Node sliderNode) {
      NodeList childNodesOfSlider = sliderNode.getChildNodes();
      for (int i = 0; i < childNodesOfSlider.getLength(); i++) {
         if (childNodesOfSlider.item(i).getNodeType() == Node.ELEMENT_NODE) {
            String childNodeNameOfSlider = childNodesOfSlider.item(i).getNodeName();
            // link
            if (LINK.equalsIgnoreCase(childNodeNameOfSlider)) {
               super.parser(childNodesOfSlider.item(i));
            } 
            // min and max nodes
            else if (MIN_VALUE.equalsIgnoreCase(childNodeNameOfSlider) || MAX_VALUE.equalsIgnoreCase(childNodeNameOfSlider)) {
               parseMinMaxNode(childNodesOfSlider.item(i));
            }
         }
      }
   }

   /**
    * Parse the min and max nodes in slider's childNodes. 
    */
   private void parseMinMaxNode(Node minOrMaxNode) {
      // minOrMaxValue
      Node valueAttributeNode = minOrMaxNode.getAttributes().getNamedItem(VALUE);
      String valueAttributeNodeValue = valueAttributeNode != null ? valueAttributeNode.getNodeValue() : null;
      int minOrMaxValue = Integer.parseInt((valueAttributeNodeValue != null && !"".equalsIgnoreCase(valueAttributeNodeValue)) ? valueAttributeNodeValue : "0");
      
      // minOrMaxImage
      Node imageAttributeNode = minOrMaxNode.getAttributes().getNamedItem(IMAGE);
      String minOrMaxImageName = imageAttributeNode != null ? imageAttributeNode.getNodeValue() : null;
      Image minOrMaxImage  = (minOrMaxImageName != null && !"".equals(minOrMaxImageName)) ? new Image(minOrMaxImageName) : null;
      
      // minOrMaxTrackImage
      Node trackImageAttributeNode = minOrMaxNode.getAttributes().getNamedItem(TRACK_IMAGE);
      String minOrMaxTrackImageName = trackImageAttributeNode != null ? trackImageAttributeNode.getNodeValue() : null;
      Image minOrMaxTrackImage = (minOrMaxTrackImageName != null && !"".equals(minOrMaxTrackImageName)) ? new Image(minOrMaxTrackImageName) : null;
      
      if (MIN_VALUE.equalsIgnoreCase(minOrMaxNode.getNodeName())) {
         minValue = minOrMaxValue;
         minImage = minOrMaxImage;
         minTrackImage = minOrMaxTrackImage;
      } else if (MAX_VALUE.equalsIgnoreCase(minOrMaxNode.getNodeName())) {
         maxValue = minOrMaxValue;
         maxImage = minOrMaxImage;
         maxTrackImage = minOrMaxTrackImage;
      }
   }

   public boolean isVertical() {
      return isVertical;
   }

   public boolean isPassive() {
      return isPassive;
   }

   public int getMinValue() {
      return minValue;
   }

   public int getMaxValue() {
      return maxValue;
   }

   public Image getThumbImage() {
      return thumbImage;
   }

   public Image getMinImage() {
      return minImage;
   }

   public Image getMaxImage() {
      return maxImage;
   }

   public Image getMinTrackImage() {
      return minTrackImage;
   }

   public Image getMaxTrackImage() {
      return maxTrackImage;
   }

}
