package three4clavin.endeca.adapter.gsite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import three4clavin.endeca.adapter.BaseAdapter;
import three4clavin.endeca.adapter.PojoPipelineSimulator;

import com.endeca.edf.adapter.AdapterConfig;
import com.endeca.edf.adapter.AdapterException;
import com.endeca.edf.adapter.AdapterHandler;
import com.endeca.edf.adapter.PVal;
import com.endeca.edf.adapter.Record;
import com.google.gdata.client.sites.SitesService;
import com.google.gdata.util.AuthenticationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sites.liberation.export.SiteExporter;
import com.google.sites.liberation.export.SiteExporterModule;
import com.google.sites.liberation.util.StdOutProgressListener;

/*
 * <p>
 *    Implements an Endeca custom Record Adapter that will pull all files
 *    from a Google site.
 * </p>
 * 
 * <p>
 *    Adapter configuration must specify:<ul>
 *      <li>Google.Host                       - Google server to connect to (e.g. sites.google.com)</li>
 *      <li>Google.Domain                     - Company domain to connect with (e.g. company.com for Google Apps)</li>
 *      <li>Google.Webspace                   - Google site to pull from</li>
 *      <li>Google.Username                   - Username/email address to connect with</li>
 *      <li>Google.Password                   - Password to connect with</li>
 *      <li>Google.Export.Revisions           - true/false</li>
 *      <li>Google.Destination.Directory      - Local directory to download to</li>
 *      <li>(optional) Google.MaxTotalRecords - stop after emitting this many records</li>
 *    </ul>
 * </p>
 * 
 * <p>
 *    Client classes from Google may require Java 1.6 or higher
 * </p>
 * 
 * <p>
 *    All google classes (everything in com.google.*) are largely cribbed from
 *    <a href="http://code.google.com/p/google-sites-liberation/">http://code.google.com/p/google-sites-liberation/</a>
 * </p>
 */
public class GSiteAdapter extends BaseAdapter {
	private String  googleHost;
	private String  googleDomain;
	private String  googleWebspace;
	private String  username;
	private String  password;
	private Boolean exportRevisions;
	private String  destinationDirectory;
	
    public GSiteAdapter(){
    	super();
    }
    
	public static void main(String[] args) throws ClassNotFoundException, AdapterException, InstantiationException, IllegalAccessException {
		getLogger().info("POJO test of Google Site Endeca Adapter (normal usage should be from Endeca forge adapter)");
		
		if(args.length < 7){
			getLogger().severe("Usage: java JIRAAdapter <host> <domain> <web space> <username> <password> <export revisions> <destination directory> <max records (optional)>");
			System.exit(1);
		}
		
		List<String> simulatorArgs = new ArrayList<String>();
		simulatorArgs.add(GSiteAdapter.class.getCanonicalName());
		simulatorArgs.add("Google.Host");
		simulatorArgs.add(args[0]);
		simulatorArgs.add("Google.Domain");
		simulatorArgs.add(args[1]);
		simulatorArgs.add("Google.Webspace");
		simulatorArgs.add(args[2]);
		simulatorArgs.add("Google.Username");
		simulatorArgs.add(args[3]);
		simulatorArgs.add("Google.Password");
		simulatorArgs.add(args[4]);
		simulatorArgs.add("Google.Export.Revisions");
		simulatorArgs.add(args[5]);
		simulatorArgs.add("Google.Destination.Directory");
		simulatorArgs.add(args[6]);
		
		if(args.length > 7){
			simulatorArgs.add("Google.MaxTotalRecords");
			simulatorArgs.add(args[7]);
		}
		
		PojoPipelineSimulator.main(simulatorArgs);
	}
    
    private void downloadSite(File downloadDirFull) throws AdapterException {
    	getLogger().info("Downloading GSite locally.  This may take some time.");
    	
		Injector     injector     = Guice.createInjector(new SiteExporterModule());
	    SiteExporter siteExporter = injector.getInstance(SiteExporter.class);
	    SitesService sitesService = new SitesService("google-sites-liberation");
	    try {
			sitesService.setUserCredentials(username, password);
		}
	    catch (AuthenticationException ae) {
	    	throw new AdapterException("Couldn't set user credentials: '" + ae + "'.");
		}
	    
	    siteExporter.exportSite(googleHost, googleDomain, googleWebspace, exportRevisions, sitesService, downloadDirFull, new StdOutProgressListener());
	    
	    getLogger().info("Site downloaded to " + downloadDirFull.getAbsolutePath() + ".");
    }
    
	@Override
	public void execute(AdapterConfig config, AdapterHandler handler) throws AdapterException {
		setGoogleHost(config.first("Google.Host"));
		setGoogleDomain(config.first("Google.Domain"));
		setGoogleWebspace(config.first("Google.Webspace"));
		setUsername(config.first("Google.Username"));
		setPassword(config.first("Google.Password"));
		setExportRevisions(config.first("Google.Export.Revisions"));
		setDestinationDirectory(config.first("Google.Destination.Directory"));
		setMaxTotalRecords(config.first("Google.MaxTotalRecords"));
		
	    if((destinationDirectory == null) || "".equals(destinationDirectory)){
	    	getLogger().severe("Destination directory not set.");
	    	System.exit(1);
	    }
	    
		File downloadDirFull = new File(destinationDirectory + "/" + googleDomain + "/" + googleWebspace);
	    if(downloadDirFull.exists()){
	    	getLogger().info("Deleting any old data in " + downloadDirFull.getAbsolutePath() + ".");
	    	recurseDelete(downloadDirFull);
	    }
	    
		downloadSite(downloadDirFull);
		
		emit(downloadDirFull, handler);
	}
	
	public void emit(File file, AdapterHandler handler) throws AdapterException {
		if(file.isDirectory()){
			for(File subFile : file.listFiles()){
				emit(subFile, handler);
			}
		}
		else{
			Record record = new Record();
			record.add(new PVal("Endeca",     "Generic"));
			record.add(new PVal("dataSource", "GSite"));
			record.add(new PVal("dataType",   "Google Site Page"));
			
			record.add(new PVal("File.AbsolutePath", file.getAbsolutePath()));
			record.add(new PVal("File.Name",         file.getName()));
			record.add(new PVal("File.Parent",       file.getParent()));
			record.add(new PVal("File.Path",         file.getPath()));
			record.add(new PVal("File.FreeSpace",    ""+file.getFreeSpace()));
			record.add(new PVal("File.TotalSpace",   ""+file.getTotalSpace()));
			record.add(new PVal("File.UsableSpace",  ""+file.getUsableSpace()));
			record.add(new PVal("File.Length",       ""+file.length()));
			try {
				record.add(new PVal("File.CanonicalPath", file.getCanonicalPath()));
			}
			catch (IOException e) {
			}
			
			record.add(new PVal("Endeca.Id", getGoogleHost() + "/" + getGoogleDomain() + "/" + getGoogleWebspace() + "/" + file.getPath()));
			
			// Add props necessary for doc conversion to work
			record.add(new PVal("Endeca.FileSystem.Path", file.getAbsolutePath()));
			record.add(new PVal("Endeca.Document.Body",   file.getAbsolutePath()));
			
			handler.emit(record);
		}
	}
	
	private void recurseDelete(File file){
		if(file.isDirectory()){
			for(File subFile : file.listFiles()){
				recurseDelete(subFile);
			}
		}
		getLogger().info("Deleting file '" + file.getAbsolutePath() + "'.");
		file.delete();
	}
	
	public String getGoogleHost(){
		return googleHost;
	}
	public void setGoogleHost(String googleHost) throws AdapterException {
		if(googleHost == null){
			throw new AdapterException("Google Host must be provided (use Google.Host PASSTHROUGH in record adapter)");
		}
		this.googleHost = googleHost;
	}
	
	public String getGoogleDomain(){
		return googleDomain;
	}
	public void setGoogleDomain(String googleDomain) throws AdapterException {
		if(googleDomain == null){
			throw new AdapterException("Google Domain must be provided (use Google.Domain PASSTHROUGH in record adapter)");
		}
		this.googleDomain = googleDomain;
	}
	
	public String getGoogleWebspace(){
		return googleWebspace;
	}
	public void setGoogleWebspace(String googleWebspace) throws AdapterException {
		if(googleWebspace == null){
			throw new AdapterException("Google Webspace must be provided (use Google.Webspace PASSTHROUGH in record adapter)");
		}
		this.googleWebspace = googleWebspace;
	}
	
	public String getUsername(){
		return username;
	}
	public void setUsername(String username) throws AdapterException {
		if(username == null){
			throw new AdapterException("Username must be provided (use Username PASSTHROUGH in record adapter)");
		}
		this.username = username;
	}
	
	public String getPassword(){
		return password;
	}
	public void setPassword(String password) throws AdapterException {
		if(password == null){
			throw new AdapterException("Password must be provided (use Password PASSTHROUGH in record adapter)");
		}
		this.password = password;
	}
	
	public Boolean getExportRevisions(){
		return exportRevisions;
	}
	public void setExportRevisions(String exportRevisions){
		if("true".equalsIgnoreCase(""+exportRevisions)){
			setExportRevisions(true);
		}
		else{
			setExportRevisions(false);
		}
	}
	public void setExportRevisions(Boolean exportRevisions){
		if(exportRevisions == null){
			this.exportRevisions = false;
		}
		else{
			this.exportRevisions = exportRevisions;
		}
	}
	
	public String getDestinationDirectory(){
		return destinationDirectory;
	}
	public void setDestinationDirectory(String destinationDirectory) throws AdapterException {
		if(destinationDirectory == null){
			throw new AdapterException("Destination directory must be provided (use Destination.Directory PASSTHROUGH in record adapter)");
		}
		this.destinationDirectory = destinationDirectory;
	}
}
