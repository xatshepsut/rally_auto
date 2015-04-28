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
	
	
	// TODO: main scenario
	//
	// get RallyTestCase (package)
	// command -> get TC
	// command -> get TS if any
	// attach ready TC to RallyTestCase 
	// attach TS? to RallyTestCase
	// get test result
	// command -> create TCR
	// attach TS from RallyTestCase to TCR
	// attach TCR to RallyTestCase
	// send updated TC to Rally !
	// done
	
	
	// TODO: attach
	// attach TC to TCR???
	// attach TS to TCR
	
	
	
}
