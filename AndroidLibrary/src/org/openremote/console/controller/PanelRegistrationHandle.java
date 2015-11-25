package org.openremote.console.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openremote.entities.panel.Panel;
import org.openremote.entities.panel.SensorLink;
import org.openremote.entities.panel.SensoryWidget;
import org.openremote.entities.panel.Widget;

public class PanelRegistrationHandle extends RegistrationHandle {
  private Panel panel;
  private List<Integer> sensorIds;
  private List<SensoryWidget> monitoredWidgets;
  
  PanelRegistrationHandle(Panel panel, AsyncRegistrationCallback callback) {
    super(callback);
    this.panel = panel;
  }

  public Panel getPanel() {
    return panel;
  }

  @Override
  public List<Integer> getSensorIds() {
    if(sensorIds == null) {
      // Get sensor and widget info
      List<Widget> widgets = panel.getWidgets();
      monitoredWidgets = new ArrayList<SensoryWidget>();
      sensorIds = new ArrayList<Integer>();
  
      // Get sensory widgets first
      if (widgets != null) {
        for (Widget widget : widgets) {
          if (widget instanceof SensoryWidget) {
            monitoredWidgets.add((SensoryWidget) widget);
            for (SensorLink link : ((SensoryWidget) widget).getSensorLinks()) {
              if (!sensorIds.contains(link.getRef())) {
                sensorIds.add(link.getRef());
              }
            }
          }
        }
      }
    }
    
    return sensorIds;
  }

  @Override
  public void onSensorsChanged(Map<Integer, String> result) {
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
}
