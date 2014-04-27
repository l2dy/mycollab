/**
 * This file is part of mycollab-scheduler.
 *
 * mycollab-scheduler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-scheduler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-scheduler.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.schedule.email.project.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.esofthead.mycollab.common.domain.SimpleAuditLog;
import com.esofthead.mycollab.common.domain.SimpleRelayEmailNotification;
import com.esofthead.mycollab.common.service.AuditLogService;
import com.esofthead.mycollab.core.utils.StringUtils;
import com.esofthead.mycollab.module.mail.TemplateGenerator;
import com.esofthead.mycollab.module.project.ProjectLinkUtils;
import com.esofthead.mycollab.module.project.ProjectResources;
import com.esofthead.mycollab.module.project.ProjectTypeConstants;
import com.esofthead.mycollab.module.project.domain.ProjectNotificationSetting;
import com.esofthead.mycollab.module.project.domain.ProjectNotificationSettingType;
import com.esofthead.mycollab.module.project.domain.ProjectRelayEmailNotification;
import com.esofthead.mycollab.module.project.domain.SimpleProject;
import com.esofthead.mycollab.module.project.domain.SimpleTask;
import com.esofthead.mycollab.module.project.service.ProjectMemberService;
import com.esofthead.mycollab.module.project.service.ProjectService;
import com.esofthead.mycollab.module.project.service.ProjectTaskService;
import com.esofthead.mycollab.module.user.UserLinkUtils;
import com.esofthead.mycollab.module.user.domain.SimpleUser;
import com.esofthead.mycollab.schedule.ScheduleUserTimeZoneUtils;
import com.esofthead.mycollab.schedule.email.ItemFieldMapper;
import com.esofthead.mycollab.schedule.email.LinkUtils;
import com.esofthead.mycollab.schedule.email.MailContext;
import com.esofthead.mycollab.schedule.email.format.DateFieldFormat;
import com.esofthead.mycollab.schedule.email.format.FieldFormat;
import com.esofthead.mycollab.schedule.email.format.html.TagBuilder;
import com.esofthead.mycollab.schedule.email.project.ProjectTaskRelayEmailNotificationAction;
import com.esofthead.mycollab.spring.ApplicationContextUtil;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Img;

/**
 * 
 * @author MyCollab Ltd.
 * @since 1.0
 * 
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProjectTaskRelayEmailNotificationActionImpl extends
		SendMailToFollowersAction implements
		ProjectTaskRelayEmailNotificationAction {

	@Autowired
	private ProjectTaskService projectTaskService;
	@Autowired
	private AuditLogService auditLogService;
	@Autowired
	private ProjectService projectService;

	private static final TaskFieldNameMapper mapper = new TaskFieldNameMapper();

	protected void setupMailHeaders(SimpleTask task,
			SimpleRelayEmailNotification emailNotification,
			TemplateGenerator templateGenerator) {
		List<Map<String, String>> listOfTitles = new ArrayList<Map<String, String>>();

		HashMap<String, String> currentProject = new HashMap<String, String>();
		currentProject.put("displayName", task.getProjectName());
		currentProject.put(
				"webLink",
				ProjectLinkUtils.generateProjectFullLink(siteUrl,
						task.getProjectid()));

		listOfTitles.add(currentProject);

		HashMap<String, String> taskCode = new HashMap<String, String>();
		SimpleProject relatedProject = projectService.findById(
				task.getProjectid(), emailNotification.getSaccountid());
		taskCode.put("displayName", "[" + relatedProject.getShortname() + "-"
				+ task.getTaskkey() + "]");
		taskCode.put(
				"webLink",
				ProjectLinkUtils.generateTaskPreviewFullLink(siteUrl,
						task.getProjectid(), task.getId()));

		listOfTitles.add(taskCode);

		String summary = task.getTaskname();
		String summaryLink = ProjectLinkUtils.generateTaskPreviewLink(
				task.getProjectid(), task.getId());

		templateGenerator.putVariable("makeChangeUser",
				emailNotification.getChangeByUserFullName());
		templateGenerator.putVariable("itemType", "task");
		templateGenerator.putVariable("titles", listOfTitles);
		templateGenerator.putVariable("summary", summary);
		templateGenerator.putVariable("summaryLink", summaryLink);
	}

	@Override
	public TemplateGenerator templateGeneratorForCreateAction(
			SimpleRelayEmailNotification emailNotification, SimpleUser user) {
		int taskId = emailNotification.getTypeid();
		SimpleTask task = projectTaskService.findById(taskId,
				emailNotification.getSaccountid());

		String subject = StringUtils.trim(task.getTaskname(), 100);
		ScheduleUserTimeZoneUtils.formatDateTimeZone(task, user.getTimezone(),
				new String[] { "startdate", "enddate", "deadline",
						"actualstartdate", "actualenddate" });

		TemplateGenerator templateGenerator = new TemplateGenerator("["
				+ task.getProjectName() + "]: "
				+ emailNotification.getChangeByUserFullName()
				+ " has created the task \"" + subject + "\"",
				"templates/email/project/itemCreatedNotifier.mt");

		setupMailHeaders(task, emailNotification, templateGenerator);

		templateGenerator.putVariable("context", new MailContext<SimpleTask>(
				task, user, siteUrl));
		templateGenerator.putVariable("mapper", mapper);
		return templateGenerator;
	}

	@Override
	public TemplateGenerator templateGeneratorForUpdateAction(
			SimpleRelayEmailNotification emailNotification, SimpleUser user) {
		int taskId = emailNotification.getTypeid();
		SimpleTask task = projectTaskService.findById(taskId,
				emailNotification.getSaccountid());
		if (task == null) {
			return null;
		}

		ScheduleUserTimeZoneUtils.formatDateTimeZone(task, user.getTimezone(),
				new String[] { "startdate", "enddate", "deadline",
						"actualstartdate", "actualenddate" });
		String subject = StringUtils.trim(task.getTaskname(), 100);

		TemplateGenerator templateGenerator = new TemplateGenerator("["
				+ task.getProjectName() + "]: "
				+ emailNotification.getChangeByUserFullName()
				+ " has updated the task \"" + subject + "\"",
				"templates/email/project/itemUpdatedNotifier.mt");

		setupMailHeaders(task, emailNotification, templateGenerator);

		if (emailNotification.getTypeid() != null) {
			SimpleAuditLog auditLog = auditLogService.findLatestLog(
					emailNotification.getTypeid(),
					emailNotification.getSaccountid());

			templateGenerator.putVariable("historyLog", auditLog);
			templateGenerator.putVariable("context",
					new MailContext<SimpleTask>(task, user, siteUrl));
			templateGenerator.putVariable("mapper", mapper);
		}

		return templateGenerator;
	}

	@Override
	public TemplateGenerator templateGeneratorForCommentAction(
			SimpleRelayEmailNotification emailNotification) {
		int taskId = emailNotification.getTypeid();
		SimpleTask task = projectTaskService.findById(taskId,
				emailNotification.getSaccountid());
		if (task == null) {
			return null;
		}

		TemplateGenerator templateGenerator = new TemplateGenerator("["
				+ task.getProjectName() + "]: "
				+ emailNotification.getChangeByUserFullName()
				+ " has commented on the task \""
				+ StringUtils.trim(task.getTaskname(), 100) + "\"",
				"templates/email/project/itemCommentNotifier.mt");
		setupMailHeaders(task, emailNotification, templateGenerator);

		templateGenerator.putVariable("comment", emailNotification);

		return templateGenerator;
	}

	@Override
	protected List<SimpleUser> getListNotififyUserWithFilter(
			ProjectRelayEmailNotification notification) {
		List<ProjectNotificationSetting> notificationSettings = projectNotificationService
				.findNotifications(notification.getProjectId(),
						notification.getSaccountid());

		ProjectMemberService projectService = ApplicationContextUtil
				.getSpringBean(ProjectMemberService.class);

		List<SimpleUser> activeUsers = projectService.getActiveUsersInProject(
				notification.getProjectId(), notification.getSaccountid());

		List<SimpleUser> inListUsers = notification.getNotifyUsers();

		if (notificationSettings != null && notificationSettings.size() > 0) {
			for (ProjectNotificationSetting notificationSetting : notificationSettings) {
				if (ProjectNotificationSettingType.NONE
						.equals(notificationSetting.getLevel())) {
					// remove users in list if he is already in list
					for (SimpleUser user : inListUsers) {
						if ((user.getUsername() != null && user.getUsername()
								.equals(notificationSetting.getUsername()))
								|| user.getEmail().equals(
										notificationSetting.getUsername())) {
							inListUsers.remove(user);
							break;
						}
					}
				} else if (ProjectNotificationSettingType.MINIMAL
						.equals(notificationSetting.getLevel())) {
					boolean isAlreadyInList = false;
					for (SimpleUser user : inListUsers) {
						if ((user.getUsername() != null && user.getUsername()
								.equals(notificationSetting.getUsername()))
								|| user.getEmail().equals(
										notificationSetting.getUsername())) {
							isAlreadyInList = true;
							break;
						}
					}

					if (!isAlreadyInList) {
						SimpleTask task = projectTaskService.findById(
								notification.getTypeid(),
								notification.getSaccountid());
						if (task.getAssignuser() != null
								&& task.getAssignuser().equals(
										notificationSetting.getUsername())) {
							for (SimpleUser user : activeUsers) {
								if ((user.getUsername() != null && user
										.getUsername().equals(
												notificationSetting
														.getUsername()))
										|| user.getEmail().equals(
												notificationSetting
														.getUsername())) {
									inListUsers.add(user);
									break;
								}
							}
						}
					}

				} else if (ProjectNotificationSettingType.FULL
						.equals(notificationSetting.getLevel())) {
					boolean isAlreadyInList = false;
					for (SimpleUser user : inListUsers) {
						if ((user.getUsername() != null && user.getUsername()
								.equals(notificationSetting.getUsername()))
								|| user.getEmail().equals(
										notificationSetting.getUsername())) {
							isAlreadyInList = true;
							break;
						}
					}

					if (!isAlreadyInList) {
						for (SimpleUser user : activeUsers) {
							if ((user.getUsername() != null && user
									.getUsername().equals(
											notificationSetting.getUsername()))
									|| user.getEmail().equals(
											notificationSetting.getUsername())) {
								inListUsers.add(user);
								break;
							}
						}
					}
				}
			}
		}

		return inListUsers;
	}

	public static class TaskFieldNameMapper extends ItemFieldMapper {

		public TaskFieldNameMapper() {

			put("taskname", "Task Name");
			put("startdate", new DateFieldFormat("startdate", "Start Date"));
			put("enddate", new DateFieldFormat("enddate", "End Date"));
			put("actualstartdate", new DateFieldFormat("actualstartdate",
					"Actual Start Date"));
			put("actualenddate", new DateFieldFormat("actualenddate",
					"Actual End Date"));
			put("assignuser", new AssigneeFieldFormat("assignuser", "Assignee"));
			put("percentagecomplete", "Complete (%)");
			put("notes", "Notes");
			put("priority", "Priority");
			put("deadline", new DateFieldFormat("deadline", "Deadline"));
			put("tasklistid", new TaskGroupFieldFormat("tasklistid",
					"Task Group"));
		}
	}

	public static class AssigneeFieldFormat extends FieldFormat {

		public AssigneeFieldFormat(String fieldName, String displayName) {
			super(fieldName, displayName);
		}

		@Override
		public String formatField(MailContext<?> context) {
			SimpleTask task = (SimpleTask) context.getWrappedBean();
			String userAvatarLink = LinkUtils.getAvatarLink(
					task.getAssignUserAvatarId(), 16);
			Img img = TagBuilder.newImg("avatar", userAvatarLink);

			String userLink = UserLinkUtils.generatePreviewFullUserLink(
					LinkUtils.getSiteUrl(task.getSaccountid()),
					task.getAssignuser());
			A link = TagBuilder.newA(userLink, task.getAssignUserFullName());
			return TagBuilder.newLink(img, link).write();
		}

		@Override
		public String formatField(MailContext<?> context, String value) {
			return value;
		}
	}

	public static class TaskGroupFieldFormat extends FieldFormat {

		public TaskGroupFieldFormat(String fieldName, String displayName) {
			super(fieldName, displayName);
		}

		@Override
		public String formatField(MailContext<?> context) {
			SimpleTask task = (SimpleTask) context.getWrappedBean();

			String taskgroupIconLink = ProjectResources
					.getResourceLink(ProjectTypeConstants.TASK_LIST);
			Img img = TagBuilder.newImg("icon", taskgroupIconLink);

			String tasklistlink = ProjectLinkUtils.generateTaskPreviewFullLink(
					context.getSiteUrl(), task.getProjectid(),
					task.getTasklistid());
			A link = TagBuilder.newA(tasklistlink, task.getTaskListName());
			return TagBuilder.newLink(img, link).write();
		}

		@Override
		public String formatField(MailContext<?> context, String value) {
			return value;
		}

	}
}