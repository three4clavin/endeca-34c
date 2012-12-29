package three4clavin.endeca.adapter.jira;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import three4clavin.endeca.adapter.BaseAdapter;
import three4clavin.endeca.adapter.PojoPipelineSimulator;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.endeca.edf.adapter.AdapterConfig;
import com.endeca.edf.adapter.AdapterException;
import com.endeca.edf.adapter.AdapterHandler;
import com.endeca.edf.adapter.Record;

/**
 * <p>
 *    Implements an Endeca custom Record Adapter that will pull all projects
 *    and issues from a JIRA server.
 * </p>
 * 
 * <p>
 *    Adapter configuration must specify:<ul>
 *      <li>JIRA.Server - host URL to connect to (e.g. https://jira.company.com:443)</li>
 *      <li>JIRA.Username - username to connect with</li>
 *      <li>JIRA.Password - password to connect with</li>
 *      <li>(optional) JIRA.MaxTotalRecords - stop after emitting this many records</li>
 *    </ul>
 * </p>
 * 
 * <p>
 *    JIRA client classes from Atlassian require Java 1.6 or higher
 * </p>
 */
public class JIRAAdapter extends BaseAdapter {
	private static final Integer JIRA_SEARCH_PAGE_SIZE = 50;
	
    private static JerseyJiraRestClientFactory factory  = new JerseyJiraRestClientFactory();
    private static NullProgressMonitor         progress = new NullProgressMonitor();
    
    private JiraRestClient restClient = null;
    
    private String  jiraHost        = null;
    private String  jiraUsername    = null;
    private String  jiraPassword    = null;
    
    public JIRAAdapter(){
    	super();
    }
    
	public static void main(String[] args) throws AdapterException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		getLogger().info("POJO test of JIRA Endeca Adapter (normal usage should be from Endeca forge adapter)");
		
		if(args.length < 3){
			getLogger().severe("Usage: java JIRAAdapter <server> <username> <password> <maxRecords>");
			System.exit(1);
		}
		
		List<String> simulatorArgs = new ArrayList<String>();
		simulatorArgs.add(JIRAAdapter.class.getCanonicalName());
		simulatorArgs.add("JIRA.Server");
		simulatorArgs.add(args[0]);
		simulatorArgs.add("JIRA.Username");
		simulatorArgs.add(args[1]);
		simulatorArgs.add("JIRA.Password");
		simulatorArgs.add(args[2]);
		
		if(args.length > 3){
			simulatorArgs.add("JIRA.MaxTotalRecords");
			simulatorArgs.add(args[3]);
		}
		
		PojoPipelineSimulator.main(simulatorArgs);
	}
	
	public void execute(AdapterConfig config, AdapterHandler handler) throws AdapterException {
		setJiraHost(config.first("JIRA.Server"));
		setJiraUsername(config.first("JIRA.Username"));
		setJiraPassword(config.first("JIRA.Password"));
		setMaxTotalRecords(config.first("JIRA.MaxTotalRecords"));
		
		if(restClient == null){
			getRestClient();
		}
		
		getLogger().info("Getting all projects.");
		Iterator<BasicProject> projects = restClient.getProjectClient().getAllProjects(progress).iterator();
		while(projects.hasNext() && emit(projects.next(), handler)){
			// log.info("Processed " + totalRecordsProcessed + " records.");
		}
		
		getLogger().info("Getting all issues");
		Integer              searchIdx           = 0;
		SearchResult         searchResults       = restClient.getSearchClient().searchJql("", JIRA_SEARCH_PAGE_SIZE, searchIdx, progress);
		Integer              totalNumResults     = searchResults.getTotal();
		Integer              processedNumResults = 0;
		while(
			(searchResults != null) &&
			(searchResults.getIssues() != null) &&
			(processedNumResults < totalNumResults) &&
			(searchResults.getIssues().iterator().hasNext()) &&
			(emit(searchResults.getIssues().iterator().next(), handler))
		){
			// log.info("Processed " + totalRecordsProcessed + " records.");
			
			searchIdx += JIRA_SEARCH_PAGE_SIZE;
			
			if(
				(getMaxTotalRecords() != null) && 
				(getMaxTotalRecords() >= 0) && 
				(getTotalRecordsProcessed() >= getMaxTotalRecords())
			){
				searchResults = null;
			}
			else{
				searchResults = restClient.getSearchClient().searchJql("", JIRA_SEARCH_PAGE_SIZE, searchIdx, progress);
			}
		}
	}
	
	private Boolean emit(BasicIssue issue, AdapterHandler handler) throws AdapterException {
		Record record = JIRAIssueUtils.BasicIssueToRecord(issue, restClient);
		return emit(record, handler);
	}
	
	private Boolean emit(BasicProject project, AdapterHandler handler) throws AdapterException {
		Record record = JIRAProjectUtils.BasicProjectToRecord(project, restClient);
		return emit(record, handler);
	}
	
	public String getJiraHost(){
		return jiraHost;
	}
	public void setJiraHost(String jiraHost) throws AdapterException {
		if(jiraHost == null){
			throw new AdapterException("JIRA host must be provided (use JIRA.Server PASSTHROUGH in record adapter)");
		}
		this.jiraHost = jiraHost;
	}
	
	public String getJiraUsername(){
		return jiraUsername;
	}
	public void setJiraUsername(String jiraUsername) throws AdapterException {
		if(jiraUsername == null){
			throw new AdapterException("JIRA username must be provided (use JIRA.Username PASSTHROUGH in record adapter)");
		}
		this.jiraUsername = jiraUsername;
	}
	
	public String getJiraPassword(){
		return jiraPassword;
	}
	public void setJiraPassword(String jiraPassword) throws AdapterException {
		if(jiraPassword == null){
			throw new AdapterException("JIRA password must be provided (use JIRA.Password PASSTHROUGH in record adapter)");
		}
		this.jiraPassword = jiraPassword;
	}
	
	private JiraRestClient createJiraClient() throws URISyntaxException {
		return createJiraClient(jiraHost, jiraUsername, jiraPassword);
	}
	
	private JiraRestClient createJiraClient(String host, String username, String password) throws URISyntaxException {
	    URI            jiraServerUri = new URI(host);
	    JiraRestClient restClient    = factory.createWithBasicHttpAuthentication(jiraServerUri, username, password);
	    return restClient;
	}
	
	public JiraRestClient getRestClient() throws AdapterException {
		if(restClient == null){
			try {
				restClient = createJiraClient();
			}
			catch (URISyntaxException urise) {
				throw new AdapterException("Couldn't create new JIRA rest client: '" + urise + "'.");
			}
		}
		return restClient;
	}
	public void setRestClient(JiraRestClient restClient){
		this.restClient = restClient;
	}
}
