package edu.uwo.csd.dcsim.projects.svm2013;

import java.util.Collection;

import org.apache.log4j.Logger;

import edu.uwo.csd.dcsim.*;
import edu.uwo.csd.dcsim.common.*;
import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.management.AutonomicManager;
import edu.uwo.csd.dcsim.projects.centralized.policies.*;

/**
 * This class serves to evaluate the Reactive Hybrid Strategy in the test 
 * environment for the SVM 2013 paper. The strategy consists of the following 
 * policies:
 * 
 * + VmPlacementPolicyFFMHybrid
 * + VmRelocationPolicyHybridReactive
 * + VmConsolidationPolicyFFDDIHybrid
 * 
 * The VM Placement policy runs as needed, while the VM Consolidation policy 
 * runs periodically. The VM Relocation policy is triggered with every Host 
 * Status Update: a Stress Check is performed on the Host and if the check is 
 * positive, the VM Relocation process is started.
 * 
 * @author Gaston Keller
 *
 */
public class ReactiveHybridStrategyExperiment extends SimulationTask {

	private static Logger logger = Logger.getLogger(ReactiveHybridStrategyExperiment.class);
	
	public ReactiveHybridStrategyExperiment(String name, long randomSeed) {
		super(name, SimTime.days(10));					// 10-day simulation
		this.setMetricRecordStart(SimTime.days(2));		// start on 3rd day (i.e., after 2 days)
		this.setRandomSeed(randomSeed);
	}

	@Override
	public void setup(Simulation simulation) {
		// Set utilization thresholds.
		double lower = 0.60;
		double upper = 0.90;
		double target = 0.85;
		
		// Create data centre and its manager.
		Tuple<DataCentre, AutonomicManager> tuple = SVM2013TestEnvironment.createDataCentre(simulation);
		AutonomicManager dcAM = tuple.b;
		
		// Create and install policy to manage Host status updates.
		dcAM.installPolicy(new ReactiveHostStatusPolicy(5));
		
		// Create and install management policies for the data centre.
		dcAM.installPolicy(new VmPlacementPolicyFFMHybrid(lower, upper, target));
		dcAM.installPolicy(new VmRelocationPolicyHybridReactive(lower, upper, target));
		dcAM.installPolicy(new VmConsolidationPolicyFFDDIHybrid(lower, upper, target), SimTime.hours(1), SimTime.hours(1) + 2);
		
		// Create and start ServiceProducer.
		SVM2013TestEnvironment.configureRandomServices(simulation, dcAM, 1, 600, 1600);
	}
	
	public static void main(String args[]) {
		
		Simulation.initializeLogging();
		
		Collection<SimulationTask> completedTasks;
		SimulationExecutor executor = new SimulationExecutor();
		
		executor.addTask(new ReactiveHybridStrategyExperiment("svm2013-reactive-hybrid-1", 6198910678692541341l));
//		executor.addTask(new HybridStrategyExperiment("svm2013-reactive-periodic-hybrid-2", 5646441053220106016l));
//		executor.addTask(new HybridStrategyExperiment("svm2013-reactive-periodic-hybrid-3", -5705302823151233610l));
//		executor.addTask(new HybridStrategyExperiment("svm2013-reactive-periodic-hybrid-4", 8289672009575825404l));
//		executor.addTask(new HybridStrategyExperiment("svm2013-reactive-periodic-hybrid-5", -4637549055860880177l));
		
		completedTasks = executor.execute();
		
		for(SimulationTask task : completedTasks) {
			logger.info(task.getName());
			task.getMetrics().printDefault(logger);
		}
	}

}
