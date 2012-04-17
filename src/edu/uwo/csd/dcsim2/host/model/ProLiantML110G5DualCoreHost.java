package edu.uwo.csd.dcsim2.host.model;

import edu.uwo.csd.dcsim2.host.*;
import edu.uwo.csd.dcsim2.host.resourcemanager.*;
import edu.uwo.csd.dcsim2.host.power.*;
import edu.uwo.csd.dcsim2.host.scheduler.*;

public class ProLiantML110G5DualCoreHost extends Host {

	private static int nCpu = 1;
	private static int nCores = 2;
	private static int coreCapacity = 2660;
	private static int memory = 4096; //4GB
	private static int bandwidth = 131072 * 2; //1 Gb + 1Gb for management TODO poor assumption!
	private static long storage = 36864; //36GB
	private static HostPowerModel powerModel = new SPECHostPowerModel(10, 93.7, 97, 101, 105, 110, 116, 121, 125, 129, 133, 135);
	
	public ProLiantML110G5DualCoreHost(CpuManager cpuManager,
			MemoryManager memoryManager,
			BandwidthManager bandwidthManager,
			StorageManager storageManager,
			CpuScheduler cpuScheduler) {
		super(nCpu, nCores, coreCapacity, memory, bandwidth, storage, cpuManager, memoryManager, bandwidthManager, storageManager, cpuScheduler, powerModel);
	}
	
}