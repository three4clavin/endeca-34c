package three4clavin.endeca.adapter;

import java.io.File;
import java.util.logging.Logger;

import three4clavin.util.logging.FlatFormatter;

import com.endeca.edf.adapter.Adapter;
import com.endeca.edf.adapter.AdapterConfig;
import com.endeca.edf.adapter.AdapterException;
import com.endeca.edf.adapter.AdapterHandler;
import com.endeca.edf.adapter.PVal;
import com.endeca.edf.adapter.Record;

public class BaseAdapter implements Adapter {
	private static Logger log = FlatFormatter.getLogger(BaseAdapter.class.getCanonicalName());
	
    private Integer maxTotalRecords       = -1;
    private Integer totalRecordsProcessed = 0;
	
	public void execute(AdapterConfig config, AdapterHandler handler) throws AdapterException {
		Record record = new Record();
		record.add(new PVal("Endeca.Id", "Hello World"));
		
		File file = new File(".");
		record.add(new PVal("Endeca.ITL.CWD",       file.getAbsolutePath()));
		record.add(new PVal("Endeca.ITL.FreeSpace", ""+file.getFreeSpace()));
		
		log.info("Emitting hello world record.");
		handler.emit(record);
	}
	
	public Boolean emit(Record record, AdapterHandler handler) throws AdapterException {
		if(maxTotalRecords < 0){
			handler.emit(record);
			totalRecordsProcessed++;
			return true;
		}
		
		if(totalRecordsProcessed < maxTotalRecords){
			handler.emit(record);
			totalRecordsProcessed++;
			return true;
		}
		
		log.info("Processed " + totalRecordsProcessed + " records.  Not emitting any more per adapter configuration.");
		return false;
	}
	
	public Integer getMaxTotalRecords(){
		return maxTotalRecords;
	}
	public void setMaxTotalRecords(Integer maxTotalRecords) {
		if(maxTotalRecords == null){
			this.maxTotalRecords = -1;
		}
		this.maxTotalRecords = maxTotalRecords;
	}
	public void setMaxTotalRecords(String maxTotalRecords) {
		if(maxTotalRecords == null){
			this.maxTotalRecords = -1;
		}
		else{
			setMaxTotalRecords(Integer.parseInt(maxTotalRecords));
		}
	}
	
	public static Logger getLogger(){
		return log;
	}
	
	public Integer getTotalRecordsProcessed(){
		return totalRecordsProcessed;
	}
}
