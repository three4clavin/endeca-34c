package three4clavin.endeca.adapter.jira;

import java.net.URI;

import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicIssueType;
import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicResolution;
import com.atlassian.jira.rest.client.domain.BasicStatus;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.BasicVotes;
import com.atlassian.jira.rest.client.domain.BasicWatchers;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.endeca.edf.adapter.AdapterException;
import com.endeca.edf.adapter.PVal;
import com.endeca.edf.adapter.Record;

public class JIRAIssueUtils {
	public static NullProgressMonitor progress = new NullProgressMonitor();
	
	public static Record BasicIssueToRecord(BasicIssue issue, JiraRestClient restClient) throws AdapterException {
		Issue  issueDetail = getIssue(issue.getKey(), restClient);
		Record record      = new Record();
		
		// ---- Header details ----
		record.add(new PVal("dataSource", "JIRA"));
		record.add(new PVal("dataType",   "JIRA-Issue"));
		record.add(new PVal("Endeca",     "Generic"));
		record.add(new PVal("recordId",   "JIRA-Issue-" + issue.getKey()));
		
		// ---- Gory details ----
		if(issueDetail.getAffectedVersions() != null){
			for(Version version : issueDetail.getAffectedVersions()){
				record.add(new PVal("affectedVersion", version.getName()));
				record.add(new PVal("affectedVersionFull", "" + 
					version.getName() + "||||" +
					version.getReleaseDate() + "||||" +
					version.getSelf() + "||||" +
					version.getDescription())
				);
			}
		}
		
		BasicUser assignee = issueDetail.getAssignee();
		if(assignee != null){
			record.add(new PVal("assignee", assignee.getName())); 
			record.add(new PVal("assigneeFull", "" + 
				assignee.getName() + "||||" +
				assignee.getSelf() + "||||" +
				assignee.getDisplayName())
			);
		}
		
		if(issueDetail.getAttachments() != null){
			for(Attachment attachment : issueDetail.getAttachments()){
				record.add(new PVal("attachment", attachment.getFilename())); 
				record.add(new PVal("attachmentFull", "" + 
					attachment.getFilename() + "||||" +
					attachment.getMimeType() + "||||" +
					attachment.getSize() + "||||" +
					attachment.getAuthor() + "||||" +
					attachment.getContentUri() + "||||" +
					attachment.getCreationDate() + "||||" +
					attachment.getSelf() + "||||" +
					attachment.getThumbnailUri())
				);
			}
		}
		
		URI attachmentsUri = issueDetail.getAttachmentsUri();
		if(attachmentsUri != null){
			record.add(new PVal("attachmentsUri", ""+attachmentsUri));
		}
		
		if(issueDetail.getComments() != null){
			for(Comment comment : issueDetail.getComments()){
				record.add(new PVal("commentFull", "" + 
					comment.getSelf() + "||||" +
					comment.getAuthor() + "||||" +
					comment.getCreationDate() + "||||" +
					comment.getVisibility() + "||||" +
					comment.getUpdateAuthor() + "||||" +
					comment.getUpdateDate() + "||||" +
					comment.getBody())
				);
			}
		}
		
		if(issueDetail.getComponents() != null){
			for(BasicComponent component : issueDetail.getComponents()){
				record.add(new PVal("componentFull", "" + 
					component.getName() + "||||" +
					component.getSelf() + "||||" +
					component.getDescription())
				);
			}
		}
		
		DateTime creationDate = issueDetail.getCreationDate();
		if(creationDate != null){
			record.add(new PVal("creationDate", "" + creationDate));
		}
		
		if(issueDetail.getExpandos() != null){
			for(String expando : issueDetail.getExpandos()){
				record.add(new PVal("expando", expando));
			}
		}
		
		if(issueDetail.getFields() != null){
			for(Field field : issueDetail.getFields()){
				record.add(new PVal("fieldFull", "" +
					field.getId() + "||||" +
					field.getName() + "||||" + 
					field.getType() + "||||" +
					field.getValue())
				);
			}
		}
		
		if(issueDetail.getFixVersions() != null){
			for(Version version : issueDetail.getFixVersions()){
				record.add(new PVal("fixVersion", version.getName()));
				record.add(new PVal("fixVersionFull", "" + 
					version.getName() + "||||" +
					version.getReleaseDate() + "||||" +
					version.getSelf() + "||||" +
					version.getDescription())
				);
			}
		}
		
		if(issueDetail.getIssueLinks() != null){
			for(IssueLink issueLink : issueDetail.getIssueLinks()){
				record.add(new PVal("issueLinkFull", "" +
					issueLink.getTargetIssueKey() + "||||" +
					issueLink.getIssueLinkType() + "||||" +
					issueLink.getTargetIssueUri())
				);
			}
		}
		
		BasicIssueType issueType = issueDetail.getIssueType();
		if(issueType != null){
			record.add(new PVal("issueType", "" + issueType.getName()));
			record.add(new PVal("issueTypeUrl", "" + issueType.getSelf()));
		}
		
		record.add(new PVal("key", "" + issueDetail.getKey()));
		
		BasicPriority priority = issueDetail.getPriority();
		if(priority != null){
			record.add(new PVal("priorityFull", "" +
				priority.getName() + "||||" +
				priority.getSelf())
			);
		}
		
		BasicProject project = issueDetail.getProject();
		if(project != null){
			record.add(new PVal("project", project.getKey()));
			
			Project projectDetail = JIRAProjectUtils.getProject(project.getKey(), restClient);
			if(projectDetail != null){
				record.add(new PVal("projectFull", "" +
					projectDetail.getKey() + "||||" +
					projectDetail.getLead() + "||||" +
					projectDetail.getSelf() + "||||" +
					projectDetail.getDescription())
				);
			}
		}
		
		BasicUser reporter = issueDetail.getReporter();
		if(reporter != null){
			record.add(new PVal("reporter", reporter.getName()));
			record.add(new PVal("reporterFull", ""+
				reporter.getName() + "||||" +
				reporter.getSelf() + "||||" +
				reporter.getDisplayName())
			);
		}
		
		BasicResolution resolution = issueDetail.getResolution();
		if(resolution != null){
			record.add(new PVal("resolution", resolution.getName()));
			record.add(new PVal("resolutionFull", ""+
				resolution.getName() + "||||" +
				resolution.getSelf())
			);
		}
		
		URI self = issueDetail.getSelf();
		if(self != null){
			record.add(new PVal("issueUrl", ""+self));
		}
		
		BasicStatus status = issueDetail.getStatus();
		if(status != null){
			record.add(new PVal("status", status.getName()));
			record.add(new PVal("statusFull", ""+
					status.getName() + "||||" +
					status.getSelf())
				);
		}
		
		String summary = issueDetail.getSummary();
		if(summary != null){
			record.add(new PVal("summary", summary));
		}
		
		TimeTracking timeTracking = issueDetail.getTimeTracking();
		if(timeTracking != null){
			record.add(new PVal("timeOriginal",  ""+timeTracking.getOriginalEstimateMinutes()));
			record.add(new PVal("timeRemaining", ""+timeTracking.getRemainingEstimateMinutes()));
			record.add(new PVal("timeSpent",     ""+timeTracking.getTimeSpentMinutes()));
		}
		
		URI transitionsUri = issueDetail.getTransitionsUri();
		if(transitionsUri != null){
			record.add(new PVal("transitionsUrl", ""+transitionsUri));
		}
		
		DateTime updateDate = issueDetail.getUpdateDate();
		if(updateDate != null){
			record.add(new PVal("updateDate", ""+updateDate));
		}
		
		BasicVotes votes = issueDetail.getVotes();
		if(votes != null){
			record.add(new PVal("votes", ""+votes));
		}
		
		URI votesUri = issueDetail.getVotesUri();
		if(votesUri != null){
			record.add(new PVal("votesUrl", ""+votesUri));
		}
		
		BasicWatchers watchers = issueDetail.getWatchers();
		if(watchers != null){
			record.add(new PVal("numWatchers", ""+watchers.getNumWatchers()));
		}
		
		if(issueDetail.getWorklogs() != null){
			for(Worklog worklog : issueDetail.getWorklogs()){
				record.add(new PVal("worklog", ""+
					worklog.getAuthor() + "||||" +
					worklog.getCreationDate() + "||||" +
					worklog.getIssueUri() + "||||" +
					worklog.getSelf() + "||||" +
					worklog.getStartDate() + "||||" +
					worklog.getVisibility() + "||||" +
					worklog.getMinutesSpent() + "||||" +
					worklog.getUpdateAuthor() + "||||" +
					worklog.getUpdateDate() + "||||" +
					worklog.getComment())
				);
			}
		}
		
		return record;
	}
	
	public static Issue getIssue(String issueKey, JiraRestClient restClient) throws AdapterException {
		Issue issue = null;
		try{
			issue = restClient.getIssueClient().getIssue(issueKey, progress);
		}
		catch(Exception e){
			e.printStackTrace();
			throw new AdapterException("Exception thrown trying to lookup issue '" + issueKey + "'");
		}
		
		return issue;
	}
}
