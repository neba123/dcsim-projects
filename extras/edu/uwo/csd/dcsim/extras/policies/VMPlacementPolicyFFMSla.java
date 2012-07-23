package edu.uwo.csd.dcsim.extras.policies;

import java.util.ArrayList;
import java.util.Collections;

import edu.uwo.csd.dcsim.*;
import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.host.Host;
import edu.uwo.csd.dcsim.host.comparator.HostComparator;

/**
 * Implements a First Fit algorithm for an SLA-friendly Strategy, where the 
 * target hosts are sorted as follows: Partially-utilized hosts in increasing 
 * order by CPU utilization, followed by Underutilized hosts in decreasing 
 * order by CPU utilization, and finally Empty hosts in decreasing order by 
 * power state.
 * 
 * @author Gaston Keller
 *
 */
public class VMPlacementPolicyFFMSla extends VMPlacementPolicyGreedy {

	/**
	 * Creates an instance of VMPlacementPolicyFFMSla.
	 */
	public VMPlacementPolicyFFMSla(Simulation simulation, DataCentre dc, DCUtilizationMonitor utilizationMonitor, double lowerThreshold, double upperThreshold, double targetUtilization) {
		super(simulation, dc, utilizationMonitor, lowerThreshold, upperThreshold, targetUtilization);
	}

	/**
	 * Sorts Partially-utilized hosts in increasing order by CPU utilization, 
	 * Underutilized hosts in decreasing order by CPU utilization, and Empty 
	 * hosts in decreasing order by power state.
	 * 
	 * Returns Partially-utilized, Underutilized and Empty hosts in that order.
	 */
	@Override
	protected ArrayList<Host> orderTargetHosts(ArrayList<Host> partiallyUtilized, ArrayList<Host> underUtilized, ArrayList<Host> empty) {
		ArrayList<Host> targets = new ArrayList<Host>();
		
		// Sort Partially-utilized in increasing order by CPU utilization.
		Collections.sort(partiallyUtilized, HostComparator.getComparator(HostComparator.CPU_UTIL));
		
		// Sort Underutilized hosts in decreasing order by CPU utilization.
		Collections.sort(underUtilized, HostComparator.getComparator(HostComparator.CPU_UTIL));
		Collections.reverse(underUtilized);
		
		// Sort Empty hosts in decreasing order by power state.
		Collections.sort(empty, HostComparator.getComparator(HostComparator.PWR_STATE));
		Collections.reverse(empty);
		
		targets.addAll(partiallyUtilized);
		targets.addAll(underUtilized);
		targets.addAll(empty);
		
		return targets;
	}

}