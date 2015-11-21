package org.openremote.console.controller;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openremote.console.controller.connector.ControllerConnector;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.ControllerResponseCode;
import org.openremote.entities.panel.SensorLink;
import org.openremote.entities.panel.SensoryWidget;

class ControllerSensorMonitor implements AsyncControllerCallback<Map<Integer, String>> {
  private List<SensoryWidget> monitoredWidgets;
  private int[] sensorIds;
  private boolean enabled = true;
  private ControllerConnector connector;
  
  ControllerSensorMonitor(List<SensoryWidget> monitoredWidgets, int[] sensorIds) {
    this.monitoredWidgets = monitoredWidgets;
    this.sensorIds = sensorIds;
  }
  
  protected void setConnector(ControllerConnector connector) {
    this.connector = connector;
  }

  void start() {
    if (connector != null && enabled) {
      // Use timeout > 50s so that controller returns timeout response
      connector.monitorSensors(sensorIds, this, 55000);
    }
  }

  void enable() {
    enabled = true;
  }
  
  void disable() {
    enabled = false;
  }

  @Override
  public void onFailure(ControllerResponseCode error) {
    if (!enabled) {
      return;
    }
    // TODO Need to do something about failed polling (notify the console
    // app?)
    start();
  }

  @Override
  public void onSuccess(Map<Integer, String> result) {
    if (!enabled) {
      return;
    }

    if (result != null) {
      // Call on sensor changed for each changed sensor
      for (Entry<Integer, String> entry : result.entrySet()) {
        for (SensoryWidget widget : monitoredWidgets) {
          for (SensorLink link : widget.getSensorLinks()) {
            if (entry.getKey().equals(link.getRef())) {
              widget.onSensorValueChanged(link.getRef(), entry.getValue());
            }
          }
        }
      }
    }

    start();
  }
}
