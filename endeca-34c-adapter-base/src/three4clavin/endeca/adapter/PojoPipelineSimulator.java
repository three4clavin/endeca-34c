package three4clavin.endeca.adapter;

import java.util.List;

import com.endeca.edf.adapter.Adapter;
import com.endeca.edf.adapter.AdapterConfig;
import com.endeca.edf.adapter.AdapterException;
import com.endeca.edf.adapter.AdapterHandler;

public class PojoPipelineSimulator {
	public static void main(List<String> args) throws ClassNotFoundException, AdapterException, InstantiationException, IllegalAccessException {
		String[] arrayArgs = new String[args.size()];
		int idx = 0;
		for(String arg : args){
			arrayArgs[idx] = arg;
			idx++;
		}
		main(arrayArgs);
	}
	
	public static void main(String[] args) throws ClassNotFoundException, AdapterException, InstantiationException, IllegalAccessException {
		AdapterConfig  config  = new AdapterConfig();
		AdapterHandler handler = new PojoAdapterHandler();
		
		if(args.length < 1){
			System.err.println("[ERR] Adapter class name to test required.");
		}
		
		for(int argIdx = 1; argIdx < args.length ; argIdx += 2){
			String key = args[argIdx];
			
			if(key == null){
				key = "";
			}
			
			String val = "";
			if(argIdx+1 < args.length){
				val = args[argIdx+1];
			}
			
			config.put(key, val);
		}
		
		Adapter adapter = (Adapter)(Class.forName(args[0]).newInstance());
		adapter.execute(config, handler);
	}
}
