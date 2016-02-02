
public class Bus {
	int busIndex;
	int currentIndex;
	public Bus(){
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
		CacheSimulator.Cores.get(CoreId).SnoopyMsgNum++; //added
		printMessage(CoreId, "read miss");
		for(int i = 0; i < CacheSimulator.Cores.size(); i++){
			if(CacheSimulator.Cores.get(i).CoreId == CoreId){
				continue;
			}
			CacheSimulator.Cores.get(i).SnoopyMsgNum++; //added
			if(CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr) == null){
				continue;
			}
			//if here, then must be the cores that is the other id and block is in the same one
			if(CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr).status == 1){  //modified
				CacheSimulator.L2Cache.makeRequest(1, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);//write back
				CacheSimulator.Cores.get(i).DataMsgNum++; //added
				CacheSimulator.L2Cache.makeRequest(0, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
				CacheSimulator.Cores.get(i).DataMsgNum++;//added
				return -1;
			} else if(CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr).status == 2){ //shared
				CacheSimulator.Cores.get(i).DataMsgNum++;
				CacheSimulator.Cores.get(CoreId).DataMsgNum++;
				return i;//may change cycle time
			}
		}
		CacheSimulator.L2Cache.makeRequest(0, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
		CacheSimulator.Cores.get(CoreId).DataMsgNum++;
		return -2;
	}

	public int writeRequest(long MemoryAddr, int CoreId, int modeCode){ //modeCode is determine if it is invalid or shared
		if(modeCode == 2){ //shared
			printMessage(CoreId, "write hit shared");
			CacheSimulator.L2Cache.makeRequest(1, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
			CacheSimulator.Cores.get(CoreId).DataMsgNum++;//added
			CacheSimulator.Cores.get(CoreId).SnoopyMsgNum++;//added
			for(int i = 0; i < CacheSimulator.Cores.size(); i++){
				if(CacheSimulator.Cores.get(i).CoreId == CoreId){
					continue;
				}
				CacheSimulator.Cores.get(i).SnoopyMsgNum++;
				if(CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr) == null){  //invalid other blocks
					continue;
				}
				if(CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr).status == 2){  //invalid other blocks
					CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr).status = 0;
				}
			}
			return 1;
		} else { //invalid
			printMessage(CoreId, "write miss");
			CacheSimulator.Cores.get(CoreId).SnoopyMsgNum++;//added
			for(int i = 0; i < CacheSimulator.Cores.size(); i++){
				if(CacheSimulator.Cores.get(i).CoreId == CoreId){
					continue;
				}
				CacheSimulator.Cores.get(i).SnoopyMsgNum++;
				if(CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr) == null){
					continue;
				}
				if(CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr).status == 2){  //invalid other blocks
					CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr).status = 0;
				}
				if(CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr).status == 1){
					CacheSimulator.Cores.get(i).DataMsgNum++;
					CacheSimulator.L2Cache.makeRequest(1, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
					CacheSimulator.Cores.get(i).L1Cache.getBlock(MemoryAddr).status = 0;
				}
			}
			if(CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr) == null){
				CacheSimulator.Cores.get(CoreId).DataMsgNum++;//added
				CacheSimulator.L2Cache.makeRequest(0, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
				return -1;//waiting for the read
			} else {
				CacheSimulator.Cores.get(CoreId).DataMsgNum++;//added
				CacheSimulator.L2Cache.makeRequest(1, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
				return 1;
			}
		}
	}
	public boolean checkResult(long MemoryAddr, int CoreId){
		if(CacheSimulator.resultOfL2[(int) (MemoryAddr % CacheSimulator.B)] == CoreId){
			return true; 
		}
		return false;
	}
	public void printMessage(int CoreId, String Message){
		if(CacheSimulator.debuggingmode){
			CacheSimulator.messages.add("Cycle: " + CacheSimulator.GlobalCycle + " | Core: " + CoreId + " | " + Message);
		}
	}
}
