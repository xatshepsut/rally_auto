package com.vmware.rally.automation.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.data.RTestCaseData;
import com.vmware.rally.automation.data.command.RCommand;
import com.vmware.rally.automation.data.command.RCommandEnvelop;
import com.vmware.rally.automation.data.command.RCreateResultCommand;
import com.vmware.rally.automation.data.command.RGetCommand;
import com.vmware.rally.automation.data.command.RGetCommand.RGetCommandType;


/**
 * Manager class that handles everything.
 * Singleton.
 * 
 * @author akaramyan
 */

public class AutomationManager {
	
	/** 
	 * Map with test data: data from annotations, data from command execution results.
	 * Used by main thread.
	 */
	private Map<String, RTestCaseData> _dataMap = new HashMap<String, RTestCaseData>();
	
	/**
	 * Queue with RCreateResultCommand objects that are not ready to be executed.
	 * Used by main thread, internal only.
	 */
	private Queue<RCommandEnvelop> pendingResultQueue = new LinkedList<RCommandEnvelop>();
	
	/** 
	 * Map with results from command call.
	 * Shared resource with command executor thread.
	 */
	private Map<String, Queue<JsonObject>> resultMap = new HashMap<String, Queue<JsonObject>> ();
	
	
	// TEMP TODO: Move to child thread
	/**
	 * Queue with all commands to be executed.
	 */
	private Queue<RCommandEnvelop> commandQueue = new LinkedList<RCommandEnvelop>();
	
	
	private AutomationManager() {
		// Initializing rallyManager before first use
		RallyManager.getInstance();
	}
	
	private static AutomationManager instance = null;
	public static AutomationManager getInstance() {
		if(instance == null) {
			instance = new AutomationManager();
		}
		return instance;
	}
	
	
	/* Public Methods */
	
	/**
	 * Processes test data and creates commands for retrieving
	 *  test related objects from Rally.
	 * @param testData  - RTestCaseData object containing annotation data
	 * @param key       - String uniquely identifying test
	 */
	public void addTestData(RTestCaseData testData, String key) {
		_dataMap.put(key, testData);	
		
		RGetCommand tcCommand = new RGetCommand(testData.getId(), RGetCommandType.GET_TEST_CASE);
		insertCommandWithKey(tcCommand, key);
		
		RGetCommand tsCommand = new RGetCommand(testData.getTestSetId(), RGetCommandType.GET_TEST_SET);
		insertCommandWithKey(tsCommand, key);
	}
	
	/**
	 * Getter for test data related to test with given key.
	 * @param key  - String uniquely identifying test
	 * @return RTestCaseData related to test with <i>key</i>
	 */
	public RTestCaseData getTestDataWithKey(String key) {
		if (_dataMap.containsKey(key)) {
			return _dataMap.get(key);
		}
		return null;
	}
	
	/**
	 * Processes test result and creates test result creation command.
	 * @param key       - String uniquely identifying test
	 * @param verdict   - String test result status
	 */
	public void onFinishedTestWithKey(String key, String verdict) {
		// Process result queue and get available data
		processResultsWithKey(key);
		
		// Process pending results
		processPendingResultCommandQueue();
		
		// Create new result command
		RTestCaseData testData = _dataMap.get(key);
		RCreateResultCommand command = 
				new RCreateResultCommand(testData.getTestCaseJson(), testData.getTestSetJson(), 
										 testData.getBuildNumber(), verdict);
		RCommandEnvelop commandEnvelop = new RCommandEnvelop(command, key);
		
		if (command.isValid()) {
			commandQueue.add(commandEnvelop);
		} else {
			pendingResultQueue.add(commandEnvelop);
		}
		
	}
	
	/**
	 * TEMP: Notifies to execute all commands.
	 */
	public void onFinishedTestSuite() {
		executeCommands();
	}
	
	
	/* Private Methods */
	
	/**
	 * Wraps command with given key inside command envelop and inserts into command queue.
	 * @param command   - RCommand
	 * @param key       - String uniquely identifying test
	 */
	private void insertCommandWithKey(RCommand command, String key) {
		insertCommandEnvelop(new RCommandEnvelop(command, key));
	}
	
	/**
	 * Inserts command envelope into command queue.
	 * @param commandEnvelop   - RCommandEnvelop
	 */
	private void insertCommandEnvelop(RCommandEnvelop commandEnvelop) {
		// TODO: add to queue inside child thread, if thread is dead create new one
		commandQueue.add(commandEnvelop);
	}
	
	/** TEMP: Executes all commands from command queue.
	 */
	private void executeCommands() {
		// Note: commandQueue contains only commands that are ready to be executed
		// i.e. all necessary data is collected
		
		while (!commandQueue.isEmpty()) {
			RCommandEnvelop commandWrapped = commandQueue.poll();
			RCommand command = commandWrapped.getCommand();
			
			// TODO: create wrapper class for this part of code
			JsonObject result = command.execute();
			String key = commandWrapped.getKey();
			
			Queue<JsonObject> queue;
			
			if (resultMap.containsKey(key)) {
				queue = resultMap.get(key);
			} else {
				queue = new LinkedList<JsonObject>();
			}
			
			queue.add(result);
			resultMap.put(key, queue);
		}
		
		// TEMP^2 block
		if (!pendingResultQueue.isEmpty()) {
			while (!pendingResultQueue.isEmpty()) {
				processPendingResultCommandQueue();
			}
			
			executeCommands();
		}
	}

	/**
	 * Processes command results related to test with given key and updates data map.
	 * @param key   - String uniquely identifying test
	 */
	private void processResultsWithKey(String key) {
		Queue<JsonObject> queue = resultMap.get(key);
		
		while (queue != null && !queue.isEmpty()) {
			JsonObject json = queue.poll();
			String type = json.get("_type").getAsString();
			
			if (type.equals("TestCase")) {
				_dataMap.get(key).setTestCaseJson(json);
			} else if (type.equals("TestSet")) {
				_dataMap.get(key).setTestSetJson(json);
			} else {
				// NOP
				// TODO: print error message ?
			}
		}
	}
	
	/**
	 * Processes queue of pending RCreateResultCommands 
	 * and updates with newest available results related to test with given key.
	 * Moves completed commands to command queue.
	 */
	private void processPendingResultCommandQueue() {
		assert pendingResultQueue != null;
		
		while (!pendingResultQueue.isEmpty()) {
			RCommandEnvelop pendingCommandEnvelop = pendingResultQueue.peek();
			RCreateResultCommand pendingCommand = (RCreateResultCommand) pendingCommandEnvelop.getCommand();
			
			// TODO: encapsulate
			processResultsWithKey(pendingCommandEnvelop.getKey());
			RTestCaseData testData = _dataMap.get(pendingCommandEnvelop.getKey());
			
			// TODO: make sure this really updates queue top element
			// TODO: revise ?
			pendingCommand.setTestCase(testData.getTestCaseJson());
			pendingCommand.setTestSet(testData.getTestSetJson());
			pendingCommandEnvelop.setCommand(pendingCommand);
			
			if (pendingCommand.isValid()) {
				insertCommandEnvelop(pendingCommandEnvelop);
				pendingResultQueue.poll();
			} else {
				// Note: We assume all tests run in sequential order, same for related REST calls.
				// This means if data for completing top element in queue is not available
				// than data for next one in queue cannot be available either.
				break;
			}
		}
	}
	
}
