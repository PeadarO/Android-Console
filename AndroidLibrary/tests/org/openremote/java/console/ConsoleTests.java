package org.openremote.java.console;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfo;
import org.openremote.entities.panel.version1.Group;
import org.openremote.entities.panel.version1.ImageWidget;
import org.openremote.entities.panel.version1.LabelWidget;
import org.openremote.entities.panel.version1.Panel;
import org.openremote.entities.panel.version1.Screen;
import org.openremote.entities.panel.version1.SliderWidget;
import org.openremote.entities.panel.version1.SwitchWidget;
import org.openremote.entities.panel.version1.TabBar;
import org.openremote.entities.panel.version1.TabBarItem;
import org.openremote.entities.panel.version1.Widget;
import org.openremote.entities.panel.version1.Navigation.SystemScreenType;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.java.console.controller.Controller;
import org.openremote.java.console.controller.ControllerConnectionStatus;

/**
 * This test class provides integration testing of the console library
 * and requires a live controller to communicate with.
 * The controller must be using the devices API see (ORCJAVA-492).
 * Set the URL of the controller in the static {@link #CONTROLLER_URL} variable.
 * 
 * These tests require a panel named test to be loaded into the controller, the panel
 * should have 2 or more screens as follows: -
 * 
 * -Screen1 (Default)
 * -Screen2
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
// TODO: This needs a controller fixture that provides static answers
public class ConsoleTests {
  abstract class WidgetChangedHandler implements PropertyChangeListener {
    final Widget widget;
    
    WidgetChangedHandler(Widget widget) {
      this.widget = widget;
    }
  }
  
  public static final String CONTROLLER_URL = "http://multimation.co.uk:8081/controller";
  private static Controller controller;

  /**
   * Setup the controller and ensure we can connect to it
   */
  @BeforeClass
  public static void connectController() {
    controller = new Controller(CONTROLLER_URL);

    controller.connect(new AsyncControllerCallback<ControllerConnectionStatus>() {
      
      @Override
      public void onSuccess(ControllerConnectionStatus result) {
        Assert.assertTrue(controller.isConnected());
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        Assert.fail();
        
      }
    }, 10000);
  }
  
  /**
   * Get all panels and iterate to find test panel
   */
  @Test
  public void GetPanelInfoTest() {
    Assert.assertNotNull(controller);
    controller.getPanelInfo(new AsyncControllerCallback<List<PanelInfo>>() {
      
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
    controller.setLoadResourceData(true);
    
    controller.getPanel("test", new AsyncControllerCallback<Panel>() {      
      @Override
      public void onSuccess(Panel result) {
        // Check group and screen counts
        List<Group> groups = result.getGroups();
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
        Assert.assertEquals(g1s1.getName(), "G1S1 - Buttons");
        Assert.assertFalse(g1s1.isLandscape());
        Assert.assertEquals(g1s1Landscape.getName(), "G1S1 - Buttons");
        Assert.assertTrue(g1s1Landscape.isLandscape());
        Assert.assertSame(g1s1.getInverseScreen(), g1s1Landscape);
        
        // Check panel tab bar
        TabBar panelTabBar = result.getTabBar();
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
        
        // Try and get tab bar image resource
//        panelTabBarItems.get(2).getImage().getResourceData(new AsyncControllerCallback<ResourceDataResponse>() {          
//          @Override
//          public void onSuccess(ResourceDataResponse result) {
//            Assert.assertEquals(result.getResponseCode(), ControllerResponseCode.OK);
//            Assert.assertNotNull(result.getData());
//            Assert.assertEquals(result.getData().length, 400);
//          }
//          
//          @Override
//          public void onFailure(ControllerResponseCode error) {
//            Assert.fail(error.getDescription());
//          }
//        });
                
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
        
      }
      @Override
      public void onFailure(ControllerResponseCode error) {
          Assert.fail(error.getDescription());
        };
      });
  }
      
  /**
   * Get panel named test from this controller and register it
   */
  @Test
  public void GetAndRegisterTestPanelTest() {
    Assert.assertNotNull(controller);
    final boolean loadData = true;
    controller.setLoadResourceData(loadData);
    
    controller.getPanel("test", new AsyncControllerCallback<Panel>() {      
      @Override
      public void onSuccess(Panel result) {
        final Panel panel = result;
        Assert.assertNotNull(result);
        
        List<Widget> widgets = result.getWidgets();
        for(Widget widget : widgets) {
          widget.addPropertyChangeListener(new WidgetChangedHandler(widget) {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              if (widget instanceof LabelWidget) {
                Assert.assertEquals("text", evt.getPropertyName());
              }
              if (widget instanceof SwitchWidget) {
                List<String> props = Arrays.asList(new String[] {"state","onImage","offImage"});
                Assert.assertTrue(props.indexOf(evt.getPropertyName()) > -1);
                // Check the data
                if (!evt.getPropertyName().equals("state")) {
                  final ResourceInfo info = (ResourceInfo)evt.getNewValue();
                  Assert.assertEquals(loadData ? true : false, info.isDataLoaded());
                  if (!loadData) {
                    // Force load it now
                    info.getData(new AsyncControllerCallback<ResourceDataResponse>() {

                      @Override
                      public void onFailure(ControllerResponseCode error) {
                        Assert.fail(error.getDescription());
                      }

                      @Override
                      public void onSuccess(ResourceDataResponse result) {
                        Assert.assertEquals(info.getName(), result.getResourceName());
                        Assert.assertNotNull(result.getData());
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
        
        controller.registerPanel(result);
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        Assert.fail(error.getDescription());
      }
    });
  }
}
