import java.util.ArrayList;
import java.util.List;

public class Bank {
	
	boolean busy = false;
	int Id;
	List<bankRequest> requestQueue; //added
	int busIndex;
	boolean printMode;

	public void BankInit(int NumofSet, int Associativity, int BankId) {
		this.Id = BankId;
		L2Block CacheBlock;
		this.requestQueue = new ArrayList<bankRequest>(); //added
		ArrayList<L2Block> set;
		ArrayList<Integer> SharerList = new ArrayList<Integer>();
		for (int i = 0; i < NumofSet; i++) {
			set = new ArrayList<L2Block>();
			for (int j = 0; j < Associativity; j++) {
				SharerList = new ArrayList<Integer>();
				CacheBlock = new L2Block(-1, 0, 0, SharerList);
				set.add(CacheBlock);
			}
			CacheSpace.add(set);
		}
		busIndex = -1;
		printMode = false;
	}
	public void MakeRequest(int rw, long address, int requestCycle, int CoreId){ //added
		requestQueue.add(new bankRequest(rw, address, requestCycle, CoreId));
	}
	public int RequestTiming(){ //working to make sure the hit time is correct //wake up in every cycle //result stored so that each core can check
		if(requestQueue.isEmpty()){
			return -1;
		}
		if(requestQueue.get(0).L2miss>0){
			requestQueue.get(0).L2miss--;
		}
		if((CacheSimulator.GlobalCycle - requestQueue.get(0).requestCycle >= CacheSimulator.d2) || (requestQueue.get(0).L2miss==0)){
			int CoreId = requestQueue.get(0).CoreId;
			if(getBlock(requestQueue.get(0).address) == null){ //L2 miss
				if(!printMode){
					System.out.print("Cycle number: "+ CacheSimulator.GlobalCycle);
					System.out.print("\tCoreId "+ CoreId);
					System.out.print("\t"+"L2 miss");
					System.out.print("\t" + requestQueue.get(0).address + "\n");
					if(CacheSimulator.debuggingmode){
						CacheSimulator.traceWriter.print("Cycle number: "+ CacheSimulator.GlobalCycle);
						CacheSimulator.traceWriter.print("\tCoreId "+ CoreId);
						CacheSimulator.traceWriter.print("\t"+"L2 miss");
						CacheSimulator.traceWriter.print("\t" + requestQueue.get(0).address + "\n");
					}
					CacheSimulator.Cores.get(CoreId).L2MissTimes++;
					printMode = true;
				}
				if(requestQueue.get(0).L2miss == -1){
					requestQueue.get(0).L2miss = CacheSimulator.dm; //make the beginning of penalty
				}
				if(requestQueue.get(0).L2miss != 0){
					return -1;
				}
			}else{ 
				if(!printMode){
					System.out.print("Cycle number: "+ CacheSimulator.GlobalCycle);
					System.out.print("\tCoreId "+ CoreId);
					System.out.print("\t"+"L2 hit");
					System.out.print("\t" + requestQueue.get(0).address + "\n");
					if(CacheSimulator.debuggingmode){
						CacheSimulator.traceWriter.print("Cycle number: "+ CacheSimulator.GlobalCycle);
						CacheSimulator.traceWriter.print("\tCoreId "+ CoreId);
						CacheSimulator.traceWriter.print("\t"+"L2 hit");
						CacheSimulator.traceWriter.print("\t" + requestQueue.get(0).address + "\n");
					}
					CacheSimulator.Cores.get(CoreId).L2HitTimes++;
					printMode = true;
				}
				if(requestQueue.get(0).rw == 1){
					getBlock(requestQueue.get(0).address).status = -1; //lock the block.
				}
			}
			this.put(requestQueue.get(0).address, this.getReplaceId(requestQueue.get(0).address, CoreId) , CacheSimulator.GlobalCycle);
			if(requestQueue.get(0).rw == 0){ //read
				//send read back
				if(getBlock(requestQueue.get(0).address).status == -1){//waiting for write
					return -1;
				} else {
					if(busIndex == -1){
						busIndex = CacheSimulator.bus.getIndex();
					}
					if(busIndex == CacheSimulator.bus.currentIndex){ //bus is available
						CacheSimulator.bus.printMessage(CoreId, "L2 write back");
						busIndex = -1;
						if(requestQueue.size() == 2){
							ArrayList<bankRequest> temp = new ArrayList<bankRequest>();
							temp.add(requestQueue.get(1));
							requestQueue = temp;
							requestQueue.get(0).requestCycle = CacheSimulator.GlobalCycle;
						} else if(requestQueue.size() != 1){
							requestQueue = new ArrayList<bankRequest>(requestQueue.subList(1, requestQueue.size()));
							requestQueue.get(0).requestCycle = CacheSimulator.GlobalCycle;
						} else {
							requestQueue.clear();
						}
						printMode = false;
						return CoreId;//request command will check the return of requestTimeing to find the time to read the data.
					}
				}
			} else { //write
				//put(requestQueue.get(0).address, CacheSimulator.L2Cache.getReplaceId(requestQueue.get(0).address, CoreId), CacheSimulator.GlobalCycle);
				getBlock(requestQueue.get(0).address).status = 0;
				if(requestQueue.size() == 2){
					ArrayList<bankRequest> temp = new ArrayList<bankRequest>();
					temp.add(requestQueue.get(1));
					requestQueue = temp;
					requestQueue.get(0).requestCycle = CacheSimulator.GlobalCycle;
				} else if(requestQueue.size() != 1){
					requestQueue = new ArrayList<bankRequest>( requestQueue.subList(1, requestQueue.size()));
					requestQueue.get(0).requestCycle = CacheSimulator.GlobalCycle;
				} else {
					requestQueue.clear();
				}
				printMode = false;
				return -2;
			}
		}
		return -1;
	}
	public void put(long address, int index, int LastUse) {
		int setIndex;
		long tag;
		int n2 = CacheSimulator.n2;
		int a2 = CacheSimulator.a2;
		int k = CacheSimulator.k; 
		int B = CacheSimulator.B; 
		int mask = (int) Math.pow(2,(n2 - k - a2));  // 2 ^ (n2 L1size - k blocksize - a2 associativity) = # of sets 
		setIndex = (int) (address % (mask / B));     
		ArrayList<L2Block> set;
		tag = address;
		set = CacheSpace.get(setIndex);
		L2Block replacement = set.get(index);
		replacement.LastUse = LastUse;
		replacement.tag = tag;
	}

	public int getReplaceId(long address, int CoreId) {
		int setIndex;
		long tag;
		int n2 = CacheSimulator.n2;
		int a2 = CacheSimulator.a2;
		int k = CacheSimulator.k;
		int B = CacheSimulator.B; 
		int mask = (int) Math.pow(2,(n2 - k - a2)); 
		setIndex = (int) (address % (mask / B)); 
		ArrayList<L2Block> set;
		tag = address;
		set = CacheSpace.get(setIndex);

		for (int i = 0; i < set.size(); i++) {
			if (set.get(i).tag == tag) {
				return i;
			}
		}

		for (int i = 0; i < set.size(); i++) {
			if (set.get(i).tag == -1)
				return i;
		}

		return LRU(setIndex, CoreId);
	}

	public int LRU(int setIndex, int CoreId) {
		ArrayList<L2Block> set = this.CacheSpace.get(setIndex);
		int evictOffset = 0;
		int min = set.get(0).LastUse;
		for (int i = 0; i < set.size(); i++) {
			if (set.get(i).LastUse < min) {
				evictOffset = i;
				min = set.get(i).LastUse;
			}
		}
		return evictOffset;

	}

	public boolean TestHitL2(long address) {
		L2Block block = this.getBlock(address);
		if (block == null)
			return false;
		return true;
	}

	public L2Block getBlock(long address) {
		int setIndex;
		long tag;
		int n2 = CacheSimulator.n2;
		int a2 = CacheSimulator.a2;
		int k = CacheSimulator.k;
		int B = CacheSimulator.B; 
		int mask = (int) Math.pow(2,(n2 - k - a2)); 
		setIndex = (int) (address % (mask / B));
		ArrayList<L2Block> set;
		
		tag = address;
		set = CacheSpace.get(setIndex);
		for (int i = 0; i < set.size(); i++) {
			if (set.get(i).tag == tag)
				return set.get(i);
		}
		return null;
	}


	public int getBlockState(long address) {
		L2Block block = this.getBlock(address);
		return block.status;
	}

	public void printL2Cache(){
		ArrayList<L2Block> set;
		for(int i = 0; i < this.CacheSpace.size(); i++ ){
			set = this.CacheSpace.get(i);
			for(int j = 0; j < set.size(); j++){
				if(set.get(j).tag != -1){
					CacheSimulator.writer.println("L2 address: "
							+ set.get(j).tag + " | L2 setIndex: " + i + " | L2 status " + set.get(j).status + " | Bank Id " + this.Id);
				}
			}
		}
	}
	
	public ArrayList<ArrayList<L2Block>> CacheSpace = new ArrayList<ArrayList<L2Block>>();
}

class L2Block {
	long tag;
	int status;          // 0 invalid, 1 modified, 2 shared, -1 waiting Request
	public int LastUse;  // for LRU
	public ArrayList<Integer> SharerList;  // Use for broadcast
	
	public L2Block(long tag, int status, int LastUse, ArrayList<Integer> SharerList) {
		this.tag = tag;
		this.status = status;
		this.LastUse = LastUse;
		this.SharerList = SharerList;
	}
}
class bankRequest{ //added
	int rw;
	long address; 
	int requestCycle;
	int CoreId;
	int L2miss;
	public bankRequest(int rw, long address, int requestCycle, int CoreId){
		this.rw = rw;
		this.address = address;
		this.requestCycle = requestCycle;
		this.CoreId = CoreId;
		this.L2miss = -1;
	}
}