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
package org.openremote.android.console.model;

import org.openremote.android.console.AppSettingsActivity;
import org.openremote.android.console.GenericActivity;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

/**
 * This is responsible for show alert message dialog.
 * 
 * @author tomsky, handy 2010-05-10
 * @author Dan Cong
 *
 */
public class ViewHelper {

   /**
    * Shows a alert view with yes/no buttons. 'no' button only dismiss this alert.
    * 
    * @param context
    *           Context instance.
    * @param title
    *           title
    * @param message
    *           msg
    * @param yesClickListener
    *           listener when you press 'yes'.
    */
   public static void showAlertViewWithTitleYesOrNo(Context context, String title, String message, 
         AlertDialog.OnClickListener yesClickListener) {
      if (!isActivityResumed(context)) {
         return;
      }
      AlertDialog alertDialog = new AlertDialog.Builder(context).create();
      alertDialog.setTitle(title);
      alertDialog.setMessage(message);
      alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          return;
        } }); 
      alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.yes), yesClickListener); 
      alertDialog.show();
   }
   
   public static void showAlertViewWithTitle(Context context, String title, String message) {
      if (!isActivityResumed(context)) {
         return;
      }
      AlertDialog alertDialog = new AlertDialog.Builder(context).create();
      alertDialog.setTitle(title);
      alertDialog.setMessage(message);
      alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          return;
        } }); 
      alertDialog.show();
   }

   /**
    * The <b>OK</b> button in alertView for leaving the no server available problem and use the local cache.
    * The <b>Setting</> button in alertView for setting the controller url.
    * 
    * @author handy
    */
   public static void showAlertViewWithSetting(final Context context, String title, String message) {
      if (!isActivityResumed(context)) {
         return;
      }
      final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
      alertDialog.setTitle(title);
      alertDialog.setMessage(message);
      alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int which) {
            alertDialog.hide();
         }
      });
      alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Setting", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == DialogInterface.BUTTON_NEGATIVE) {
				Intent intent = new Intent();
				intent.setClass(context, AppSettingsActivity.class);
				context.startActivity(intent);
				Log.i("OpenRemote-INFO", "To setting after click setting btn in alertView of switch controller fail while console loading.");
			}
			alertDialog.hide();
		}
      });
      alertDialog.show();
   }
   
   private static boolean isActivityResumed(Context context) {
      if (context instanceof GenericActivity) {
         return ((GenericActivity)context).isActivityResumed();
      }
      return true;
   }
}
