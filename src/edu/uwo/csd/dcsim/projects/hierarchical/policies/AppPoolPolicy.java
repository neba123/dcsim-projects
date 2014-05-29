package edu.uwo.csd.dcsim.projects.hierarchical.policies;

import edu.uwo.csd.dcsim.management.Policy;
import edu.uwo.csd.dcsim.management.events.VmInstantiationCompleteEvent;
import edu.uwo.csd.dcsim.projects.hierarchical.capabilities.AppPoolManager;
import edu.uwo.csd.dcsim.projects.hierarchical.events.IncomingMigrationEvent;

public class AppPoolPolicy extends Policy {

	public AppPoolPolicy() {
		addRequiredCapability(AppPoolManager.class);
	}
	
	public void execute(IncomingMigrationEvent event) {
		
		simulation.getLogger().debug(String.format("[AppPool] Processing IncomingMigrationEvent for App #%d.", event.getApplication().getId()));
		
		manager.getCapability(AppPoolManager.class).addApplication(event.getApplication());
	}
	
	public void execute(VmInstantiationCompleteEvent event) {
		
		simulation.getLogger().debug(String.format("[AppPool] Processing VmInstantiationCompleteEvent for VM #%d in Host #%d.", event.getVmId(), event.getHostId()));
		
		manager.getCapability(AppPoolManager.class).getApplication(event.getApplicationId()).getTask(event.getTaskId()).setHostingVm(event.getVmId());
	}
	
	@Override
	public void onInstall() {
		// Auto-generated method stub
	}

	@Override
	public void onManagerStart() {
		// Auto-generated method stub
	}

	@Override
	public void onManagerStop() {
		// Auto-generated method stub
	}

}
