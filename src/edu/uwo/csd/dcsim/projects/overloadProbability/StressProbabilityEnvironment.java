package edu.uwo.csd.dcsim.projects.overloadProbability;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import edu.uwo.csd.dcsim.DataCentre;
import edu.uwo.csd.dcsim.application.Application;
import edu.uwo.csd.dcsim.application.ApplicationGenerator;
import edu.uwo.csd.dcsim.application.InteractiveApplication;
import edu.uwo.csd.dcsim.application.loadbalancer.LoadBalancer;
import edu.uwo.csd.dcsim.application.sla.InteractiveServiceLevelAgreement;
import edu.uwo.csd.dcsim.application.workload.TraceWorkload;
import edu.uwo.csd.dcsim.common.ObjectBuilder;
import edu.uwo.csd.dcsim.common.SimTime;
import edu.uwo.csd.dcsim.common.Tuple;
import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.host.Cluster;
import edu.uwo.csd.dcsim.host.Host;
import edu.uwo.csd.dcsim.host.HostModels;
import edu.uwo.csd.dcsim.host.Rack;
import edu.uwo.csd.dcsim.host.Resources;
import edu.uwo.csd.dcsim.host.SwitchFactory;
import edu.uwo.csd.dcsim.host.resourcemanager.DefaultResourceManagerFactory;
import edu.uwo.csd.dcsim.host.scheduler.DefaultResourceSchedulerFactory;
import edu.uwo.csd.dcsim.management.AutonomicManager;
import edu.uwo.csd.dcsim.management.events.ApplicationPlacementEvent;

public abstract class StressProbabilityEnvironment {

	public static final long ARRIVAL_SYNC_INTERVAL = SimTime.minutes(1);
	
	public static final int N_APP_TEMPLATES = 4;
	public static final int N_TRACES = 3; 
	public static final String[] TRACES = {"traces/clarknet", 
		"traces/epa",
		"traces/sdsc",
		"traces/google_cores_job_type_0", 
		"traces/google_cores_job_type_1",
		"traces/google_cores_job_type_2",
		"traces/google_cores_job_type_3"};	
	public static final long[] OFFSET_MAX = {200000000, 40000000, 40000000, 15000000, 15000000, 15000000, 15000000};
	public static final double[] TRACE_AVG = {0.32, 0.25, 0.32, 0.72, 0.74, 0.77, 0.83};
	public static final long APP_RAMPUP_TIME = SimTime.hours(6);
	
//	public static final int VM_CORES = 2;
//	public static final int VM_CORE_CAPACITY = 2500;
//	public static final int VM_MEM = 1635; //1024;
	public static final int VM_BW = 0;
	public static final int VM_STORE = 0;
	
	public static final int[] VM_SIZES = {2500, 2500, 2500};
	public static final int[] VM_CORES = {2, 1, 2};
	public static final int[] VM_RAM = {1024, 1024, 1024};
	public static final int N_VM_SIZES = 1;
	
	int hostsPerRack;
	int nRacks;
	int nClusters;
	int nApps = 0;
	Simulation simulation;
	AutonomicManager dcAM;
	ObjectBuilder<LoadBalancer> loadBalancerBuilder;
	Random envRandom;
	Random appGenerationRandom;
	boolean halfSize = false;
		
	public StressProbabilityEnvironment(Simulation simulation, int hostsPerRack, int nRacks, int nClusters, ObjectBuilder<LoadBalancer> loadBalancerBuilder, boolean halfSize) {
		this.simulation = simulation;
		this.hostsPerRack = hostsPerRack;
		this.nRacks = nRacks;
		this.nClusters = nClusters;
		
		envRandom = new Random(simulation.getRandom().nextLong());
		appGenerationRandom = new Random(simulation.getRandom().nextLong());
		this.loadBalancerBuilder = loadBalancerBuilder;

		this.halfSize = halfSize;
		
	}
	
	public DataCentre createDataCentre(Simulation simulation) {
		// Create data centre.
		
		Host.Builder proLiantDL160G5E5420;
		Host.Builder proLiantDL380G5QuadCore;
		
		//Define Hosts
		if (!halfSize) {
		proLiantDL160G5E5420 = HostModels.ProLiantDL160G5E5420(simulation).privCpu(500).privBandwidth(131072)
				.resourceManagerFactory(new DefaultResourceManagerFactory())
				.resourceSchedulerFactory(new DefaultResourceSchedulerFactory());
		
		proLiantDL380G5QuadCore = HostModels.ProLiantDL380G5QuadCore(simulation).privCpu(500).privBandwidth(131072)
				.resourceManagerFactory(new DefaultResourceManagerFactory())
				.resourceSchedulerFactory(new DefaultResourceSchedulerFactory());
		} else{
			proLiantDL160G5E5420 = HostModels.HalfProLiantDL160G5E5420(simulation).privCpu(500).privBandwidth(131072)
					.resourceManagerFactory(new DefaultResourceManagerFactory())
					.resourceSchedulerFactory(new DefaultResourceSchedulerFactory());
			
			proLiantDL380G5QuadCore = HostModels.HalfProLiantDL380G5QuadCore(simulation).privCpu(500).privBandwidth(131072)
					.resourceManagerFactory(new DefaultResourceManagerFactory())
					.resourceSchedulerFactory(new DefaultResourceSchedulerFactory());
		}
		
		//Define Racks
		SwitchFactory switch10g48p = new SwitchFactory(10000000, 48, 100);
		
		//uses proLiantDL160G5E5420
		Rack.Builder seriesA = new Rack.Builder(simulation).nSlots(40).nHosts(hostsPerRack)
				.hostBuilder(proLiantDL160G5E5420)
				.switchFactory(switch10g48p);
		
		//uses proLiantDL380G5QuadCore
		Rack.Builder seriesB = new Rack.Builder(simulation).nSlots(40).nHosts(hostsPerRack)
				.hostBuilder(proLiantDL380G5QuadCore)
				.switchFactory(switch10g48p);
		
		// Define Cluster
		SwitchFactory switch40g24p = new SwitchFactory(40000000, 24, 100);
		
		Cluster.Builder series09 = new Cluster.Builder(simulation).nRacks(nRacks).nSwitches(1)
				.rackBuilder(seriesA)
				.switchFactory(switch40g24p);
		
		Cluster.Builder series11 = new Cluster.Builder(simulation).nRacks(nRacks).nSwitches(1)
				.rackBuilder(seriesB)
				.switchFactory(switch40g24p);
		
		
		DataCentre dc = new DataCentre(simulation, switch40g24p);
		simulation.addDatacentre(dc);
		
		dcAM = new AutonomicManager(simulation);

		processDcAM(dcAM);
		
		// Create clusters in data centre.
		for (int i = 0; i < nClusters; i++) {
			if (i % 2 == 0)
				dc.addCluster(series09.build());
			else
				dc.addCluster(series11.build());
		}
		
//		dc.addCluster(series09.build());
		
		for (Cluster cluster : dc.getClusters()) {
			for (Rack rack : cluster.getRacks()) {
				for (Host host : rack.getHosts()) {
					processHost(host, rack, cluster, dc, dcAM);
				}
			}
		}
	
		return dc;
	}
	
	public abstract void processDcAM(AutonomicManager dcAM);
	
	public abstract void processHost(Host host, Rack rack, Cluster cluster, DataCentre dc, AutonomicManager dcAM);
	
	public Application createApplication() {
		++nApps;
		
		int trace = appGenerationRandom.nextInt(N_TRACES);		
		TraceWorkload workload = new TraceWorkload(simulation, 
				TRACES[trace], 
				(long)(appGenerationRandom.nextDouble() * OFFSET_MAX[trace]));
		
		workload.setRampUp(APP_RAMPUP_TIME);
		
		InteractiveApplication.Builder appBuilder;
		
			
		int cores = VM_CORES[nApps % N_VM_SIZES];
		int coreCapacity = VM_SIZES[nApps % N_VM_SIZES];
		int memory = VM_RAM[nApps % N_VM_SIZES];
		int bandwidth = VM_BW;	// 100 Mb/s
		int storage = VM_STORE;	// 1 GB
		
		appBuilder = new InteractiveApplication.Builder(simulation).thinkTime(4)
				.task(1, 1, new Resources(cores, coreCapacity, memory, bandwidth, storage), 0.01, 1, loadBalancerBuilder);
		
		InteractiveApplication app = appBuilder.build();
		app.setWorkload(workload);
		
		workload.setScaleFactor(app.calculateMaxWorkloadResponseTimeLimit(0.9)); //1s response time SLA

		InteractiveServiceLevelAgreement sla = new InteractiveServiceLevelAgreement(app).responseTime(1, 1); //sla limit at 1s response time, penalty rate of 1 per second in violation
		app.setSla(sla);
		
		processApplication(app);
		
		return app;
	}
	
	public abstract void processApplication(InteractiveApplication application);
	
	public AutonomicManager getDcAM() {
		return dcAM;
	}
	
	
	/*
	 * APPLICATION GENERATION
	 * 
	 */
	
	public void configureStaticApplications(Simulation simulation, int nApps) {
		ArrayList<Application> applications = new ArrayList<Application>();
		for (int i = 0; i < nApps; ++i) {
			applications.add(this.createApplication());
		}
		simulation.sendEvent(new ApplicationPlacementEvent(dcAM, applications));
	}

	/**
	 * Configure applications to arrive such that the overall utilization of the datacentre changes randomly.
	 * @param simulation
	 * @param dc
	 * @param changesPerDay The number of utilization changes (arrival rate changes) per day
	 * @param minServices The minimum number of services running in the data centre
	 * @param maxServices The maximum number of services running in the data centre
	 */
	public void configureRandomApplications(Simulation simulation, double changesPerDay, int minServices, int maxServices, long rampUpTime, long startTime, long duration) {

		/*
		 * Configure minimum service level. Create the minimum number of services over the first 40 hours,
		 * and leave them running for the entire simulation.
		 */
		ArrayList<Tuple<Long, Double>> serviceRates = new ArrayList<Tuple<Long, Double>>();
		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), (minServices / SimTime.toHours(rampUpTime))));		
		serviceRates.add(new Tuple<Long, Double>(rampUpTime, 0d));		
		serviceRates.add(new Tuple<Long, Double>(duration, 0d));		// 10 days
		
		ApplicationGenerator appGenerator = new AppManApplicationGenerator(simulation, dcAM, null, serviceRates);
		appGenerator.start();
		
		//Create a uniform random distribution to generate the number of services within the data centre.
		UniformIntegerDistribution serviceCountDist = new UniformIntegerDistribution(0, (maxServices - minServices));
		serviceCountDist.reseedRandomGenerator(simulation.getRandom().nextLong());
		
		/*
		 * Generate the service arrival rates for the rest of the simulation
		 */
		long time;		//start time of the current arrival rate
		long nextTime;	//the time of the next arrival rate change
		double rate;	//the current arrival rate
		serviceRates = new ArrayList<Tuple<Long, Double>>(); //list of arrival rates
		
		time = startTime; //start at beginning of 3rd day (end of 2nd)
		
		//loop while we still have simulation time to generate arrival rates for
		while (time < duration) {

			//calculate the next time the rate will changes
			nextTime = time + Math.round(SimTime.days(1) / changesPerDay);
			
			//generate a target VM count to reach by the next rate change
			double target = serviceCountDist.sample();
			
			//caculate the current arrival rate necessary to reach the target VM count
			rate = target / ((nextTime - time) / 1000d / 60d / 60d);
			
			//add the current rate to the list of arrival rates
			serviceRates.add(new Tuple<Long, Double>(time, rate));
			
			//advance to the next time interval
			time = nextTime;
		}
		//add a final rate of 0 to run until the end of the simulation
		serviceRates.add(new Tuple<Long, Double>(duration, 0d));
		
		appGenerator = new AppManApplicationGenerator(simulation, dcAM, new NormalDistribution(SimTime.days(1) / changesPerDay, SimTime.hours(1)), serviceRates);
		//appGenerator = new AppManApplicationGenerator(simulation, dcAM, new NormalDistribution(SimTime.days(5), SimTime.hours(1)), serviceRates);
		appGenerator.start();
	}
	
	public class AppManApplicationGenerator extends ApplicationGenerator {
		
		int id;
		
		public AppManApplicationGenerator(Simulation simulation, AutonomicManager dcTarget, RealDistribution lifespanDist, List<Tuple<Long, Double>> servicesPerHour) {
			super(simulation, dcTarget, lifespanDist, servicesPerHour);
			
			this.setArrivalSyncInterval(ARRIVAL_SYNC_INTERVAL);
		}

		@Override
		public Application buildApplication() {
			return createApplication();
		}
		
		
	}
	
}
