package edu.uwo.csd.dcsim.projects.hierarchical.policies;

import java.util.Collection;

import edu.uwo.csd.dcsim.host.Resources;
import edu.uwo.csd.dcsim.management.*;
import edu.uwo.csd.dcsim.projects.hierarchical.*;
import edu.uwo.csd.dcsim.projects.hierarchical.capabilities.RackManager;
import edu.uwo.csd.dcsim.projects.hierarchical.capabilities.RackPoolManager;
import edu.uwo.csd.dcsim.projects.hierarchical.events.*;

/**
 * 
 * 
 * @author Gaston Keller
 *
 */
public abstract class VmRelocationPolicyLevel1 extends Policy {

	protected AutonomicManager target;
	
	/**
	 * Creates an instance of VmRelocationPolicyLevel1.
	 */
	public VmRelocationPolicyLevel1(AutonomicManager target) {
		addRequiredCapability(RackPoolManager.class);
		
		this.target = target;
	}
	
	/**
	 * 
	 */
	public void execute(MigRequestEvent event) {
		RackPoolManager rackPool = manager.getCapability(RackPoolManager.class);
		Collection<RackData> racks = rackPool.getRacks();
		
		
		
		
		// TODO Check all Racks EXCEPT the one that sent the migration request (if said Rack belongs in this Cluster).		
		
		
		
		
		double maxSpareCapacity = 0;
		RackData maxSpareCapacityRack = null;
		int minInactiveHosts = Integer.MAX_VALUE;
		RackData mostLoadedWithSuspended = null;
		RackData mostLoadedWithPoweredOff = null;
		for (RackData rack : racks) {
			// Filter out Racks with a currently invalid status.
			if (rack.isStatusValid()) {
				RackStatus status = rack.getCurrentStatus();
				
				// Find the Rack with the most spare capacity.
				if (status.getMaxSpareCapacity() > maxSpareCapacity) {
					maxSpareCapacity = status.getMaxSpareCapacity();
					maxSpareCapacityRack = rack;
				}
				
				// Find the most loaded Racks (i.e., the Racks with the smallest number of inactive 
				// Hosts) that have at least one suspended or powered off Host.
				int inactiveHosts = status.getSuspendedHosts() + status.getPoweredOffHosts();
				if (inactiveHosts > 0 && inactiveHosts < minInactiveHosts) {
					minInactiveHosts = inactiveHosts;
					if (status.getSuspendedHosts() > 0)
						mostLoadedWithSuspended = rack;
					if (status.getPoweredOffHosts() > 0)
						mostLoadedWithPoweredOff = rack;
				}
			}
		}
		
		RackData targetRack = null;
		
		// Check if Rack with most spare capacity has enough resources to take the VM (i.e., become target).
		if (null != maxSpareCapacityRack && this.canHost(event.getVm(), maxSpareCapacityRack)) {
			targetRack = maxSpareCapacityRack;
		}
		// Otherwise, make the most loaded Rack with a suspended Host the target.
		else if (null != mostLoadedWithSuspended) {
			targetRack = mostLoadedWithSuspended;
		}
		// Last recourse: make the most loaded Rack with a powered off Host the target.
		else if (null != mostLoadedWithPoweredOff) {
			targetRack = mostLoadedWithPoweredOff;
		}
		
		if (null != targetRack) {
			// Found target. Send migration request.
			simulation.sendEvent(new MigRequestEvent(targetRack.getRackManager(), event.getVm(), event.getOrigin()));
			
			
			// TODO Should I record here that a MigRequest for VM X from Host Y was sent to Host Z ???
			// Should I store the info in a new capability? Probably...
			// Should I record as well the Host to which the request is forwarded? Don't think it's necessary...
			
			
		}
		// Could not find suitable target Rack in the Cluster.
		else {
			// If event's origin belongs in this Cluster, request assistance from DC Manager 
			// to find a target Host for the VM migration in another Cluster.
			
			// TODO I shouldn't be able to access info through the reference to the manager Origin. Said reference 
			// should be used only as a pointer to send messages. If I need more info, it may have to be sent in 
			// the message's payload.
			
			if (null != rackPool.getRack(event.getOrigin().getCapability(RackManager.class).getRack().getId())) {
				simulation.sendEvent(new MigRequestEvent(target, event.getVm(), event.getOrigin()));
				
				// TODO Should I record here that a MigRequest for VM X from Host Y was forwarded to DC Manager ???
				// Should I store the info in a new capability? Probably...
				// Can probably assume this to be the last time to hear about this request... unless unsuccessful.
				
			}
			// Event's origin does not belong in this Cluster.
			else {
				// Migration request was sent by DC Manager. Reject migration request.
				simulation.sendEvent(new MigRejectEvent(target, event.getVm(), event.getOrigin()));
			}	
		}
	}
	
	/**
	 * 
	 */
	protected boolean canHost(VmStatus vm, RackData rack) {
		// Check Host capabilities (e.g. core count, core capacity).
		HostDescription hostDescription = rack.getRackDescription().getHostDescription();
		if (hostDescription.getCpuCount() * hostDescription.getCoreCount() < vm.getCores())
			return false;
		if (hostDescription.getCoreCapacity() < vm.getCoreCapacity())
			return false;
		
		// Check available resources.
		Resources availableResources = AverageVmSizes.convertCapacityToResources(rack.getCurrentStatus().getMaxSpareCapacity());
		Resources vmResources = vm.getResourcesInUse();
		if (availableResources.getCpu() < vmResources.getCpu())
			return false;
		if (availableResources.getMemory() < vmResources.getMemory())
			return false;
		if (availableResources.getBandwidth() < vmResources.getBandwidth())
			return false;
		if (availableResources.getStorage() < vmResources.getStorage())
			return false;
		
		return true;
	}
	
	@Override
	public void onInstall() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onManagerStart() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onManagerStop() {
		// TODO Auto-generated method stub
	}

}
