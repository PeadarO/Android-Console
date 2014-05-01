package org.openremote.java.console.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfo;
import org.openremote.entities.panel.version1.ImageWidget;
import org.openremote.entities.panel.version1.LabelWidget;
import org.openremote.entities.panel.version1.Panel;
import org.openremote.entities.panel.version1.SliderWidget;
import org.openremote.entities.panel.version1.SwitchWidget;
import org.openremote.entities.panel.version1.Widget;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;

// TODO: This needs a controller fixture that provides static answers
public class ControllerTest {
  abstract class WidgetChangedHandler implements PropertyChangeListener {
    final Widget widget;
    
    WidgetChangedHandler(Widget widget) {
      this.widget = widget;
    }
  }
  
  
  private static Controller controller;
  
  
  @BeforeClass
  public static void connectController() {
    try {
      controller = new Controller(new URL("http://multimation.co.uk:8081/controller"));
    } catch (Exception e) {
      // Will get here if the URL is not valid
      Assert.fail(e.getMessage());
    }
    
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
  
  @Test
  public void GetAndRegisterPanelTest() {
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
                    info.getResourceData(new AsyncControllerCallback<ResourceDataResponse>() {

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
