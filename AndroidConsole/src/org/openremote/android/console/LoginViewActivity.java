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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * The activity start when load resources occur unauthorized and navigate to login.
 */
public class LoginViewActivity extends GenericActivity {

   private EditText usernameText;
   private EditText passwordText;
   private boolean isFromMain;
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      setTitle(R.string.login);
      if (Main.LOAD_RESOURCE.equals(getIntent().getDataString())) {
         isFromMain = true;
      }
      this.setContentView(R.layout.login_view);
      usernameText = (EditText)findViewById(R.id.username);
      usernameText.setText(UserCache.getUsername(this));
      passwordText = (EditText)findViewById(R.id.password);
      Button loginButton = (Button)findViewById(R.id.login);
      loginButton.setOnClickListener(new OnloginClickListener());
      Button cancelButton = (Button)findViewById(R.id.login_cancel);
      cancelButton.setOnClickListener(new OnCancelClickListener());
   }
   
   /**
    * Inner class to save username and password.
    * 
    */
   private final class OnloginClickListener implements OnClickListener {
      public void onClick(View v) {
         String username = usernameText.getText().toString();
         String password = passwordText.getText().toString();
         if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            ViewHelper.showAlertViewWithTitle(LoginViewActivity.this, "Warn", "No username or password entered.");
            return;
         } else {
            UserCache.saveUser(LoginViewActivity.this, username, password);
         }
         
         if (isFromMain) {
            Intent intent = new Intent();
            intent.setClass(LoginViewActivity.this, Main.class);
            startActivity(intent);
         }
         finish();
      }
   }
   
   private final class OnCancelClickListener implements OnClickListener {
      public void onClick(View v) {
         finish();
      }
   }
   
}
