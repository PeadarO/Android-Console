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
package org.openremote.android.test.console.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.openremote.android.console.bindings.AbsoluteLayoutContainer;
import org.openremote.android.console.bindings.Gesture;
import org.openremote.android.console.bindings.GridCell;
import org.openremote.android.console.bindings.GridLayoutContainer;
import org.openremote.android.console.bindings.Group;
import org.openremote.android.console.bindings.Image;
import org.openremote.android.console.bindings.Label;
import org.openremote.android.console.bindings.LayoutContainer;
import org.openremote.android.console.bindings.Navigate;
import org.openremote.android.console.bindings.ORButton;
import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.bindings.SensorState;
import org.openremote.android.console.bindings.Slider;
import org.openremote.android.console.bindings.Switch;
import org.openremote.android.console.bindings.TabBar;
import org.openremote.android.console.bindings.TabBarItem;
import org.openremote.android.console.exceptions.AppInitializationException;
import org.openremote.android.console.model.XMLEntityDataBase;
import org.openremote.android.console.util.FileUtil;
import org.xml.sax.SAXException;

import android.content.Context;
import android.test.InstrumentationTestCase;

/**
 * The Class test to parse panel.xml.
 */
public class FileUtilTest extends InstrumentationTestCase {

   private Context ctx;

   public void setUp() {
     this.ctx = getInstrumentation().getContext();
   }
   
   /**
    * Test parse panel_grid_button.xml.
    */
   public void testParsePanelGridButtonXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_grid_button"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);
      int imageIndex = 0;
      int buttonIndex = 0;
      List<GridCell> cells = new ArrayList<GridCell>();
      List<ORButton> buttons = new ArrayList<ORButton>();

      // check buttons and images.
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof GridLayoutContainer) {
                  GridLayoutContainer grid = (GridLayoutContainer) layout;
                  Assert.assertEquals(20, grid.getLeft());
                  Assert.assertEquals(20, grid.getTop());
                  Assert.assertEquals(300, grid.getWidth());
                  Assert.assertEquals(400, grid.getHeight());

                  for (GridCell cell : grid.getCells()) {
                     cells.add(cell);
                     if (cell.getComponent() instanceof ORButton) {
                        ORButton button = (ORButton) cell.getComponent();
                        buttons.add(button);
                        Assert.assertEquals(String.valueOf((char) (65 + buttonIndex)), button.getName());
                        int expectedId = (59 + buttonIndex++);
                        Assert.assertEquals(expectedId, button.getComponentId());

                        if (button.getDefaultImage() != null) {
                           String expectedDefaultImageName = String.valueOf((char) (97 + imageIndex++)) + ".png";
                           Assert.assertEquals(expectedDefaultImageName, button.getDefaultImage().getSrc());
                        }
                        if (button.getPressedImage() != null) {
                           String expectedPressedImageName = String.valueOf((char) (97 + imageIndex++)) + ".png";
                           Assert.assertEquals(expectedPressedImageName, button.getPressedImage().getSrc());
                        }
                     }
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      Assert.assertEquals(11, cells.size());
      Assert.assertEquals(1, cells.get(0).getColspan());
      Assert.assertEquals(1, cells.get(0).getRowspan());
      Assert.assertEquals(1, cells.get(1).getRowspan());
      Assert.assertEquals(1, cells.get(2).getColspan());
      Assert.assertEquals(1, cells.get(3).getColspan());
      Assert.assertEquals(2, cells.get(4).getColspan());

      Iterator<Integer> ids = screens[0].getPollingComponentsIds().iterator();
      String pollingStatusIds = "";
      if (ids.hasNext()) {
         pollingStatusIds = ids.next().toString();
      }
      while (ids.hasNext()) {
         pollingStatusIds = pollingStatusIds + "," + ids.next();
      }
      Assert.assertEquals("", pollingStatusIds);

      Assert.assertEquals(11, buttons.size());
      Assert.assertFalse(buttons.get(0).isHasControlCommand());
      Assert.assertTrue(buttons.get(1).isHasControlCommand());
      Assert.assertNull(buttons.get(1).getNavigate());
      Assert.assertFalse(buttons.get(2).isHasControlCommand());
      Assert.assertFalse(buttons.get(3).isHasControlCommand());
      Assert.assertEquals(9, buttons.get(3).getNavigate().getToGroup());
      Assert.assertFalse(buttons.get(4).isHasControlCommand());
      Assert.assertEquals(9, buttons.get(4).getNavigate().getToGroup());
      Assert.assertEquals(10, buttons.get(4).getNavigate().getToScreen());
      Assert.assertFalse(buttons.get(5).isHasControlCommand());
      Assert.assertTrue(buttons.get(5).getNavigate().isPreviousScreen());
      Assert.assertFalse(buttons.get(6).isHasControlCommand());
      Assert.assertTrue(buttons.get(6).getNavigate().isNextScreen());
      Assert.assertTrue(buttons.get(7).getNavigate().isSetting());
      Assert.assertTrue(buttons.get(8).getNavigate().isBack());
      Assert.assertTrue(buttons.get(9).getNavigate().isLogin());
      Assert.assertTrue(buttons.get(10).getNavigate().isLogout());

      XMLEntityDataBase.labels.clear();
      XMLEntityDataBase.imageSet.clear();
   }

   /**
    * Test parse panel_grid_switch.xml.
    */
   public void testParsePanelGridSwitchXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_grid_switch"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int stateIndex = 0;
      int switchIndex = 0;
      int stateValueIndex = 0;
      List<GridCell> cells = new ArrayList<GridCell>();
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof GridLayoutContainer) {
                  GridLayoutContainer grid = (GridLayoutContainer) layout;
                  Assert.assertEquals(20, grid.getLeft());
                  Assert.assertEquals(20, grid.getTop());
                  Assert.assertEquals(300, grid.getWidth());
                  Assert.assertEquals(400, grid.getHeight());

                  for (GridCell cell : grid.getCells()) {
                     cells.add(cell);
                     if (cell.getComponent() instanceof Switch) {
                        Switch theSwitch = (Switch) cell.getComponent();
                        int expectedId = (59 + switchIndex++);
                        Assert.assertEquals(expectedId, theSwitch.getComponentId());
                        String expectedOnImageName = String.valueOf((char) (97 + stateIndex++)) + ".png";
                        Assert.assertEquals(expectedOnImageName, theSwitch.getOnImage().getSrc());
                        String expectedOffImageName = String.valueOf((char) (97 + stateIndex++)) + ".png";
                        Assert.assertEquals(expectedOffImageName, theSwitch.getOffImage().getSrc());

                        // check sensor
                        int stateSize = theSwitch.getSensor().getStates().size();
                        for (int i = 0; i < stateSize; i++) {
                           String expectedStateName = null;
                           String expectedStateValue = String.valueOf((char) (97 + stateValueIndex++)) + ".png";
                           if (i % 2 == 0) {
                              expectedStateName = "on";
                           } else {
                              expectedStateName = "off";
                           }
                           SensorState sensorState = theSwitch.getSensor().getStates().get(i);
                           Assert.assertEquals(expectedStateName, sensorState.getName());
                           Assert.assertEquals(expectedStateValue, sensorState.getValue());
                        }
                     }
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      Assert.assertEquals(5, cells.size());
      Assert.assertEquals(1, cells.get(0).getColspan());
      Assert.assertEquals(1, cells.get(0).getRowspan());
      Assert.assertEquals(1, cells.get(1).getRowspan());
      Assert.assertEquals(1, cells.get(2).getColspan());
      Assert.assertEquals(1, cells.get(3).getColspan());
      Assert.assertEquals(2, cells.get(4).getColspan());

      Set<Integer> ids = screens[0].getPollingComponentsIds();
      Assert.assertEquals(4, ids.size());
      Assert.assertTrue(ids.contains(59));
      Assert.assertTrue(ids.contains(60));
      Assert.assertTrue(ids.contains(61));
      Assert.assertTrue(ids.contains(62));
   }

   /**
    * Test parse panel_absolute_switch.xml.
    */
   public void testParsePanelAbsoluteSwitchXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_absolute_switch"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int stateIndex = 0;
      int switchIndex = 0;
      int stateValueIndex = 0;
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof AbsoluteLayoutContainer) {
                  AbsoluteLayoutContainer absolute = (AbsoluteLayoutContainer) layout;
                  Assert.assertEquals(20, absolute.getLeft());
                  Assert.assertEquals(320, absolute.getTop());
                  Assert.assertEquals(100, absolute.getWidth());
                  Assert.assertEquals(100, absolute.getHeight());

                  if (absolute.getComponent() instanceof Switch) {
                     Switch theSwitch = (Switch) absolute.getComponent();
                     int expectedId = (59 + switchIndex++);
                     Assert.assertEquals(expectedId, theSwitch.getComponentId());
                     String expectedOnImageName = String.valueOf((char) (97 + stateIndex++)) + ".png";
                     Assert.assertEquals(expectedOnImageName, theSwitch.getOnImage().getSrc());
                     String expectedOffImageName = String.valueOf((char) (97 + stateIndex++)) + ".png";
                     Assert.assertEquals(expectedOffImageName, theSwitch.getOffImage().getSrc());

                     // check sensor
                     int stateSize = theSwitch.getSensor().getStates().size();
                     for (int i = 0; i < stateSize; i++) {
                        String expectedStateName = null;
                        String expectedStateValue = String.valueOf((char) (97 + stateValueIndex++)) + ".png";
                        if (i % 2 == 0) {
                           expectedStateName = "on";
                        } else {
                           expectedStateName = "off";
                        }
                        SensorState sensorState = theSwitch.getSensor().getStates().get(i);
                        Assert.assertEquals(expectedStateName, sensorState.getName());
                        Assert.assertEquals(expectedStateValue, sensorState.getValue());
                     }
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      Iterator<Integer> ids = screens[0].getPollingComponentsIds().iterator();
      String pollingStatusIds = "";
      if (ids.hasNext()) {
         pollingStatusIds = ids.next().toString();
      }
      while (ids.hasNext()) {
         pollingStatusIds = pollingStatusIds + "," + ids.next();
      }
      Assert.assertEquals("59,60", pollingStatusIds);
   }

   /**
    * Test parse panel_grid_slider.xml.
    */
   public void testParsePanelGridSliderXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_grid_slider"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int sliderIndex = 0;
      List<GridCell> cells = new ArrayList<GridCell>();
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof GridLayoutContainer) {
                  GridLayoutContainer grid = (GridLayoutContainer) layout;
                  Assert.assertEquals(20, grid.getLeft());
                  Assert.assertEquals(20, grid.getTop());
                  Assert.assertEquals(300, grid.getWidth());
                  Assert.assertEquals(400, grid.getHeight());

                  for (GridCell cell : grid.getCells()) {
                     cells.add(cell);
                     if (cell.getComponent() instanceof Slider) {
                        Slider slider = (Slider) cell.getComponent();
                        int expectedId = (59 + sliderIndex);
                        Assert.assertEquals(expectedId, slider.getComponentId());
                        Assert.assertEquals(100, slider.getMaxValue());
                        Assert.assertEquals(0, slider.getMinValue());

                        Assert.assertEquals("thumbImage.png", slider.getThumbImage().getSrc());
                        Assert.assertEquals("mute.png", slider.getMinImage().getSrc());
                        Assert.assertEquals("red.png", slider.getMinTrackImage().getSrc());
                        Assert.assertEquals("loud.png", slider.getMaxImage().getSrc());
                        Assert.assertEquals("green.png", slider.getMaxTrackImage().getSrc());

                        boolean expectedVertical = false;
                        boolean expectedPassive = false;
                        if (sliderIndex % 2 == 0) {
                           expectedVertical = true;
                           expectedPassive = true;
                        }
                        Assert.assertEquals(expectedVertical, slider.isVertical());
                        Assert.assertEquals(expectedPassive, slider.isPassive());
                        sliderIndex++;
                     }
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      Assert.assertEquals(5, cells.size());
      Assert.assertEquals(1, cells.get(0).getColspan());
      Assert.assertEquals(1, cells.get(0).getRowspan());
      Assert.assertEquals(1, cells.get(1).getRowspan());
      Assert.assertEquals(1, cells.get(2).getColspan());
      Assert.assertEquals(1, cells.get(3).getColspan());
      Assert.assertEquals(2, cells.get(4).getColspan());

      Set<Integer> ids = screens[0].getPollingComponentsIds();
      Assert.assertEquals(4, ids.size());
      Assert.assertTrue(ids.contains(59));
      Assert.assertTrue(ids.contains(60));
      Assert.assertTrue(ids.contains(61));
      Assert.assertTrue(ids.contains(62));

   }

   /**
    * Test parse panel_absolute_slider.xml.
    */
   public void testParsePanelAbsoluteSliderXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_absolute_slider"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int sliderIndex = 0;
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof AbsoluteLayoutContainer) {
                  AbsoluteLayoutContainer absolute = (AbsoluteLayoutContainer) layout;
                  Assert.assertEquals(20, absolute.getLeft());
                  Assert.assertEquals(320, absolute.getTop());
                  Assert.assertEquals(100, absolute.getWidth());
                  Assert.assertEquals(100, absolute.getHeight());

                  if (absolute.getComponent() instanceof Slider) {
                     Slider slider = (Slider) absolute.getComponent();
                     int expectedId = (59 + sliderIndex);
                     Assert.assertEquals(expectedId, slider.getComponentId());
                     Assert.assertEquals(100, slider.getMaxValue());
                     Assert.assertEquals(0, slider.getMinValue());

                     Assert.assertEquals("thumbImage.png", slider.getThumbImage().getSrc());
                     Assert.assertEquals("mute.png", slider.getMinImage().getSrc());
                     Assert.assertEquals("red.png", slider.getMinTrackImage().getSrc());
                     Assert.assertEquals("loud.png", slider.getMaxImage().getSrc());
                     Assert.assertEquals("green.png", slider.getMaxTrackImage().getSrc());

                     boolean expectedVertical = false;
                     boolean expectedPassive = false;
                     if (sliderIndex % 2 == 0) {
                        expectedVertical = true;
                        expectedPassive = true;
                     }
                     Assert.assertEquals(expectedVertical, slider.isVertical());
                     Assert.assertEquals(expectedPassive, slider.isPassive());
                     sliderIndex++;
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      Iterator<Integer> ids = screens[0].getPollingComponentsIds().iterator();
      String pollingStatusIds = "";
      if (ids.hasNext()) {
         pollingStatusIds = ids.next().toString();
      }
      while (ids.hasNext()) {
         pollingStatusIds = pollingStatusIds + "," + ids.next();
      }
      Assert.assertEquals("59,60", pollingStatusIds);
   }

   /**
    * Test parse panel_grid_label.xml.
    */
   public void testParsePanelGridLabelXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_grid_label"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int labelIndex = 0;
      List<GridCell> cells = new ArrayList<GridCell>();
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof GridLayoutContainer) {
                  GridLayoutContainer grid = (GridLayoutContainer) layout;
                  Assert.assertEquals(20, grid.getLeft());
                  Assert.assertEquals(20, grid.getTop());
                  Assert.assertEquals(300, grid.getWidth());
                  Assert.assertEquals(400, grid.getHeight());

                  for (GridCell cell : grid.getCells()) {
                     cells.add(cell);
                     if (cell.getComponent() instanceof Label) {
                        Label label = (Label) cell.getComponent();

                        // check attributes
                        int expectedId = (59 + labelIndex);
                        int expectedFontSize = 14;
                        String expectedString = String.valueOf((char) (65 + labelIndex));
                        String expectedColor = "#" + expectedString + expectedString + expectedString + expectedString
                              + expectedString + expectedString;
                        String expectedText = expectedString + "Waiting";

                        Assert.assertEquals(expectedId, label.getComponentId());
                        Assert.assertEquals(expectedFontSize, label.getFontSize());
                        Assert.assertEquals(expectedColor, label.getColor());
                        Assert.assertEquals(expectedText, label.getText());

                        // check sensor
                        int stateSize = label.getSensor().getStates().size();
                        for (int i = 0; i < stateSize; i++) {
                           String expectedStateName = null;
                           String expectedStateValue = null;
                           if (i % 2 == 0) {
                              expectedStateName = "on";
                              expectedStateValue = "LAMP_ON";
                           } else {
                              expectedStateName = "off";
                              expectedStateValue = "LAMP_OFF";
                           }
                           SensorState sensorState = label.getSensor().getStates().get(i);
                           Assert.assertEquals(expectedStateName, sensorState.getName());
                           Assert.assertEquals(expectedStateValue, sensorState.getValue());
                        }

                        labelIndex++;
                     }
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      Assert.assertEquals(5, cells.size());
      Assert.assertEquals(1, cells.get(0).getColspan());
      Assert.assertEquals(1, cells.get(0).getRowspan());
      Assert.assertEquals(1, cells.get(1).getRowspan());
      Assert.assertEquals(1, cells.get(2).getColspan());
      Assert.assertEquals(1, cells.get(3).getColspan());
      Assert.assertEquals(2, cells.get(4).getColspan());

      XMLEntityDataBase.labels.clear();
      XMLEntityDataBase.imageSet.clear();
   }

   /**
    * Test parse panel_absolute_label.xml.
    */
   public void testParsePanelAbsoluteLabelXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_absolute_label"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int labelIndex = 0;
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof AbsoluteLayoutContainer) {
                  AbsoluteLayoutContainer absolute = (AbsoluteLayoutContainer) layout;
                  Assert.assertEquals(20, absolute.getLeft());
                  Assert.assertEquals(320, absolute.getTop());
                  Assert.assertEquals(100, absolute.getWidth());
                  Assert.assertEquals(100, absolute.getHeight());

                  if (absolute.getComponent() instanceof Label) {
                     Label label = (Label) absolute.getComponent();

                     // check attributes
                     int expectedId = (59 + labelIndex);
                     int expectedFontSize = 14;
                     String expectedString = String.valueOf((char) (65 + labelIndex));
                     String expectedColor = "#" + expectedString + expectedString + expectedString + expectedString
                           + expectedString + expectedString;
                     String expectedText = expectedString + "Waiting";

                     Assert.assertEquals(expectedId, label.getComponentId());
                     Assert.assertEquals(expectedFontSize, label.getFontSize());
                     Assert.assertEquals(expectedColor, label.getColor());
                     Assert.assertEquals(expectedText, label.getText());

                     // check sensor
                     int stateSize = label.getSensor().getStates().size();
                     for (int i = 0; i < stateSize; i++) {
                        String expectedStateName = null;
                        String expectedStateValue = null;
                        if (i % 2 == 0) {
                           expectedStateName = "on";
                           expectedStateValue = "LAMP_ON";
                        } else {
                           expectedStateName = "off";
                           expectedStateValue = "LAMP_OFF";
                        }
                        SensorState sensorState = label.getSensor().getStates().get(i);
                        Assert.assertEquals(expectedStateName, sensorState.getName());
                        Assert.assertEquals(expectedStateValue, sensorState.getValue());
                     }

                     labelIndex++;
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      XMLEntityDataBase.labels.clear();
      XMLEntityDataBase.imageSet.clear();
   }

   /**
    * Test parse panel_grid_image.xml.
    */
   public void testParsePanelGridImageXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_grid_image"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int imageIndex = 0;
      int stateIndex = 0;
      List<GridCell> cells = new ArrayList<GridCell>();
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof GridLayoutContainer) {
                  GridLayoutContainer grid = (GridLayoutContainer) layout;
                  Assert.assertEquals(20, grid.getLeft());
                  Assert.assertEquals(20, grid.getTop());
                  Assert.assertEquals(300, grid.getWidth());
                  Assert.assertEquals(400, grid.getHeight());

                  for (GridCell cell : grid.getCells()) {
                     cells.add(cell);
                     if (cell.getComponent() instanceof Image) {
                        Image image = (Image) cell.getComponent();
                        int expectedId = (59 + imageIndex++);
                        String expectedImageName = String.valueOf((char) (97 + stateIndex++)) + ".png";
                        Assert.assertEquals(expectedId, image.getComponentId());
                        Assert.assertEquals(expectedImageName, image.getSrc());

                        // check sensor
                        int stateSize = image.getSensor().getStates().size();
                        for (int i = 0; i < stateSize; i++) {
                           String expectedStateName = null;
                           String expectedStateValue = null;
                           if (i % 2 == 0) {
                              expectedStateName = "on";
                              expectedStateValue = "on.png";
                           } else {
                              expectedStateName = "off";
                              expectedStateValue = "off.png";
                           }
                           SensorState sensorState = image.getSensor().getStates().get(i);
                           Assert.assertEquals(expectedStateName, sensorState.getName());
                           Assert.assertEquals(expectedStateValue, sensorState.getValue());
                        }

                        // check include label
                        image.setLinkedLabel();
                        Label includeLabel = image.getLabel();
                        Assert.assertEquals(64, includeLabel.getComponentId());
                        Assert.assertEquals(1001, includeLabel.getSensor().getSensorId());

                        // check include label's sensor
                        int labelStateSize = includeLabel.getSensor().getStates().size();
                        for (int i = 0; i < labelStateSize; i++) {
                           String expectedStateName = null;
                           String expectedStateValue = null;
                           if (i % 2 == 0) {
                              expectedStateName = "on";
                              expectedStateValue = "LAMP_ON";
                           } else {
                              expectedStateName = "off";
                              expectedStateValue = "LAMP_OFF";
                           }
                           SensorState sensorState = includeLabel.getSensor().getStates().get(i);
                           Assert.assertEquals(expectedStateName, sensorState.getName());
                           Assert.assertEquals(expectedStateValue, sensorState.getValue());
                        }
                     }
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      XMLEntityDataBase.labels.clear();
      XMLEntityDataBase.imageSet.clear();
   }

   /**
    * Test parse panel_absolute_image.xml.
    */
   public void testParsePanelAbsoluteImageXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_absolute_image"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int imageIndex = 0;
      int stateIndex = 0;
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof AbsoluteLayoutContainer) {
                  AbsoluteLayoutContainer absolute = (AbsoluteLayoutContainer) layout;
                  Assert.assertEquals(20, absolute.getLeft());
                  Assert.assertEquals(320, absolute.getTop());
                  Assert.assertEquals(100, absolute.getWidth());
                  Assert.assertEquals(100, absolute.getHeight());

                  if (absolute.getComponent() instanceof Image) {
                     Image image = (Image) absolute.getComponent();

                     // check attributes
                     int expectedId = (59 + imageIndex++);
                     String expectedImageName = String.valueOf((char) (97 + stateIndex++)) + ".png";
                     Assert.assertEquals(expectedId, image.getComponentId());
                     Assert.assertEquals(expectedImageName, image.getSrc());

                     // check sensor
                     int stateSize = image.getSensor().getStates().size();
                     for (int i = 0; i < stateSize; i++) {
                        String expectedStateName = null;
                        String expectedStateValue = null;
                        if (i % 2 == 0) {
                           expectedStateName = "on";
                           expectedStateValue = "on.png";
                        } else {
                           expectedStateName = "off";
                           expectedStateValue = "off.png";
                        }
                        SensorState sensorState = image.getSensor().getStates().get(i);
                        Assert.assertEquals(expectedStateName, sensorState.getName());
                        Assert.assertEquals(expectedStateValue, sensorState.getValue());
                     }

                     // check include label
                     image.setLinkedLabel();
                     Label includeLabel = image.getLabel();
                     Assert.assertEquals(62, includeLabel.getComponentId());
                     Assert.assertEquals(1001, includeLabel.getSensor().getSensorId());

                     // check include label's sensor
                     int labelStateSize = includeLabel.getSensor().getStates().size();
                     for (int i = 0; i < labelStateSize; i++) {
                        String expectedStateName = null;
                        String expectedStateValue = null;
                        if (i % 2 == 0) {
                           expectedStateName = "on";
                           expectedStateValue = "LAMP_ON";
                        } else {
                           expectedStateName = "off";
                           expectedStateValue = "LAMP_OFF";
                        }
                        SensorState sensorState = includeLabel.getSensor().getStates().get(i);
                        Assert.assertEquals(expectedStateName, sensorState.getName());
                        Assert.assertEquals(expectedStateValue, sensorState.getValue());
                     }
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

      XMLEntityDataBase.labels.clear();
      XMLEntityDataBase.imageSet.clear();
   }

   /**
    * Test parse panel_absolute_screen_backgroundimage.xml.
    */
   public void testParsePanelAbsoluteScreenBackgroundimageXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_absolute_screen_backgroundimage"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int backgroundIndex = 1;
      int imageIndex = 0;
      int stateIndex = 0;

      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            Assert.assertTrue(screen.getBackground().isBackgroundImageAbsolutePosition());
            Assert.assertEquals(100 * backgroundIndex, screen.getBackground().getBackgroundImageAbsolutePositionLeft());
            Assert.assertEquals(100 * backgroundIndex, screen.getBackground().getBackgroundImageAbsolutePositionTop());
            Assert.assertFalse(screen.getBackground().isFillScreen());
            Assert.assertEquals("basement" + backgroundIndex + ".png", screen.getBackground().getBackgroundImage()
                  .getSrc());
            backgroundIndex++;

            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof AbsoluteLayoutContainer) {
                  AbsoluteLayoutContainer absolute = (AbsoluteLayoutContainer) layout;
                  Assert.assertEquals(20, absolute.getLeft());
                  Assert.assertEquals(320, absolute.getTop());
                  Assert.assertEquals(100, absolute.getWidth());
                  Assert.assertEquals(100, absolute.getHeight());

                  if (absolute.getComponent() instanceof Image) {
                     Image image = (Image) absolute.getComponent();
                     int expectedId = (59 + imageIndex++);
                     Assert.assertEquals(expectedId, image.getComponentId());
                     String expectedOnImageName = String.valueOf((char) (97 + stateIndex++)) + ".png";
                     Assert.assertEquals(expectedOnImageName, image.getSrc());
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

   }

   /**
    * Test parse panel_relative_screen_backgroundimage.xml.
    */
   public void testParsePanelRelativeScreenBackgroundimageXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_relative_screen_backgroundimage"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int backgroundIndex = 1;
      int imageIndex = 0;
      int stateIndex = 0;

      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            Assert.assertFalse(screen.getBackground().isBackgroundImageAbsolutePosition());

            String expectedBackgroundImageRelativePosition = "";
            if (backgroundIndex % 2 != 0) {
               expectedBackgroundImageRelativePosition = "left";
            } else {
               expectedBackgroundImageRelativePosition = "right";
            }
            Assert.assertEquals(expectedBackgroundImageRelativePosition, screen.getBackground()
                  .getBackgroundImageRelativePosition());
            Assert.assertFalse(screen.getBackground().isFillScreen());
            Assert.assertEquals("basement" + backgroundIndex + ".png", screen.getBackground().getBackgroundImage()
                  .getSrc());
            backgroundIndex++;

            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof AbsoluteLayoutContainer) {
                  AbsoluteLayoutContainer absolute = (AbsoluteLayoutContainer) layout;
                  Assert.assertEquals(20, absolute.getLeft());
                  Assert.assertEquals(320, absolute.getTop());
                  Assert.assertEquals(100, absolute.getWidth());
                  Assert.assertEquals(100, absolute.getHeight());

                  if (absolute.getComponent() instanceof Image) {
                     Image image = (Image) absolute.getComponent();
                     int expectedId = (59 + imageIndex++);
                     Assert.assertEquals(expectedId, image.getComponentId());
                     String expectedOnImageName = String.valueOf((char) (97 + stateIndex++)) + ".png";
                     Assert.assertEquals(expectedOnImageName, image.getSrc());
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }
   }

   /**
    * Test parse panel_absolute_slider_gesture.xml.
    */
   public void testParsePanelAbsoluteSliderGestureXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_absolute_slider_gesture"));

      Group[] groups = new Group[2];
      XMLEntityDataBase.groups.values().toArray(groups);
      Screen[] screens = new Screen[2];
      XMLEntityDataBase.screens.values().toArray(screens);

      int sliderIndex = 0;
      int gestureIndex = 0;
      
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            Assert.assertEquals(4, screen.getGestures().size());
            
            for (Gesture gesture : screen.getGestures()) {
               Assert.assertEquals(gestureIndex % 4,gesture.getSwipeType());
               Assert.assertTrue(gesture.isHasControlCommand());
               
               switch (gestureIndex % 4) {
                  case 0:
                     Assert.assertEquals(19, gesture.getNavigate().getToScreen());
                     break;
                  case 1:
                     Assert.assertEquals(19, gesture.getNavigate().getToGroup());
                     break;
                  case 2:
                     Assert.assertTrue(gesture.getNavigate().isSetting());
                     break;
                  case 3:
                     Assert.assertNull(gesture.getNavigate());
                     break;
                  default:
                     break;
               }
               
               gestureIndex++;
               
            }
            for (LayoutContainer layout : screen.getLayouts()) {
               if (layout instanceof AbsoluteLayoutContainer) {
                  AbsoluteLayoutContainer absolute = (AbsoluteLayoutContainer) layout;
                  Assert.assertEquals(20, absolute.getLeft());
                  Assert.assertEquals(320, absolute.getTop());
                  Assert.assertEquals(100, absolute.getWidth());
                  Assert.assertEquals(100, absolute.getHeight());

                  if (absolute.getComponent() instanceof Slider) {
                     Slider slider = (Slider) absolute.getComponent();
                     int expectedId = (59 + sliderIndex++);
                     Assert.assertEquals(expectedId, slider.getComponentId());
                     Assert.assertEquals(100, slider.getMaxValue());
                     Assert.assertEquals(0, slider.getMinValue());
                  }
               }
            }
         }
      }

      String[] groupNames = { "All rooms", "living room" };
      String[] screenNames = { "basement", "floor" };
      // check groups
      for (int i = 0; i < groupNames.length; i++) {
         Assert.assertEquals(groupNames[i], groups[i].getName());
         Assert.assertEquals(i + 1, groups[i].getGroupId());
      }
      // check screens.
      for (int i = 0; i < screenNames.length; i++) {
         Assert.assertEquals(screenNames[i], screens[i].getName());
         Assert.assertEquals(i + 5, screens[i].getScreenId());
      }

   }
   
   /**
    * Test parse panel_global_tabbar.xml.
    */
   public void testParsePanelGlobalTabbarXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_global_tabbar"));

      TabBar globalTabBar = XMLEntityDataBase.globalTabBar;
      String[] expectedTabBarItemsName = {"previous", "next", "setting"};
      String[] expectedTabBarItemsImageSrc = {"previous.png", "next.png", "setting.png"};
      List<TabBarItem> tabBarItems = globalTabBar.getTabBarItems();
      
      for (int i = 0; i < tabBarItems.size(); i++) {
         TabBarItem tabBarItem = tabBarItems.get(i);
         
         Assert.assertEquals(expectedTabBarItemsName[i], tabBarItem.getName());
         
         // check tabbar item navigate
         Navigate navigate = tabBarItem.getNavigate();
         if (i % 3 == 0) {
            Assert.assertTrue(navigate.isPreviousScreen());
         } else if (i % 3 == 1) {
            Assert.assertTrue(navigate.isNextScreen());
         } else if (i % 3 == 2) {
            Assert.assertTrue(navigate.isSetting());
         }
         
         // check tabbar item image
         Assert.assertEquals(expectedTabBarItemsImageSrc[i], tabBarItem.getImage().getSrc());
      }
   }
   
   /**
    * Test parse panel_local_tabbar.xml.
    */
   public void testParsePanelLocalTabbarXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_local_tabbar"));

      Collection<Group> groups = XMLEntityDataBase.groups.values();

      for (Group group : groups) {
         TabBar localTabBar = group.getTabBar();
         if (localTabBar == null) {
            return;
         }
         String[] expectedTabBarItemsName = {"previous", "next", "setting"};
         String[] expectedTabBarItemsImageSrc = {"previous.png", "next.png", "setting.png"};
         List<TabBarItem> tabBarItems = localTabBar.getTabBarItems();
         
         for (int i = 0; i < tabBarItems.size(); i++) {
            TabBarItem tabBarItem = tabBarItems.get(i);
            
            Assert.assertEquals(expectedTabBarItemsName[i], tabBarItem.getName());
            
            // check tabbar item navigate
            Navigate navigate = tabBarItem.getNavigate();
            if (i % 3 == 0) {
               Assert.assertTrue(navigate.isPreviousScreen());
            } else if (i % 3 == 1) {
               Assert.assertTrue(navigate.isNextScreen());
            } else if (i % 3 == 2) {
               Assert.assertTrue(navigate.isSetting());
            }
            
            // check tabbar item image
            Assert.assertEquals(expectedTabBarItemsImageSrc[i], tabBarItem.getImage().getSrc());
         }
      }
   }
   
   /**
    * Test parse panel_portrait_landscape.xml .
    */
   public void testParsePanelPortraitLandscapeXML() throws IOException, SAXException,
         AppInitializationException {
      FileUtil.parsePanelXMLInputStream(readFile("panel_portrait_landscape"));
      
      Collection<Group> groups = XMLEntityDataBase.groups.values();
      
      int screenIndex = 0;
      for (Group group : groups) {
         for (Screen screen : group.getScreens()) {
            Assert.assertEquals("Starting Screen", screen.getName());
            if (screenIndex == 0) {
               Assert.assertEquals(1, screen.getScreenId());
               Assert.assertFalse(screen.isLandscape());
               Assert.assertEquals(0, screen.getInverseScreenId());
            } else if (screenIndex == 1) {
               Assert.assertEquals(21, screen.getScreenId());
               Assert.assertFalse(screen.isLandscape());
               Assert.assertEquals(23, screen.getInverseScreenId());
            } else if (screenIndex == 2) {
               Assert.assertEquals(23, screen.getScreenId());
               Assert.assertTrue(screen.isLandscape());
               Assert.assertEquals(21, screen.getInverseScreenId());
            }
            screenIndex++;
         }
         Assert.assertEquals(2, group.getPortraitScreens().size());
         Assert.assertEquals(1, group.getLandscapeScreens().size());
      }
   }
   
   /**
    * Read fixture file, the file is end with ".xml".
    * 
    * @param fileName the file name
    * 
    * @return the input stream
    */
   private InputStream readFile(String fileName) {
      try {
         return ctx.getAssets().open("fixture/" + fileName + ".xml");
      } catch (IOException e) {
         fail("Can't read file: " + "assets/fixture/" + fileName + ".xml");
         return null;
      }
   }
}
