
public class BusMESI {
	int busIndex;
	int currentIndex;
	public BusMESI(){
		busIndex = 0;
		currentIndex = 0;
	}
	
	public int getIndex(){
		return busIndex++;
	}
	public int getCurrentIndex(){
		return currentIndex;
	}
	public int nextRequest(){
		if(currentIndex < busIndex){
			currentIndex++;
			return currentIndex;
		} else {
			return -1;
		}
	}

	public int readMissRequest(long MemoryAddr, int CoreId){
		CacheSimulatorMESI.Cores.get(CoreId).SnoopyMsgNum++; //added
		printMessage(CoreId, "read miss");
		for(int i = 0; i < CacheSimulatorMESI.Cores.size(); i++){
			if(CacheSimulatorMESI.Cores.get(i).CoreId == CoreId){
				continue;
			}
			if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr) == null){
				continue;
			}
			//if here, then must be the cores that is the other id and block is in the same one
			if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status == 1){  //modified
				CacheSimulatorMESI.L2CacheMESI.makeRequest(1, MemoryAddr, CacheSimulatorMESI.GlobalCycle, CoreId);//write back
				CacheSimulatorMESI.Cores.get(i).DataMsgNum++; //added
				CacheSimulatorMESI.L2CacheMESI.makeRequest(0, MemoryAddr, CacheSimulatorMESI.GlobalCycle, CoreId);
				CacheSimulatorMESI.Cores.get(i).DataMsgNum++; //added
				return -1;
			} else if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status == 2){ //shared
				return i;//may change cycle time
			}else if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status == 3){ //shared
				CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status = 2;
				CacheSimulatorMESI.Cores.get(i).DataMsgNum++;
				CacheSimulatorMESI.Cores.get(CoreId).DataMsgNum++;
				return i;//may change cycle time
			}
			
		}
		CacheSimulatorMESI.L2CacheMESI.makeRequest(0, MemoryAddr, CacheSimulatorMESI.GlobalCycle, CoreId);
		CacheSimulatorMESI.Cores.get(CoreId).DataMsgNum++;
		return -2;
	}

	public int writeRequest(long MemoryAddr, int CoreId, int modeCode){ //modeCode is determine if it is invalid or shared
		if(modeCode == 2){ //shared
			printMessage(CoreId, "write hit shared");
			CacheSimulatorMESI.L2CacheMESI.makeRequest(1, MemoryAddr, CacheSimulatorMESI.GlobalCycle, CoreId);
			CacheSimulatorMESI.Cores.get(CoreId).DataMsgNum++;//added
			CacheSimulatorMESI.Cores.get(CoreId).SnoopyMsgNum++;//added
			
			for(int i = 0; i < CacheSimulatorMESI.Cores.size(); i++){
				if(CacheSimulatorMESI.Cores.get(i).CoreId == CoreId){
					continue;
				}
				if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr) == null){  //invalid other blocks
					continue;
				}
				if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status == 2){  //invalid other blocks
					CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status = 0;
				}
			}
			return 1;
		} else { //invalid
			printMessage(CoreId, "write miss");
			CacheSimulatorMESI.Cores.get(CoreId).SnoopyMsgNum++;//added
			for(int i = 0; i < CacheSimulatorMESI.Cores.size(); i++){
				if(CacheSimulatorMESI.Cores.get(i).CoreId == CoreId){
					continue;
				}
				CacheSimulatorMESI.Cores.get(i).SnoopyMsgNum++;
				if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr) == null){
					continue;
				}
				if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status == 2){  //invalid other blocks
					CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status = 0;
				}
				if(CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status == 1){
					CacheSimulatorMESI.Cores.get(i).SnoopyMsgNum++;
					CacheSimulatorMESI.L2CacheMESI.makeRequest(1, MemoryAddr, CacheSimulatorMESI.GlobalCycle, CoreId);
					CacheSimulatorMESI.Cores.get(i).L1CacheMESI.getBlock(MemoryAddr).status = 0;
				}
			}
			if(CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr) == null){
				CacheSimulatorMESI.Cores.get(CoreId).SnoopyMsgNum++;
				CacheSimulatorMESI.L2CacheMESI.makeRequest(0, MemoryAddr, CacheSimulatorMESI.GlobalCycle, CoreId);
				return -1;//waiting for the read
			} else {
				CacheSimulatorMESI.Cores.get(CoreId).SnoopyMsgNum++;
				CacheSimulatorMESI.L2CacheMESI.makeRequest(1, MemoryAddr, CacheSimulatorMESI.GlobalCycle, CoreId);
				return 1;
			}
		}
	}
	public boolean checkResult(long MemoryAddr, int CoreId){
		if(CacheSimulatorMESI.resultOfL2[(int) (MemoryAddr % CacheSimulatorMESI.B)] == CoreId){
			return true; 
		}
		return false;
	}
	public void printMessage(int CoreId, String Message){
		if(CacheSimulatorMESI.debuggingmode){
			CacheSimulatorMESI.messages.add("Cycle: " + CacheSimulatorMESI.GlobalCycle + " | Core: " + CoreId + " | " + Message);
		}
	}
}
