import java.math.BigInteger;
import java.util.ArrayList;

public class CoreMESI {
	public int CoreId;
	public L1CacheMESI L1CacheMESI;
	public ArrayList<CommandMESI> CommandWaitingList;
	public int instNum = 0;
	public int cycleNum = 0;
	
	public int startingCycle = -1;
	public int originalCycleNum = 0;
	
	public int MissInstNum = 0;
	public double AvgMissPenalty = 0;
	public int TotalMissPenalty = 0;
	
	
	public int L1HitTimes = 0;
	public int L1MissTimes = 0;
	public double L1MissRate = -1;
	
	public int L2HitTimes = 0;
	public int L2MissTimes = 0;
	public double L2MissRate = -1;
	
	
	public int SnoopyMsgNum = 0;
	public int DataMsgNum = 0;

	public CoreMESI(int CoreId, int setNum1, int assoc1) {
		this.CoreId = CoreId;
		this.L1CacheMESI = new L1CacheMESI(setNum1, assoc1);
		this.CommandWaitingList = new ArrayList<CommandMESI>();
	}

	public void printCore(){
		System.out.println("CmdList:");
		for(int i=0;i<CommandWaitingList.size();++i)
		{
			CommandWaitingList.get(i).printCmd();
		}
	}
	
}