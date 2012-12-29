package three4clavin.endeca.adapter;

import java.util.logging.Logger;

import three4clavin.util.logging.FlatFormatter;

import com.endeca.edf.adapter.AdapterException;
import com.endeca.edf.adapter.AdapterHandlerSuper;
import com.endeca.edf.adapter.Record;

public class PojoAdapterHandler extends AdapterHandlerSuper {
	public static Logger log = FlatFormatter.getLogger(PojoAdapterHandler.class.getCanonicalName());
	
	public PojoAdapterHandler(){
		super();
	}

	@Override
	public void emit(Record record) throws AdapterException {
		log.info("[EMIT] " + record);
	}

	@Override
	public void flushLog() throws AdapterException {
		log.info("[FLUSH LOG]");
	}

	@Override
	public String getInputDirectory() {
		log.info("[GET INPUT DIRECTORY]");
		return null;
	}

	@Override
	public String getName() {
		log.info("[GET NAME]");
		return null;
	}

	@Override
	public int getNumInputs() {
		log.info("[GET NUM INPUTS]");
		return 0;
	}

	@Override
	public double getPercentComplete() {
		log.info("[GET PERCENT COMPLETE]");
		return 0;
	}

	@Override
	public double getPercentComplete(int percent) throws AdapterException {
		log.info("[GET PERCENT COMPLETE] " + percent);
		return 0;
	}

	@Override
	public Record getRecord(int num) throws AdapterException {
		log.info("[GET RECORD] " + num);
		return null;
	}

	@Override
	public Record getRecord(String key) throws AdapterException {
		log.info("[GET RECORD] " + key);
		return null;
	}

	@Override
	public String getStateDirectory() {
		log.info("[GET STATE DIRECTORY]");
		return null;
	}

	@Override
	public String getTmpDirectory() {
		log.info("[GET TMP DIRECTORY]");
		return null;
	}

	@Override
	public void setAutoFlushLog(boolean autoFlush) {
		log.info("[SET AUTO FLUSH LOG] " + autoFlush);
	}

	@Override
	public void setPercentComplete(double percent) {
		log.info("[SET PERCENT COMPLETE] " + percent);
	}
}
