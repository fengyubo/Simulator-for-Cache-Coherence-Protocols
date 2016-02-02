import java.util.ArrayList;

public class CommandMESI {
	public CommandMESI(int cycle, int Coreid, int rw, long addr,
			boolean readyToIssue) {
		this.issueCycle = cycle;
		this.readyToIssue = readyToIssue;
		this.CoreId = Coreid;
		this.RW = rw;
		this.MemoryAddr = addr;
		this.remainCycles = 0;
		this.Ready = false;
		this.busIndex = -1;//-1 means not ask for bus operation
		this.writeRequestResult = 0;
		this.printMode = false;
	}

	public boolean execute() { //This is just a very basic test,  need to do a lot of work
		
		switch (this.RW) {
		case 0: // read
			boolean t1 = CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.TestHitL1(MemoryAddr);
			if(t1){ //if L1 cache hit
				System.out.print("Cycle number: "+ CacheSimulatorMESI.GlobalCycle);
				System.out.print("\tCoreId "+ CoreId);
				System.out.print("\t"+"L1 Hit");
				System.out.print("\t" + CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).tag + "\n");
				if(CacheSimulatorMESI.debuggingmode){
					CacheSimulatorMESI.traceWriter.print("Cycle number: "+ CacheSimulatorMESI.GlobalCycle);
					CacheSimulatorMESI.traceWriter.print("\tCoreId "+ CoreId);
					CacheSimulatorMESI.traceWriter.print("\t"+"L1 Hit");
					CacheSimulatorMESI.traceWriter.print("\t" + CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).tag + "\n");
				}
				CacheSimulatorMESI.Cores.get(CoreId).L1HitTimes++;
				return true;
			} else { //L1 cache miss
				if(!printMode){
					System.out.print("Cycle number: "+ CacheSimulatorMESI.GlobalCycle);
					System.out.print("\tCoreId "+ CoreId);
					System.out.print("\t"+"L1 miss");
					System.out.print("\t" + MemoryAddr + "\n");
					if(CacheSimulatorMESI.debuggingmode){
						CacheSimulatorMESI.traceWriter.print("Cycle number: "+ CacheSimulatorMESI.GlobalCycle);
						CacheSimulatorMESI.traceWriter.print("\tCoreId "+ CoreId);
						CacheSimulatorMESI.traceWriter.print("\t"+"L1 miss");
						CacheSimulatorMESI.traceWriter.print("\t" + MemoryAddr + "\n");
					}
					CacheSimulatorMESI.Cores.get(CoreId).L1MissTimes++;
					printMode = true;
				}
				if(busIndex == -1){
					busIndex = CacheSimulatorMESI.bus.getIndex();//cache miss
				}
				if(busIndex == CacheSimulatorMESI.bus.currentIndex){ //bus is available
					int returnVal = CacheSimulatorMESI.bus.readMissRequest(MemoryAddr, CoreId);
					if(returnVal >= 0){
						CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.put(MemoryAddr, CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getReplaceId(MemoryAddr, CoreId) , CacheSimulatorMESI.GlobalCycle);
						//CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status=2;
						//CacheSimulatorMESI.L2CacheMESI.getBlock(MemoryAddr).setL2Block(CoreId);
						if(CacheSimulatorMESI.SharersMap.containsKey(MemoryAddr)){
							if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).contains(CoreId)){
								if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).size() > 1){
									for(int i = 0; i < CacheSimulatorMESI.SharersMap.get(MemoryAddr).size(); i++){
										CacheSimulatorMESI.Cores.get(CacheSimulatorMESI.SharersMap.get(MemoryAddr).get(i)).L1CacheMESI.getBlock(MemoryAddr).status = 2;
									}
								}else if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).size() == 1){
									CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 3;
								}
							}else{
								CacheSimulatorMESI.SharersMap.get(MemoryAddr).add(CoreId);
								if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).size() > 1){
									for(int i = 0; i < CacheSimulatorMESI.SharersMap.get(MemoryAddr).size(); i++){
										CacheSimulatorMESI.Cores.get(CacheSimulatorMESI.SharersMap.get(MemoryAddr).get(i)).L1CacheMESI.getBlock(MemoryAddr).status = 2;
									}
								}else if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).size() == 1){
									CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 3;
								}
							}
						}else{
							ArrayList<Integer> listbuffer = new ArrayList<Integer>();
							listbuffer.add(CoreId);
							CacheSimulatorMESI.SharersMap.put(MemoryAddr, listbuffer);
							CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 3;
						}
						
						//CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).setL1Block(CoreId);
						return true;
					}
				}
				if(CacheSimulatorMESI.bus.checkResult(MemoryAddr, CoreId)){
					//store in L1
					CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.put(MemoryAddr, CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getReplaceId(MemoryAddr, CoreId) , CacheSimulatorMESI.GlobalCycle);
					//CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status=2;
					//CacheSimulatorMESI.L2CacheMESI.getBlock(MemoryAddr).setL2Block(CoreId);
					
					if(CacheSimulatorMESI.SharersMap.containsKey(MemoryAddr)){
						if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).contains(CoreId)){
							if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).size() > 1){
								for(int i = 0; i < CacheSimulatorMESI.SharersMap.get(MemoryAddr).size(); i++){
									CacheSimulatorMESI.Cores.get(CacheSimulatorMESI.SharersMap.get(MemoryAddr).get(i)).L1CacheMESI.getBlock(MemoryAddr).status = 2;
								}
							}else if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).size() == 1){
								CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 3;
							}
						}else{
							CacheSimulatorMESI.SharersMap.get(MemoryAddr).add(CoreId);
							if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).size() > 1){
								for(int i = 0; i < CacheSimulatorMESI.SharersMap.get(MemoryAddr).size(); i++){
									CacheSimulatorMESI.Cores.get(CacheSimulatorMESI.SharersMap.get(MemoryAddr).get(i)).L1CacheMESI.getBlock(MemoryAddr).status = 2;
								}
							}else if(CacheSimulatorMESI.SharersMap.get(MemoryAddr).size() == 1){
								CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 3;
							}
						}
					}else{
						ArrayList<Integer> listbuffer = new ArrayList<Integer>();
						listbuffer.add(CoreId);
						CacheSimulatorMESI.SharersMap.put(MemoryAddr, listbuffer);
						CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 3;
					}
					
					//CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).setL1Block(CoreId);
					return true; //read success
				} else{
					return false;
				}
				
			}
		case 1: // write
			boolean t2 = CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.TestHitL1(MemoryAddr);
			if(t2){
				System.out.print("Cycle number: "+ CacheSimulatorMESI.GlobalCycle);
				System.out.print("\tCoreId "+ CoreId);
				System.out.print("\t"+"L1 Hit");
				System.out.print("\t" + CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).tag + "\n");
				if(CacheSimulatorMESI.debuggingmode){
					CacheSimulatorMESI.traceWriter.print("Cycle number: "+ CacheSimulatorMESI.GlobalCycle);
					CacheSimulatorMESI.traceWriter.print("\tCoreId "+ CoreId);
					CacheSimulatorMESI.traceWriter.print("\t"+"L1 Hit");
					CacheSimulatorMESI.traceWriter.print("\t" + CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).tag + "\n");
				}
				CacheSimulatorMESI.Cores.get(CoreId).L1HitTimes++;
				if(CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status == 2){
					if(busIndex == -1){
						busIndex = CacheSimulatorMESI.bus.getIndex();//cache miss
					}
					if(busIndex == CacheSimulatorMESI.bus.currentIndex){
						CacheSimulatorMESI.bus.writeRequest(MemoryAddr, CoreId, 2);
						CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 1;
						return true;// need to think about it
					} else {
						return false;
					}
				} else if(CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status == 3){
					CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 1;
				} else { //modified
					return true;
				}
			} else {
				if(!printMode){
					System.out.print("Cycle number: "+ CacheSimulatorMESI.GlobalCycle);
					System.out.print("\tCoreId "+ CoreId);
					System.out.print("\t"+"L1 miss");
					System.out.print("\t" + MemoryAddr + "\n");
					if(CacheSimulatorMESI.debuggingmode){
						CacheSimulatorMESI.traceWriter.print("Cycle number: "+ CacheSimulatorMESI.GlobalCycle);
						CacheSimulatorMESI.traceWriter.print("\tCoreId "+ CoreId);
						CacheSimulatorMESI.traceWriter.print("\t"+"L1 miss");
						CacheSimulatorMESI.traceWriter.print("\t" + MemoryAddr + "\n");
					}
					CacheSimulatorMESI.Cores.get(CoreId).L1MissTimes++;
					printMode = true;
				}
				if(writeRequestResult == 0){
					if(busIndex == -1){
						busIndex = CacheSimulatorMESI.bus.getIndex();//cache miss
					}
					if(busIndex == CacheSimulatorMESI.bus.currentIndex){
						writeRequestResult = CacheSimulatorMESI.bus.writeRequest(MemoryAddr, CoreId, 1);
					}
				}
				if(writeRequestResult == -1){
					if(CacheSimulatorMESI.bus.checkResult(MemoryAddr, CoreId)){
						CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.put(MemoryAddr, CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getReplaceId(MemoryAddr, CoreId) , CacheSimulatorMESI.GlobalCycle);
						CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status = 1;
						return true; //read success
					} else{
						return false;
					}
				} else { //equal to 1		
					if(CacheSimulatorMESI.resultOfL2[(int) (MemoryAddr % CacheSimulatorMESI.B)] == -2){
						CacheSimulatorMESI.Cores.get(CoreId).L1CacheMESI.getBlock(MemoryAddr).status=1;
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
	int writeRequestResult;
	boolean printMode;
	
}
 