	/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2012, OpenRemote Inc.
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
package org.openremote.android.console.view;

import java.lang.ref.WeakReference;

import org.openremote.android.console.net.AsyncControllerAvailabilityChecker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Custom Layout that supports check marking, it actually passes the check marking down
 * to a child that implements the checkable interface, it also supports isCheckable flag
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
public class ControllerListItemLayout extends RelativeLayout {
  private Checkable mCheckable;
  private boolean isCheckable = false;
  private WeakReference<AsyncControllerAvailabilityChecker> checkerTaskReference;
  
  public ControllerListItemLayout(Context context) {
      this(context, null);
  }

  public ControllerListItemLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
  }

//  @Override
//  public boolean isChecked() {
//      return mCheckable == null ? false : mCheckable.isChecked();
//  }

  @Override
  protected void onFinishInflate() {
      super.onFinishInflate();
      //findCheckable(this);
  }
  
//  private void findCheckable(ViewGroup vg) {
//    // Find Checkable child
//    int childCount = vg.getChildCount();
//    for (int i = 0; i < childCount; ++i) {
//        View v = vg.getChildAt(i);
//        if (v instanceof Checkable) {
//          mCheckable = (Checkable) v;
//          return;
//        } else if (v instanceof ViewGroup) {
//        	findCheckable((ViewGroup)v);
//        }
//        if (mCheckable != null)
//        	return;
//    }
//  }

//  @Override
//  public void setChecked(boolean checked) {
//      if(mCheckable != null && isCheckable) {
//        mCheckable.setChecked(checked);
//      }
//  }
//  
//  public void setCheckable(boolean checkable) {
//  	isCheckable = checkable;
//  }
//  
//  public boolean isCheckable() {
//  	return isCheckable;
//  }
//
//  @Override
//  public void toggle() {
//      if(mCheckable != null)
//          mCheckable.toggle();
//  }
  
  public AsyncControllerAvailabilityChecker getCheckerTask() {
  	return checkerTaskReference.get();
  }
  
  public void setCheckerTask(AsyncControllerAvailabilityChecker checkerTask) {
    checkerTaskReference = new WeakReference<AsyncControllerAvailabilityChecker>(checkerTask);
  }
}
