package edu.uwo.csd.dcsim.extras.policies;

import java.util.ArrayList;
import java.util.Collections;

import edu.uwo.csd.dcsim.DCUtilizationMonitor;
import edu.uwo.csd.dcsim.DataCentre;
import edu.uwo.csd.dcsim.management.VMRelocationPolicyGreedy;
import edu.uwo.csd.dcsim.management.stub.*;

/**
 * Implements the following VM relocation policy:
 * 
 * - relocation candidates: VMs with less CPU load than the CPU load by which 
 *   the host is stressed are ignored. The rest of the VMs are sorted in 
 *   increasing order by <CPU load>;
 * - target hosts: sort Partially-utilized hosts in increasing order by 
 *   <CPU utilization, power efficiency>, Underutilized hosts in decreasing 
 *   order by <CPU utilization, power efficiency>, and Empty hosts in 
 *   decreasing order by <power efficiency, power state>. Return the hosts in 
 *   the following order: Partially-utilized, Underutilized, and Empty;
 * - source hosts: any host with its average CPU utilization over the last CPU 
 *   load monitoring window exceeding _upperThreshold_ is considered Stressed. 
 *   These hosts are sorted in decreasing order by <CPU utilization>.
 * 
 * @author Gaston Keller
 *
 */
public class VMRelocationPolicyFFIMBalanced extends VMRelocationPolicyGreedy {

	/**
	 * Creates a new instance of VMRelocationPolicyFFIMBalanced.
	 */
	public VMRelocationPolicyFFIMBalanced(DataCentre dc, DCUtilizationMonitor utilizationMonitor, double lowerThreshold, double upperThreshold, double targetUtilization) {
		super(dc, utilizationMonitor, lowerThreshold, upperThreshold, targetUtilization);
	}
	
	/**
	 * Sorts Stressed hosts in decreasing order by <CPU utilization>.
	 */
	@Override
	protected ArrayList<HostStub> orderSourceHosts(ArrayList<HostStub> stressed) {
		ArrayList<HostStub> sorted = new ArrayList<HostStub>(stressed);
		
		// Sort Stressed hosts in decreasing order by CPU utilization.
		Collections.sort(sorted, HostStubComparator.getComparator(HostStubComparator.CPU_UTIL));
		Collections.reverse(sorted);
		
		return sorted;
	}
	
	/**
	 * Sorts the relocation candidates in increasing order by <CPU load>, 
	 * previously removing from consideration those VMs with less CPU load 
	 * than the CPU load by which the host is stressed.
	 */
	@Override
	protected ArrayList<VmStub> orderSourceVms(ArrayList<VmStub> sourceVms) {
		ArrayList<VmStub> sorted = new ArrayList<VmStub>();
		
		// Remove VMs with less CPU load than the CPU load by which the source 
		// host is stressed.
		HostStub source = sourceVms.get(0).getHost();
		double cpuExcess = source.getCpuInUse() - source.getTotalCpu() * this.upperThreshold;
		for (VmStub vm : sourceVms)
			if (vm.getCpuInUse() >= cpuExcess)
				sorted.add(vm);
		
		if (!sorted.isEmpty())
			// Sort VMs in increasing order by CPU load.
			Collections.sort(sorted, VmStubComparator.getComparator(VmStubComparator.CPU_IN_USE));
		else {
			// Add original list of VMs and sort them in decreasing order by 
			// CPU load, so as to avoid trying to migrate the smallest VMs 
			// first (which would not help resolve the stress situation).
			sorted.addAll(sourceVms);
			Collections.sort(sorted, VmStubComparator.getComparator(VmStubComparator.CPU_IN_USE));
			Collections.reverse(sorted);
		}
		
		return sorted;
	}
	
	/**
	 * Sorts Partially-utilized hosts in increasing order by <CPU utilization, 
	 * power efficiency>, Underutilized hosts in decreasing order by 
	 * <CPU utilization, power efficiency>, and Empty hosts in decreasing 
	 * order by <power efficiency, power state>.
	 * 
	 * Returns Partially-utilized, Underutilized, and Empty hosts, in that 
	 * order.
	 */
	@Override
	protected ArrayList<HostStub> orderTargetHosts(ArrayList<HostStub> partiallyUtilized, ArrayList<HostStub> underUtilized, ArrayList<HostStub> empty) {
		ArrayList<HostStub> targets = new ArrayList<HostStub>();
		
		// Sort Partially-utilized hosts in increasing order by 
		// <CPU utilization, power efficiency>.
		Collections.sort(partiallyUtilized, HostStubComparator.getComparator(HostStubComparator.CPU_UTIL, HostStubComparator.EFFICIENCY));
		
		// Sort Underutilized hosts in decreasing order by <CPU utilization, 
		// power efficiency>.
		Collections.sort(underUtilized, HostStubComparator.getComparator(HostStubComparator.CPU_UTIL, HostStubComparator.EFFICIENCY));
		Collections.reverse(underUtilized);
		
		// Sort Empty hosts in decreasing order by <power efficiency, 
		// power state>.
		Collections.sort(empty, HostStubComparator.getComparator(HostStubComparator.EFFICIENCY, HostStubComparator.PWR_STATE));
		Collections.reverse(empty);
		
		targets.addAll(partiallyUtilized);
		targets.addAll(underUtilized);
		targets.addAll(empty);
		
		return targets;
	}

}