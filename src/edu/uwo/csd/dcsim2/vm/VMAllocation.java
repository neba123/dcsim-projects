package edu.uwo.csd.dcsim2.vm;

import java.util.Vector;

public class VMAllocation {

	VM vm;
	VMDescription vmDescription;
	
	private Vector<Integer> vCoreCapacityAlloc;
	private Vector<Integer> vCoreCapacityScheduled;
	private int memoryAlloc;	
	private int bandwidthAlloc;
	private long storageAlloc;
	
	public VMAllocation(VMDescription vmDescription) {
		this.vmDescription = vmDescription;
		vCoreCapacityAlloc = new Vector<Integer>();
		vCoreCapacityScheduled = new Vector<Integer>();
		vm = null;
	}
	
	public void setVm(VM vm) {
		this.vm = vm;
	}
	
	public VM getVm() {
		return vm;
	}
	
	public VMDescription getVMDescription() {
		return vmDescription;
	}
	
	public Vector<Integer> getVCoreCapacityAlloc() {
		return vCoreCapacityAlloc;
	}
	
	public Vector<Integer> getVCoreCapacityScheduled() {
		return vCoreCapacityScheduled;
	}
	
	public void setMemoryAlloc(int memoryAlloc) {
		this.memoryAlloc = memoryAlloc;
	}
	
	public int getMemoryAlloc() {
		return memoryAlloc;
	}
	
	public void setBandwidthAlloc(int bandwidthAlloc) {
		this.bandwidthAlloc = bandwidthAlloc;
	}
	
	public int getBandwidthAlloc() {
		return bandwidthAlloc;
	}
	
	public void setStorageAlloc(long storageAlloc) {
		this.storageAlloc = storageAlloc;
	}
	
	public long getStorageAlloc() {
		return storageAlloc;
	}
	
}
