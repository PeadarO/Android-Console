package org.openremote.console.controller;

import org.openremote.console.controller.connector.ControllerConnector;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.entities.panel.ResourceDataResponse;
import org.openremote.entities.panel.ResourceInfoDetails;
import org.openremote.entities.panel.ResourceLocator;

class ControllerResourceLocator implements ResourceLocator {
  private boolean enabled = true;
  private int timeout = Controller.DEFAULT_TIMEOUT;
  private ControllerConnector connector;
  
  @Override
  public void getResourceInfoDetails(String resourceName, final AsyncControllerCallback<ResourceInfoDetails> resourceCallback) {
    if (connector != null) {
      connector.getResourceInfoDetails(this, resourceName,
              new AsyncControllerCallback<ResourceInfoDetails>() {

                @Override
                public void onFailure(ControllerResponseCode error) {
                  if (!enabled) {
                    return;
                  }
                  resourceCallback.onFailure(error);
                }

                @Override
                public void onSuccess(ResourceInfoDetails result) {
                  if (!enabled) {
                    return;
                  }
                  resourceCallback.onSuccess(result);
                }

              }, timeout);
    }
  }

  @Override
  public void getResourceData(String resourceName,
          AsyncControllerCallback<ResourceDataResponse> resourceDataCallback) {
    if (connector != null) {
      connector.getResourceData(resourceName, resourceDataCallback, timeout);
    }
  }

  @Override
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
  
  protected void setConnector(ControllerConnector connector) {
    this.connector = connector;
  }
  
  protected void enable() {
    enabled = true;
  }
  
  protected void disable() {
    enabled = false;
  }
}
