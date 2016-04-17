package org.openremote.java.console;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openremote.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.console.controller.AsyncRegistrationCallback;
import org.openremote.console.controller.Controller;
import org.openremote.console.controller.ControllerConnectionStatus;
import org.openremote.console.controller.DeviceRegistrationHandle;
import org.openremote.console.controller.PanelRegistrationHandle;
import org.openremote.console.controller.connector.ControllerConnector;
import org.openremote.console.controller.connector.SingleThreadHttpConnector;
import org.openremote.console.controller.service.ControllerDiscoveryService;
import org.openremote.entities.panel.AbsoluteLayout;
import org.openremote.entities.panel.ButtonWidget;
import org.openremote.entities.panel.ColorPickerWidget;
import org.openremote.entities.panel.GridLayout;
import org.openremote.entities.panel.Group;
import org.openremote.entities.panel.ImageWidget;
import org.openremote.entities.panel.LabelWidget;
import org.openremote.entities.panel.Panel;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfoDetails;
import org.openremote.entities.panel.Screen;
import org.openremote.entities.panel.SliderWidget;
import org.openremote.entities.panel.SwitchState;
import org.openremote.entities.panel.SwitchWidget;
import org.openremote.entities.panel.TabBar;
import org.openremote.entities.panel.TabBarItem;
import org.openremote.entities.panel.Widget;
import org.openremote.entities.panel.Navigation.SystemScreenType;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.Command;
import org.openremote.entities.controller.CommandResponse;
import org.openremote.entities.controller.ControlCommandResponse;
import org.openremote.entities.controller.ControllerInfo;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.entities.controller.Device;
import org.openremote.entities.controller.DeviceInfo;
import org.openremote.entities.controller.Sensor;
import org.openremote.entities.controller.SensorType;

import android.test.UiThreadTest;
import android.view.MotionEvent;
import android.view.View;

/**
 * This test class provides integration testing of the console library
 * and requires a live controller to communicate with.
 * The controller must be using the devices API see (ORCJAVA-492).
 * Set the URL of the controller in the static {@link #CONTROLLER_URL} variable.
 * 
 * These tests require a predefined test panel to be loaded into the controller, see
 * included openremote.zip.
 * 
 * Panel and commands are defined in rich.test pro designer account. Contact the
 * author for access to this designer account.
 *  
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
public class ConsoleTests {
  class Tuple<T,U> {
    private T t;
    private U u;
    public Tuple(T t, U u) {
      this.t = t;
      this.u = u;
    }
    
    public T getItem1() {
      return t;
    }
    
    public U getItem2() {
      return u;
    }
  }
  abstract class WidgetChangedHandler implements PropertyChangeListener {
    final Widget widget;
    
    WidgetChangedHandler(Widget widget) {
      this.widget = widget;
    }
  }
  
  public static final String CONTROLLER_URL = "http://multimation.co.uk:8081/controller";
  private static Controller controller;
  private PanelRegistrationHandle panelRegistration = null;
  private DeviceRegistrationHandle deviceRegistration = null;
  
   
  /**
   * Setup the controller and ensure we can connect to it
   */
  @BeforeClass
  public static void connectController() {
    Controller.Builder builder = new Controller.Builder(CONTROLLER_URL);
    controller = builder.setConnector(new SingleThreadHttpConnector()).build();

    controller.connect(new AsyncControllerCallback<ControllerConnectionStatus>() {
      
      @Override
      public void onSuccess(ControllerConnectionStatus result) {
        Assert.assertTrue(controller.isConnected());
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        Assert.fail();
        
      }
    });
  }
  
  @Test
  public void controllerDiscoveryTest() {
    // Start controller discovery
    ControllerDiscoveryService.startDiscovery(new AsyncControllerDiscoveryCallback() {
      
      @Override
      public void onStartDiscoveryFailed(ControllerResponseCode arg0) {
        System.out.println("Start Discovery Failed! " + arg0.getDescription());
      }
      
      @Override
      public void onDiscoveryStopped() {
        System.out.println("Discovery Finished!");
      }
      
      @Override
      public void onDiscoveryStarted() {
        System.out.println("Start Discovery!");
      }
      
      @Override
      public void onControllerFound(ControllerInfo controllerInfo) {
        System.out.println("Controller Found!");
        System.out.println("    URL: " + controllerInfo.getUrl());
        System.out.println("    NAME: " + controllerInfo.getName());
        System.out.println("    VERSION: " + controllerInfo.getVersion());
        System.out.println("    IDENTITY: " + controllerInfo.getIdentity());
        
        ControllerDiscoveryService.stopDiscovery();
      }
    });
  }
  
  /**
   * Get all panels and iterate to find test panel
   */
  @Test
  public void GetPanelInfoTest() {
    Assert.assertNotNull(controller);
    controller.getPanelList(new AsyncControllerCallback<List<PanelInfo>>() {
      
      @Override
      public void onSuccess(List<PanelInfo> result) {
        Assert.assertNotNull(result);
        
        // Find test panel
        boolean found = false;
        for (PanelInfo info : result) {
          found = info.getName().equalsIgnoreCase("test");
          if (found) {
            break;
          }
        }
        Assert.assertTrue(found);
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        Assert.fail(error.getDescription());
      }
    });
  }  
  
  /**
   * Verify the groups and screen structure for the test panel
   * -G1
   * --G1S1 - Buttons
   * --G1S2 - Switches
   * --G1S3 - Labels
   * --G1S4 - Images
   * --G1S5 - Sliders
   * -G2 (has tab bar with scope of group)
   * --G2S1 - ColorPicker
   * --G2S2 - Mixed
   */
  @Test
  public void groupsAndScreensTest()
  {
    Assert.assertNotNull(controller);

    controller.getPanel("test", new AsyncControllerCallback<Panel>() {      
      @Override
      public void onSuccess(Panel panel) {
        // Check group and screen counts
        List<Group> groups = panel.getGroups();
        Assert.assertEquals(groups.size(), 2);        
        Group g1 = groups.get(0);
        Assert.assertEquals(g1.getName(), "G1");
        Group g2 = groups.get(1);
        Assert.assertEquals(g2.getName(), "G2");
        List<Screen> g1Screens = g1.getScreens();
        Assert.assertEquals(g1Screens.size(), 6);
        List<Screen> g2Screens = g2.getScreens();
        Assert.assertEquals(g2Screens.size(), 2);

        // Check default screen and landscape screen
        Screen g1s1 = g1.getScreens().get(0);
        Screen g1s1Landscape = g1.getScreens().get(1);
        Screen g1s2 = g1.getScreens().get(2);
        Assert.assertEquals(g1s1.getName(), "G1S1 - Buttons");
        Assert.assertFalse(g1s1.isLandscape());
        Assert.assertEquals(g1s1Landscape.getName(), "G1S1 - Buttons");
        Assert.assertTrue(g1s1Landscape.isLandscape());
        Assert.assertSame(g1s1.getInverseScreen(), g1s1Landscape);
        
        // Check panel tab bar
        TabBar panelTabBar = panel.getTabBar();
        List<TabBarItem> panelTabBarItems = panelTabBar.getItems();
        Assert.assertEquals(panelTabBarItems.size(), 3);
        Assert.assertEquals(panelTabBarItems.get(0).getName(), "TI1 - G1S1L");
        Assert.assertSame(panelTabBarItems.get(0).getNavigation().getToGroup(), g1);
        Assert.assertSame(panelTabBarItems.get(0).getNavigation().getToScreen(), g1s1Landscape);
        Assert.assertNull(panelTabBarItems.get(0).getImage());
        
        Assert.assertEquals(panelTabBarItems.get(1).getName(), "TI2 - NavG2");
        Assert.assertSame(panelTabBarItems.get(1).getNavigation().getToGroup(), g2);
        Assert.assertNull(panelTabBarItems.get(1).getNavigation().getToScreen());
        Assert.assertNotNull(panelTabBarItems.get(1).getImage());
        Assert.assertEquals(panelTabBarItems.get(1).getImage().getName(), "home.png");
        
        Assert.assertEquals(panelTabBarItems.get(2).getName(), "TI3 - ActLogout");
        Assert.assertNull(panelTabBarItems.get(2).getNavigation().getToGroup());
        Assert.assertNull(panelTabBarItems.get(2).getNavigation().getToScreen());
        Assert.assertEquals(panelTabBarItems.get(2).getNavigation().getTo(),SystemScreenType.LOGOUT);
        Assert.assertNotNull(panelTabBarItems.get(2).getImage());
        Assert.assertEquals(panelTabBarItems.get(2).getImage().getName(), "OpenRemote.Logo.30x32.png");
                
        // Check G1 doesn't have tab bar
        Assert.assertNull(g1.getTabBar());
        
        // Check G2 tab bar
        TabBar g2TabBar = g2.getTabBar();
        List<TabBarItem> g2TabBarItems = g2TabBar.getItems();
        Assert.assertEquals(g2TabBarItems.size(), 1); 
        TabBarItem g2TabBarItem1 = g2TabBarItems.get(0);
        Assert.assertEquals(g2TabBarItem1.getName(), "TI1 - NavG1S1");
        Assert.assertNull(g2TabBarItem1.getImage());
        Assert.assertSame(g2TabBarItem1.getNavigation().getToGroup(),g1);
        Assert.assertSame(g2TabBarItem1.getNavigation().getToScreen(),g1.getScreens().get(0));
        
        // Check Screen Contents 
        for (int i=0; i<panel.getScreens().size(); i++) {
          Screen s = panel.getScreens().get(i);
          
          // Should be 1 1x2 grid layout on every screen with a widget
          // in each cell - grid should be in a fixed location and size
          Assert.assertEquals(s.getGridLayouts().size(), 1);
          GridLayout grid = s.getGridLayouts().get(0);
          Assert.assertEquals(grid.getRows(), 2);
          Assert.assertEquals(grid.getColumns(), 1);
          Assert.assertEquals(grid.getCells().size(), 2);
          Assert.assertEquals(grid.getHeight(), 200);
          Assert.assertEquals(grid.getWidth(), 200);
          Assert.assertEquals(grid.getLeft(), s.isLandscape() ? 140 : 60);
          Assert.assertEquals(grid.getTop(), s.isLandscape() ? 75 : 220);
          Assert.assertNotNull(grid.getCells().get(0).getWidget());
          Assert.assertNotNull(grid.getCells().get(1).getWidget());
          
          // Check absolute layouts and widgets for each screen
          if (s.getName().equals("G1S1 - Buttons") && !s.isLandscape()) {
            Assert.assertEquals(s.getAbsoluteLayouts().size(), 7);
            final List<Tuple<Integer,Integer>> positions = new ArrayList<Tuple<Integer,Integer>>() {{
              add(new Tuple<Integer, Integer>(50, 20));
              add(new Tuple<Integer, Integer>(100, 20));
              add(new Tuple<Integer, Integer>(150, 20));
              add(new Tuple<Integer, Integer>(200, 20));
              add(new Tuple<Integer, Integer>(50, 100));
              add(new Tuple<Integer, Integer>(100, 100));
              add(new Tuple<Integer, Integer>(150, 100));
            }};
            for (AbsoluteLayout abs : s.getAbsoluteLayouts()) {
              Assert.assertEquals(abs.getWidth(), 50);
              Assert.assertEquals(abs.getHeight(), 50);
              Tuple<Integer,Integer> match = null;
              for (Tuple<Integer,Integer> position : positions) {
                if (position.getItem1() == abs.getLeft() && position.getItem2() == abs.getTop()) {
                  match = position;
                  break;
                }
              }
              positions.remove(match);
              
              // Test a selection of the buttons on this screen
              ButtonWidget btn = (ButtonWidget)abs.getWidget();
              if(btn.getName().equals("B1 - Images")) {
                Assert.assertTrue(btn.hasControlCommand());
                Assert.assertNotNull(btn.getDefaultImage());
                Assert.assertEquals("home.png", btn.getDefaultImage().getName());
                Assert.assertNotNull(btn.getPressedImage());
                Assert.assertEquals("OpenRemote.Logo.30x32.png", btn.getPressedImage().getName());
              } else if (btn.getName().equals("B2 - Repeat")) {
                Assert.assertTrue(btn.isRepeater());
              } else if (btn.getName().equals("B3 - NavG1S2")) {
                Assert.assertFalse(btn.hasControlCommand());
                Assert.assertNotNull(btn.getNavigation());
                Assert.assertSame(btn.getNavigation().getToGroup(), g1);
                Assert.assertSame(btn.getNavigation().getToScreen(), g1s2);
              } else if (btn.getName().equals("B6 - ActLogin")) {
                Assert.assertFalse(btn.hasControlCommand());
                Assert.assertNotNull(btn.getNavigation());
                Assert.assertSame(SystemScreenType.LOGIN, btn.getNavigation().getTo());
              } else if (btn.getName().equals("B7 - ActNext")) {
                Assert.assertFalse(btn.hasControlCommand());
                Assert.assertNotNull(btn.getNavigation());
                Assert.assertSame(SystemScreenType.NEXT, btn.getNavigation().getTo());
              }
            }
            
            // Ensure all positions are accounted for
            Assert.assertEquals(positions.size(), 0);
            continue;
          }
          
          if (s.getName().equals("G1S2 - Switches")) {
            Assert.assertEquals(s.getAbsoluteLayouts().size(), 1);
            AbsoluteLayout abs = s.getAbsoluteLayouts().get(0);
            Assert.assertEquals(abs.getWidth(), 170);
            Assert.assertEquals(abs.getHeight(), 50);
            Assert.assertEquals(abs.getLeft(), 75);
            Assert.assertEquals(abs.getTop(), 25);
                          
            // Test a selection of the widgets on this screen
            SwitchWidget sw = (SwitchWidget)abs.getWidget();
            Assert.assertNotNull(sw.getOnImage());
            Assert.assertNotNull(sw.getOffImage());
            Assert.assertEquals("play.png", sw.getOnImage().getName());
            Assert.assertEquals("pause.png", sw.getOffImage().getName());
            sw = (SwitchWidget)s.getGridLayouts().get(0).getCells().get(0).getWidget();
            Assert.assertNull(sw.getOnImage());
            Assert.assertNull(sw.getOffImage());

            continue;
          }
          
          if (s.getName().equals("G1S3 - Labels")) {
            Assert.assertEquals(s.getAbsoluteLayouts().size(), 1);
            AbsoluteLayout abs = s.getAbsoluteLayouts().get(0);
            Assert.assertEquals(abs.getWidth(), 280);
            Assert.assertEquals(abs.getHeight(), 80);
            Assert.assertEquals(abs.getLeft(), 20);
            Assert.assertEquals(abs.getTop(), 20);
                          
            // Test a selection of the widgets on this screen
            LabelWidget lbl = (LabelWidget)abs.getWidget();
            Assert.assertTrue("Label Text".equals(lbl.getText()));

            continue;
          }
          
          if (s.getName().equals("G1S4 - Images")) {
            Assert.assertEquals(s.getAbsoluteLayouts().size(), 1);
            AbsoluteLayout abs = s.getAbsoluteLayouts().get(0);
            Assert.assertEquals(abs.getWidth(), 150);
            Assert.assertEquals(abs.getHeight(), 150);
            Assert.assertEquals(abs.getLeft(), 85);
            Assert.assertEquals(abs.getTop(), 10);
                          
            // Test a selection of the widgets on this screen
            ImageWidget img = (ImageWidget)abs.getWidget();
            Assert.assertNotNull(img.getCurrentImage());
            Assert.assertEquals(3, img.getResources().size());

            continue;
          }
          
          if (s.getName().equals("G1S5 - Sliders")) {
            Assert.assertEquals(s.getAbsoluteLayouts().size(), 2);
            for (AbsoluteLayout abs : s.getAbsoluteLayouts()) {
              SliderWidget slider = (SliderWidget)abs.getWidget();
              
              if (abs.getTop() == 10) {
                Assert.assertFalse(slider.isVertical());
                Assert.assertEquals(20, abs.getLeft());
                Assert.assertEquals(280, abs.getWidth());
                Assert.assertEquals(40, abs.getHeight());
                Assert.assertNull(slider.getMinImage());
                Assert.assertNull(slider.getMaxImage());
              } else if (abs.getTop() == 55) {
                Assert.assertTrue(slider.isVertical());
                Assert.assertEquals(135, abs.getLeft());
                Assert.assertEquals(50, abs.getWidth());
                Assert.assertEquals(150, abs.getHeight());
                Assert.assertNotNull(slider.getMinImage());
                Assert.assertNotNull(slider.getMaxImage());
                Assert.assertNotNull(slider.getMinTrackImage());
                Assert.assertNotNull(slider.getMaxTrackImage());
                Assert.assertNotNull(slider.getThumbImage());
              } else {
                Assert.fail("Slider top position didn't match expected value of 10 or 55");
              }
            }
                          
            continue;
          }
          
          if (s.getName().equals("G2S1 - ColorPicker")) {
            Assert.assertEquals(s.getAbsoluteLayouts().size(), 1);
            AbsoluteLayout abs = s.getAbsoluteLayouts().get(0);
            Assert.assertEquals(abs.getWidth(), 200);
            Assert.assertEquals(abs.getHeight(), 200);
            Assert.assertEquals(abs.getLeft(), 60);
            Assert.assertEquals(abs.getTop(), 20);
                          
            // Test a selection of the widgets on this screen
            ColorPickerWidget cPicker = (ColorPickerWidget)abs.getWidget();
            Assert.assertNotNull(cPicker);
            Assert.assertNotNull(cPicker.getImage());
            Assert.assertEquals("color.wheel.220X223.png", cPicker.getImage().getName());
            continue;
          }
        }
      }
      @Override
      public void onFailure(ControllerResponseCode error) {
          Assert.fail(error.getDescription());
        };
      });
  }

  /**
   * Get list of devices and check the commands and sensors of each
   */
  @Test
  public void getDevicesApiTest() {
    Assert.assertNotNull(controller);
    
    controller.getDeviceList(new AsyncControllerCallback<List<DeviceInfo>>() {
      
      @Override
      public void onSuccess(List<DeviceInfo> deviceList) {
        List<String> deviceNames = Arrays.asList(new String[] {
          "SWITCH TEST",
          "BUTTON TEST",
          "SLIDER TEST",
          "COLOR TEST"
        });
        Assert.assertNotNull(deviceList);
        Assert.assertEquals(deviceNames.size(), deviceList.size());
        
        for(DeviceInfo deviceInfo : deviceList) {
          Assert.assertNotNull(deviceInfo);
          Assert.assertTrue(deviceNames.indexOf(deviceInfo.getName()) >= 0);
          
          if (deviceInfo.getName().equals("SWITCH TEST")) {
            controller.getDevice(deviceInfo.getName(), new AsyncControllerCallback<Device>() {
              
              @Override
              public void onSuccess(final Device device) {
                Assert.assertNotNull(device.getCommands());
                Assert.assertNotNull(device.getSensors());
                Assert.assertEquals(6, device.getCommands().size());
                Assert.assertEquals(2, device.getSensors().size());
                
                for (Command command : device.getCommands()) {
                  Assert.assertNotNull(command);
                  Assert.assertTrue(command.getName().indexOf("SWITCH ") == 0);
                  Assert.assertEquals("virtual", command.getProtocol());
                }
                
                for (Sensor sensor : device.getSensors()) {
                  Assert.assertNotNull(sensor);
                  Assert.assertTrue(sensor.getName().indexOf("SWITCH") == 0);
                  Assert.assertEquals(SensorType.SWITCH, sensor.getType());
                }
                
                final Command offCommand = device.findCommandByName("SWITCH OFF");
                final Command onCommand = device.findCommandByName("SWITCH ON");
                Sensor switchSensor = device.findSensorByName("SWITCH1");
                Assert.assertNotNull(onCommand);
                Assert.assertNotNull(offCommand);
                Assert.assertNotNull(switchSensor);
                
                switchSensor.addPropertyChangeListener(new PropertyChangeListener() {
                  
                  @Override
                  public void propertyChange(PropertyChangeEvent evt) {
                    System.out.println("Sensor Value Changed from '" + evt.getOldValue() + "' to '" + evt.getNewValue());
                    Assert.assertEquals("value", evt.getPropertyName());
                    SwitchState state = (SwitchState)evt.getNewValue();
                    Command sendCommand = null;
                    
                    switch (state) {
                    case OFF:
                      sendCommand = onCommand;
                      break;
                    case ON:
                      sendCommand = offCommand;
                      break;
                    default:
                      Assert.fail("Unknown Switch State " + state);
                      break;
                    
                    }
                    
                    device.sendCommand(sendCommand, new AsyncControllerCallback<CommandResponse>() {
                      
                      @Override
                      public void onSuccess(CommandResponse result) {
                        // TODO Auto-generated method stub
                        
                      }
                      
                      @Override
                      public void onFailure(ControllerResponseCode error) {
                        // TODO Auto-generated method stub
                        
                      }
                    });
                  }
                });
                
                deviceRegistration = controller.registerDevice(device, new AsyncRegistrationCallback() {
                  
                  @Override
                  public void onSuccess() {
                    System.out.println("Device Registered Successfully");
                  }
                  
                  @Override
                  public void onFailure(ControllerResponseCode error) {
                    Assert.fail(error.getDescription());
                  }
                });
              }
              
              @Override
              public void onFailure(ControllerResponseCode error) {
                Assert.fail(error.getDescription());
              }
            });
            
            continue;
          }
          
        }
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        Assert.fail(error.getDescription());
      }
    });
  }
  
  /**
   * Get panel named test from this controller and register it.
   * Then try and load image resources.
   * Requires someone to change sensor values on the panel so that
   * change events are fired on the widgets.
   * 
   * This test runs indefinitely - you can stop the test by going
   * to screen G2S2 of the test panel on a console and toggling the
   * switch. 
   * 
   */
  @Test
  @UiThreadTest
  public void registerPanelTest() {
    Assert.assertNotNull(controller);
    
    controller.getPanel("test", new AsyncControllerCallback<Panel>() {      
      @Override
      public void onSuccess(final Panel panel) {
        Assert.assertNotNull(panel);
        List<Widget> widgets = panel.getWidgets();
        for(Widget widget : widgets) {
          widget.addPropertyChangeListener(new WidgetChangedHandler(widget) {
            int toggleCount = 0;
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              
              System.out.println("PROPERTY CHANGE: '" + evt.getPropertyName() + "' property changed on '" + evt.getSource() + "' from '" +evt.getOldValue() + "' to '" + evt.getNewValue() + "'");
              
              // Check event matches what is expected for each widget type
              if (widget instanceof LabelWidget) {
                Assert.assertEquals("text", evt.getPropertyName());
              }
              if (widget instanceof SwitchWidget) {
                // Check if the state of the special test stop switch has been triggered
                final SwitchWidget sw = (SwitchWidget)widget;
                if ("testend.1.png".equals(sw.getOnImageName()) && evt.getPropertyName().equals("state")) {
                  if (toggleCount > 0) {
                    controller.unregisterPanel(panelRegistration);
                  }
                  toggleCount++;
                  return;
                }
                
                List<String> props = Arrays.asList(new String[] {"state","onImage","offImage"});
                Assert.assertTrue(props.indexOf(evt.getPropertyName()) > -1);

                if (evt.getPropertyName().equals("state")) {
                  if (sw.getOnImage() != null && sw.getOnImageName().equals("play.png")) {
                    // Get modified date
                    sw.getOnImage().getDetails(new AsyncControllerCallback<ResourceInfoDetails>() {
                      
                      @Override
                      public void onSuccess(ResourceInfoDetails details) {
                        Assert.assertEquals("image/png", details.getContentType());
                        Assert.assertNotNull(details.getModifiedTime());
                        
                        // Load image data
                        sw.getOnImage().getData(new AsyncControllerCallback<ResourceDataResponse>() {
                          @Override
                          public void onSuccess(ResourceDataResponse result) {
                            Assert.assertEquals(sw.getOnImageName(), result.getResourceName());
                            Assert.assertNotNull(result.getData());
                          }
                          
                          @Override
                          public void onFailure(ControllerResponseCode error) {
                            Assert.fail(error.getDescription());
                          }
                        }); 
                      }
                      
                      @Override
                      public void onFailure(ControllerResponseCode error) {
                       Assert.fail(error.getDescription()); 
                      }
                    });
                  }
                }
              }
              if (widget instanceof SliderWidget) {
                List<String> props = Arrays.asList(new String[] {"value","minImage","maxImage","minTrackImage","maxTrackImage"});
                Assert.assertTrue(props.indexOf(evt.getPropertyName()) > -1);
              }
              if (widget instanceof ImageWidget) {
                Assert.assertEquals("currentImage", evt.getPropertyName());
              }              
            }
          });
        }
        
        panelRegistration = controller.registerPanel(panel, new AsyncRegistrationCallback() {
          
          @Override
          public void onSuccess() {
            System.out.println("Panel Registered Successfully");
          }
          
          @Override
          public void onFailure(ControllerResponseCode error) {
            Assert.fail(error.getDescription());
          }
        });
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        Assert.fail(error.getDescription());
      }
    });
  }
}
