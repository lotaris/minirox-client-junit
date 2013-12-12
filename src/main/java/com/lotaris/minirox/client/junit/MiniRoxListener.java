package com.lotaris.minirox.client.junit;

import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import com.lotaris.rox.client.junit.AbstractRoxListener;
import com.lotaris.minirox.client.MiniRoxConfiguration;
import java.util.HashSet;
import java.util.Set;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Junit MiniROX Listener is a wrapper to the {@link com.lotaris.rox.mini.MiniRoxListener} to
 * get the Junit {@link Description}, transform and send them to MiniROX through the MiniROX listener.
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
	private com.lotaris.minirox.client.MiniRoxListener miniRoxListener = new com.lotaris.minirox.client.MiniRoxListener();
	
	/**
	 * Store the test that fail to handle correctly the difference between test
	 * failures and test success in the testFinished method.
	 */
	private Set<String> testFailures = new HashSet<>();

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
		
		RoxableTest annotation = getMethodAnnotation(description);

		if (annotation != null) {
			if (annotation.key() != null || !annotation.key().isEmpty()) {
				testStartDates.put(annotation.key(), System.currentTimeMillis());
			}
			else {
				LOGGER.warn("@{} annotation is present but missconfigured. The key is missing", RoxableTest.class.getSimpleName());
			}
		} 
		else {
			LOGGER.warn("@{} annotation is missing on method name : {}.{}", RoxableTest.class.getSimpleName(), description.getClassName(), description.getMethodName());
		}
	}

	@Override
	public void testFinished(Description description) throws Exception {
		super.testFinished(description);

		if (!miniRoxConfiguration.isEnabled()) {
			return;
		}
		
		RoxableTest methodAnnotation = getMethodAnnotation(description);

		if (methodAnnotation != null && !methodAnnotation.key().isEmpty() && !testFailures.contains(methodAnnotation.key())) {
			// Send it to minirox if required
			miniRoxListener.testResult(
				createTest(description, methodAnnotation, getClassAnnotation(description), true, null),
				configuration.getProjectApiId(), 
				configuration.getProjectVersion(), 
				getCategory(null, null)
			);
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

		if (methodAnnotation != null && !methodAnnotation.key().isEmpty()) {
			testFailures.add(methodAnnotation.key());
			
			// Send it to minirox if required
			miniRoxListener.testResult(
				createTest(failure.getDescription(), methodAnnotation, cAnnotation, false, createAndlogStackTrace(failure)),
				configuration.getProjectApiId(), 
				configuration.getProjectVersion(), 
				getCategory(null, null)
			);
		} 
	}	
	
}