package org.openremote.console.controller;

import org.openremote.entities.controller.ControllerResponseCode;

public interface AsyncRegistrationCallback {
  void onFailure(ControllerResponseCode error);
  
  void onSuccess();
}
