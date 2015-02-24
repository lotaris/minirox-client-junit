package com.lotaris.minirox.client.junit;

import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import com.lotaris.rox.client.junit.AbstractRoxListener;
import com.lotaris.minirox.client.MiniRoxConfiguration;
import com.lotaris.rox.common.model.v1.ModelFactory;
import com.lotaris.rox.common.model.v1.Test;
import com.lotaris.rox.common.utils.MetaDataBuilder;
import java.util.HashSet;
import java.util.Set;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Junit MiniROX Listener is a wrapper to the {@link com.lotaris.minirox.client.MiniRoxListener} to
 * get the Junit {@link org.junit.runner.Description}, transform and send them to MiniROX through the MiniROX listener.
 *
 * @author Laurent Prévost, laurent.prevost@lotaris.com
 */
public class MiniRoxListener extends AbstractRoxListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(MiniRoxListener.class);
	
	/**
	 * Mini ROX configuration
	 */
	private static final MiniRoxConfiguration miniRoxConfiguration = MiniRoxConfiguration.getInstance();
	
	/**
	 * Mini ROX listener wrapped
	 */
	private final com.lotaris.minirox.client.MiniRoxListener miniRoxListener = new com.lotaris.minirox.client.MiniRoxListener();
	
	/**
	 * Store the test that fail to handle correctly the difference between test
	 * failures and test success in the testFinished method.
	 */
	private final Set<String> testFailures = new HashSet<>();

	public MiniRoxListener() {}
	
	public MiniRoxListener(String category) {
		super(category);
	}
	
	@Override
	public void testRunStarted(Description description) throws Exception {
		super.testRunStarted(description);
		
		if (!miniRoxConfiguration.isEnabled()) {
			return;
		}

		miniRoxListener.testRunStart(
			configuration.getProjectApiId(), 
			configuration.getProjectVersion(), 
			getCategory(null, null)
		);
	}
	
	@Override
	public void testRunFinished(Result result) throws Exception {
		if (!miniRoxConfiguration.isEnabled()) {
			return;
		}
		
		long runEndedDate = System.currentTimeMillis();
			
		// Notify mini ROX that the test run is finished
		miniRoxListener.testRunEnd(
			configuration.getProjectApiId(), 
			configuration.getProjectVersion(), 
			getCategory(null, null),
			runEndedDate - runStartedDate
		);
	}
	
	@Override
	public void testStarted(Description description) throws Exception {
		super.testStarted(description);

		if (!miniRoxConfiguration.isEnabled()) {
			return;
		}
		
		// Register the test for date calculation by the technical name
		testStartDates.put(getTechnicalName(description), System.currentTimeMillis());
	}

	@Override
	public void testFinished(Description description) throws Exception {
		super.testFinished(description);

		if (!miniRoxConfiguration.isEnabled()) {
			return;
		}
		
		String name = getTechnicalName(description);
		
		RoxableTest methodAnnotation = getMethodAnnotation(description);

		// Detect if the test is in failure
		if (!testFailures.contains(name)) {
			// Detect if the test is enriched by the rox annotation
			if (methodAnnotation != null) {
				miniRoxListener.testResult(
					createTest(description, methodAnnotation, getClassAnnotation(description), true, null),
					configuration.getProjectApiId(), 
					configuration.getProjectVersion(), 
					getCategory(null, null)
				);
			}
			
			// No annotation enrichment
			else {
				miniRoxListener.testResult(
					createTest(name, true, null),
					configuration.getProjectApiId(), 
					configuration.getProjectVersion(), 
					getCategory(null, null)
				);
			}
		}
	}	

	@Override
	public void testFailure(Failure failure) throws Exception {
		super.testFailure(failure);

		if (!miniRoxConfiguration.isEnabled()) {
			return;
		}
		
		RoxableTest methodAnnotation = getMethodAnnotation(failure.getDescription());
		RoxableTestClass cAnnotation = getClassAnnotation(failure.getDescription());

		String name = getTechnicalName(failure.getDescription());
		
		// Register the test in the failures
		testFailures.add(name);

		// Detect rox annotation enrichment
		if (methodAnnotation != null) {
			// Send it to minirox if required
			miniRoxListener.testResult(
				createTest(failure.getDescription(), methodAnnotation, cAnnotation, false, createAndlogStackTrace(failure)),
				configuration.getProjectApiId(), 
				configuration.getProjectVersion(), 
				getCategory(null, null)
			);
		} 
		else {
			miniRoxListener.testResult(
				createTest(name, false, createAndlogStackTrace(failure)),
				configuration.getProjectApiId(), 
				configuration.getProjectVersion(), 
				getCategory(null, null)
			);
		}
	}	

	/**
	 * Create a test based on the different information gathered from the description
	 * 
	 * @param name The test name in case there is no available rox key
	 * @param passed Test passing or not
	 * @param message Message associated to the test result
	 * @return The test created from all the data available
	 */
	private Test createTest(String name, boolean passed, String message) {
		MetaDataBuilder data = new MetaDataBuilder();
		
		System.out.println(name);
		
		return ModelFactory.createTest(
			name,
			name,
			getCategory(null, null),
			System.currentTimeMillis(),
			System.currentTimeMillis() - testStartDates.get(name),
			message,
			passed,
			-1,
			new HashSet<String>(),
			new HashSet<String>(),
			data.toMetaData()
		);
	}	
}