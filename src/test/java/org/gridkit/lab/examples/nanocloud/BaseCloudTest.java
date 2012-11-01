package org.gridkit.lab.examples.nanocloud;

import org.gridkit.vicluster.ViManager;
import org.junit.After;

public abstract class BaseCloudTest {

	protected ViManager cloud;

	@After
	public void recycleCloud() {
		if (cloud != null) {
			cloud.shutdown();
		}
	}	
}
