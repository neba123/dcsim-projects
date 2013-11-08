package edu.uwo.csd.dcsim.projects.applicationManagement;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.uwo.csd.dcsim.DataCentre;
import edu.uwo.csd.dcsim.SimulationExecutor;
import edu.uwo.csd.dcsim.SimulationTask;
import edu.uwo.csd.dcsim.application.Application;
import edu.uwo.csd.dcsim.application.ApplicationListener;
import edu.uwo.csd.dcsim.application.InteractiveApplication;
import edu.uwo.csd.dcsim.application.TaskInstance;
import edu.uwo.csd.dcsim.application.loadbalancer.ShareLoadBalancer;
import edu.uwo.csd.dcsim.common.SimTime;
import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.host.Cluster;
import edu.uwo.csd.dcsim.host.Host;
import edu.uwo.csd.dcsim.host.Rack;
import edu.uwo.csd.dcsim.management.AutonomicManager;
import edu.uwo.csd.dcsim.management.capabilities.*;
import edu.uwo.csd.dcsim.management.events.ApplicationPlacementEvent;
import edu.uwo.csd.dcsim.management.policies.*;
import edu.uwo.csd.dcsim.projects.applicationManagement.capabilities.ApplicationManager;
import edu.uwo.csd.dcsim.projects.applicationManagement.capabilities.ApplicationPoolManager;
import edu.uwo.csd.dcsim.projects.applicationManagement.capabilities.DataCentreManager;
import edu.uwo.csd.dcsim.projects.applicationManagement.capabilities.TaskInstanceManager;
import edu.uwo.csd.dcsim.projects.applicationManagement.policies.*;

public class AutoscaleReallocationIntegratedExperiment extends SimulationTask {

	private static Logger logger = Logger.getLogger(AutoscaleReallocationIntegratedExperiment.class);
	
	private static final long DURATION = SimTime.days(1);
//	private static final long DURATION = SimTime.minutes(5);
	private static final long METRIC_RECORD_START = SimTime.days(0);
	
	private static final int RACK_SIZE = 5; //40
	private static final int N_RACKS = 4; //5
	private static final int N_APPS = 20; //50
	
	private static final long[] randomSeeds = {6198910678692541341l,
		5646441053220106016l,
		-5705302823151233610l,
		8289672009575825404l,
		-4637549055860880177l,
		-4280782692131378509l,
		-1699811527182374894l,
		-6452776964812569334l,
		-7148920787255940546l,
		8311271444423629559l};
	private static final long N_SEEDS = 1;
	
	public static void main(String args[]) {
		Simulation.initializeLogging();

		PrintStream printStream;
		try {
			printStream = new PrintStream("out_integrated");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		//runSimulationSet(out, slaWarningThreshold, slaSafeThreshold, cpuSafeThreshold, upper, target, lower)
		
		//with SLA - SLA (true, 0.3, 0.2, 0.3, 0.9)
			//90 - 85 - 40
		runSimulationSet(printStream, 0.3, 0.2, 0.3, 0.9, 0.85, 0.4, 2, 12);		
	
		printStream.close();
		
	}
	
	public static void runSimulationSet(PrintStream out, 
			double slaWarningThreshold, 
			double slaSafeThreshold,
			double cpuSafeThreshold,
			double upper,
			double target,
			double lower,
			double stressWindow,
			double underutilWindow) {
		
		logger.info("Started New Simulation Set");
		logger.info(upper + "," + target + "," + lower + "," + slaWarningThreshold + "," + slaSafeThreshold + "," + "," + cpuSafeThreshold + "," + stressWindow  + "," + underutilWindow);
		
		List<SimulationTask> completedTasks;
		SimulationExecutor executor = new SimulationExecutor();
		for (int i = 0; i < N_SEEDS; ++i)  {
			AutoscaleReallocationIntegratedExperiment e = new AutoscaleReallocationIntegratedExperiment("integrated-" + (i + 1), randomSeeds[i]);
			e.setParameters(slaWarningThreshold, slaSafeThreshold, cpuSafeThreshold, upper, target, lower, stressWindow, underutilWindow);
			executor.addTask(e);
		}
		
		completedTasks = executor.execute(4);
		
		for(SimulationTask task : completedTasks) {
			logger.info(task.getName());
			task.getMetrics().printDefault(logger);
		}
		
//		//output CSV
//		out.println("Autoscale+Reallocation Experiment");
//		out.println("upper=" + upper + " | target=" + target + " | lower=" + lower +
//				" | slaWarning=" + slaWarningThreshold + " | slaSafe=" + slaSafeThreshold + 
//				" | cpuSafe=" + cpuSafeThreshold +
//				" | stressWindow=" + stressWindow +
//				" | underutilWindow=" + underutilWindow);
//		
//		for(SimulationTask task : completedTasks) {
//			if (completedTasks.indexOf(task) == 0) {
//				task.getMetrics().printCSV(out);
//			} else {
//				task.getMetrics().printCSV(out, false);
//			}
//		}
//		out.println("");
//		out.println("");
//		
//		out.flush();
		
	}
	
	public AutoscaleReallocationIntegratedExperiment(String name) {
		super(name, DURATION);
		this.setMetricRecordStart(METRIC_RECORD_START);
	}
	
	public AutoscaleReallocationIntegratedExperiment(String name, long randomSeed) {
		super(name, DURATION);
		this.setMetricRecordStart(METRIC_RECORD_START);
		this.setRandomSeed(randomSeed);
	}

	private double slaWarningThreshold = 0.8;
	private double slaSafeThreshold = 0.6;
	private long scaleDownFreeze = SimTime.minutes(60);
	private double cpuSafeThreshold = 0.5;
	private int shortWindow = 5;
	private int longWindow = 30;
	private long scalingInterval = SimTime.minutes(5);
	private double upper = 0.90;
	private double target = 0.85;
	private double lower = 0.60;
	private double stressWindow = 2; //* 5 min intervals = 10 min
	private double underutilWindow = 12; //* 5 min intervals = 60 min
	
	public void setParameters(double slaWarningThreshold, 
			double slaSafeThreshold,
			double cpuSafeThreshold,
			double upper,
			double target,
			double lower,
			double stressWindow,
			double underutilWindow) {
		
		this.slaWarningThreshold = slaWarningThreshold;
		this.slaSafeThreshold = slaSafeThreshold;
		this.cpuSafeThreshold = cpuSafeThreshold;
		this.upper = upper;
		this.target = target;
		this.lower = lower;
		this.stressWindow = stressWindow;
		this.underutilWindow = underutilWindow;
		
	}
	
	@Override
	public void setup(Simulation simulation) {
		
		simulation.getSimulationMetrics().addCustomMetricCollection(new ApplicationManagementMetrics(simulation));
		
		Environment environment = new Environment(simulation, RACK_SIZE, N_RACKS);
		environment.createDataCentre(simulation);
		
		ArrayList<Application> applications = new ArrayList<Application>();
		for (int i = 0; i < N_APPS; ++i) {
			applications.add(environment.createApplication());
		}
		simulation.sendEvent(new ApplicationPlacementEvent(environment.getDcAM(), applications));
	}
	
	public class Environment extends AppManagementTestEnvironment {

		DataCentreManager hostPool;
		ApplicationPoolManager applicationPool;
		
		public Environment(Simulation simulation, int hostsPerRack, int nRacks) {
			super(simulation, hostsPerRack, nRacks, new ShareLoadBalancer.Builder());
		}

		@Override
		public void processDcAM(AutonomicManager dcAM) {
			
			hostPool = new DataCentreManager();
			dcAM.addCapability(hostPool);
			hostPool.setAutonomicManager(dcAM);
			applicationPool = new ApplicationPoolManager(shortWindow, longWindow);
			dcAM.addCapability(applicationPool);
			applicationPool.setAutonomicManager(dcAM);
			
			dcAM.installPolicy(new DcHostStatusPolicy(10));
			dcAM.installPolicy(new IntegratedApplicationPlacementPolicy(lower, upper, target));
			
			ApplicationManagementPolicy appManagementPolicy = new ApplicationManagementPolicy(lower, upper, target);
			appManagementPolicy.setParameters(slaWarningThreshold, slaSafeThreshold, scaleDownFreeze, cpuSafeThreshold, stressWindow, underutilWindow);
			dcAM.installPolicy(appManagementPolicy, scalingInterval, 0);
		}

		@Override
		public void processHost(Host host, Rack rack, Cluster cluster, DataCentre dc, AutonomicManager dcAM) {
			AutonomicManager hostAM = new AutonomicManager(simulation, new HostManager(host));
			hostAM.installPolicy(new HostMonitoringPolicy(dcAM), SimTime.minutes(5), SimTime.minutes(envRandom.nextInt(5)));
			hostAM.installPolicy(new HostOperationsPolicy());

			host.installAutonomicManager(hostAM);
			
			hostPool.addHost(host, hostAM);
		}

		@Override
		public void processApplication(InteractiveApplication application) {
			applicationPool.addApplication(application);
			
			application.addApplicationListener(new ManagedApplicationListener(simulation, applicationPool));
		}
		
	}
	
	public class ManagedApplicationListener implements ApplicationListener {

		ApplicationPoolManager applicationPoolManager;
		Simulation simulation;
		
		public ManagedApplicationListener(Simulation simulation, ApplicationPoolManager applicationPoolManager) {
			this.simulation = simulation;
			this.applicationPoolManager = applicationPoolManager;
		}
		
		@Override
		public void onCreateTaskInstance(TaskInstance taskInstance) {
			AutonomicManager instanceManager = new AutonomicManager(simulation, new TaskInstanceManager(taskInstance));
			instanceManager.installPolicy(new TaskInstanceMonitoringPolicy(applicationPoolManager), SimTime.minutes(1), simulation.getSimulationTime());
			
			applicationPoolManager.getApplicationData(taskInstance.getTask().getApplication()).addInstanceManager(taskInstance, instanceManager);
		}

		@Override
		public void onRemoveTaskInstance(TaskInstance taskInstance) {
			//shutdown the instance manager
			
			applicationPoolManager.getApplicationData(taskInstance.getTask().getApplication()).getInstanceManagers().get(taskInstance).shutdown();
			
			//remove the instance manager from the list
			applicationPoolManager.getApplicationData(taskInstance.getTask().getApplication()).removeInstanceManager(taskInstance);	
		}

		@Override
		public void onShutdownApplication(Application application) {
			applicationPoolManager.removeApplication(application);
		}
		
	}

}