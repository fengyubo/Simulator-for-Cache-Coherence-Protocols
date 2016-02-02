import java.util.ArrayList;

public class L1Cache {

	public L1Cache(int NumofSet, int Associativity) {
		L1Block CacheBlock;
		ArrayList<L1Block> set;
		for (int i = 0; i < NumofSet; i++) {
			set = new ArrayList<L1Block>();
			for (int j = 0; j < Associativity; j++) {
				CacheBlock = new L1Block(-1, 0, 0);
				set.add(CacheBlock);
			}
			CacheSpace.add(set);
		}
	}

	public void put(long address, int index, int LastUse) {
		int setIndex;
		long tag;
		int n1 = CacheSimulator.n1;
		int a1 = CacheSimulator.a1;
		int k = CacheSimulator.k; 
		int mask = (int) Math.pow(2,(n1 - k - a1));  // 2 ^ (n1 L1size - k blocksize - a1 associativity) = # of sets 
		setIndex = (int) (address % (mask));    
		ArrayList<L1Block> set;
		tag = address;
		set = CacheSpace.get(setIndex);
		L1Block replace = set.get(index);
		replace.LastUse = LastUse;
		replace.tag = tag;
	}

	public L1Block returnblock(long address, int index) {
		int setIndex;
		long tag;
		int n1 = CacheSimulator.n1;
		int a1 = CacheSimulator.a1;
		int k = CacheSimulator.k; 
		int mask = (int) Math.pow(2,(n1 - k - a1));  // 2 ^ (n1 L1size - k blocksize - a1 associativity) = # of sets 
		setIndex = (int) (address % (mask));    
		ArrayList<L1Block> set;
		tag = address;
		set = CacheSpace.get(setIndex);
		L1Block replace = set.get(index);
		return replace;
	}
	
	public int getReplaceId(long address, int CoreId) {
		int setIndex;
		long tag;
		int n1 = CacheSimulator.n1;
		int a1 = CacheSimulator.a1;
		int k = CacheSimulator.k;
		int mask = (int) Math.pow(2,(n1 - k - a1)); 
		setIndex = (int) (address % (mask)); 
		ArrayList<L1Block> set;
		tag = address;
		set = CacheSpace.get(setIndex);

		for (int i = 0; i < set.size(); i++) {
			if (set.get(i).tag == tag) {   // Still need to check state from out side (check state in command)
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
		ArrayList<L1Block> set = this.CacheSpace.get(setIndex);
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

	public L1Block getBlock(long address) {
		int setIndex;
		long tag;
		int n1 = CacheSimulator.n1;
		int a1 = CacheSimulator.a1;
		int k = CacheSimulator.k;
		int mask = (int) Math.pow(2,(n1 - k - a1)); 
		setIndex = (int) (address % (mask));   
		ArrayList<L1Block> set;
		
		tag = address;
		set = CacheSpace.get(setIndex);
		for (int i = 0; i < set.size(); i++) {
			if (set.get(i).tag == tag)
				return set.get(i);
		}
		return null;
	}

	public boolean TestHitL1(long address) {
		L1Block block = this.getBlock(address);
		if (block == null)
			return false;
		return block.status != 0;
	}

	public int getBlockState(int address) {
		L1Block block = this.getBlock(address);
		return block.status;
	}

	public void printL1Cache(){
		ArrayList<L1Block> set;
		for(int i = 0; i < this.CacheSpace.size(); i++ ){
			set = this.CacheSpace.get(i);
			for(int j = 0; j < set.size(); j++){
				if(set.get(j).tag != -1){
					CacheSimulator.writer.println("L1 address: " + set.get(j).tag + " | L1 setIndex: " + i + " | L1 tag: "
							+ set.get(j).tag + " | L1 status " + set.get(j).status);
				}
			}
		}
	}
	
	public ArrayList<ArrayList<L1Block>> CacheSpace = new ArrayList<ArrayList<L1Block>>();
}

// class L1Block {
// 	public L1Block(long tag, int status, int LastUse) {
// 		this.tag = tag;
// 		this.status = status;
// 		this.LastUse = LastUse;
// 	}

// 	long tag;
// 	int status; // 0 invalid, 1 modified, 2 shared
// 	int LastUse; // for LRU

// }