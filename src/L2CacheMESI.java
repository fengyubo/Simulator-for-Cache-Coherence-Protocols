import java.util.ArrayList;


public class L2CacheMESI{

	int B = CacheSimulatorMESI.B; 
    ArrayList<BankMESI> banklist = new ArrayList<BankMESI>();

	
	public L2CacheMESI(int NumofSet, int Associativity) {
		for (int i = 0; i < B; i++){
			BankMESI SingleBank = new BankMESI();
			banklist.add(SingleBank);
			banklist.get(i).BankInit((NumofSet / B), Associativity, i);
		}
	}

	public void put(long address, int index, int LastUse) {
		
		banklist.get((int) (address % B)).put(address, index, LastUse);
		
	}
	public void makeRequest(int rw, long address, int requestCycle, int CoreId){ //added
		banklist.get((int) (address % B)).MakeRequest(rw, address, requestCycle, CoreId);
	}

	public int getReplaceId(long address, int CoreId) {
		
		return banklist.get((int) (address % B)).getReplaceId(address, CoreId);
		
	}

	public L2Block getBlock(long address) {
		
		return banklist.get((int) (address % B)).getBlock(address);
		
	}
	
	
	public boolean TestHitL2(long address) {
		
		return banklist.get((int) (address % B)).TestHitL2(address);
		
	}

	public int getBlockState(long address) {
		
		return banklist.get((int) (address % B)).getBlockState(address);
		
	}
	
}

