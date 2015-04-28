package com.vmware.rally.automation.controller;


@SuppressWarnings("unused")
public class AutomationManager {
	
	private AutomationManager() {
		// Initilizing rallyManager before first use
		RallyManager.getInstance();
	}
	
	private static AutomationManager instance = null;
	public static AutomationManager getInstance() {
		if(instance == null) {
			instance = new AutomationManager();
		}
		return instance;
	}
	
	
	
}
