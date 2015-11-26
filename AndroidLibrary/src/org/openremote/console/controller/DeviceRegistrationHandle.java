package org.openremote.console.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openremote.entities.controller.Device;
import org.openremote.entities.controller.Sensor;

public class DeviceRegistrationHandle extends RegistrationHandle {
  private Device device;
  private List<Integer> sensorIds;

  DeviceRegistrationHandle(Device device, AsyncRegistrationCallback callback) {
    super(callback);
    this.device = device;
  }

  public Device getDevice() {
    return device;
  }

  @Override
  public void onSensorsChanged(Map<Integer, String> result) {
    for (Entry<Integer, String> entry : result.entrySet()) {
      if (device.getSensors() != null) {
        for (Sensor sensor : device.getSensors()) {
          if (sensor.getId() == entry.getKey()) {
            sensor.setValue(entry.getValue());
          }
        }
      }
    }
  }

  @Override
  List<Integer> getSensorIds() {
    if (sensorIds == null) {
      sensorIds = new ArrayList<Integer>();
      if (device.getSensors() != null) {
        for (Sensor sensor : device.getSensors()) {
          sensorIds.add(sensor.getId());
        }
      }
    }
    return sensorIds;
  }
}
