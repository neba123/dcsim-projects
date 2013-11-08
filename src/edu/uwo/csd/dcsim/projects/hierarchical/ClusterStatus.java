package edu.uwo.csd.dcsim.projects.hierarchical;

import edu.uwo.csd.dcsim.host.*;
import edu.uwo.csd.dcsim.projects.hierarchical.capabilities.RackPoolManager;

public class ClusterStatus {

	private long timeStamp;
	private int id;
	private Cluster.ClusterState state = Cluster.ClusterState.OFF;
	
	private int activeRacks = 0;				// Number of active Racks.
	private int minInactiveHosts = 0;			// Number of inactive Hosts in the Rack with more active Hosts.
	private double maxSpareCapacity = 0;		// Amount of spare resources available in the least loaded active Host in the Cluster.
	private double powerConsumption = 0;		// Sum of power consumption from all Racks and Switches in the Cluster.
	
	/**
	 * Creates an *empty* ClusterStatus instance. It only includes time stamp and ID.
	 */
	public ClusterStatus(Cluster cluster, long timeStamp) {
		this.timeStamp = timeStamp;
		id = cluster.getId();
	}
	
	/**
	 * Creates a *complete* ClusterStatus instance.
	 */
	public ClusterStatus(Cluster cluster, RackPoolManager capability, long timeStamp) {
		this.timeStamp = timeStamp;
		id = cluster.getId();
		state = cluster.getState();
		
		minInactiveHosts = Integer.MAX_VALUE;
		for (RackData rack : capability.getRacks()) {
			
			// Calculate number of active Racks.
			RackStatus status = rack.getCurrentStatus();
			if (status.getState() == Rack.RackState.ON) {
				activeRacks++;
				
				// Check Rack status. If invalid, we cannot make any assertions.
				if (rack.isStatusValid()) {
					
					// Find minimum number of inactive Hosts among active Racks.
					int inactiveHosts = status.getSuspendedHosts() + status.getPoweredOffHosts();
					if (inactiveHosts < minInactiveHosts)
						minInactiveHosts = inactiveHosts;
					
					// Find max spare capacity value among active Racks.
					if (status.getMaxSpareCapacity() > maxSpareCapacity)
						maxSpareCapacity = status.getMaxSpareCapacity();
					
				}
			}
			
			// Calculate Cluster's total power consumption.
			// Note: Even Racks with an invalid status are accounted for here, given that
			// skipping them would represent a good chunk of missing info.
			powerConsumption += rack.getCurrentStatus().getPowerConsumption();
		}
		
		// Add power consumption of the Cluster's Switches.
		powerConsumption += cluster.getMainDataSwitch().getPowerConsumption();
		powerConsumption += cluster.getMainMgmtSwitch().getPowerConsumption();
		if (cluster.getSwitchCount() > 1) {		// Star topology.
			for (Switch s : cluster.getDataSwitches())
				powerConsumption += s.getPowerConsumption();
			for (Switch s : cluster.getMgmtSwitches())
				powerConsumption += s.getPowerConsumption();
		}
	}
	
	public ClusterStatus(ClusterStatus status) {
		timeStamp = status.timeStamp;
		id = status.id;
		state = status.state;
		activeRacks = status.activeRacks;
		minInactiveHosts = status.minInactiveHosts;
		maxSpareCapacity = status.maxSpareCapacity;
		powerConsumption = status.powerConsumption;
	}
	
	public ClusterStatus copy() {
		return new ClusterStatus(this);
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public int getId() {
		return id;
	}
	
	public int getActiveRacks() {
		return activeRacks;
	}
	
	public int getMinInactiveHosts() {
		return minInactiveHosts;
	}
	
	public double getMaxSpareCapacity() {
		return maxSpareCapacity;
	}
	
	public double getPowerConsumption() {
		return powerConsumption;
	}
	
	public Cluster.ClusterState getState() {
		return state;
	}

}