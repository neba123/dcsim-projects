package edu.uwo.csd.dcsim.projects.hierarchical.manfi2014;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.*;

import edu.uwo.csd.dcsim.DataCentre;
import edu.uwo.csd.dcsim.application.*;
import edu.uwo.csd.dcsim.application.sla.InteractiveServiceLevelAgreement;
import edu.uwo.csd.dcsim.application.workload.*;
import edu.uwo.csd.dcsim.common.*;
import edu.uwo.csd.dcsim.core.Simulation;
import edu.uwo.csd.dcsim.host.*;
import edu.uwo.csd.dcsim.host.resourcemanager.DefaultResourceManagerFactory;
import edu.uwo.csd.dcsim.host.scheduler.DefaultResourceSchedulerFactory;
import edu.uwo.csd.dcsim.management.AutonomicManager;
import edu.uwo.csd.dcsim.projects.hierarchical.HierarchicalMetrics;

/**
 * This class serves to create a common virtualized data centre environment in 
 * which to run experiments for ManFI 2014.
 * 
 * @author Gaston Keller
 *
 */
public class ManFI2014TestEnvironment {

	public static final long ARRIVAL_SYNC_INTERVAL = SimTime.minutes(1);
	
	public static final int N_CLUSTERS = 5;
	public static final int N_RACKS = 4;
	public static final int N_HOSTS = 10;
	
	public static final int[] VM_SIZES = {1500, 2500, 2500};
	public static final int[] VM_CORES = {1, 1, 2};
	public static final int[] VM_RAM = {512, 1024, 1024};
	public static final int N_VM_SIZES = 3;
//	public static final int[] VM_SIZES = {1500, 2500, 3000, 3000};
//	public static final int[] VM_CORES = {1, 1, 1, 2};
//	public static final int[] VM_RAM = {512, 1024, 1024, 1024};
//	public static final int N_VM_SIZES = 4;
	
	public static final int N_TRACES = 5; 
	public static final String[] TRACES = {"traces/clarknet", 
		"traces/epa",
		"traces/sdsc",
		"traces/google_cores_job_type_0", 
		"traces/google_cores_job_type_1",
		"traces/google_cores_job_type_2",
		"traces/google_cores_job_type_3"};	
	public static final long[] OFFSET_MAX = {200000000, 40000000, 40000000, 15000000, 15000000, 15000000, 15000000};
//	public static final double[] TRACE_AVG = {0.32, 0.25, 0.32, 0.72, 0.74, 0.77, 0.83};
	
	public ManFI2014TestEnvironment() {
		// TODO Auto-generated constructor stub
	}
	
	public static HierarchicalMetrics getHierarchicalMetrics(Simulation simulation) {
		HierarchicalMetrics metrics = simulation.getSimulationMetrics().getCustomMetricCollection(HierarchicalMetrics.class);
		
		if (metrics == null) {
			metrics = new HierarchicalMetrics(simulation);
			simulation.getSimulationMetrics().addCustomMetricCollection(metrics);
			return metrics;
		} else {
			return (HierarchicalMetrics)metrics;
		}
	}
	
	
	/**
	 * Creates a data centre. The data centre is organized in Clusters, which consist of Racks, 
	 * which in turn consist of Hosts.
	 */
	public static DataCentre createInfrastructure(Simulation simulation) {
		// Define Switch types.
		SwitchFactory switch10g48p = new SwitchFactory(10000000, 48, 100);
		SwitchFactory switch40g24p = new SwitchFactory(40000000, 24, 100);
		
		// Switches defined according to Mahadevan2009.
		SwitchFactory edgeSwitch = new SwitchFactory(1000000, 48, 102);	// 1 Gbps
		SwitchFactory coreSwitch = new SwitchFactory(1000000, 48, 656);	// 1 Gbps
		
		// Define Host types.
		Host.Builder proLiantDL380G5QuadCore = HostModels.ProLiantDL380G5QuadCore(simulation).privCpu(500).privBandwidth(131072)
				.resourceManagerFactory(new DefaultResourceManagerFactory())
				.resourceSchedulerFactory(new DefaultResourceSchedulerFactory());
		
		Host.Builder proLiantDL160G5E5420 = HostModels.ProLiantDL160G5E5420(simulation).privCpu(500).privBandwidth(131072)
				.resourceManagerFactory(new DefaultResourceManagerFactory())
				.resourceSchedulerFactory(new DefaultResourceSchedulerFactory());
		
		// Define Rack types.
		Rack.Builder seriesA = new Rack.Builder(simulation).nSlots(40).nHosts(N_HOSTS)
				.hostBuilder(proLiantDL380G5QuadCore)
				.switchFactory(edgeSwitch);
//				.switchFactory(switch10g48p);
		
		Rack.Builder seriesB = new Rack.Builder(simulation).nSlots(40).nHosts(N_HOSTS)
				.hostBuilder(proLiantDL160G5E5420)
				.switchFactory(edgeSwitch);
//				.switchFactory(switch10g48p);
		
		// Define Cluster types.
		Cluster.Builder series09 = new Cluster.Builder(simulation).nRacks(N_RACKS).nSwitches(1)
				.rackBuilder(seriesA)
				.switchFactory(coreSwitch);
//				.switchFactory(switch40g24p);
		
		Cluster.Builder series11 = new Cluster.Builder(simulation).nRacks(N_RACKS).nSwitches(1)
				.rackBuilder(seriesB)
				.switchFactory(coreSwitch);
//				.switchFactory(switch40g24p);
		
		// Create data centre.
		DataCentre dc = new DataCentre(simulation, coreSwitch);
//		DataCentre dc = new DataCentre(simulation, switch40g24p);
		simulation.addDatacentre(dc);
		
		// Create clusters in data centre.
		for (int i = 0; i < N_CLUSTERS; i++) {
			if (i % 2 == 0)
				dc.addCluster(series09.build());
			else
				dc.addCluster(series11.build());
		}
		
		return dc;
	}
	
	/**
	 * Creates a Service Producer to spawn new services over time and thus populate the data centre. 
	 * The services respond to the single-tier interactive service model.
	 */
	public static void configureStaticServices(Simulation simulation, AutonomicManager dcAM, boolean legacy) {
		Object serviceProducer = null;
		
		// Create a service rate _trace_ for the ServiceProducer.
		ArrayList<Tuple<Long, Double>> serviceRates = new ArrayList<Tuple<Long, Double>>();
		
		// EXP 1A
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(40), 0d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
		
		// EXP 1B
		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(80), 0d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(12), 0d));
		
		// EXP 1C
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(144), 0d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.days(14), 0d));
		
		// EXP 1D
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(160), 0d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.days(15), 0d));
		
		if (legacy) {
			serviceProducer = new VmProducer(simulation, dcAM, null, serviceRates);
			((ApplicationGeneratorLegacy) serviceProducer).start();
		}
		else {
			serviceProducer = new ServiceProducer(simulation, dcAM, null, serviceRates);
			((ApplicationGenerator) serviceProducer).start();
		}
	}
	
	/**
	 * Creates Service Producers to spawn services over time in such a manner as to dynamically vary 
	 * the number of services within the simulation over time, according to a fixed plan.
	 */
	public static void configureDynamicServices(Simulation simulation, AutonomicManager dcAM, boolean legacy) {
		Object serviceProducer = null;
		
		/*
		 * 1. Create ~800 Services (VMs) over first 80 hours. These Services do not terminate.
		 * 2. Simulation recording starts after 4 days.
		 * 3. Hold on ~800 Services for day 5.
		 * 4. Increase from ~800 to ~1280 throughout days 6 & 7.
		 * 5. Hold on ~1280 for day 8.
		 * 6. Increase from ~1280 to ~1520 throughout day 9.
		 * 7. Hold on ~1520 for day 10.
		 * 8. Decrease from ~1520 to ~1280 throughout day 11.
		 * 9. Hold on ~1280 for day 12.
		 * 10. Complete 8 days of recorded simulation.
		 */
		
		/*
		 * Configure and start the base 800 services which do not terminate.
		 */
		ArrayList<Tuple<Long, Double>> serviceRates = new ArrayList<Tuple<Long, Double>>();
		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(80), 0d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(12), 0d));
		
		if (legacy) {
			serviceProducer = new VmProducer(simulation, dcAM, null, serviceRates);
			((ApplicationGeneratorLegacy) serviceProducer).start();
		}
		else {
			serviceProducer = new ServiceProducer(simulation, dcAM, null, serviceRates);
			((ApplicationGenerator) serviceProducer).start();
		}
		
		/*
		 * Create time varying service levels. Each service has a lifespan of ~6 days, normally distributed with a std. dev. of 2 hours
		 */
		serviceRates = new ArrayList<Tuple<Long, Double>>();
		
		//Day 6: Create ~240 new services throughout the day, for a total of ~1040.
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(5), 10d));
		
		//Day 7: Create ~240 new services throughout the day, for a total of ~1280.
		
		//Day 8: Hold at ~1280.
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(7), 0d));
		
		//Day 9: Create ~240 new services throughout the day, for a total of ~1520.
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(8), 10d));
		
		//Day 10: Create ~240 new services throughout the day, for a total of ~1520 -- services from Day 6 terminate throughout the day.
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(9), 0d));
		
		//Day 11: Load goes down to ~1280 -- services from Day 7 terminate throughout the day.
//		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
		
		//Day 12: Hold at ~1280.
		
		if (legacy) {
			serviceProducer = new VmProducer(simulation, dcAM, new NormalDistribution(SimTime.days(4), SimTime.hours(6)), serviceRates);
			((ApplicationGeneratorLegacy) serviceProducer).start();
		}
		else {
			serviceProducer = new ServiceProducer(simulation, dcAM, new NormalDistribution(SimTime.days(4), SimTime.hours(6)), serviceRates);
			((ApplicationGenerator) serviceProducer).start();
		}	
	}
	
	/**
	 * Configure services to arrival such that the overall utilization of the datacentre changes randomly.
	 * @param simulation
	 * @param dc
	 * @param changesPerDay The number of utilization changes (arrival rate changes) per day
	 * @param minServices The minimum number of services running in the data centre
	 * @param maxServices The maximum number of services running in the data centre
	 */
	public static void configureRandomServices(Simulation simulation, AutonomicManager dcAM, double changesPerDay, int minServices, int maxServices, boolean legacy) {
		Object serviceProducer = null;
		
		/*
		 * Configure minimum service level. Create a base of ~800 services over the first 80 hours,
		 * and leave them running for the entire simulation.
		 */
		ArrayList<Tuple<Long, Double>> serviceRates = new ArrayList<Tuple<Long, Double>>();
		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));	
		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(80), 0d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(12), 0d));
		
		if (legacy) {
			serviceProducer = new VmProducer(simulation, dcAM, null, serviceRates);
			((ApplicationGeneratorLegacy) serviceProducer).start();
		}
		else {
			serviceProducer = new ServiceProducer(simulation, dcAM, null, serviceRates);
			((ApplicationGenerator) serviceProducer).start();
		}
		
		//Create a uniform random distribution to generate the number of services within the data centre.
		UniformIntegerDistribution serviceCountDist = new UniformIntegerDistribution(0, (maxServices - minServices));
//		UniformIntegerDistribution serviceCountDist = new UniformIntegerDistribution(0, 150);
		serviceCountDist.reseedRandomGenerator(simulation.getRandom().nextLong());
		
		/*
		 * Generate the service arrival rates for the rest of the simulation
		 */
		long time;		//start time of the current arrival rate
		long nextTime;	//the time of the next arrival rate change
		double rate;	//the current arrival rate
		serviceRates = new ArrayList<Tuple<Long, Double>>(); //list of arrival rates
		
		time = SimTime.days(4); //start at beginning of 5th day (end of 4th)
		
		//loop while we still have simulation time to generate arrival rates for
		while (time < SimTime.days(12)) {

			//calculate the next time the rate will changes
			nextTime = time + Math.round(SimTime.days(1) / changesPerDay);
			
			//generate a target VM count to reach by the next rate change
			double target = serviceCountDist.sample();
			
			//calculate the current arrival rate necessary to reach the target VM count
			rate = target / ((nextTime - time) / 1000d / 60d / 60d);
			
			//add the current rate to the list of arrival rates
			serviceRates.add(new Tuple<Long, Double>(time, rate));
			
			//advance to the next time interval
			time = nextTime;
		}
		//add a final rate of 0 to run until the end of the simulation
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(12), 0d));
		
		
		for (Tuple<Long, Double> t : serviceRates) {
			System.out.println(SimTime.toHours(t.a) + " - " + t.b);
		}
		
		
		if (legacy) {
			serviceProducer = new VmProducer(simulation, dcAM, new NormalDistribution(SimTime.days(1) / changesPerDay, SimTime.hours(1)), serviceRates);
			((ApplicationGeneratorLegacy) serviceProducer).start();
		}
		else {
			serviceProducer = new ServiceProducer(simulation, dcAM, new NormalDistribution(SimTime.days(1) / changesPerDay, SimTime.hours(1)), serviceRates);
			((ApplicationGenerator) serviceProducer).start();
		}
	}
	
	/**
	 * Creates services to submit and deploy in the data centre.
	 */
	public static class VmProducer extends ApplicationGeneratorLegacy {

		private int counter = 0;
		
		public VmProducer(Simulation simulation, AutonomicManager dcTarget, RealDistribution lifespanDist, List<Tuple<Long, Double>> servicesPerHour) {
			super(simulation, dcTarget, lifespanDist, servicesPerHour);
		}

		@Override
		public Application buildApplication() {
			++counter;
			
			String trace = TRACES[counter % N_TRACES];
			long offset = (int)(simulation.getRandom().nextDouble() * OFFSET_MAX[counter % N_TRACES]);
			
			int cores = VM_CORES[counter % N_VM_SIZES];
			int coreCapacity = VM_SIZES[counter % N_VM_SIZES];
			int memory = VM_RAM[counter % N_VM_SIZES];
			int bandwidth = 12800;	// 100 Mb/s
			int storage = 1024;	// 1 GB
			
			// Create workload (external) for the service.
			TraceWorkload workload = new TraceWorkload(simulation, trace, offset); //scale to n replicas
			
			InteractiveApplication application = Applications.singleTaskInteractiveApplication(simulation, workload, cores, coreCapacity, memory, bandwidth, storage, 0.01);
			
			//workload.setScaleFactor(application.calculateMaxWorkloadUtilizationLimit(0.98));
			
			workload.setScaleFactor(application.calculateMaxWorkloadResponseTimeLimit(0.9)); //1s response time SLA
			InteractiveServiceLevelAgreement sla = new InteractiveServiceLevelAgreement(application).responseTime(1, 1); //sla limit at 1s response time, penalty rate of 1 per second in violation
			application.setSla(sla);
			
			return application;
		}
		
	}
	
	/**
	 * Creates services (applications) to submit and deploy in the data centre.
	 */
	public static class ServiceProducer extends ApplicationGenerator {
		
		private int counter = 0;
		
		public ServiceProducer(Simulation simulation, AutonomicManager dcTarget, RealDistribution lifespanDist, List<Tuple<Long, Double>> servicesPerHour) {
			super(simulation, dcTarget, lifespanDist, servicesPerHour);
			
			this.setArrivalSyncInterval(ARRIVAL_SYNC_INTERVAL);
		}
		
		@Override
		public Application buildApplication() {
			++counter;
			
			String trace = TRACES[counter % N_TRACES];
			long offset = (int)(simulation.getRandom().nextDouble() * OFFSET_MAX[counter % N_TRACES]);
			
			int cores = VM_CORES[counter % N_VM_SIZES];
			int coreCapacity = VM_SIZES[counter % N_VM_SIZES];
			int memory = VM_RAM[counter % N_VM_SIZES];
			int bandwidth = 12800;	// 100 Mb/s
			int storage = 1024;	// 1 GB
			
			// Create workload (external) for the service.
			TraceWorkload workload = new TraceWorkload(simulation, trace, offset); //scale to n replicas
			
			InteractiveApplication application = Applications.singleTaskInteractiveApplication(simulation, workload, cores, coreCapacity, memory, bandwidth, storage, 0.01);
			
			//workload.setScaleFactor(application.calculateMaxWorkloadUtilizationLimit(0.98));
			
			workload.setScaleFactor(application.calculateMaxWorkloadResponseTimeLimit(0.9)); //1s response time SLA
			InteractiveServiceLevelAgreement sla = new InteractiveServiceLevelAgreement(application).responseTime(1, 1); //sla limit at 1s response time, penalty rate of 1 per second in violation
			application.setSla(sla);
			
			return application;
		}
	}

}
