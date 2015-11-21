package org.openremote.console.controller;

import org.openremote.console.controller.connector.ControllerConnector;
import org.openremote.entities.controller.AsyncControllerCallback;
import org.openremote.entities.controller.Command;
import org.openremote.entities.controller.CommandResponse;
import org.openremote.entities.controller.CommandSender;
import org.openremote.entities.controller.ControlCommand;
import org.openremote.entities.controller.ControlCommandResponse;
import org.openremote.entities.controller.ControllerResponseCode;

class ControllerCommandSender implements CommandSender {
  private boolean enabled = true;
  private int timeout = Controller.DEFAULT_TIMEOUT;
  private ControllerConnector connector;


  @Override
  public void sendControlCommand(ControlCommand command,
          final AsyncControllerCallback<ControlCommandResponse> callback) {
    if (connector != null) {
      connector.sendControlCommand(command,
              new AsyncControllerCallback<ControlCommandResponse>() {

                @Override
                public void onFailure(ControllerResponseCode error) {
                  if (!enabled) {
                    return;
                  }
                  callback.onFailure(error);
                }

                @Override
                public void onSuccess(ControlCommandResponse result) {
                  if (!enabled) {
                    return;
                  }
                  callback.onSuccess(result);
                }

              }, timeout);
    }
  }
  
  @Override
  public void sendCommand(Command command, String parameter,
          final AsyncControllerCallback<CommandResponse> callback) {
    if (connector != null) {
      connector.sendCommand(command, parameter,
              new AsyncControllerCallback<CommandResponse>() {

                @Override
                public void onFailure(ControllerResponseCode error) {
                  if (!enabled) {
                    return;
                  }
                  callback.onFailure(error);
                }

                @Override
                public void onSuccess(CommandResponse result) {
                  if (!enabled) {
                    return;
                  }
                  callback.onSuccess(result);
                }

              }, timeout);
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
};
