package edu.uwo.csd.dcsim.projects.hierarchical.policies;

import java.util.Map;

import edu.uwo.csd.dcsim.host.Host;
import edu.uwo.csd.dcsim.management.HostData;
import edu.uwo.csd.dcsim.management.Policy;
import edu.uwo.csd.dcsim.management.VmStatus;
import edu.uwo.csd.dcsim.management.capabilities.HostPoolManager;
import edu.uwo.csd.dcsim.management.events.HostStatusEvent;
import edu.uwo.csd.dcsim.management.events.MigrationCompleteEvent;
import edu.uwo.csd.dcsim.management.events.VmInstantiationCompleteEvent;
import edu.uwo.csd.dcsim.projects.hierarchical.VmData;
import edu.uwo.csd.dcsim.projects.hierarchical.capabilities.AppPoolManager;
import edu.uwo.csd.dcsim.projects.hierarchical.capabilities.VmPoolManager;
import edu.uwo.csd.dcsim.projects.hierarchical.events.IncomingMigrationEvent;

public class VmPoolPolicy extends Policy {

	public VmPoolPolicy() {
		addRequiredCapability(AppPoolManager.class);
		addRequiredCapability(HostPoolManager.class);
		addRequiredCapability(VmPoolManager.class);
	}
	
	public void execute(HostStatusEvent event) {
		VmPoolManager vmPool = manager.getCapability(VmPoolManager.class);
		for (VmStatus status : event.getHostStatus().getVms()) {
			vmPool.getVm(status.getId()).setCurrentStatus(status);
		}
	}
	
	public void execute(IncomingMigrationEvent event) {
		VmPoolManager vmPool = manager.getCapability(VmPoolManager.class);
		HostPoolManager hostPool = manager.getCapability(HostPoolManager.class);
		Map<Integer, Host> targetHostMap = event.getTargetHosts();
		for (VmData vm : event.getVms()) {
			vm.updateHost(hostPool.getHost(targetHostMap.get(vm.getId()).getId()));
			vmPool.addVm(vm);
		}
	}
	
	public void execute(MigrationCompleteEvent event) {
		VmPoolManager vmPool = manager.getCapability(VmPoolManager.class);
		HostData targetHost = manager.getCapability(HostPoolManager.class).getHost(event.getTargetHostId());
		if (null != targetHost) {		// Target Host is local.
			vmPool.getVm(event.getVmId()).updateHost(targetHost);
		}
		else {		// Target Host is remote. Remove VM from pool.
			vmPool.removeVm(event.getVmId());
		}
	}
	
	public void execute(VmInstantiationCompleteEvent event) {
		manager.getCapability(VmPoolManager.class).addVm(new VmData(event.getVmId(),
				manager.getCapability(AppPoolManager.class).getApplication(event.getApplicationId()).getTask(event.getTaskId()),
				manager.getCapability(HostPoolManager.class).getHost(event.getHostId())));
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
