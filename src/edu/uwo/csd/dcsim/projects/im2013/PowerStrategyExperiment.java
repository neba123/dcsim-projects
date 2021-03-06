package edu.uwo.csd.dcsim.projects.im2013;

import java.util.Collection;

import org.apache.log4j.Logger;

import edu.uwo.csd.dcsim.*;
import edu.uwo.csd.dcsim.common.*;
import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.management.AutonomicManager;
import edu.uwo.csd.dcsim.projects.im2013.policies.*;

/**
 * This class serves to test the set of policies that conform the Power Strategy:
 * 
 * + VmPlacementPolicyFFDPower
 * + VmRelocationPolicyFFIDDPower
 * + VmConsolidationPolicyFFDDIPower
 *   
 * @author Gaston Keller
 *
 */
public class PowerStrategyExperiment extends SimulationTask {

	private static Logger logger = Logger.getLogger(PowerStrategyExperiment.class);
	
	public PowerStrategyExperiment(String name, long randomSeed) {
		super(name, SimTime.days(10));					// 10-day simulation
		this.setMetricRecordStart(SimTime.days(2));		// start on 3rd day (i.e., after 2 days)
		this.setRandomSeed(randomSeed);
	}

	@Override
	public void setup(Simulation simulation) {
		// Set utilization thresholds.
		double lower = 0.6;
		double upper = 0.95;	// 0.90
		double target = 0.90;	// 0.85
		
		// Create data centre and its manager.
		Tuple<DataCentre, AutonomicManager> tuple = IM2013TestEnvironment.createDataCentre(simulation);
		AutonomicManager dcAM = tuple.b;
		
		// Create and install management policies for the data centre.
		dcAM.installPolicy(new VmPlacementPolicyFFDPower(lower, upper, target));
		dcAM.installPolicy(new VmRelocationPolicyFFIDDPower(lower, upper, target), SimTime.minutes(10), SimTime.minutes(10) + 1);
		dcAM.installPolicy(new VmConsolidationPolicyFFDDIPower(lower, upper, target), SimTime.hours(1), SimTime.hours(1) + 2);
		
		// Create and start ServiceProducer.
//		IM2013TestEnvironment.configureStaticServices(simulation, dcAM);
//		IM2013TestEnvironment.configureDynamicServices(simulation, dcAM);
		IM2013TestEnvironment.configureRandomServices(simulation, dcAM, 1, 600, 1600);
	}
	
	public static void main(String args[]) {
		
		Simulation.initializeLogging();
		
		Collection<SimulationTask> completedTasks;
		SimulationExecutor executor = new SimulationExecutor();
		
		executor.addTask(new PowerStrategyExperiment("power-1", 6198910678692541341l));
//		executor.addTask(new PowerStrategyExperiment("power-2", 5646441053220106016l));
//		executor.addTask(new PowerStrategyExperiment("power-3", -5705302823151233610l));
//		executor.addTask(new PowerStrategyExperiment("power-4", 8289672009575825404l));
//		executor.addTask(new PowerStrategyExperiment("power-5", -4637549055860880177l));
		
		completedTasks = executor.execute();
		
		for(SimulationTask task : completedTasks) {
			logger.info(task.getName());
			task.getMetrics().printDefault(logger);
		}
	}

}
