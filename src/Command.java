import java.util.ArrayList;

public class Command {
	public Command(int cycle, int Coreid, int rw, long addr,
			boolean readyToIssue) {
		this.issueCycle = cycle;
		this.readyToIssue = readyToIssue;
		this.CoreId = Coreid;
		this.RW = rw;
		this.MemoryAddr = addr;
		this.remainCycles = 0;
		this.Ready = false;
		this.busIndex = -1;//-1 means not ask for bus operation
		this.writeBusIndex = -1;
		this.writeRequestResult = 0;
		this.printMode = false;
	}

	public boolean execute() { //This is just a very basic test,  need to do a lot of work
		
		switch (this.RW) {
		case 0: // read
			boolean t1 = CacheSimulator.Cores.get(CoreId).L1Cache.TestHitL1(MemoryAddr);
			if(t1){ //if L1 cache hit
				System.out.print("Cycle number: "+ CacheSimulator.GlobalCycle);
				System.out.print("\tCoreId "+ CoreId);
				System.out.print("\t"+"L1 Hit");
				System.out.print("\t" + CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).tag + "\n");
				if(CacheSimulator.debuggingmode){
					CacheSimulator.traceWriter.print("Cycle number: "+ CacheSimulator.GlobalCycle);
					CacheSimulator.traceWriter.print("\tCoreId "+ CoreId);
					CacheSimulator.traceWriter.print("\t"+"L1 Hit");
					CacheSimulator.traceWriter.print("\t" + CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).tag + "\n");
				}
				CacheSimulator.Cores.get(CoreId).L1HitTimes++;
				return true;
			} else { //L1 cache miss
				if(!printMode){
					System.out.print("Cycle number: "+ CacheSimulator.GlobalCycle);
					System.out.print("\tCoreId "+ CoreId);
					System.out.print("\t"+"L1 miss");
					System.out.print("\t" + MemoryAddr + "\n");
					if(CacheSimulator.debuggingmode){
						CacheSimulator.traceWriter.print("Cycle number: "+ CacheSimulator.GlobalCycle);
						CacheSimulator.traceWriter.print("\tCoreId "+ CoreId);
						CacheSimulator.traceWriter.print("\t"+"L1 miss");
						CacheSimulator.traceWriter.print("\t" + MemoryAddr + "\n");
					}
					CacheSimulator.Cores.get(CoreId).L1MissTimes++;
					printMode = true;
				}
				if(busIndex == -1){
					busIndex = CacheSimulator.bus.getIndex();//cache miss
				}
				if(busIndex == CacheSimulator.bus.currentIndex){ //bus is available
					int returnVal = CacheSimulator.bus.readMissRequest(MemoryAddr, CoreId);
					if(returnVal >= 0){
						//int coreidbuffer = CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId);
						if(CacheSimulator.Cores.get(CoreId).L1Cache.returnblock(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId)) == null){
							
						} else if(CacheSimulator.Cores.get(CoreId).L1Cache.returnblock(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId)).status == 1){
							//write to L2 bus limit
							CacheSimulator.L2Cache.makeRequest(1, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
						}
						CacheSimulator.Cores.get(CoreId).L1Cache.put(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId) , CacheSimulator.GlobalCycle);
						CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).status=2;
						return true;
					}
				}
				if(CacheSimulator.bus.checkResult(MemoryAddr, CoreId)){
					//store in L1
					//int coreidbuffer = CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId);
					if(CacheSimulator.Cores.get(CoreId).L1Cache.returnblock(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId)) == null){
						
					} else if(CacheSimulator.Cores.get(CoreId).L1Cache.returnblock(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId)).status == 1){
						//write to L2 bus limit
						CacheSimulator.L2Cache.makeRequest(1, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
					}
					CacheSimulator.Cores.get(CoreId).L1Cache.put(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId) , CacheSimulator.GlobalCycle);
					CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).status=2;
					return true; //read success
				} else{
					return false;
				}
				
			}
		case 1: // write
			boolean t2 = CacheSimulator.Cores.get(CoreId).L1Cache.TestHitL1(MemoryAddr);
			if(t2){
				System.out.print("Cycle number: "+ CacheSimulator.GlobalCycle);
				System.out.print("\tCoreId "+ CoreId);
				System.out.print("\t"+"L1 Hit");
				System.out.print("\t" + CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).tag + "\n");
				if(CacheSimulator.debuggingmode){
					CacheSimulator.traceWriter.print("Cycle number: "+ CacheSimulator.GlobalCycle);
					CacheSimulator.traceWriter.print("\tCoreId "+ CoreId);
					CacheSimulator.traceWriter.print("\t"+"L1 Hit");
					CacheSimulator.traceWriter.print("\t" + CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).tag + "\n");
				}
				CacheSimulator.Cores.get(CoreId).L1HitTimes++;
				if(CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).status == 2){
					if(busIndex == -1){
						busIndex = CacheSimulator.bus.getIndex();//cache miss
					}
					if(busIndex == CacheSimulator.bus.currentIndex){ //bus is available
						CacheSimulator.bus.writeRequest(MemoryAddr, CoreId, 2);
						CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).status=1;
						return true;// need to think about it
					} else {
						return false;
					}
					
				} else { //modified
					return true;
				}
			} else {
				if(!printMode){
					System.out.print("Cycle number: "+ CacheSimulator.GlobalCycle);
					System.out.print("\tCoreId "+ CoreId);
					System.out.print("\t"+"L1 miss");
					System.out.print("\t" + MemoryAddr + "\n");
					if(CacheSimulator.debuggingmode){
						CacheSimulator.traceWriter.print("Cycle number: "+ CacheSimulator.GlobalCycle);
						CacheSimulator.traceWriter.print("\tCoreId "+ CoreId);
						CacheSimulator.traceWriter.print("\t"+"L1 miss");
						CacheSimulator.traceWriter.print("\t" + MemoryAddr + "\n");
					}
					CacheSimulator.Cores.get(CoreId).L1MissTimes++;
					printMode = true;
				}
				if(writeRequestResult == 0){
					if(busIndex == -1){
						busIndex = CacheSimulator.bus.getIndex();//cache miss
					}
					if(busIndex == CacheSimulator.bus.currentIndex){ //bus is available
						writeRequestResult = CacheSimulator.bus.writeRequest(MemoryAddr, CoreId, 1);
					}
				}
				if(writeRequestResult == -1){
					if(CacheSimulator.bus.checkResult(MemoryAddr, CoreId)){
						//int coreidbuffer = CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId);
						if(CacheSimulator.Cores.get(CoreId).L1Cache.returnblock(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId)) == null){
							
						} else if(CacheSimulator.Cores.get(CoreId).L1Cache.returnblock(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId)).status == 1){
							//write to L2 bus limit
							CacheSimulator.L2Cache.makeRequest(1, MemoryAddr, CacheSimulator.GlobalCycle, CoreId);
						}			
						CacheSimulator.Cores.get(CoreId).L1Cache.put(MemoryAddr, CacheSimulator.Cores.get(CoreId).L1Cache.getReplaceId(MemoryAddr, CoreId) , CacheSimulator.GlobalCycle);
						CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).status=1;
						return true; //read success
					} else{
						return false;
					}
				} else { //equal to 1
					if(CacheSimulator.resultOfL2[(int) (MemoryAddr % CacheSimulator.B)] == -2){
						CacheSimulator.Cores.get(CoreId).L1Cache.getBlock(MemoryAddr).status=1;
						return true;
					} else {
						return false;
					}
				}
			}
		default:
			return false;
		}
	}

	public void printCmd(){
		System.out.println("_____________________________________");
		System.out.println("\tissueCycle="+this.issueCycle);
		System.out.println("\tremainCycles="+this.remainCycles);
		System.out.println("\tinterval="+this.interval);
		System.out.println("\tCoreId="+this.CoreId);
		System.out.println("\tRW="+this.RW);
		System.out.println("\tMemoryAddr="+this.MemoryAddr);
		if(this.readyToIssue==true)
			System.out.println("\treadyToIssue=true");
		else
			System.out.println("\treadyToIssue=false");
		if(this.Ready==true)
			System.out.println("\tReady=true");
		else
			System.out.println("\tReady=false");
		System.out.println("\tbusIndex="+this.busIndex);
		System.out.println("\twriteRequestResult="+this.writeRequestResult);
	}

	public int issueCycle;
	public int remainCycles;
	public int interval;
	public int CoreId;
	public int RW;
	public long MemoryAddr;
	public boolean readyToIssue;
	public boolean Ready;
	int busIndex;
	int writeBusIndex;
	int writeRequestResult;
	boolean printMode;
	
}
 