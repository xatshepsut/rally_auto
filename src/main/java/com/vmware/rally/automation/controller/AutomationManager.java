package com.vmware.rally.automation.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.data.RTestData;
import com.vmware.rally.automation.data.command.RCommand;
import com.vmware.rally.automation.data.command.RCommandCallable;
import com.vmware.rally.automation.data.command.RCommandCallableResultEnvelop;
import com.vmware.rally.automation.data.command.RCommandEnvelop;
import com.vmware.rally.automation.data.command.RCreateResultCommand;
import com.vmware.rally.automation.data.command.RGetCommand;
import com.vmware.rally.automation.data.command.RGetCommand.RGetCommandType;
import com.vmware.rally.automation.data.enums.RJsonObjectType;
import com.vmware.rally.automation.data.enums.RTestResultVerdict;
import com.vmware.rally.automation.exception.InvalidRCommandException;
import com.vmware.rally.automation.exception.RTaskException;
import com.vmware.rally.automation.exception.UninitializedRallyApiException;
import com.vmware.rally.automation.utils.LoggerWrapper;


/**
 * Manager class that handles everything.
 * Singleton.
 * 
 * @author akaramyan
 */

public class AutomationManager {
	
	/** 
	 * Map with test data: data from annotations, data from command execution results.
	 */
	private Map<String, RTestData> _dataMap = new HashMap<String, RTestData>();
	
	/** 
	 * Map with results from command call.
	 * Shared resource with command executor.
	 */
	private Map<String, Queue<RCommandCallableResultEnvelop>> _resultMap = new HashMap<String, Queue<RCommandCallableResultEnvelop>> ();

	/**
	 * Queue with RCreateResultCommand objects that are not ready to be executed.
	 */
	private Queue<RCommandEnvelop> _pendingResultQueue = new LinkedList<RCommandEnvelop>();
	
	/**
	 * Black labeled keys i.e. keys of tests that we are unable to further process
	 */
	private ArrayList<String> _blackLabeledKeys = new ArrayList<String>();
	
	/**
	 * ExecutorService for executing RCommands wrapped in RCommandCallable.
	 */
	private ExecutorService _commandExecutor;

	
	public String _rallyUrl;
	public String _apiKey;
	public String _userEmail;
	
	
	public AutomationManager(String rallyUrl, String apiKey, String userEmail) {
		_rallyUrl = rallyUrl;
		_apiKey = apiKey;
		_userEmail = userEmail;
		
		// Initializing RallyManager before first use
		RallyManager.getInstance().initialize(_rallyUrl, _userEmail, _apiKey);
		
		// Initializing command executor with single thread
		_commandExecutor = Executors.newSingleThreadExecutor();
		
		// Initializing logger for this class
		LoggerWrapper.getInstance().registerLogger();
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
		LoggerWrapper.getInstance().logInfo("Created get command for TestCase with id: " + testData.getId());
		
		RGetCommand tsCommand = new RGetCommand(testData.getTestSetId(), RGetCommandType.GET_TEST_SET);
		insertCommandWithKey(tsCommand, key);
		LoggerWrapper.getInstance().logInfo("Created get command for TestSet with id: " + testData.getTestSetId());
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
		// xx Update pending results, clean black labeled items, move to executable queue
		processPendingResultCommandQueue();
		
		if (!_pendingResultQueue.isEmpty()) {
			// Wait for all pending results to be ready in a loop
			while (!_pendingResultQueue.isEmpty()) {
				// xx will clean broken commands ie with failed results 
				// xx so no blocking !
				processPendingResultCommandQueue();
			}
			
			// Prevent executor from accepting new commands 
			// and wait for all commands to complete
			_commandExecutor.shutdown();
			try {
				_commandExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
				
				for (String key : _resultMap.keySet()) {
					processResultsWithKey(key);
				}
			} catch (InterruptedException e) {
				// Unexpected error: interrupted while waiting
				// TODO: retry command
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
		
		addResultFutureWithTypeAndKey(future, command.getResultType(), key);
	}
	
	/**
	 * Adds future to given key queue in result map.
	 * @param future   - Future<JsonObject> containing JSON result
	 * @param key      - String uniquely identifying test
	 */
	private void addResultFutureWithTypeAndKey(Future<JsonObject> future, RJsonObjectType type, String key) {
		Queue<RCommandCallableResultEnvelop> resultQueue;
		
		
		if (_resultMap.containsKey(key)) {
			resultQueue = _resultMap.get(key);
		} else {
			resultQueue = new LinkedList<RCommandCallableResultEnvelop>();
		}
		
		// Keeping wrapped future in result map to extract JSON result later
		resultQueue.add(new RCommandCallableResultEnvelop(future, type));
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
		// So if top future in the queue is not done yet, than others also cannot be ready.
		
		Queue<RCommandCallableResultEnvelop> resultQueue = _resultMap.get(key);
		
		while (resultQueue != null && !resultQueue.isEmpty()) {
			RCommandCallableResultEnvelop wrappedFuture = resultQueue.peek();
			Future<JsonObject> future = wrappedFuture.getFuture();
			RJsonObjectType type = wrappedFuture.getType();
			
			if (future.isDone()) {
				resultQueue.poll();
				
				try {
					JsonObject jsonResult = future.get();

					RTestData testData  = getTestDataForKey(key);
					assert testData != null;
					
					if (RJsonObjectType.JSON_OBJECT_TESTCASE == type) {
						testData.setTestCaseJson(jsonResult);
						LoggerWrapper.getInstance().logInfo("Got JSON object for TestCase with id: " + testData.getId());
					} else if (RJsonObjectType.JSON_OBJECT_TESTSET == type) {
						testData.setTestSetJson(jsonResult);
						LoggerWrapper.getInstance().logInfo("Got JSON object for TestSet with id: " + testData.getTestSetId());
					} else if (RJsonObjectType.JSON_OBJECT_TESTCASERESULT == type) {
						LoggerWrapper.getInstance().logInfo("Created TestCaseResult object for key: " + key);
					}
				} catch (InterruptedException e) {
					// Unexpected error: Command was interrupted
					// TODO: retry command
				} catch (ExecutionException e) {
					// Error: Exception was thrown by command
					Exception exception = (Exception) e.getCause();
					
					assert !exception.getClass().equals(InvalidRCommandException.class);
					LoggerWrapper.getInstance().logError(exception.getMessage());
					
					if ((exception.getClass().equals(UninitializedRallyApiException.class) || 
							exception.getClass().equals(RTaskException.class)) && 
							((RJsonObjectType.JSON_OBJECT_TESTCASE == type) || (RJsonObjectType.JSON_OBJECT_TESTSET == type))) {
						// Black label this key
						// We are unable to make any Rally related actions after these exceptions
						_blackLabeledKeys.add(key);
						
						if (RJsonObjectType.JSON_OBJECT_TESTCASE == type) {
							LoggerWrapper.getInstance().logInfo("Failed to get TestCase, unable to further process test with key: " + key);
						} else {
							LoggerWrapper.getInstance().logInfo("Failed to get TestSet, unable to further process test with key: " + key);
						}
						
						// Clean result queue for given key
						while (!resultQueue.isEmpty()) {
							resultQueue.poll();
						} 
					}
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
		
		boolean canContinueProccessing = true;
		while (!_pendingResultQueue.isEmpty() && canContinueProccessing) {
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
				addResultFutureWithTypeAndKey(future, pendingCommand.getResultType(), key);
				
				_pendingResultQueue.poll();
				
			} else if (_blackLabeledKeys.contains(key)){
				_pendingResultQueue.poll();
				
			} else {
				// Note: We assume all tests run in sequential order, same for related REST calls.
				// This means if data for completing top element in queue is not available
				// than data for next one in queue cannot be available either.
				
				// Queue top results are still pending, update and exit
				pendingCommandEnvelop.setCommand(pendingCommand);
				
				canContinueProccessing = false;
			}
		}
	}
	
}
