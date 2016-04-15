/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2011, OpenRemote
 * Inc.
 * 
 * See the contributors.txt file in the distribution for a full listing of
 * individual contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.android.console.view;

import org.openremote.android.console.Constants;
import org.openremote.android.console.R;
import org.openremote.android.console.bindings.Image;
import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.bindings.Slider;
import org.openremote.android.console.model.ListenerConstant;
import org.openremote.android.console.model.OREvent;
import org.openremote.android.console.model.OREventListener;
import org.openremote.android.console.model.ORListenerManager;
import org.openremote.android.console.model.PollingStatusParser;
import org.openremote.android.console.util.ImageUtil;

import android.content.Context;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.View;

/**
 * This class is responsible for rendering the slider in screen with the slider
 * data.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 * @author handy 2010-05-12
 * 
 */
public class SliderView extends SensoryControlView implements View.OnTouchListener {
	public static final int TRACK_HEIGHT = 14;
	public static final int TRACK_BORDER = 0;
	public static final int THUMB_SIZE = 30;
	public static final double MAX_MIN_IMAGE_SIZE_RATIO_LIMIT = 0.2;
	public static final int MAX_MIN_IMAGE_TRACK_SPACING = 2;
	private Context context;
	private Slider slider;
	private ImageView minImage;
	private ImageView maxImage;
	private ImageView thumb;
	private View minTrack;
	private ImageView maxTrack;
	private boolean isVertical = false;
	private boolean isPassive = false;
	private int u = 0;
	private int v = 0;
	private int minValue = 0;
	private int maxValue = 100;
	private int thumbRange = 0;
	private int trackLength = 0;
	private int thumbPos = 0;
	private int halfThumb = 0;
	private int value = 0;
	private double valuePerPixel = 0;

	protected SliderView(Context context, Slider slider) {
		super(context);
		this.context = context;

		if (slider != null) {
			setComponent(slider);
			this.slider = slider;
			initSlider();

			// If the sensor is null, the sliderview is one-way.
			if (slider.getSensor() != null) {
				addPollingSensoryListener();
			}
		}
	}

	private void initSlider() {
		int width = slider.getFrameWidth();
		int height = slider.getFrameHeight();
		isVertical = slider.isVertical();
		isPassive = slider.isPassive();
		maxValue = slider.getMaxValue();
		minValue = slider.getMinValue();
		value = minValue;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		RelativeLayout sliderLayout = (RelativeLayout) inflater.inflate(R.layout.slider, (ViewGroup) findViewById(R.id.slider_layout));
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		sliderLayout.setLayoutParams(params);

		u = isVertical ? height : width;
		v = isVertical ? width : height;
		trackLength = u;

		minImage = (ImageView) sliderLayout.findViewById(R.id.slider_minvalue_image);
		maxImage = (ImageView) sliderLayout.findViewById(R.id.slider_maxvalue_image);
		minTrack = (ImageView) sliderLayout.findViewById(R.id.slider_mintrack);
		maxTrack = (ImageView) sliderLayout.findViewById(R.id.slider_maxtrack);
		thumb = (ImageView) sliderLayout.findViewById(R.id.slider_thumb);
		minImage.setId(1);
		maxImage.setId(2);
		minTrack.setId(3);
		maxTrack.setId(4);
		thumb.setId(5);

		// Configure min and max images
		RelativeLayout.LayoutParams mnLayoutParams = (RelativeLayout.LayoutParams) minImage.getLayoutParams();
		RelativeLayout.LayoutParams mxLayoutParams = (RelativeLayout.LayoutParams) maxImage.getLayoutParams();
		int spacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_MIN_IMAGE_TRACK_SPACING, getResources().getDisplayMetrics());
		int trackHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TRACK_HEIGHT, getResources().getDisplayMetrics());
		int minHeight = 0;
		int minWidth = 0;
		int minSpacing = 0;
		int maxHeight = 0;
		int maxWidth = 0;
		int maxSpacing = 0;
		int minMaxWidthLimit = (int)Math.round(isVertical ? slider.getFrameWidth() : MAX_MIN_IMAGE_SIZE_RATIO_LIMIT * slider.getFrameWidth());
		int minMaxHeightLimit = (int)Math.round(isVertical ? MAX_MIN_IMAGE_SIZE_RATIO_LIMIT * slider.getFrameHeight() : slider.getFrameWidth());

		Image minImg = slider.getMinImage();		
		if (minImg != null) {		  
			Drawable drawable = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH + minImg.getSrc(), minMaxWidthLimit, minMaxHeightLimit, true, true);
			minImage.setImageDrawable(drawable);
			minWidth = drawable.getIntrinsicWidth();
			minHeight = drawable.getIntrinsicHeight();
			minSpacing = spacing;
		}

		Image maxImg = slider.getMaxImage();
		if (maxImg != null) {
      Drawable drawable = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH + maxImg.getSrc(), minMaxWidthLimit, minMaxHeightLimit, true, true);
			maxImage.setImageDrawable(drawable);
			maxWidth = drawable.getIntrinsicWidth();
			maxHeight = drawable.getIntrinsicHeight();
			maxSpacing = spacing;
		}
		
		mnLayoutParams.height = minHeight;
		mnLayoutParams.width = minWidth;
		mxLayoutParams.height = maxHeight;
		mxLayoutParams.width = maxWidth;
		
		if (isVertical) {
			mnLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			mnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			mnLayoutParams.topMargin = minSpacing;
			mxLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			mxLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			mxLayoutParams.bottomMargin = maxSpacing;			
		} else {
			mnLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			mnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			mnLayoutParams.rightMargin = minSpacing;
			mxLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			mxLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			mxLayoutParams.leftMargin = maxSpacing;
		}

		// int minDrawableHeight = isVertical ? minDrawable.getIntrinsicWidth() :
		// minDrawable.getIntrinsicHeight();
		minImage.setLayoutParams(mnLayoutParams);
		maxImage.setLayoutParams(mxLayoutParams);
		minImage.setOnTouchListener(this);
		maxImage.setOnTouchListener(this);
		
		trackLength -= isVertical ? (minHeight + maxHeight) : (minWidth + maxWidth);
		trackLength -= (minSpacing + maxSpacing);
		
		// Configure the thumb
		Drawable thumbDrawable;
		
		RelativeLayout.LayoutParams thumbLayoutParams = (RelativeLayout.LayoutParams) thumb.getLayoutParams();

		Image thumbImg = slider.getThumbImage();
		if (thumbImg != null) {
			thumbDrawable = ImageUtil.createScaledDrawableFromPath(context, Constants.FILE_FOLDER_PATH
					+ thumbImg.getSrc(), slider.getFrameWidth(), slider.getFrameHeight(), true, true);
		} else {
			thumbDrawable = getResources().getDrawable(R.drawable.slider_thumb);
		}

		int thumbU = isVertical ? thumbDrawable.getIntrinsicHeight() : thumbDrawable.getIntrinsicWidth();
		int thumbV = isVertical ? thumbDrawable.getIntrinsicWidth() : thumbDrawable.getIntrinsicHeight();
		halfThumb = (int)Math.round((double)thumbU / 2);
		thumbLayoutParams.width = thumbU;
		thumbLayoutParams.height = thumbV;
		
		if (isVertical) 
		{
			thumbLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			thumbLayoutParams.addRule(RelativeLayout.ABOVE, minImage.getId());
		} 
		else 
		{
			thumbLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			thumbLayoutParams.addRule(RelativeLayout.RIGHT_OF, minImage.getId());
		}

		thumb.setScaleType(ScaleType.CENTER);
		thumb.setImageDrawable(thumbDrawable);
		thumb.setLayoutParams(thumbLayoutParams);
		thumb.setOnTouchListener(this);

		// Configure track
		int minTrackV = TRACK_HEIGHT;
		int maxTrackV = TRACK_HEIGHT;
		Drawable minTrackDrawable, maxTrackDrawable;
		RelativeLayout.LayoutParams minLayoutParams = (RelativeLayout.LayoutParams) minTrack.getLayoutParams();
		RelativeLayout.LayoutParams maxLayoutParams = (RelativeLayout.LayoutParams) maxTrack.getLayoutParams();

		Image minTImg = slider.getMinTrackImage();
		if (minTImg != null) {
			BitmapDrawable minTrackBitmap = ImageUtil.createClipedDrawableFromPath(context, Constants.FILE_FOLDER_PATH
					+ minTImg.getSrc(), slider.getFrameWidth(), slider.getFrameHeight(), slider.getFrameWidth(), slider.getFrameHeight());
			
			// Position the image within the view
			if (isVertical) {
				// Position center bottom - can't align background image to bottom so have to do this
				// a more complicated way
				minTrackBitmap.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
				LinearLayout minContainer = new LinearLayout(context);
				minContainer.setOrientation(LinearLayout.VERTICAL);
				int imgHeight = minTrackBitmap.getIntrinsicHeight();
				int repeats = (int)Math.ceil((double)trackLength / imgHeight);
				
				for (int i=0; i<repeats; i++) {
					// Add an imageview for each repeat
					ImageView img = new ImageView(context);
					img.setImageDrawable(minTrackBitmap);
					img.setScaleType(ScaleType.MATRIX);
					img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, imgHeight));
					minContainer.addView(img,0);
				}
				
				// Swap out Image View for this LinearLayout
				FrameLayout.LayoutParams minContainerLayout = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, repeats * imgHeight);
				minContainerLayout.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
				minContainer.setLayoutParams(minContainerLayout);
				FrameLayout frame = new FrameLayout(context);
				frame.addView(minContainer);
				sliderLayout.removeView(minTrack);
				sliderLayout.addView(frame,0);
				minTrack = frame;
				minTrack.setId(3);
			} else {
				// Position left center
				minTrackBitmap.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
				minTrackBitmap.setTileModeY(TileMode.REPEAT);
			}
			
			minTrackDrawable = minTrackBitmap;
		} else {
			int minId = isVertical ? R.drawable.slider_min_track_v : R.drawable.slider_min_track;
			minTrackDrawable = getResources().getDrawable(minId);
		}
	
		Image maxTImg = slider.getMaxTrackImage();
		
		if (maxTImg != null) {
      BitmapDrawable maxTrackBitmap = ImageUtil.createClipedDrawableFromPath(context, Constants.FILE_FOLDER_PATH
              + maxTImg.getSrc(), slider.getFrameWidth(), slider.getFrameHeight(), slider.getFrameWidth(), slider.getFrameHeight());
			
			// Position the image within the view
			if (isVertical) {
				// Position center top
				maxTrackBitmap.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
				maxTrackBitmap.setTileModeY(TileMode.REPEAT);
			} else {
				// Position right center
				maxTrackBitmap.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
				maxTrackBitmap.setTileModeX(TileMode.REPEAT);
			}

			maxTrackDrawable = maxTrackBitmap;
		} else {
			int maxId = isVertical ? R.drawable.slider_max_track_v : R.drawable.slider_max_track;
			maxTrackDrawable = getResources().getDrawable(maxId);
		}

		int minDrawableV = isVertical ? minTrackDrawable.getIntrinsicWidth() : minTrackDrawable
				.getIntrinsicHeight();
		minTrackV = minDrawableV > v ? v : minDrawableV;
		
		int maxDrawableV = isVertical ? maxTrackDrawable.getIntrinsicWidth() : maxTrackDrawable
				.getIntrinsicHeight();
		maxTrackV = maxDrawableV > v ? v : maxDrawableV;

//		int minTrackMargin = (int) Math.round(((double) v - minTrackV) / 2);
//		int maxTrackMargin = (int) Math.round(((double) v - maxTrackV) / 2);

		if (isVertical) {
			minLayoutParams.width = minTrackV;
			minLayoutParams.height = halfThumb;
			minLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			minLayoutParams.addRule(RelativeLayout.ABOVE, minImage.getId());
			maxLayoutParams.width = maxTrackV;
			maxLayoutParams.height = LayoutParams.MATCH_PARENT;
			maxLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			maxLayoutParams.addRule(RelativeLayout.ABOVE, minTrack.getId());
			maxLayoutParams.addRule(RelativeLayout.BELOW, maxImage.getId());
		} else {
			minLayoutParams.width = halfThumb;
			minLayoutParams.height = minTrackV;
			minLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			minLayoutParams.addRule(RelativeLayout.RIGHT_OF, minImage.getId());
			maxLayoutParams.width = LayoutParams.MATCH_PARENT;
			maxLayoutParams.height = maxTrackV;
			maxLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			maxLayoutParams.addRule(RelativeLayout.RIGHT_OF, minTrack.getId());
			maxLayoutParams.addRule(RelativeLayout.LEFT_OF, maxImage.getId());
		}

		if (!(minTrack instanceof FrameLayout))
			minTrack.setBackgroundDrawable(minTrackDrawable);
		
		minTrack.setLayoutParams(minLayoutParams);
		minTrack.setOnTouchListener(this);
		maxTrack.setBackgroundDrawable(maxTrackDrawable);
		maxTrack.setLayoutParams(maxLayoutParams);
		maxTrack.setOnTouchListener(this);
		
		// Calculate variables
		thumbRange = trackLength - thumbU;
		valuePerPixel = (double)(maxValue - minValue) / thumbRange;
		
		// Set the view
		this.addView(sliderLayout);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 * Used to capture thumb drag events and track click events
	 */
  @Override
  public boolean onTouch(View v, MotionEvent event) {
     if (isPassive || !isEnabled() || event == null) {
        return false;
     }

     switch (event.getAction()) {
	     case MotionEvent.ACTION_DOWN:
	       break;
	
	     case MotionEvent.ACTION_MOVE:
	       if (v == thumb) {
	      	 thumbPos += isVertical ? -1 * ((int) event.getY() - halfThumb) : (int) event.getX() - halfThumb;
	     		 thumbPos = thumbPos < 0 ? 0 : thumbPos > thumbRange ? thumbRange : thumbPos;
		    	 updateThumbPos();
	       }
	       break;
	
	     case MotionEvent.ACTION_UP:
	    	 if (v == thumb) {
	    		 thumbPos += isVertical ? -1 * ((int) event.getY() - halfThumb) : (int) event.getX() - halfThumb;
	    	 }
	    	 else if (v == minTrack) {
	    		 thumbPos = (int) (isVertical ? (thumbPos - event.getY()) : event.getX());
	    	 } else if (v == maxTrack) {
	    		 thumbPos = (int) (isVertical ? (trackLength - event.getY()) : thumbPos + event.getX());
	    	 } else if (v == minImage) {
	    	   setValue(value-1, false);
	         updatePosFromValue();
	         break;
	    	 } else if (v == maxImage) {
	         setValue(value+1, false);
           updatePosFromValue();
           break;
	    	 }	    	   
	    	 
	    	 thumbPos = thumbPos < 0 ? 0 : thumbPos > thumbRange ? thumbRange : thumbPos;
	    	 updateThumbPos();
	    	 updateValueFromPos();
	       break;
	
	     case MotionEvent.ACTION_CANCEL:
	        break;
     }
     
     invalidate();
     return true;
  }
  
  /*
   * Adjusts the views so thumb shows in correct position
   */
  private void updateThumbPos() {
  	RelativeLayout.LayoutParams thumbLayout = (RelativeLayout.LayoutParams)thumb.getLayoutParams();
  	RelativeLayout.LayoutParams minLayout = (RelativeLayout.LayoutParams)minTrack.getLayoutParams();
  	
  	if (isVertical) {
  		thumbLayout.bottomMargin = thumbPos;
  		minLayout.height = thumbPos + halfThumb;
  	} else {
  		thumbLayout.leftMargin = thumbPos;
  		minLayout.width = thumbPos + halfThumb;  		
  	}
  	
  	thumb.setLayoutParams(thumbLayout);
  	minTrack.setLayoutParams(minLayout);
  }
  
  /*
   * Update the value from the thumb position
   */
  private void updateValueFromPos() {
  	int newValue = (int)Math.round(valuePerPixel * thumbPos) + minValue;
  	setValue(newValue, false);
  }
  
  private void setValue(int newValue, boolean fromPolling) {
  	newValue = newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue;
  	if (newValue != value) {
  		value = newValue;
   		Log.i("OpenRemote-SLIDER", "Set value: " + value);
   		if (fromPolling) {
   			updatePosFromValue();
   		} else {
   			sendCommandRequest(String.valueOf(value));
   		}
  	}
  }

  /*
   * Update the thumb position from the value
   */
  private void updatePosFromValue() {
  	int pos = (int)Math.round((value - minValue) / valuePerPixel);
  	pos = pos < 0 ? 0 : pos > thumbRange ? thumbRange : pos;
  	thumbPos = pos;
  	updateThumbPos();
  }
  
	public void addPollingSensoryListener() {
		final Integer sensorId = ((Slider) getComponent()).getSensor().getSensorId();
		Log.i("OpenRemote-SLIDER", "sensor id is " + sensorId);
		if (sensorId != null && sensorId > 0) {
			ORListenerManager.getInstance().addOREventListener(
					ListenerConstant.ListenerPollingStatusIdFormat + sensorId, new OREventListener() {
						public void handleEvent(OREvent event) {
							String value = PollingStatusParser.statusMap.get(sensorId.toString()).toLowerCase();
							int valueInt = 0;
							try {
								valueInt = Integer.parseInt(value);
							} catch (NumberFormatException e) {
								Log.e("OpenRemote-SLIDER", "The returned format of polling value " + value
										+ " for slider is wrong", e);
								return;
							}
							pollingValueUIHandler.sendEmptyMessage(valueInt);
						}
					});
		}
	}

	private Handler pollingValueUIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int newValue = msg.what;
			setValue(newValue, true);
			super.handleMessage(msg);
		}
	};
}
