import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CacheSimulator_start{
	public static int p = 4, n1 = 14, n2 = 22, k = 5, a1 = 2, a2 = 2, B = 4,
			d2 = 4, dm = 100, s = 0, debug = 0;
	public static CacheSimulator CacheSimulator;
	public static CacheSimulatorMESI CacheSimulatorMESI;

	public static void main(String[] args) throws IOException {
		String Config = args[0];

		InitSimulator(Config);
		if(s==0){
			CacheSimulator.startMSI(args);
		}
		else if(s==1){
			CacheSimulatorMESI.startMESI(args);
		}
		return ;
	}

	private static void InitSimulator(String Config) throws IOException {
		String cf = Config;

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
	}


}