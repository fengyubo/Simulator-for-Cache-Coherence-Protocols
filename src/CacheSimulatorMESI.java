import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CacheSimulatorMESI {

	public static ArrayList<CommandMESI> commandList = new ArrayList<CommandMESI>();
	public static ArrayList<CoreMESI> Cores = new ArrayList<CoreMESI>();
	public static int GlobalCycle = 1;
	public static int p = 4, n1 = 14, n2 = 22, k = 5, a1 = 2, a2 = 2, B = 4,
			d2 = 4, dm = 100, s = 0, debug=0;
	public static boolean debuggingmode = true; // Didn't implement this.
	public static L2CacheMESI L2CacheMESI;
	public static BufferedReader br;
	public static BusMESI bus;
	public static int[] resultOfL2;
	public static ArrayList<String> messages = new ArrayList<String>();
	public static HashMap<Long, ArrayList<Integer>> SharersMap = new HashMap<Long, ArrayList<Integer>>();
	public static PrintWriter writer;
	public static PrintWriter traceWriter;
	
	public static void startMESI(String[] args) throws IOException {

		String Config = args[0];
		String tracefile = args[1];
		InitSimulator(Config, tracefile);

		int NumofSetsL2 = (int) Math.pow(2, n2 - k - a2);
		int L2Associativity = (int) Math.pow(2, a2);
		L2CacheMESI L2CacheMESIbuffer = new L2CacheMESI(NumofSetsL2, L2Associativity);
		CacheSimulatorMESI.L2CacheMESI = L2CacheMESIbuffer;
		writer = new PrintWriter("executionResult.txt", "UTF-8");//added
		traceWriter = new PrintWriter("executionTrace.txt", "UTF-8");//added
		if( (args.length!=0) && (args[args.length-1].equals("debug"))){
			//debuggingmode = true;
            p = 2;
		}
		else
		{

		}
		bus = new BusMESI(); //added
		resultOfL2 = new int[B];
		 for(int i=0;i<B;++i){
		 	resultOfL2[i]=-1;
		 }
		//Start trace-driven simulation.
		boolean hasPendingCommand = false;
		System.out.println("p = " + p );
		System.out.println("n1 = " + n1 );
		System.out.println("n2 = " + n2 );
		System.out.println("k = " + k );
		System.out.println("a1 = " + a1 );
		System.out.println("a2 = " + a2 );
		System.out.println("B = " + B );
		System.out.println("d2 = " + d2 );
		System.out.println("dm = " + dm );
		System.out.println("NumofSetsL2 = " + NumofSetsL2 );

		while (true) {
			hasPendingCommand = false;
			for (int i = 0; i < Cores.size(); i++) {
				CoreMESI Core = Cores.get(i);
				CommandMESI command = null;
				if (!Core.CommandWaitingList.isEmpty()) {
					hasPendingCommand = true;
					command = Core.CommandWaitingList.get(0);
					if (command.issueCycle == GlobalCycle     //Issue command according to sequence and L1 miss
							|| command.Ready == true) {
						command.Ready = true;
						if (command.execute() == true) {
							Core.CommandWaitingList.remove(0);
							if (!Core.CommandWaitingList.isEmpty()) {
								Core.CommandWaitingList.get(0).issueCycle = GlobalCycle
										+ Core.CommandWaitingList.get(0).interval;
							}
						}
					}
				}
			}
			if (hasPendingCommand == false)
				break;
			for(int i = 0; i < B; i++){ //think about the position of this call
				resultOfL2[i] = L2CacheMESI.banklist.get(i).RequestTiming();
			}
			bus.nextRequest();
			GlobalCycle++;
		}
	writer.println("Global varible");
		writer.println("Total cycle: " + GlobalCycle);
		double totallihitrate = 0;
		int totall1hittime= 0;
		int totall1misstime= 0;
		int totall2hittime= 0;
		int totall2misstime= 0;
		double totallimissrate= 0;
		double totall2hitrate= 0;
		double totlal2missrate= 0;
		double totalcpi= 0;
		double totalmiss= 0;
		int totalIns = 0;
		for(int i = 0; i < Cores.size(); i++){
			totall1hittime += Cores.get(i).L1HitTimes;
			totall1misstime += Cores.get(i).L1MissTimes;
			totall2hittime += Cores.get(i).L2HitTimes;
			totall2misstime += Cores.get(i).L2MissTimes;
			totalIns+=Cores.get(i).instNum;
			totalcpi += (double)Cores.get(i).cycleNum/(double)Cores.get(i).instNum;
			totalmiss += (double)(Cores.get(i).cycleNum - Cores.get(i).originalCycleNum)/(double)Cores.get(i).L1MissTimes;
		}
		writer.println("total l1 hit rate" + (double)totall1hittime/(double)totalIns);
		writer.println("total l1 miss rate" + (double)totall1misstime/(double)totalIns);
		writer.println("total l2 hit rate" + (double)totall2hittime/(double)(totalIns-totall1hittime));
		writer.println("total l2 miss rate" + (double)totall2misstime/(double)(totalIns-totall1hittime));
		writer.println("total miss penalty" + (double)totalmiss/16);
		

		writer.println("Global varible");
		writer.println("Total cycle: " + GlobalCycle);
		writer.println("Mem & Cache content");
		for (int i = 0; i < Cores.size(); i++) {
			writer.println("Core " + i + ":");
			if(Cores.get(i).instNum == 0){
				continue;
			}
			writer.println("Cycles for complete: " + Cores.get(i).cycleNum);
			writer.println("L1 Hit time: "+ Cores.get(i).L1HitTimes);
			writer.println("L1 Hit rate: "+ Cores.get(i).L1HitTimes + "/" + Cores.get(i).instNum + " = " + (double)Cores.get(i).L1HitTimes / (double)Cores.get(i).instNum);
			writer.println("L2 Hit time: "+ Cores.get(i).L2HitTimes);
			writer.println("L2 Hit rate: "+ Cores.get(i).L2HitTimes + "/" + (Cores.get(i).instNum - Cores.get(i).L1HitTimes) + " = " + (double)Cores.get(i).L2HitTimes / (double)(Cores.get(i).instNum - Cores.get(i).L1HitTimes));
			
			writer.println("L1 miss time: "+ Cores.get(i).L1MissTimes);
			writer.println("L1 miss rate: "+ Cores.get(i).L1MissTimes + "/" + Cores.get(i).instNum + " = " + (double)Cores.get(i).L1MissTimes / (double)Cores.get(i).instNum);
			writer.println("L2 miss time: "+ Cores.get(i).L2MissTimes);
			writer.println("L2 miss rate: "+ Cores.get(i).L2MissTimes + "/" + (Cores.get(i).instNum - Cores.get(i).L1HitTimes) + " = " + (double)Cores.get(i).L2MissTimes / (double)(Cores.get(i).instNum - Cores.get(i).L1HitTimes));
			
			writer.println("Average miss Penalty for L1 miss: (" + Cores.get(i).cycleNum + "-" + Cores.get(i).originalCycleNum + ")/" + Cores.get(i).L1MissTimes + " = " + (double)(Cores.get(i).cycleNum - Cores.get(i).originalCycleNum)/(double)Cores.get(i).L1MissTimes);
			
			writer.println("Number of snoopy message: " + Cores.get(i).SnoopyMsgNum);
			writer.println("Number of data message: " + Cores.get(i).DataMsgNum);
			
			writer.println("\n\n");
		}
		if(debuggingmode){
			writer.println("messages:");
			for(int i = 0; i < messages.size(); i++){
				writer.println(messages.get(i));
			}
			writer.println("\n\nCache status:");
			for (int i = 0; i < Cores.size(); i++) {
				Cores.get(i).L1CacheMESI.printL1CacheMESI();
			}

			for (int i = 0; i < B; i++) {
				L2CacheMESI.banklist.get(i).printL2CacheMESI();
			}
		}
		writer.close();
		traceWriter.close();

	}

	private static void InitSimulator(String Config, String tracefile) throws IOException {
		int cycle;
		int CoreId;
		int rw;
		long addr;
		boolean IssueRdy = false;
		CommandMESI command;
		String cf = Config;
		String CommendStr;
		String[] CommendFields = new String[4];

		BufferedReader initbuffer = new BufferedReader(new FileReader(cf));
		String input; String[] prase;
		input = initbuffer.readLine(); prase = input.split(" = "); p = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); n1 = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); n2 = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); k = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); a1 = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); a2 = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); B = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); d2 = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); dm = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); s = Integer.parseInt(prase[1].replace(" ",""));
		input = initbuffer.readLine(); prase = input.split(" = "); debug = Integer.parseInt(prase[1].replace(" ",""));
		//String tracefile = initbuffer.readLine();
		br = new BufferedReader(new FileReader(tracefile));

		//Setup each commends
		while ((CommendStr = br.readLine()) != null) {
			CommendFields = CommendStr.split("\\s+");
			cycle = Integer.parseInt(CommendFields[0]);
			CoreId = Integer.parseInt(CommendFields[1]);
			rw = Integer.parseInt(CommendFields[2]);
			addr = (long) ((Long.valueOf(CommendFields[3].substring(CommendFields[3]
									.indexOf("0x") + 2), 16).longValue())/ (Math.pow(2,k)));
			// System.out.println("rw = "+ rw + " bank = " + addr % B);
			command = new CommandMESI(cycle, CoreId, rw, addr, IssueRdy);
			commandList.add(command);
		}
		
		
		//Setup each core and L1 cache
		int NumofSetsL1 = (int) Math.pow(2, n1 - k - a1);
		int L2Associativity = (int) Math.pow(2, a1);
		
		for (int i = 0; i < p; i++) {  //For each core
			CoreMESI Core = new CoreMESI(i, NumofSetsL1, L2Associativity);
			for (int j = 0; j < commandList.size(); j++) {   // Assign each command to related core
				CommandMESI commandbuffer = commandList.get(j);
				if (i == commandbuffer.CoreId) {
					if (Core.CommandWaitingList.isEmpty()) {
						Core.CommandWaitingList.add(commandbuffer);
					} else {
						int lc = Core.CommandWaitingList.size() - 1;
						CommandMESI LastCommand = Core.CommandWaitingList.get(lc);

						if (commandbuffer.issueCycle <= LastCommand.issueCycle) {
							commandbuffer.interval = 1;
						} else {
							commandbuffer.interval = commandbuffer.issueCycle - LastCommand.issueCycle;
						}
						Core.CommandWaitingList.add(commandbuffer);
					}
				}
			}
			Core.instNum = Core.CommandWaitingList.size();
			if(Core.instNum == 0){
				
			} else {
				Core.originalCycleNum = Core.CommandWaitingList.get(Core.CommandWaitingList.size()-1).issueCycle + 1 - Core.CommandWaitingList.get(0).issueCycle;    
			}
			Cores.add(Core);
		}
	}
}