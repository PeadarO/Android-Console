package org.openremote.java.console.controller;

import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerInfo;
import org.openremote.entities.controller.ControllerResponseCode;

public abstract class AsyncControllerDiscoveryCallback implements AsyncControllerCallback<ControllerInfo> {
  public abstract void onDiscoveryStarted();
  public abstract void onDiscoveryStopped();
  public abstract void onControllerFound(ControllerInfo controllerInfo);
  public abstract void onStartDiscoveryFailed(ControllerResponseCode error);
  
  @Override
  public void onSuccess(ControllerInfo result) {
    onControllerFound(result);
  }
  
  @Override
  public void onFailure(ControllerResponseCode error) {
    onStartDiscoveryFailed(error);
  }
}
