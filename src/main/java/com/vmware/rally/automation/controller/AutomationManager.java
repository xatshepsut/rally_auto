package com.vmware.rally.automation.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.data.RTestData;
import com.vmware.rally.automation.data.command.RCommand;
import com.vmware.rally.automation.data.command.RCommandCallable;
import com.vmware.rally.automation.data.command.RCommandEnvelop;
import com.vmware.rally.automation.data.command.RCreateResultCommand;
import com.vmware.rally.automation.data.command.RGetCommand;
import com.vmware.rally.automation.data.command.RGetCommand.RGetCommandType;
import com.vmware.rally.automation.data.enums.RTestResultVerdict;


/**
 * Manager class that handles everything.
 * Singleton.
 * 
 * @author akaramyan
 */

// TODO: add exception handling and logs !

@SuppressWarnings("unused")
public class AutomationManager {
	
	/** 
	 * Map with test data: data from annotations, data from command execution results.
	 */
	private Map<String, RTestData> _dataMap = new HashMap<String, RTestData>();
	
	/** 
	 * Map with results from command call.
	 * Shared resource with command executor.
	 */
	private Map<String, Queue<Future<JsonObject>>> _resultMap = new HashMap<String, Queue<Future<JsonObject>>> ();

	/**
	 * Queue with RCreateResultCommand objects that are not ready to be executed.
	 */
	private Queue<RCommandEnvelop> _pendingResultQueue = new LinkedList<RCommandEnvelop>();
	
	/**
	 * ExecutorService for executing RCommands wrapped in RCommandCallable.
	 */
	private ExecutorService _commandExecutor;

	
	// TODO: replace
	public static String API_KEY = "_OhYE7czYRo2y2tR6il3lyJQsoJGhP0T1gM8JqCFZMlg";
	public static String USER_EMAIL = "bobbrown@dispostable.com";
	
	
	private AutomationManager() {
		// Initializing RallyManager before first use
		RallyManager.getInstance().initialize(USER_EMAIL, API_KEY);
		
		// Initializing command executor with single thread
		_commandExecutor = Executors.newSingleThreadExecutor();
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
	public void addTestData(RTestData testData, String key) {
		setTestDataForKey(testData, key);
		
		RGetCommand tcCommand = new RGetCommand(testData.getId(), RGetCommandType.GET_TEST_CASE);
		insertCommandWithKey(tcCommand, key);
		
		RGetCommand tsCommand = new RGetCommand(testData.getTestSetId(), RGetCommandType.GET_TEST_SET);
		insertCommandWithKey(tsCommand, key);
	}
	
	/**
	 * Processes test result and creates test result creation command.
	 * @param key       - String uniquely identifying test
	 * @param verdict   - String test result status
	 */
	public void onFinishedTestWithVerdict(String key, RTestResultVerdict verdict) {
		// Process result queue and get available data
		processResultsWithKey(key);
		
		// Process pending results
		processPendingResultCommandQueue();
		
		// Create new create result command
		RTestData testData = getTestDataForKey(key);
		RCreateResultCommand command = 
				new RCreateResultCommand(testData.getTestCaseJson(), testData.getTestSetJson(), 
										 testData.getBuildNumber(), verdict, new Date());
		
		if (command.isValid()) {
			insertCommandWithKey(command, key);
		} else {
			_pendingResultQueue.add(new RCommandEnvelop(command, key));
		}
		
	}
	
	/**
	 * Processes all pending commands and exits the application.
	 */
	public void onComplete() {
		if (!_pendingResultQueue.isEmpty()) {
			// Wait for all pending results to be ready in a loop
			while (!_pendingResultQueue.isEmpty()) {
				processPendingResultCommandQueue();
			}
			
			// Prevent executor from accepting new commands 
			// and wait for all commands to complete
			_commandExecutor.shutdown();
			try {
				_commandExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// Error: interrupted while waiting 
			}
		}
	}
	
	
	/* Private Methods */
	
	/**
	 * Submits command with given key for execution.
	 * @param command   - RCommand
	 * @param key       - String uniquely identifying test
	 */
	private void insertCommandWithKey(RCommand command, String key) {
		RCommandCallable commandCallable = new RCommandCallable(command);
		Future<JsonObject> future = _commandExecutor.submit(commandCallable);
		addFutureToResultsWithKey(future, key);
	}
	
	/**
	 * Adds future to given key queue in result map.
	 * @param future   - Future<JsonObject> containing JSON result
	 * @param key      - String uniquely identifying test
	 */
	private void addFutureToResultsWithKey(Future<JsonObject> future, String key) {
		Queue<Future<JsonObject>> resultQueue;
		
		if (_resultMap.containsKey(key)) {
			resultQueue = _resultMap.get(key);
		} else {
			resultQueue = new LinkedList<Future<JsonObject>>();
		}
		
		// Keeping future in result map to extract JSON result later
		resultQueue.add(future);
		_resultMap.put(key, resultQueue);
	}
	
	/**
	 * Returns test data with given key from data map.
	 * @param key  - String uniquely identifying test
	 * @return RTestCaseData related to test with <i>key</i>
	 */
	private RTestData getTestDataForKey(String key) {
		return _dataMap.get(key);
	}
	
	/**
	 * Sets test data for given key in data map.
	 * @param testData  - RTestCaseData to add
	 * @param key       - String uniquely identifying test
	 */
	private void setTestDataForKey(RTestData testData, String key) {
		_dataMap.put(key, testData);	
	}
	
	/**
	 * Processes command results related to test with given key and updates data map.
	 * @param key   - String uniquely identifying test
	 */
	private void processResultsWithKey(String key) {
		// Note: Here we assume all commands are executed in sequence.
		// So if top future in the queue is not done yet, than others also are not ready.
		
		Queue<Future<JsonObject>> resultQueue = _resultMap.get(key);
		
		while (resultQueue != null && !resultQueue.isEmpty()) {
			Future<JsonObject> future = resultQueue.peek();
			
			if (future.isDone()) {
				resultQueue.poll();
				
				try {
					JsonObject jsonResult = future.get();

					String type = jsonResult.get("_type").getAsString();
					RTestData testData  = getTestDataForKey(key);
					assert testData != null;
					
					if (type.equals("TestCase")) {
						testData.setTestCaseJson(jsonResult);
					} else if (type.equals("TestSet")) {
						testData.setTestSetJson(jsonResult);
					}
				} catch (InterruptedException e) {
					// Error: command was interrupted
				} catch (ExecutionException e) {
					// Error: exception was thrown by command
					// TODO: handle
				}
			} else {
				break;
			}
		}
	}
	
	/**
	 * Processes queue of pending RCreateResultCommands 
	 * and updates with newest available results related to test with given key.
	 * Moves completed commands to command queue.
	 */
	private void processPendingResultCommandQueue() {
		assert _pendingResultQueue != null;
		
		while (!_pendingResultQueue.isEmpty()) {
			RCommandEnvelop pendingCommandEnvelop = _pendingResultQueue.peek();
			RCreateResultCommand pendingCommand = (RCreateResultCommand) pendingCommandEnvelop.getCommand();			
			String key = pendingCommandEnvelop.getKey();
			
			processResultsWithKey(key);
			RTestData testData = getTestDataForKey(key);
			
			pendingCommand.setTestCase(testData.getTestCaseJson());
			pendingCommand.setTestSet(testData.getTestSetJson());
			
			if (pendingCommand.isValid()) {
				RCommandCallable commandCallable = new RCommandCallable(pendingCommand);
				Future<JsonObject> future = _commandExecutor.submit(commandCallable);
				addFutureToResultsWithKey(future, key);
				
				_pendingResultQueue.poll();
			} else {
				// Note: We assume all tests run in sequential order, same for related REST calls.
				// This means if data for completing top element in queue is not available
				// than data for next one in queue cannot be available either.
				
				// Update queue top and exit
				pendingCommandEnvelop.setCommand(pendingCommand);
				break;
			}
		}
	}
	
}
