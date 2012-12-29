package three4clavin.endeca.adapter.jira;

import java.net.URI;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import com.endeca.edf.adapter.AdapterException;
import com.endeca.edf.adapter.PVal;
import com.endeca.edf.adapter.Record;

public class JIRAProjectUtils {
	public static NullProgressMonitor progress = new NullProgressMonitor();
	
	public static Record BasicProjectToRecord(BasicProject project, JiraRestClient restClient) throws AdapterException {
		Project projectDetail = getProject(project.getSelf(), restClient);
		Record  record        = new Record();
		
		// ---- Header details ----
		record.add(new PVal("dataSource", "JIRA"));
		record.add(new PVal("dataType",   "JIRA-Project"));
		record.add(new PVal("Endeca",     "Generic"));
		record.add(new PVal("Endeca.Id",  "JIRA-Project-" + projectDetail.getKey()));
		
		// ---- Gory details ----
		record.add(new PVal("projectUrl",   "" + projectDetail.getSelf()));
		record.add(new PVal("description",  "" + projectDetail.getDescription()));
		
		if(projectDetail.getComponents() != null){
			for(BasicComponent component : projectDetail.getComponents()){
				record.add(new PVal("component",     component.getName()));
				record.add(new PVal("componentFull", ""+
					component.getName() +"||||" +
					component.getSelf() + "||||" +
					component.getDescription())
				);
			}
		}
		
		record.add(new PVal("key", projectDetail.getKey()));
		
		BasicUser lead = projectDetail.getLead();
		if(lead != null){
			record.add(new PVal("lead",     lead.getName()));
			record.add(new PVal("leadFull", ""+
				lead.getName() + "||||" +
				lead.getDisplayName() + "||||" +
				lead.getSelf())
			);
		}
		
		record.add(new PVal("projectUrl",   "" + projectDetail.getLead()));
		
		if(projectDetail.getVersions() != null){
			for(Version version : projectDetail.getVersions()){
				record.add(new PVal("version", version.getName()));
				record.add(new PVal("versionFull", "" + 
					version.getName() + "||||" +
					version.getReleaseDate() + "||||" +
					version.getSelf() + "||||" +
					version.getDescription())
				);
			}
		}
		
		return record;
	}
	
	public static Project getProject(String projectKey, JiraRestClient restClient) throws AdapterException {
		Project project = restClient.getProjectClient().getProject(projectKey, progress);
		return project;
	}
	
	public static Project getProject(URI projectUri, JiraRestClient restClient) throws AdapterException {
		Project project = restClient.getProjectClient().getProject(projectUri, progress);
		return project;
	}
}
