package org.openremote.android.console.demo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfo;
import org.openremote.entities.panel.version1.SwitchWidget.State;
import org.openremote.entities.panel.version1.ImageWidget;
import org.openremote.entities.panel.version1.LabelWidget;
import org.openremote.entities.panel.version1.Panel;
import org.openremote.entities.panel.version1.SliderWidget;
import org.openremote.entities.panel.version1.SwitchWidget;
import org.openremote.entities.panel.version1.Widget;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerInfo;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.java.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.java.console.controller.Controller;
import org.openremote.java.console.controller.ControllerConnectionStatus;
import org.openremote.java.console.controller.service.ControllerDiscoveryService;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DemoActivity extends Activity {
  abstract class WidgetChangedHandler implements PropertyChangeListener {
    final Widget widget;
    
    WidgetChangedHandler(Widget widget) {
      this.widget = widget;
    }
  }
  
  private Controller controller;
  private AsyncControllerCallback<ControllerConnectionStatus> connectionCallback;
  private static final String CONTROLLER_URL = "http://multimation.co.uk:8081/controller";
  private TextView textView;
  private Button button;
  private ImageView image;
  Panel currentPanel;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo);    
    textView = (TextView)findViewById(R.id.TextView);
    button = (Button)findViewById(R.id.Switch);
    image = (ImageView)findViewById(R.id.Image);
    
    // Setup connection callback - This will be called anytime
    // something occurs related to the controller connection (connect, disconnect, error)
    connectionCallback = new AsyncControllerCallback<ControllerConnectionStatus>() {
      @Override
      public void onSuccess(ControllerConnectionStatus result) {
        writeLine("Connected");
        getPanelInfo();
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        // TODO: Re-act to connection failure
        writeLine("Connection Failed!");
      }
    };
    
    // Search for controllers
    searchForControllers();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.demo, menu);
    return true;
  }

  private void searchForControllers() {
    ControllerDiscoveryService.startDiscovery(new AsyncControllerDiscoveryCallback() {
      
      @Override
      public void onStartDiscoveryFailed(ControllerResponseCode arg0) {
        writeLine("Start Discovery Failed! " + arg0.getDescription());
      }
      
      @Override
      public void onDiscoveryStopped() {
        writeLine("Discovery Finished!");

        // Initialise the controller
        initController();
      }
      
      @Override
      public void onDiscoveryStarted() {
        writeLine("Start Discovery!");
        
        button.setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
              ControllerDiscoveryService.stopDiscovery();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
              // Do nothing here for switch
            }
            return true;
          }
        });
      }
      
      @Override
      public void onControllerFound(ControllerInfo controllerInfo) {
        writeLine("Controller Found!");
        writeLine("    " + controllerInfo.getUrl());
      }
    });
  }
  
  private void initController() {
    // Create controller instance using URL
    controller = new Controller(CONTROLLER_URL);
    
    // Set load resource data to true (rather than lazy loading) if caching
    // images then this should be false and then you can compare modified times
    // before retrieving the resource data
    controller.setLoadResourceData(true);

    writeLine("Connecting...");
    
    // Connect to the controller and use specified callback
    controller.connect(connectionCallback);
  }
  
  private void getPanelInfo() {
    writeLine("Getting Panel List:");
    controller.getPanelInfo(new AsyncControllerCallback<List<PanelInfo>>() {
      @Override
      public void onSuccess(List<PanelInfo> result) {
        writeLine("Success! Panel Names:");
        for (PanelInfo info : result) {
          writeLine("       " + info.getName());
        }        
        registerPanel();
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        writeLine("Failed");
      }
    });
  }
  
  private void registerPanel() {
    writeLine("Getting Panel Test:");
    controller.getPanel("test", new AsyncControllerCallback<Panel>() {      
      @Override
      public void onSuccess(Panel result) {
        final Panel panel = result;
        currentPanel = panel;
        writeLine("Panel received");
        
        List<Widget> widgets = result.getWidgets();
        for(Widget widget : widgets) {
          widget.addPropertyChangeListener(new WidgetChangedHandler(widget) {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              if (widget instanceof LabelWidget) {
                writeLine("Label Widget Changed: " + evt.getPropertyName());
              }
              if (widget instanceof SwitchWidget) {
                writeLine("Switch Widget Changed: " + evt.getPropertyName());
                
                // Update the text of the switch in the demo view to match the switch state
                // There's only one switch in this demo panel
                if (evt.getPropertyName().equals("state")) {
                  State state = (State)evt.getNewValue();
                  button.setText(state.toString());
                }
                
//                List<String> props = Arrays.asList(new String[] {"state","onImage","offImage"});
//                
//                // Check the data
//                if (!evt.getPropertyName().equals("state")) {
//                  final ResourceInfo info = (ResourceInfo)evt.getNewValue();
//                  
//                  if (!info.isDataLoaded()) {
//                    // Force load it now
//                    info.getResourceData(new AsyncControllerCallback<ResourceDataResponse>() {
//
//                      @Override
//                      public void onFailure(ControllerResponseCode error) {
//                        writeLine("Failed to get data");
//                      }
//
//                      @Override
//                      public void onSuccess(ResourceDataResponse result) {
//                        writeLine("Resource Data received: " + result.getData().length + "bytes");
//                      }
//                    });
//                  }
//                }
              }
              if (widget instanceof SliderWidget) {
                writeLine("Slider Widget Changed: " + evt.getPropertyName());
              }
              if (widget instanceof ImageWidget) {
                writeLine("Image Widget Changed: " + evt.getPropertyName());
                // Get the on image from the byte array - note how all resources are resource info
                // objects
                if (evt.getPropertyName().equals("currentImage")) {
                  ResourceInfo resource = (ResourceInfo)evt.getNewValue();
                  // Look at modified date and compare to cached image (if we have a cached image otherwise let's load the data)
                  // if cached image is up to date then use that otherwise call getResourceData - if setLoadResourceData = true
                  // then data would have automatically been populated and getResourceData will return immediately with the answer
                  resource.getResourceData(new AsyncControllerCallback<ResourceDataResponse>() {

                    @Override
                    public void onFailure(ControllerResponseCode arg0) {
                      writeLine("Failed to get image resource for switch");
                    }

                    @Override
                    public void onSuccess(ResourceDataResponse arg0) {
                      setImage(arg0);
                    }
                  }); 
                }
              }              
            }
          });
        }
        
        // Register the panel to start receiving property change notifications
        controller.registerPanel(result);
        
        wireupSwitchHandler();
      }
      
      private void wireupSwitchHandler() {
        // Find switch widget 
        if (currentPanel == null) {
          return;
        }
        
        List<SwitchWidget> switches = currentPanel.getWidgets(SwitchWidget.class);
        if (switches.size() > 0) {
          final SwitchWidget testSwitch = switches.get(0);
          
          // Listen for click on button
          button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                  testSwitch.setState(testSwitch.getState() == State.ON ? State.OFF : State.ON);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                  // Do nothing here for switch
                }
                return true;
            }
          });
        }
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        writeLine("Failed to get Panel");
      }
    });
  }
  
  private void setImage(ResourceDataResponse data) {
    Bitmap bmp = BitmapFactory.decodeByteArray(data.getData(), 0, data.getData().length);
    image.setImageBitmap(bmp);
  }
  
  private void writeLine(String text) {
    textView.append(text + "\n");
  }
}
