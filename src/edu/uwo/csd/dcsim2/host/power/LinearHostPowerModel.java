package edu.uwo.csd.dcsim2.host.power;

import edu.uwo.csd.dcsim2.host.Host;

public class LinearHostPowerModel implements HostPowerModel {

	double idlePower;
	double maxPower;
	
	public LinearHostPowerModel(double idlePower, double maxPower) {
		this.idlePower = idlePower;
		this.maxPower = maxPower;
	}
	
	@Override
	public double getPowerConsumption(Host host) {
		return idlePower + ((maxPower - idlePower) * host.getCpuManager().getCpuUtilization());
	}
	
}