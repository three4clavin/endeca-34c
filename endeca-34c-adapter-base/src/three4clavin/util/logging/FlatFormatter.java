package three4clavin.util.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Just a simple class to print log messages on one line instead of multiple lines.
 */
public class FlatFormatter extends Formatter {
	public String format(LogRecord log) {
		Date date = new Date();
		date.setTime(log.getMillis());
		
		SimpleDateFormat sdf        = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z zzzz");
		String           dateString = sdf.format(date);
		String           level      = log.getLevel().getName();
		StringBuffer     output     = new StringBuffer();
		String           message    = log.getMessage();
		
		if(message == null){
			message = "";
		}
		
		for(String line : message.split("\r*\n+")){
			output.append("{"+level+"} {" + log.getLoggerName() + "} {"+dateString+"} " + line + "\n");
		}
		
		Throwable thrown = log.getThrown();
		if (thrown != null) {
			output.append("(Thrown " + thrown.toString() + ")");
		}
		return output.toString();
	}
	
	public static Logger getLogger(String name){
		if(name == null){
			name = "[Anonymous Class]";
		}
		
		Logger logger = Logger.getLogger(name);
		logger.setUseParentHandlers(false);
		for(Handler handler : logger.getHandlers()){
			handler.setLevel(Level.OFF);
			logger.removeHandler(handler);
		}
		
		ConsoleHandler handler   = new ConsoleHandler();
		Formatter      formatter = new FlatFormatter();
		
		handler.setFormatter(formatter);
		logger.addHandler(handler);
		
		return logger;
	}
}
