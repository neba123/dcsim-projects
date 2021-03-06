package edu.uwo.csd.dcsim.projects.hierarchical;

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

/**
 * This class serves to create a common virtualized data centre environment in 
 * which to run experiments for NOMS 2014.
 * 
 * @author Gaston Keller
 *
 */
public class HierarchicalTestEnvironment {

	public static final int N_CLUSTERS = 5;
	public static final int N_RACKS = 4;
	public static final int N_HOSTS = 10;
	
	public static final int CPU_OVERHEAD = 200;
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
	public static final double[] TRACE_AVG = {0.32, 0.25, 0.32, 0.72, 0.74, 0.77, 0.83};
	
	public HierarchicalTestEnvironment() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Creates a data centre. The data centre is organized in Clusters, which consist of Racks, 
	 * which in turn consist of Hosts.
	 */
	public static DataCentre createInfrastructure(Simulation simulation) {
		// Define Switch types.
		SwitchFactory switch10g48p = new SwitchFactory(10000000, 48, 100);
		SwitchFactory switch40g24p = new SwitchFactory(40000000, 24, 100);
		
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
				.switchFactory(switch10g48p);
		
		Rack.Builder seriesB = new Rack.Builder(simulation).nSlots(40).nHosts(N_HOSTS)
				.hostBuilder(proLiantDL160G5E5420)
				.switchFactory(switch10g48p);
		
		// Define Cluster types.
		Cluster.Builder series09 = new Cluster.Builder(simulation).nRacks(N_RACKS).nSwitches(1)
				.rackBuilder(seriesA)
				.switchFactory(switch40g24p);
		
		Cluster.Builder series11 = new Cluster.Builder(simulation).nRacks(N_RACKS).nSwitches(1)
				.rackBuilder(seriesB)
				.switchFactory(switch40g24p);
		
		// Create data centre.
		DataCentre dc = new DataCentre(simulation, switch40g24p);
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
	public static void configureStaticServices(Simulation simulation, AutonomicManager dcAM) {
		// Create a service rate _trace_ for the ServiceProducer.
		ArrayList<Tuple<Long, Double>> serviceRates = new ArrayList<Tuple<Long, Double>>();
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));		// Create ~400 VMs.
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 30d));		// Create ~1200 VMs.
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 40d));		// Create ~1600 VMs.
//		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(40), 0d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
		
		// Config for following experiments:
		// public static final int N_CLUSTERS = 5;
		// public static final int N_RACKS = 4;
		// public static final int N_HOSTS = 10;
		
		// EXP 1: 10-day exp. / log 6th / ~1200 VMs / failed alloc after day 3
/*		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(120), 0d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
*/
		
		// EXP 2: 10-day exp. / log 6th / ~400 VMs / no failure
		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(40), 0d));
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
		
		// EXP 3: 10-day exp. / log 4th / ~700 VMs / no failure
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(70), 0d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
		
		// EXP 4: 10-day exp. / log 5th / ~900 VMs / no failure
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(90), 0d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
		
		// Config for following experiments:
		// public static final int N_CLUSTERS = 2;
		// public static final int N_RACKS = 3;
		// public static final int N_HOSTS = 4;
		
		// EXP 5: 5-day exp. / log 2nd / ~200 VMs / ...
//		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 10d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(20), 0d));
//		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
		
		ApplicationGenerator serviceProducer = new ServiceProducer(simulation, dcAM, null, serviceRates);
		serviceProducer.start();
	}
	
	/**
	 * Creates Service Producers to spawn services over time in such a manner as to dynamically vary 
	 * the number of services within the simulation over time, according to a fixed plan.
	 */
	public static void configureDynamicServices(Simulation simulation, AutonomicManager dcAM) {
		
		/*
		 * 1. Create 600 Services (VMs) over first 40 hours. These Services to not terminate.
		 * 2. Simulation recording starts after 2 days
		 * 3. Hold on 600 Services for day 3
		 * 4. Increase from 600 to 1200 throughout day 4
		 * 5. Hold on 1200 for day 5
		 * 6. Decrease from 1200 to 800 throughout day 6
		 * 7. Hold on 800 for day 7
		 * 8. Increase from 800 to 1600 throughout day 8
		 * 9. Hold on 1600 for day 9
		 * 10. Decrease from 1600 to 600 for day 10
		 * 11. Complete 8 days of recorded simulation
		 */
		
		/*
		 * Configure and start the base 600 services which do not terminate
		 */
		ArrayList<Tuple<Long, Double>> serviceRates = new ArrayList<Tuple<Long, Double>>();
		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), 15d));		// Create ~600 VMs.
		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(40), 0d));		// over 40 hours
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));		// 10 days
		
		//ApplicationGeneratorLegacy serviceProducer = new NOMSServiceProducer(simulation, dcAM, null, serviceRates);
		ApplicationGenerator serviceProducer = new ServiceProducer(simulation, dcAM, null, serviceRates);
		serviceProducer.start();

		/*
		 * Create time varying service levels. Each service has a lifespan of ~2 days, normally distributed with a std. dev. of 2 hours
		 */
		serviceRates = new ArrayList<Tuple<Long, Double>>();
		
		//Day 4: Create 600 new services throughout the day
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(3), 25d));
		
		//Day 5: Hold at 1200
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(4), 0d));

		//Day 6: Create 200 new services, which combined with the termination of previous will bring us down to 800
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(5), 8.3d));
		
		//Day 7: Hold at 800
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(6), 0d));
		
		//Day 8: Create 1000 new services throughout the day
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(7), 41.6d));
		
		//Day 9: Hold at 1600
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(8), 0d));
		
		//Day 10: Let servers terminate until left with base 600
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));
		
		//serviceProducer = new NOMSServiceProducer(simulation, dcAM, new NormalDistribution(SimTime.days(2), SimTime.hours(2)), serviceRates);
		serviceProducer = new ServiceProducer(simulation, dcAM, new NormalDistribution(SimTime.days(2), SimTime.hours(2)), serviceRates);
		serviceProducer.start();	
	}
	
	/**
	 * Configure services to arrival such that the overall utilization of the datacentre changes randomly.
	 * @param simulation
	 * @param dc
	 * @param changesPerDay The number of utilization changes (arrival rate changes) per day
	 * @param minServices The minimum number of services running in the data centre
	 * @param maxServices The maximum number of services running in the data centre
	 */
	public static void configureRandomServices(Simulation simulation, AutonomicManager dcAM, double changesPerDay, int minServices, int maxServices) {

		/*
		 * Configure minimum service level. Create the minimum number of services over the first 40 hours,
		 * and leave them running for the entire simulation.
		 */
		ArrayList<Tuple<Long, Double>> serviceRates = new ArrayList<Tuple<Long, Double>>();
		serviceRates.add(new Tuple<Long, Double>(SimTime.seconds(1), (minServices / 40d)));		
		serviceRates.add(new Tuple<Long, Double>(SimTime.hours(40), 0d));		
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));		// 10 days
		
		//ApplicationGeneratorLegacy serviceProducer = new NOMSServiceProducer(simulation, dcAM, null, serviceRates);
		ApplicationGenerator serviceProducer = new ServiceProducer(simulation, dcAM, null, serviceRates);
		serviceProducer.start();
		
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
		
		time = SimTime.days(2); //start at beginning of 3rd day (end of 2nd)
		
		//loop while we still have simulation time to generate arrival rates for
		while (time < SimTime.days(10)) {

			//calculate the next time the rate will changes
			nextTime = time + Math.round(SimTime.days(1) / changesPerDay);
			
			//generate a target VM count to reach by the next rate change
			double target = serviceCountDist.sample();
			
			//caculate the current arrival rate necessary to reach the target VM count
			rate = target / ((nextTime - time) / 1000d / 60d / 60d);
			
			//add the current rate to the list of arrival rates
			serviceRates.add(new Tuple<Long, Double>(time, rate));
			
			//advance to the next time intrerval
			time = nextTime;
		}
		//add a final rate of 0 to run until the end of the simulation
		serviceRates.add(new Tuple<Long, Double>(SimTime.days(10), 0d));

		
		//serviceProducer = new NOMSServiceProducer(simulation, dcAM, new NormalDistribution(SimTime.days(1) / changesPerDay, SimTime.hours(1)), serviceRates);
		serviceProducer = new ServiceProducer(simulation, dcAM, new NormalDistribution(SimTime.days(1) / changesPerDay, SimTime.hours(1)), serviceRates);
		serviceProducer.start();
	}
	
	/**
	 * Creates services to submit and deploy in the data centre.
	 */
	public static class NOMSServiceProducer extends ApplicationGeneratorLegacy {

		private int counter = 0;
		
		public NOMSServiceProducer(Simulation simulation, AutonomicManager dcTarget, RealDistribution lifespanDist, List<Tuple<Long, Double>> servicesPerHour) {
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
			workload.setScaleFactor(application.calculateMaxWorkloadUtilizationLimit(0.98));
			
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
