package edu.uwo.csd.dcsim.application.sla;

import edu.uwo.csd.dcsim.application.InteractiveApplication;

public class InteractiveServiceLevelAgreement implements ServiceLevelAgreement {

	InteractiveApplication application;
	float responseTime;
	float throughput;
	double responseTimePenalty = 0;
	double throughputPenalty = 0;
	
	public InteractiveServiceLevelAgreement(InteractiveApplication application) {
		this.application = application;
		this.responseTime  = Float.MAX_VALUE;
		this.throughput = Float.MAX_VALUE;
	}
	
	//Usage: InteractiveServiceLevelAgreement i = new InteractiveServiceLevelAgreement(application).responseTime(1f).throughput(2.5f);
	public InteractiveServiceLevelAgreement responseTime(float responseTime) {
		this.responseTime = responseTime;
		return this;
	}
	
	public InteractiveServiceLevelAgreement responseTime(float responseTime, double penaltyRate) {
		this.responseTime = responseTime;
		this.responseTimePenalty = penaltyRate;
		return this;
	}
	
	public InteractiveServiceLevelAgreement throughput(float throughput) {
		this.throughput = throughput;
		return this;
	}
	
	public InteractiveServiceLevelAgreement throughput(float throughput, double penaltyRate) {
		this.throughput = throughput;
		this.throughputPenalty = penaltyRate;
		return this;
	}
	
	@Override
	public boolean evaluate() {

		if (application.getResponseTime() > responseTime) return false;
		
		if (application.getThroughput() > throughput) return false;

		return true;
	}

	@Override
	public double calculatePenalty() {
		double penalty = 0;
		
		if (application.getResponseTime() > responseTime) {
			penalty += application.getSimulation().getElapsedSeconds() * responseTimePenalty;
		}
		
		if (application.getThroughput() > throughput) {
			penalty += application.getSimulation().getElapsedSeconds() * throughputPenalty;
		}
		
		return penalty;
	}
	
}
