package com.vmware.rally.automation.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.data.RTestCaseData;
import com.vmware.rally.automation.data.command.RCommand;
import com.vmware.rally.automation.data.command.RCommandCallable;
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

// TODO: Global todos
// TODO: add status report logs
// TODO: catch IOException in commands and fire custom exception more meaningful to user


public class AutomationManager {
	
	/** 
	 * Map with test data: data from annotations, data from command execution results.
	 * Used by main thread.
	 */
	private Map<String, RTestCaseData> _dataMap = new HashMap<String, RTestCaseData>();
	
	/** 
	 * Map with results from command call.
	 * Shared resource with command executor.
	 */
	private Map<String, Queue<Future<JsonObject>>> resultMap = new HashMap<String, Queue<Future<JsonObject>>> ();
	
	/**
	 * ExecutorService for executing RCommands wrapped in RCommandCallable.
	 */
	private ExecutorService _commandExecutor = null;

	/**
	 * Queue with RCreateResultCommand objects that are not ready to be executed.
	 */
	private Queue<RCommandEnvelop> pendingResultQueue = new LinkedList<RCommandEnvelop>();
	
	
	private AutomationManager() {
		// Initializing rallyManager before first use
		RallyManager.getInstance();
		
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
		// Date/time when test finished execution
		String date = RallyManager.getInstance().getCurrentDate();
		
		// Process result queue and get available data
		processResultsWithKey(key);
		
		// Process pending results
		processPendingResultCommandQueue();
		
		// Create new result command
		RTestCaseData testData = _dataMap.get(key);
		RCreateResultCommand command = 
				new RCreateResultCommand(testData.getTestCaseJson(), testData.getTestSetJson(), 
										 testData.getBuildNumber(), verdict, date);
		
		if (command.isValid()) {
			insertCommandWithKey(command, key);
		} else {
			pendingResultQueue.add(new RCommandEnvelop(command, key));
		}
		
	}
	
	/**
	 * Processes all pending commands and exits the application.
	 */
	public void onFinishedTestSuite() {
		if (!pendingResultQueue.isEmpty()) {
			// Wait for all pending results to be ready in a loop
			while (!pendingResultQueue.isEmpty()) {
				processPendingResultCommandQueue();
			}
			
			// Prevent executor from accepting new commands 
			//and wait for all commands to complete
			_commandExecutor.shutdown();
			try {
				_commandExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
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
		// Submitting command for execution
		RCommandCallable commandCallable = new RCommandCallable(command);
		Future<JsonObject> future = _commandExecutor.submit(commandCallable);

		addFutureWithKeyToResultMap(future, key);
	}
	
	/**
	 * Adds given future to result map under given key.
	 * @param future   - Future<JsonObject> containing JSON result
	 * @param key      - String uniquely identifying test
	 */
	private void addFutureWithKeyToResultMap(Future<JsonObject> future, String key) {
		Queue<Future<JsonObject>> resultQueue;
		
		if (resultMap.containsKey(key)) {
			resultQueue = resultMap.get(key);
		} else {
			resultQueue = new LinkedList<Future<JsonObject>>();
		}
		
		// Keeping future in result map to extract JSON result later
		resultQueue.add(future);
		resultMap.put(key, resultQueue);
	}
	
	/**
	 * Processes command results related to test with given key and updates data map.
	 * @param key   - String uniquely identifying test
	 */
	private void processResultsWithKey(String key) {
		// Note: Here we assume all commands are executed in sequence.
		// So if top future in the queue is not done yet, than others also are not ready.
		
		Queue<Future<JsonObject>> resultQueue = resultMap.get(key);
		
		while (resultQueue != null && !resultQueue.isEmpty()) {
			Future<JsonObject> future = resultQueue.peek();
			
			if (future.isDone()) {
				// TODO: exception handling
				JsonObject jsonResult;
				
				try {
					jsonResult = future.get();
					resultQueue.poll();
					
					if (jsonResult != null) {
						String type = jsonResult.get("_type").getAsString();
						
						if (type.equals("TestCase")) {
							_dataMap.get(key).setTestCaseJson(jsonResult);
						} else if (type.equals("TestSet")) {
							_dataMap.get(key).setTestSetJson(jsonResult);
						}
					}
				} catch (/*InterruptedException | ExecutionException*/Exception e) {
					e.printStackTrace();
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
		assert pendingResultQueue != null;
		
		while (!pendingResultQueue.isEmpty()) {
			RCommandEnvelop pendingCommandEnvelop = pendingResultQueue.peek();
			RCreateResultCommand pendingCommand = (RCreateResultCommand) pendingCommandEnvelop.getCommand();			
			String key = pendingCommandEnvelop.getKey();
			
			// TODO: encapsulate
			processResultsWithKey(key);
			RTestCaseData testData = _dataMap.get(key);
			
			// TODO: make sure this really updates queue top element
			// TODO: revise ?
			pendingCommand.setTestCase(testData.getTestCaseJson());
			pendingCommand.setTestSet(testData.getTestSetJson());
			pendingCommandEnvelop.setCommand(pendingCommand);
			
			if (pendingCommand.isValid()) {
				RCommandCallable commandCallable = new RCommandCallable(pendingCommand);
				Future<JsonObject> future = _commandExecutor.submit(commandCallable);
				addFutureWithKeyToResultMap(future, key);
				
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
