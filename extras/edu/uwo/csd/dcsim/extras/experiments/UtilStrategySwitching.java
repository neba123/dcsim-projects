package edu.uwo.csd.dcsim.extras.experiments;

import java.util.Collection;

import org.apache.log4j.*;

import edu.uwo.csd.dcsim.*;
import edu.uwo.csd.dcsim.common.SimTime;
import edu.uwo.csd.dcsim.core.*;
import edu.uwo.csd.dcsim.extras.policies.*;
import edu.uwo.csd.dcsim.management.VMPlacementPolicy;

/**
 * This simulation switching strategies between a power-friendly and SLA-friendly strategy.
 * 
 * @author Michael Tighe and Graham Foster
 *
 */
public class UtilStrategySwitching extends DCSimulationTask {

	private static Logger logger = Logger.getLogger(FullStrategySwitching.class);
	
	private double toPowerThreshold, toSlaThreshold;
	
	public static void main(String args[]) {
		
		Simulation.initializeLogging();
		
		Collection<DCSimulationTask> completedTasks;
		SimulationExecutor<DCSimulationTask> executor = new SimulationExecutor<DCSimulationTask>();
		
		executor.addTask(new UtilStrategySwitching("strat-switching-1", 6198910678692541341l));
//		executor.addTask(new UtilStrategySwitching("strat-switching-2", 5646441053220106016l));
//		executor.addTask(new UtilStrategySwitching("strat-switching-3", -5705302823151233610l));
//		executor.addTask(new UtilStrategySwitching("strat-switching-4", 8289672009575825404l));
//		executor.addTask(new UtilStrategySwitching("strat-switching-5", -4637549055860880177l));
		
		completedTasks = executor.execute();
		
		for(DCSimulationTask task : completedTasks) {
			logger.info(task.getName());
			IM2012TestEnvironment.printMetrics(task.getResults());
			
			DCSimulationTraceWriter traceWriter = new DCSimulationTraceWriter(task);
			traceWriter.writeTrace();
		}

	}

	/**
	 * Constructs a Strategy Switching task with default thresholds
	 * @param name
	 * @param randomSeed
	 */
	public UtilStrategySwitching(String name, long randomSeed) {
		super(name, SimTime.days(10));					// 10-day simulation
		this.setMetricRecordStart(SimTime.days(2));	// start on 3rd day (i.e. after 2 days)
		this.setRandomSeed(randomSeed);
		this.toPowerThreshold = 0;
		this.toSlaThreshold = 0;
	}
	
	/**
	 * Constructs a Strategy Switching task with supplied thresholds for switching on utilization slope
	 * @param name
	 * @param randomSeed
	 * @param toPowerThreshold threshold which would cause a switch to the power strategy
	 * @param toSlaThreshold threshold which would case a switch to the sla strategy
	 */
	public UtilStrategySwitching(String name, long randomSeed, double toPowerThreshold, double toSlaThreshold){
		super(name, SimTime.days(10));					// 10-day simulation
		this.setMetricRecordStart(SimTime.days(2));	// start on 3rd day (i.e. after 2 days)
		this.setRandomSeed(randomSeed);
		this.toPowerThreshold = toPowerThreshold;
		this.toSlaThreshold = toSlaThreshold;
	}
	 

	@Override
	public void setup(DataCentreSimulation simulation) {

		// Create data centre (with default VM Placement policy).
		DataCentre dc = IM2012TestEnvironment.createDataCentre(simulation);
		simulation.addDatacentre(dc);
		
		// Create CPU load utilization monitor (for use with policy-sets).
		DCUtilizationMonitor hostsMon = new DCUtilizationMonitor(simulation, SimTime.minutes(2), 5, dc);
		simulation.addMonitor(hostsMon);

		// Create utilization monitor for strategy switching
		DCUtilizationMonitor dcMon = new DCUtilizationMonitor(simulation, SimTime.minutes(2), 15, dc);
		simulation.addMonitor(dcMon);

		// Create and start ServiceProducer.
//		IM2012TestEnvironment.configureStaticServices(simulation, dc);
		IM2012TestEnvironment.configureDynamicServices(simulation, dc);
		
		
		/*
		 * Create Power-friendly Strategy
		 */
		
		// Set utilization thresholds.
		double powerLower = 0.6;
		double powerUpper = 0.95;	// 0.90
		double powerTarget = 0.90;	// 0.85
		
		// Create and set desired VM Placement policy for the data centre.
		VMPlacementPolicy powerVMPlacementPolicy = new VMPlacementPolicyFFDGreen(simulation, dc, hostsMon, powerLower, powerUpper, powerTarget);
		
		// Relocation policy
		VMRelocationPolicyFFIDGreen powerRelocationPolicy = new VMRelocationPolicyFFIDGreen(dc, hostsMon, powerLower, powerUpper, powerTarget);
		DaemonScheduler powerRelocationPolicyDaemon = new FixedIntervalDaemonScheduler(simulation, 600000, powerRelocationPolicy);
		
		// Consolidation policy
		VMConsolidationPolicyFFDDIGreen powerConsolidationPolicy = new VMConsolidationPolicyFFDDIGreen(dc, hostsMon, powerLower, powerUpper, powerTarget);
		DaemonScheduler powerConsolidationPolicyDaemon = new FixedIntervalDaemonScheduler(simulation, 3600000, powerConsolidationPolicy);

		DaemonSchedulerGroup powerDaemonGroup = new DaemonSchedulerGroup(simulation);
		powerDaemonGroup.addDaemon(powerRelocationPolicyDaemon, 600000);
		powerDaemonGroup.addDaemon(powerConsolidationPolicyDaemon, 3601000);
		
		
		
		/*
		 * Create SLA-friendly Strategy
		 */
		
		// Set utilization thresholds.
		double slaLower = 0.6;
		double slaUpper = 0.85;
		double slaTarget = 0.8;
		
		// Create and set desired VM Placement policy for the data centre.
		VMPlacementPolicy slaVMPlacementPolicy = new VMPlacementPolicyFFMSla(simulation, dc, hostsMon, slaLower, slaUpper, slaTarget);
		
		// Relocation policy
		VMRelocationPolicyFFIMSla slaRelocationPolicy = new VMRelocationPolicyFFIMSla(dc, hostsMon, slaLower, slaUpper, slaTarget);
		DaemonScheduler slaRelocationPolicyDaemon = new FixedIntervalDaemonScheduler(simulation, 600000, slaRelocationPolicy);
		
		// Consolidation policy
		VMConsolidationPolicyFFDMISla slaConsolidationPolicy = new VMConsolidationPolicyFFDMISla(dc, hostsMon, slaLower, slaUpper, slaTarget);
		DaemonScheduler slaConsolidationPolicyDaemon = new FixedIntervalDaemonScheduler(simulation, 14400000, slaConsolidationPolicy);

		DaemonSchedulerGroup slaDaemonGroup = new DaemonSchedulerGroup(simulation);
		slaDaemonGroup.addDaemon(slaRelocationPolicyDaemon, 600000);
		slaDaemonGroup.addDaemon(slaConsolidationPolicyDaemon, 14401000);
		
		/*
		 * Configure strategy switching
		 */
		UtilStrategySwitchPolicy switchingPolicy = new UtilStrategySwitchPolicy.Builder(dc, dcMon)
			.slaPolicy(slaDaemonGroup, slaVMPlacementPolicy)
			.powerPolicy(powerDaemonGroup, powerVMPlacementPolicy)
			.startingPolicy(powerDaemonGroup)
			.toPowerThreshold(toPowerThreshold)
			.toSlaThreshold(toSlaThreshold)
			.build();
		
		DaemonScheduler policyDaemon = new FixedIntervalDaemonScheduler(simulation, SimTime.hours(1), switchingPolicy);
		policyDaemon.start(SimTime.hours(1) - SimTime.seconds(1)); 
	}

}
