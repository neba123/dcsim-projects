package edu.uwo.csd.dcsim.host.scheduler;

import edu.uwo.csd.dcsim.host.Host;
import edu.uwo.csd.dcsim.host.Resources;
import edu.uwo.csd.dcsim.vm.VMAllocation;

public abstract class ResourceScheduler {

	protected Host host;
		
	/**
	 * Resets all scheduling to zero. If the host is off, scheduleResources will not be called and scheduling will remain at zero.
	 */
	public final void resetScheduling() {
		host.getPrivDomainAllocation().getVm().scheduleResources(new Resources());
		for (VMAllocation vmAlloc : host.getVMAllocations()) {
			vmAlloc.getVm().scheduleResources(new Resources());
		}
	}

	public abstract void scheduleResources();
	
	public void setHost(Host host) {
		this.host = host;
	}
	
}
