package com.lotaris.minirox.client.junit;

import com.lotaris.minirox.client.MiniRoxFilter;
import com.lotaris.rox.client.junit.RoxFilter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Extend the standard junit runner to add the minirox filtering features
 * 
 * @author Laurent Prevost <laurent.prevost@.com>
 */
public class MiniRoxRunner extends BlockJUnit4ClassRunner {
	private final MiniRoxFilter miniroxfilter = new MiniRoxFilter();
	
	public MiniRoxRunner(Class<?> klass) throws InitializationError {
		super(klass);
		
		final String[] filters = miniroxfilter.getFilters();
			
		if (filters != null) {
			try {
				filter(new RoxFilter(miniroxfilter.getFilters()));
			}
			catch (NoTestsRemainException ntre) {
			}
		}
	}
}
