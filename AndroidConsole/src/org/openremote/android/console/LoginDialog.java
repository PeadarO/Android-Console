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
package org.openremote.android.console;

import org.openremote.android.console.model.UserCache;
import org.openremote.android.console.model.ViewHelper;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

/**
 * The dialog is shown when http request unauthorized.
 * It is used in configure applications and send control command.
 */
public class LoginDialog extends Dialog {

   private EditText usernameText;
   private EditText passwordText;
   private Button loginButton;
   public LoginDialog(Context context) {
      super(context);
      setContentView(R.layout.login_view);
      getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
      setTitle(R.string.login);
      usernameText = (EditText)findViewById(R.id.username);
      usernameText.setText(UserCache.getUsername(context));
      passwordText = (EditText)findViewById(R.id.password);
      loginButton = (Button)findViewById(R.id.login);
      loginButton.setOnClickListener(new OnloginClickListener());
      Button cancelButton = (Button)findViewById(R.id.login_cancel);
      cancelButton.setOnClickListener(new OnCancelClickListener());
      show();
   }
   
   
   public void setOnClickListener(OnloginClickListener onloginClickListener) {
      loginButton.setOnClickListener(onloginClickListener);
   }
   
   private void checkAndSave() {
      String username = usernameText.getText().toString();
      String password = passwordText.getText().toString();
      if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
         ViewHelper.showAlertViewWithTitle(getContext(), "Warn", "No username or password entered.");
         return;
      } else {
         UserCache.saveUser(getContext(), username, password);
      }
      dismiss();
   }
   
   public class OnloginClickListener implements android.view.View.OnClickListener {
      public void onClick(View v) {
         checkAndSave();
      }
   }
   
   private final class OnCancelClickListener implements android.view.View.OnClickListener {
      public void onClick(View v) {
         dismiss();
      }
   }
}
