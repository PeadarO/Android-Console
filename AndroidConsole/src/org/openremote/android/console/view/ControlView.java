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
package org.openremote.android.console.view;

import java.io.InputStream;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.openremote.android.console.Constants;
import org.openremote.android.console.LoginDialog;
import org.openremote.android.console.bindings.ColorPicker;
import org.openremote.android.console.bindings.Component;
import org.openremote.android.console.bindings.ORButton;
import org.openremote.android.console.bindings.Slider;
import org.openremote.android.console.bindings.Switch;
import org.openremote.android.console.exceptions.ControllerAuthenticationFailureException;
import org.openremote.android.console.exceptions.ORConnectionException;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.ControllerException;
import org.openremote.android.console.model.ViewHelper;
import org.openremote.android.console.net.ControllerService;
import org.openremote.android.console.net.ORConnectionDelegate;
import org.openremote.android.console.net.ORHttpMethod;
import org.openremote.android.console.net.ORUnBlockConnection;
import org.xml.sax.InputSource;

import roboguice.inject.InjectorProvider;

import android.content.Context;
import android.util.Log;

/**
 * The super class of all control view, include ButtonView, SwitchView and SliderView.
 *
 * TODO Remove reference to ORConnectionDelegate, using the controllerService and a RoboAsyncTask instead.
 */
public class ControlView extends ComponentView implements ORConnectionDelegate {
   public static final String LOG_CATEGORY = Constants.LOG_CATEGORY + "ControlView";

   /** The repeat send command timer. */
   private Timer timer;

   private ControllerService controllerService;

   public ControllerService getControllerService()
   {
      return controllerService;
   }

   public void setControllerservice(ControllerService controllerService)
   {
      this.controllerService = controllerService;
   }

   protected ControlView(Context context) {
      super(context);
      injectCollaboratorsFromContext(context);
   }

   public static ControlView buildWithControl(Context context, Component control) {
      ControlView controlView = null;
      if (control instanceof ORButton) {
         controlView = new ButtonView(context, (ORButton) control);
      } else if (control instanceof Switch) {
         controlView = new SwitchView(context, (Switch) control);
      } else if (control instanceof Slider) {
         controlView = new SliderView(context, (Slider) control);
      } else if (control instanceof ColorPicker) {
         controlView = new ColorPickerView(context, (ColorPicker)control);
      }

      if (controlView != null) {
         controlView.injectCollaboratorsFromContext(context);
      }

      return controlView;
   }

   /**
    * If a context that implements InjectorProvider is provided, inject
    * instances of collaborators (currently just ControllerService).
    *
    * This is the only way that I know of to inject collaborators into a view
    * with RoboGuice. (aball)
    *
    * RoboActivity implements InjectorProvider and would be the most likely
    * Context used for instantiating a ControlView at runtime.
    *
    * If context does not implement InjectorProvider, an error message
    * will be logged.
    *
    * @param context Android Context (useful only if implements InjectorProvider)
    */
   protected void injectCollaboratorsFromContext(Context context) {
      try {
         InjectorProvider ip = (InjectorProvider) context;
         controllerService = ip.getInjector().getInstance(ControllerService.class);
      } catch (ClassCastException e) {
         Log.e(LOG_CATEGORY, "context is not a Guice InjectorProvider.  Cannot inject collaborators.");
      }
   }

   /**
    * Send command request to controller by command type.
    * 
    * @param commandType the command type
    * 
    * @return true, if successful
    */
   public boolean sendCommandRequest(String commandType) {
	   Log.i("ControlView", "sendWriteCommand");
	   
	      new ORUnBlockConnection(this.context, ORHttpMethod.POST, true, AppSettingsModel.getSecuredServer(getContext())
	              + "/rest/control/" + getComponent().getComponentId() + "/" + commandType, this);
	        return true;
	        
 /*     try {
		controllerService.sendWriteCommand(getComponent().getComponentId(), commandType);
	} catch (ControllerAuthenticationFailureException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ORConnectionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      //new ORUnBlockConnection(this.context, ORHttpMethod.POST, true, AppSettingsModel.getSecuredServer(getContext())
           // + "/rest/control/" + getComponent().getComponentId() + "/" + commandType, this);
      return true;*/
   }

   /**
    * Send command request to controller by command name
    * 
    * @param commandName the command name
    * 
    * @return true, if successful
    */
   public boolean sendCommandRequestByName(String commandName) {
     Log.i("ControlView", "sendWriteCommand");
     
        new ORUnBlockConnection(this.context, ORHttpMethod.POST, true, AppSettingsModel.getSecuredServer(getContext())
                + "/rest/commands?name=" + commandName, this);
          return true;
          
 /*     try {
    controllerService.sendWriteCommand(getComponent().getComponentId(), commandType);
  } catch (ControllerAuthenticationFailureException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } catch (ORConnectionException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } catch (Exception e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
      //new ORUnBlockConnection(this.context, ORHttpMethod.POST, true, AppSettingsModel.getSecuredServer(getContext())
           // + "/rest/control/" + getComponent().getComponentId() + "/" + commandType, this);
      return true;*/
   }
   
   /**
    * Cancel repeat send command.
    */
   protected void cancelTimer() {
      if (timer != null) {
         timer.cancel();
      }
      timer = null;
   }

   protected void setTimer(Timer timer) {
      this.timer = timer;
   }

   /**
    * Handle server error with status code.
    * If status code not equals 200, cancel the timer, and display alert with error message.
    * 
    * TODO Get rid of this.  Use a RoboAsyncTask instead.
    *
    * @param statusCode the status code
    */
   public void handleServerErrorWithStatusCode(int statusCode) {
      if (statusCode != 200) {
         cancelTimer();
         ViewHelper.showAlertViewWithTitle(getContext(), "Send Request Error", ControllerException
               .exceptionMessageOfCode(statusCode));
      }
   }

   // TODO get rid of this (use a RoboAsyncTask instead)
   @Override
   public void urlConnectionDidFailWithException(Exception e) {
      cancelTimer();
   }

   // TODO get rid of this (use a RoboAsyncTask instead)
   @Override
   public void urlConnectionDidReceiveData(InputSource data) {
      // do nothing.
   }

   // TODO get rid of this (use a RoboAsyncTask instead)
   @Override
   public void urlConnectionDidReceiveResponse(HttpResponse httpResponse) {
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      if (responseCode != 200 && responseCode != 204) {
         cancelTimer();
         if (responseCode == 401) {
            new LoginDialog(getContext());
         } else {
            handleServerErrorWithStatusCode(responseCode);
         }
      }
   }
}
