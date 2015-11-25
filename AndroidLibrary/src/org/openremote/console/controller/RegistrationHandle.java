package org.openremote.console.controller;

import java.util.List;
import java.util.Map;

abstract class RegistrationHandle {
  private AsyncRegistrationCallback callback;
  private boolean registered;
  
  RegistrationHandle(AsyncRegistrationCallback callback) {
    this.callback = callback;
  }

  public AsyncRegistrationCallback getCallback() {
    return callback;
  }
  
  boolean isRegistered() {
    return registered;
  }
  
  void setIsRegistered(boolean registered) {
    this.registered = registered;
  }
  
  abstract List<Integer> getSensorIds();
    
  abstract void onSensorsChanged(Map<Integer, String> sensorValues);
}
