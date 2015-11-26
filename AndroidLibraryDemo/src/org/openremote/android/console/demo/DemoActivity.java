package org.openremote.android.console.demo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.openremote.console.controller.AsyncControllerDiscoveryCallback;
import org.openremote.console.controller.AsyncRegistrationCallback;
import org.openremote.console.controller.Controller;
import org.openremote.console.controller.ControllerConnectionStatus;
import org.openremote.console.controller.DeviceRegistrationHandle;
import org.openremote.console.controller.PanelRegistrationHandle;
import org.openremote.console.controller.service.ControllerDiscoveryService;
import org.openremote.entities.panel.ImageWidget;
import org.openremote.entities.panel.LabelWidget;
import org.openremote.entities.panel.Panel;
import org.openremote.entities.panel.PanelInfo;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfo;
import org.openremote.entities.panel.SliderWidget;
import org.openremote.entities.panel.SwitchState;
import org.openremote.entities.panel.SwitchWidget;
import org.openremote.entities.panel.Widget;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.Command;
import org.openremote.entities.controller.CommandResponse;
import org.openremote.entities.controller.ControllerInfo;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.entities.controller.Device;
import org.openremote.entities.controller.Sensor;

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
  
  private static final String CONTROLLER_URL = "http://multimation.co.uk:8081/controller";
  private Controller controller = new Controller(CONTROLLER_URL);
  private TextView textView;
  private Button button;
  private Button btnDiscovery;
  private Button btnConnect;
  private Button btnRegister;
  private Button btnDevice;
  private ImageView image;
  private Panel currentPanel;
  private PanelRegistrationHandle panelRegistration;
  private DeviceRegistrationHandle deviceRegistration;
  private Device currentDevice;
  private SwitchWidget switchWidget = null;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo);    
    textView = (TextView)findViewById(R.id.TextView);
    button = (Button)findViewById(R.id.Switch);
    image = (ImageView)findViewById(R.id.Image);
    
    
    // Wire up discovery button
    btnDiscovery = (Button)findViewById(R.id.BtnDiscovery);
    updateDiscoveryButton(false);
    
    // Wire up connect button
    btnConnect = (Button)findViewById(R.id.BtnConnect);
    updateConnectButton(false);
    
    // Wire up registration button
    btnRegister = (Button)findViewById(R.id.BtnRegister);
    updateRegistrationButton(false);
    
    // Wire up device button
    btnDevice = (Button)findViewById(R.id.BtnDevice);
    updateDeviceButton(false);
    updateDeviceSendButtons();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.demo, menu);
    return true;
  }
  
  private void doDiscovery() {
    // Start controller discovery
    ControllerDiscoveryService.startDiscovery(new AsyncControllerDiscoveryCallback() {
      
      @Override
      public void onStartDiscoveryFailed(ControllerResponseCode response) {
        writeLine("Start Discovery Failed! " + response.getDescription());
      }
      
      @Override
      public void onDiscoveryStopped() {
        writeLine("Discovery Finished!");
        updateDiscoveryButton(false);
      }
      
      @Override
      public void onDiscoveryStarted() {
        writeLine("Start Discovery!");
        updateDiscoveryButton(true);
      }
      
      @Override
      public void onControllerFound(ControllerInfo controllerInfo) {
        writeLine("Controller Found '" + controllerInfo.getUrl() + "'");
      }
    });
  }
  
  private void doConnectController() {
    writeLine("Connecting to '" + CONTROLLER_URL + "'");
    
    // Connect to the controller and use specified callback
    controller.connect(new AsyncControllerCallback<ControllerConnectionStatus>() {
      @Override
      public void onSuccess(ControllerConnectionStatus result) {
        writeLine("Connected");
        updateConnectButton(true);
        doGetPanelInfo();
        doGetDevice();
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        if (error == ControllerResponseCode.DISCONNECTED) {
          writeLine("Disconnected");
          updateConnectButton(false);
        } else {
          // TODO: Re-act to connection failure
          writeLine("Connection Failed!");
        }
      }
    });
  } 
  
  private void doGetPanelInfo() {
    if (currentPanel != null) {
      return;
    }
    
    writeLine("Getting Panel List:");
    controller.getPanelList(new AsyncControllerCallback<List<PanelInfo>>() {
      @Override
      public void onSuccess(List<PanelInfo> result) {
        writeLine("Success! Panel Names:");
        for (PanelInfo info : result) {
          writeLine("       " + info.getName());
        }        
        doGetPanel();
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        writeLine("Failed");
      }
    });
  }
  
  private void doGetPanel() {
    writeLine("Getting Panel Test:");
    controller.getPanel("test", new AsyncControllerCallback<Panel>() {      
      @Override
      public void onSuccess(final Panel result) {
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
                // of the first switch
                if (evt.getSource() == result.getWidgets(SwitchWidget.class).get(0)) {
                  if (evt.getPropertyName().equals("state")) {
                    SwitchState state = (SwitchState)evt.getNewValue();
                    button.setText(state.toString());
                  }
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
                  resource.getData(new AsyncControllerCallback<ResourceDataResponse>() {

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
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        writeLine("Failed to get Panel");
      }
    });
  }
  
  private void doGetDevice() {
    if (currentDevice != null) {
      return;
    }
    
    writeLine("Getting Device Switch Test:");
    controller.getDevice("SWITCH TEST", new AsyncControllerCallback<Device>() {
      
      @Override
      public void onSuccess(final Device result) {
        currentDevice = result;
        writeLine("Device received");
        
        List<Sensor> sensors = currentDevice.getSensors();
        if (sensors != null) {
          for (Sensor sensor : sensors) {
            sensor.addPropertyChangeListener(new PropertyChangeListener() {
              
              @Override
              public void propertyChange(PropertyChangeEvent evt) {
                writeLine("Sensor Value Changed from '" + evt.getOldValue() + "' to '" + evt.getNewValue());
              }
            });
          }
        }
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        writeLine("Failed to get Device");        
      }
    });
  }
  
  private void doRegisterPanel() {
    // Register the panel to start receiving property change notifications
    panelRegistration = controller.registerPanel(currentPanel, new AsyncRegistrationCallback() {
      
      @Override
      public void onSuccess() {
        writeLine("Panel Registration successful");
        updateRegistrationButton(true);
        updateSwitchButton(true);
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        if (error == ControllerResponseCode.UNREGISTERED) {
          writeLine("Panel Unregistered successfully!");
          panelRegistration = null;
          updateRegistrationButton(false);
          updateSwitchButton(false);
        } else {
          writeLine("Panel Registration error: " + error.getDescription());
        }
      }
    });
  }
  
  private void doRegisterDevice() {
    // Register the device to start receiving property change notifications
    deviceRegistration = controller.registerDevice(currentDevice, new AsyncRegistrationCallback() {
      
      @Override
      public void onSuccess() {
        writeLine("Device Registration successful");
        updateDeviceButton(true);
      }
      
      @Override
      public void onFailure(ControllerResponseCode error) {
        if (error == ControllerResponseCode.UNREGISTERED) {
          writeLine("Device Unregistered successfully!");
          deviceRegistration = null;
          updateDeviceButton(false);
        } else {
          writeLine("Device Registration error: " + error.getDescription());
        }
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
  
 private void updateConnectButton(boolean connected) {
   if (connected) {
     btnConnect.setText("DISCONNECT CONTROLLER");
     btnConnect.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_UP) { return true; }
         if (controller != null && controller.isConnected()) {
           btnConnect.setOnTouchListener(null); // Prevent multiple fires
           controller.disconnect();
         }          
         return true;
       }
     });
   } else {
     btnConnect.setText("CONNECT CONTROLLER");
     btnConnect.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_UP) { return true; }
         if (controller == null || !controller.isConnected()) {
           btnConnect.setOnTouchListener(null); // Prevent multiple fires
           doConnectController();
         }
         return true;
       }
     });
   }    
 }
 
 private void updateRegistrationButton(boolean registered) {
   if (registered) {
     btnRegister.setText("UNREGISTER PANEL");
     btnRegister.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_UP) { return true; }
         if (panelRegistration != null) {
           btnRegister.setOnTouchListener(null); // Prevent multiple fires
           controller.unregisterPanel(panelRegistration);
         }          
         return true;
       }
     });
   } else {
     btnRegister.setText("REGISTER PANEL");
     btnRegister.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_UP) { return true; }
         if (panelRegistration == null && currentPanel != null) {
           btnRegister.setOnTouchListener(null); // Prevent multiple fires
           doRegisterPanel();
         }          
         return true;
       }
     });
   }    
 }
 
 private void updateDeviceButton(boolean registered) {
   if (registered) {
     btnDevice.setText("UNREGISTER DEVICE");
     btnDevice.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_UP) { return true; }
         if (deviceRegistration != null) {
           btnDevice.setOnTouchListener(null); // Prevent multiple fires
           controller.unregisterDevice(deviceRegistration);
         }          
         return true;
       }
     });
   } else {
     btnDevice.setText("REGISTER DEVICE");
     btnDevice.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_UP) { return true; }
         if (deviceRegistration == null && currentDevice != null) {
           btnDevice.setOnTouchListener(null); // Prevent multiple fires
           doRegisterDevice();
         }          
         return true;
       }
     });
   }    
 }
 
 private void updateDiscoveryButton(boolean discoveryRunning) {
   if (discoveryRunning) {
     btnDiscovery.setText("STOP DISCOVERY");
     btnDiscovery.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_UP) { return true; }
         if (ControllerDiscoveryService.isDiscoveryRunning()) {
           btnDiscovery.setOnTouchListener(null); // Prevent multiple fires
           ControllerDiscoveryService.stopDiscovery();
         }
         return true;
       }
     });
   } else {
     btnDiscovery.setText("START DISCOVERY");
     btnDiscovery.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() != MotionEvent.ACTION_UP) { return true; }
         if(!ControllerDiscoveryService.isDiscoveryRunning()) {
           btnDiscovery.setOnTouchListener(null); // Prevent multiple fires
           doDiscovery();
         }
         return true;
       }
     });
   }    
 }
 
 private void updateSwitchButton(boolean panelRegistered) {
   if (panelRegistered) {
       List<SwitchWidget> switches = currentPanel.getWidgets(SwitchWidget.class);
       if (switches.size() > 0) {
         switchWidget = switches.get(0);
       }
       
       button.setText(switchWidget.getState().toString());       
       button.setOnTouchListener(new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
           if(event.getAction() == MotionEvent.ACTION_DOWN && switchWidget != null) {
             switchWidget.setState(switchWidget.getState() == SwitchState.ON ? SwitchState.OFF : SwitchState.ON);
           }
           return true;
         }
       });
   } else {
     switchWidget = null;
     button.setText("SWITCH N/A");       
     button.setOnTouchListener(null);
   }
 }
 
 private void updateDeviceSendButtons() {
   Button onBtn = (Button)findViewById(R.id.BtnDeviceOn);
   Button offBtn = (Button)findViewById(R.id.BtnDeviceOff);
   
   onBtn.setOnTouchListener(new View.OnTouchListener() {
     @Override
     public boolean onTouch(View v, MotionEvent event) {
       if(event.getAction() == MotionEvent.ACTION_DOWN && currentDevice != null) {
         Command cmd = currentDevice.findCommandByName("SWITCH ON");
         if (cmd != null) {
           currentDevice.sendCommand(cmd, new AsyncControllerCallback<CommandResponse>() {

            @Override
            public void onFailure(ControllerResponseCode error) {
              writeLine("Failed to send command '" + error.getDescription() + "'");
            }

            @Override
            public void onSuccess(CommandResponse result) {
              writeLine("Command Sent");
            }
             
           });
         }
       }
       return true;
     }
   });
   
   offBtn.setOnTouchListener(new View.OnTouchListener() {
     @Override
     public boolean onTouch(View v, MotionEvent event) {
       if(event.getAction() == MotionEvent.ACTION_DOWN && currentDevice != null) {
         Command cmd = currentDevice.findCommandByName("SWITCH OFF");
         if (cmd != null) {
           currentDevice.sendCommand(cmd, new AsyncControllerCallback<CommandResponse>() {

            @Override
            public void onFailure(ControllerResponseCode error) {
              writeLine("Failed to send command '" + error.getDescription() + "'");
            }

            @Override
            public void onSuccess(CommandResponse result) {
              writeLine("Command Sent");
            }
             
           });
         }
       }
       return true;
     }
   });
 }
}
