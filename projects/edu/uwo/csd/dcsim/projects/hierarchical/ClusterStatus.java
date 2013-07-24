package edu.uwo.csd.dcsim.projects.hierarchical;

import edu.uwo.csd.dcsim.host.*;
import edu.uwo.csd.dcsim.projects.hierarchical.capabilities.RackPoolManager;

public class ClusterStatus {

	private long timeStamp;
	private int id;
	
	private int activeRacks = 0;				// Percentage [0,1] or number?
	private int minInactiveHosts = 0;			// Mininum number of inactive Hosts among active Racks in the Cluster.
	// max spare capacity: list of vectors or single vector  ( Resources object ? )
	
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
		
		minInactiveHosts = Integer.MAX_VALUE;
		for (RackData rack : capability.getRacks()) {
			// Calculate number of active Racks.
			// TODO If in the future we add *state* to Rack, we could simply check said attribute in each Rack.
			// In the meantime, we consider a Rack to be active if it has any active Hosts; otherwise, it's consider inactive.
			RackStatus status = rack.getCurrentStatus();
			if (status.getActiveHosts() > 0) {
				activeRacks++;
				
				// Find minimum number of inactive Hosts among active Racks.
				int inactiveHosts = status.getSuspendedHosts() + status.getPoweredOffHosts();
				if (inactiveHosts < minInactiveHosts)
					minInactiveHosts = inactiveHosts;
			}
			
			// max spare capacity ??
			
			// Calculate Cluster's total power consumption.
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
		
		activeRacks = status.activeRacks;
		minInactiveHosts = status.minInactiveHosts;
		
		// max spare capacity
		
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
	
	public double getPowerConsumption() {
		return powerConsumption;
	}

}