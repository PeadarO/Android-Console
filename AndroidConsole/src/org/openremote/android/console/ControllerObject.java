package org.openremote.android.console;

import java.util.ArrayList;
import java.util.List;

//This is for showing the View only
public class ControllerObject {

		private String url;
    private String defaultPanel;
		private String username;
		private String userpass;
	private Double xScale;
	private Double yScale;
    private String group;
    private boolean isControllerUp;
    private boolean availabilityCheckDone;
    private boolean availabilityCheckInProgress;
    private List<String> failoverControllers = new ArrayList<String>();
    
    public ControllerObject(String url, String defaultPanel, String username, String userpass, double xScale, double yScale) {
    	url = formatUrl(url);
    	
    	this.url = url;
    	this.defaultPanel = defaultPanel;
    	this.username = username;
    	this.userpass = userpass;
    	this.xScale = xScale;
		this.yScale = yScale;
    }
    
//   public boolean isIs_Selected(){
//	   return isSelected;
//   }
//   
//   public void setIs_Selected(boolean selected){
//	   isSelected=selected;
//   }
//   
//   public String getFailoverFor(){
//	   return failoverForcontroller;
//   }
    
    public Double getXScale() {
        return xScale;
    }
   
	public Double getYScale() {
        return yScale;
    }

    public String getUrl() {
        return url;
    }
    
    public String getGroup() {
    	return group;
    }
    
    public void setGroup(String group) {
    	this.group = group;
    }

    public String getDefaultPanel() {
    	return defaultPanel;
    }
    
    public String getUsername() {
    	return username;
    }
    
    public String getUserPass() {
    	return userpass;
    }

//    public boolean isAuto() {
//        return auto;
//    }
//    public void setAuto(boolean auto) {
//        this.auto = auto;
//    }
    
    public boolean isControllerUp() {
        return isControllerUp;
    }
    
    public void setIsControllerUp(boolean isControllerUp) {
        this.isControllerUp = isControllerUp;
    }
    
    public void setAvailabilityCheckDone(boolean availabilityCheckDone) {
    	this.availabilityCheckDone = availabilityCheckDone;
    }
    
    public boolean isAvailabilityCheckDone() {
    	return availabilityCheckDone;
    }

    public void setAvailabilityCheckInProgress(boolean availabilityCheckInProgress) {
      this.availabilityCheckInProgress = availabilityCheckInProgress;
    }
    
    public boolean isAvailabilityCheckInProgress() {
      return availabilityCheckInProgress;
    }
    
    public void addFailoverController(String url) {
    	failoverControllers.add(formatUrl(url));
    }
    
    public String[] getFailoverControllers() {
    	return failoverControllers.toArray(new String[]{});
    }

    private static String formatUrl(String url) {
    	if (url.endsWith("/"))
    		url = url.substring(0, url.length() - 2);
    	if (url.indexOf("http") < 0)
    		url = "http://" + url;
    	
    	return url;
    }
    //the controller needs to keep this for immediatr fetch
 //   public ControllerObject[] getFailoverControllers(){
    	
//    }
}