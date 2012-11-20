package org.gridkit.lab.examples.nanocloud;

import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.telecontrol.ssh.RemoteNodeProps;
import org.junit.Test;

public class StartingWithDistributedCloud extends StartingWithLocalCloud {

	@Test
	public void test_distributed_hello_world__version1() throws InterruptedException {
		
		// Using SSH for remote execution requires some configuration
		// it could be done programaticaly but we will use config file in this example
		cloud = CloudFactory.createSshCloud("resource:cbox-cluster.viconf");
		
		// config (box-cluster.viconf) is using first segment of node name 
		// for mapping of node node to hostname
		cloud.node("cbox1.node1");
		cloud.node("cbox2.node1");
		
		// Alternatively we could override configuration for particular node
		// host, java command and jar cache path should be configured for node to start
		RemoteNodeProps.at(cloud.node("extranode"))
			.setRemoteHost("cbox1")
			.setRemoteJavaExec("java")
			.setRemoteJarCachePath("/tmp/extra");
				
		
		// now we have 3 nodes configured to run across two servers
		// let say them hello
		
		// warm up is optional, but it makes console out put less messy
		warmUp(cloud);
		sayHelloWorld(cloud);

		// give console output a chance to reach us from remote node
		Thread.sleep(300);
	}

	// hiding @Test annotation
	
	@Override
	public void test_hello_world__version1() {
		super.test_hello_world__version1();
	}

	@Override
	public void test_hello_world__version2() {
		super.test_hello_world__version2();
	}

	@Override
	public void test_hello_world__version3() throws InterruptedException {
		super.test_hello_world__version3();
	}

	@Override
	public void test_jvm_args__version1() throws InterruptedException {
		super.test_jvm_args__version1();
	}
}
