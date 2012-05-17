package edu.uwo.csd.dcsim.management;

import edu.uwo.csd.dcsim.core.*;

/**
 * A policy to manage some aspect of the DataCentre.
 * 
 * @author Michael Tighe
 *
 */
public abstract class ManagementPolicy implements SimulationEventListener {

	public static final int MANAGEMENT_POLICY_EXECUTE_EVENT = 1;
	
	protected Simulation simulation;
	private long firstEvent = 0;
	
	public ManagementPolicy(Simulation simulation) {
		this(simulation, 0);
	}
	
	public ManagementPolicy(Simulation simulation, long firstEvent) {
		this.simulation = simulation;
		//schedule initial update event
		this.firstEvent = firstEvent;
		
		simulation.sendEvent(new Event(ManagementPolicy.MANAGEMENT_POLICY_EXECUTE_EVENT, firstEvent, this, this));
	}
	
	public abstract void execute();
	public abstract long getNextExecutionTime();
	public abstract void processEvent(Event e);
	
	protected long getFirstEvent() {
		return firstEvent;
	}
	
	@Override
	public void handleEvent(Event e) {
		if (e.getType() == MANAGEMENT_POLICY_EXECUTE_EVENT) {
			execute();
			simulation.sendEvent(new Event(ManagementPolicy.MANAGEMENT_POLICY_EXECUTE_EVENT, getNextExecutionTime(), this, this));
		} else {
			processEvent(e);
		}
	}

	
}
