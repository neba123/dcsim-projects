package edu.uwo.csd.dcsim2.examples.svm;

import java.util.ArrayList;

import org.apache.log4j.*;

import edu.uwo.csd.dcsim2.*;
import edu.uwo.csd.dcsim2.core.*;
import edu.uwo.csd.dcsim2.management.*;
import edu.uwo.csd.dcsim2.vm.*;

public class DynamicManagement {

	private static Logger logger = Logger.getLogger(DynamicManagement.class);
	
	public static void main(String args[]) {
		
		PropertyConfigurator.configure(Simulation.getConfigDirectory() +"/logger.properties"); //configure logging from file

		logger.info(DynamicManagement.class.toString());
		
		//Set random seed to repeat run
		//Utility.setRandomSeed(6225674672952014821l);
		
		DataCentre dc = SVMHelper.createDataCentre();
		
		ArrayList<VMAllocationRequest> vmList = SVMHelper.createVmList(false);
				
		SVMHelper.placeVms(vmList, dc);
		
		//create the VM relocation policy
		
		/*
		 * Basic Greedy Relocation & Consolidation together. Relocation same as RelocST03, Consolidation similar but
		 * evicts ALL VMs from underprovisioned hosts, not 1.
		 */
		@SuppressWarnings("unused")
		VMAllocationPolicyGreedy vmAllocationPolicyGreedy = new VMAllocationPolicyGreedy(dc, 600000, 600000, 0.4, 0.85, 0.85);

		
		SVMHelper.runSimulation(864000000, 86400000);
		
	}
	
}
