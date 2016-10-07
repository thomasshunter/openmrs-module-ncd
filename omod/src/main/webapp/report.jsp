<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="View NCD Scheduled Reports" otherwise="/login.htm" />

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />

<%
	String labelWidth="20%";
	String controlWidth="80%";
	String filterEditSize="60";
%>

<script type="text/javascript">

function debug(text) {
	//alert(text);
}

function getSelectedDivName(id) {
	debug("getSelectedDivName(" + id + ")");
	var ctrl = document.getElementById(id);
	debug("ctrl=" + ctrl);
	var classNameParts = ctrl.value.split('.');
	debug("classNameParts=" + classNameParts);
	var divName = classNameParts[classNameParts.length - 1];
	debug("divName=" + divName);
	return divName;
}

function setDisplay(id, activeId) {
	var display = 'none';
	if (id == activeId) {
		display = 'inline';
	}
	debug("set div " + id + " to " + display);
	document.getElementById(id).style.display = display;
}

function setVisible(divId) {
	debug('setVisible(' + divId + ')');
	document.getElementById(divId).style.display = 'inline';
}

function setInvisible(divId) {
	debug('setInvisible(' + divId + ')');
	document.getElementById(divId).style.display = 'none';
}

function updateVisibility() {

	debug("updateVisibility");

	// Make all divs invisible
	setInvisible('ExtractTask');
	setInvisible('ExtractTaskFilter');
	setInvisible('DailyExtractTask');
	setInvisible('DailyExtractTaskFilter');
	setInvisible('AggregateSummaryReportTask');
	setInvisible('DataExtractorFakeAll');
	setInvisible('ConditionRateReportTask');
	setInvisible('ConditionRateReport');
	setInvisible('ConditionRateExport');
	setInvisible('DataFeedSinkAccess');
	setInvisible('DataFeedSinkDelimited');
	setInvisible('DataFeedSenderFTP');
	setInvisible('DataFeedSenderSFTP');
	setInvisible('ZeroCountConditionReportTask');
	setInvisible('ZeroCountConditionReport');
	setInvisible('ZeroCountConditionExport');
	setInvisible('DataSourceReportTask');
	setInvisible('DataSourceReport');
	setInvisible('DataSourceExport');
	
	// Get the selected task type
	var taskType = getSelectedDivName('taskClass');
	
	debug("updateVisibility: taskType=" + taskType);

	// Based on the selected task type, make the appropriate div visible
	if (taskType == 'ExtractTask') {
		setVisible('ExtractTask');
		setVisible('ExtractTaskFilter');
		
		// Make the appropriate data feed sink div visible
		var dataFeedSinkType = getSelectedDivName("extractSinkClass");
		if (dataFeedSinkType == 'DataFeedSinkAccess') {
			setVisible('DataFeedSinkAccess');
		} else if (dataFeedSinkType == 'DataFeedSinkDelimited') {
			setVisible('DataFeedSinkDelimited');
		}
		
		// Make the appropriate data feed sender div visible
		var dataFeedSenderType = getSelectedDivName("extractSenderClass");
		if (dataFeedSenderType == 'DataFeedSenderFTP') {
			setVisible('DataFeedSenderFTP');
		} else if (dataFeedSenderType == 'DataFeedSenderSFTP') {
			setVisible('DataFeedSenderSFTP');
		}
	} else if (taskType == 'DailyExtractTask') {
		setVisible('DailyExtractTask');
		setVisible('DailyExtractTaskFilter');

		// Make the appropriate data feed sink div visible
		setVisible('DataFeedSinkDelimited');
		
		// Make the appropriate data feed sender div visible
		var dataFeedSenderType = getSelectedDivName("dailyExtractSenderClass");
		if (dataFeedSenderType == 'DataFeedSenderFTP') {
			setVisible('DataFeedSenderFTP');
		} else if (dataFeedSenderType == 'DataFeedSenderSFTP') {
			setVisible('DataFeedSenderSFTP');
		}
	} else if (taskType == 'AggregateSummaryReportTask') {
		setVisible('AggregateSummaryReportTask');
		
		// Make the appropriate data extractor div visible
		var dataExtractorName = getSelectedDivName("properties'DataExtractorFactory.class'");
		if (dataExtractorName == 'DataExtractorFakeAll') {
			setVisible('DataExtractorFakeAll');
		}
	} else if (taskType == 'ConditionRateReportTask') {
		debug("updateVisibility: ConditionRateReportTask");

		setVisible('ConditionRateReportTask');
		
		// Make the appropriate data feed sink div visible
		var dataFeedSinkType = getSelectedDivName("conditionRateExportSinkClass");
		if (dataFeedSinkType == 'Report') {
			setVisible('ConditionRateReport');
		}
		else if (dataFeedSinkType == 'DataFeedSinkDelimited') {
			setVisible('ConditionRateExport');
			setVisible('DataFeedSinkDelimited');
			
			// Make the appropriate data feed sender div visible
			var dataFeedSenderType = getSelectedDivName("conditionRateExportSenderClass");
			if (dataFeedSenderType == 'DataFeedSenderFTP') {
				setVisible('DataFeedSenderFTP');
			} else if (dataFeedSenderType == 'DataFeedSenderSFTP') {
				setVisible('DataFeedSenderSFTP');
			}
		}
	} else if (taskType == 'ZeroCountConditionReportTask') {
		debug("updateVisibility: ZeroCountConditionReportTask");
	
		setVisible('ZeroCountConditionReportTask');
		
		// Make the appropriate data feed sink div visible
		var dataFeedSinkType = getSelectedDivName("zeroCountConditionExportSinkClass");
		if (dataFeedSinkType == 'Report') {
			setVisible('ZeroCountConditionReport');
		}
		else if (dataFeedSinkType == 'DataFeedSinkDelimited') {
			setVisible('ZeroCountConditionExport');
			setVisible('DataFeedSinkDelimited');
			
			// Make the appropriate data feed sender div visible
			var dataFeedSenderType = getSelectedDivName("zeroCountConditionExportSenderClass");
			if (dataFeedSenderType == 'DataFeedSenderFTP') {
				setVisible('DataFeedSenderFTP');
			} else if (dataFeedSenderType == 'DataFeedSenderSFTP') {
				setVisible('DataFeedSenderSFTP');
			}
		}
	} else if (taskType == 'DataSourceReportTask') {
		debug("updateVisibility: DataSourceReportTask");
	
		setVisible('DataSourceReportTask');
		
		// Make the appropriate data feed sink div visible
		var dataFeedSinkType = getSelectedDivName("dataSourceExportSinkClass");
		if (dataFeedSinkType == 'Report') {
			setVisible('DataSourceReport');
		}
		else if (dataFeedSinkType == 'DataFeedSinkDelimited') {
			setVisible('DataSourceExport');
			setVisible('DataFeedSinkDelimited');
			
			// Make the appropriate data feed sender div visible
			var dataFeedSenderType = getSelectedDivName("dataSourceExportSenderClass");
			if (dataFeedSenderType == 'DataFeedSenderFTP') {
				setVisible('DataFeedSenderFTP');
			} else if (dataFeedSenderType == 'DataFeedSenderSFTP') {
				setVisible('DataFeedSenderSFTP');
			}
		}
	}
}

</script>

<h2>
	<c:choose>
		<c:when test="${getNcdReportObj.id != null}">
			<spring:message code="ncd.pages.report.title.existing" />
		</c:when>
		<c:otherwise>
			<spring:message code="ncd.pages.report.title.new" />
		</c:otherwise>
	</c:choose>
</h2>

<br/>

<style type="text/css">

div.scrollable {
	border: 1px solid #8FABC7;
	overflow-x: hidden;
}

</style>

<form:form commandName="getNcdReportObj">

	<openmrs:hasPrivilege privilege="Add NCD Scheduled Reports,Delete NCD Scheduled Reports">
		<input type="submit" value="<spring:message code="ncd.buttons.save"/>" name="action">
	</openmrs:hasPrivilege>
	<input type="submit" value="<spring:message code="ncd.buttons.cancel"/>" name="action">
	<br><%@include file="requiredLegend.jsp" %>
	<div class="scrollable" style="height:580px">		
		<b class="boxHeader"><spring:message code="ncd.pages.report.section.common"/></b>
		<table class="box">
			<c:if test="${getNcdReportObj.id != null}">
				<tr>
					<td width="<%=labelWidth%>" nowrap>ID:</td>
					<td width="<%=controlWidth%>">${getNcdReportObj.id}</td>
				</tr>
			</c:if>
			<tr>
				<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.name"/><spring:message code="ncd.required"/></td>
				<td width="<%=controlWidth%>">
					<c:set var="title"><spring:message code="ncd.pages.report.name.title"/></c:set>
					<form:input size="30" path="name" title="${title}"/>
					<form:errors path="name" cssClass="error" />
				</td>
	   			
			</tr>
			<tr>
				<td nowrap><spring:message code="ncd.pages.report.description"/></td>
				<td>
					<c:set var="title"><spring:message code="ncd.pages.report.description.title"/></c:set>
					<form:input size="100" path="description" title="${title}"/>
					<form:errors path="description" cssClass="error" />
				</td>
			</tr>
			<tr>
				<td nowrap><spring:message code="ncd.pages.report.starttime"/><spring:message code="ncd.required"/></td>
				<td>
					<c:set var="title"><spring:message code="ncd.pages.report.starttime.title"/></c:set>
					<form:input size="30" path="startTime" title="${title}"/>
					<form:errors path="startTime" cssClass="error" />
				</td>
			</tr>
			<tr>
				<td nowrap><spring:message code="ncd.pages.report.repeatinterval"/><spring:message code="ncd.required"/></td>
				<td>
					<c:set var="title"><spring:message code="ncd.pages.report.repeatinterval.title"/></c:set>
					<form:input size="6" path="repeatInterval" title="${title}"/>
					<form:errors path="repeatInterval" cssClass="error" />
					<form:select path="repeatIntervalUnits">
						<form:options items="${repeatIntervalUnits}" />
					</form:select>
					<form:errors path="repeatIntervalUnits" cssClass="error" />
				</td>
			</tr>
			<tr>
				<td nowrap><spring:message code="ncd.pages.report.enabled"/></td>
				<td>
					<c:set var="title"><spring:message code="ncd.pages.report.enabled.title"/></c:set>
					<form:checkbox path="enabled" title="${title}"/>
					<form:errors path="enabled" cssClass="error" />
				</td>
			</tr>
			<tr>
				<td nowrap><spring:message code="ncd.pages.report.tasktype"/></td>
				<td>
					<c:set var="title"><spring:message code="ncd.pages.report.tasktype.title"/></c:set>
					<form:select path="taskClass" onchange="updateVisibility()" title="${title}">
						<form:options items="${reportTypes}" />
					</form:select>
					<form:errors path="taskClass" cssClass="error" />
				</td>
			</tr>
		</table>
		<div id="AggregateSummaryReportTask" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.aggregate"/></b>
			<table class="box">
				<tr>
					<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.aggregate.dataextractor"/></td>
					<td width="<%=controlWidth%>">
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.dataextractor.title"/></c:set>
						<form:select path="properties['DataExtractorFactory.class']" onchange="updateVisibility()" title="${title}">
							<form:options items="${dataExtractors}" />
						</form:select>
						<form:errors path="properties['DataExtractorFactory.class']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.cutoff.datetime"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.cutoff.datetime.title"/></c:set>
						<form:input size="30" path="properties['DataExtractorFactory.cutoffDate']" title="${title}"/>
						<form:errors path="properties['DataExtractorFactory.cutoffDate']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.column.count"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.column.count.title"/></c:set>
						<form:input path="properties['DataExtractorFactory.dateBucketCount']" title="${title}"/>
						<form:errors path="properties['DataExtractorFactory.dateBucketCount']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.recipient.name"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.recipient.name.title"/></c:set>
						<form:input size="50" path="aggregateReportTitle" title="${title}"/>
						<form:errors path="aggregateReportTitle" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.template"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.template.title"/></c:set>
						<form:textarea rows="10" cols="100" path="aggregateReportTemplate" title="${title}"/>
						<form:errors path="aggregateReportTemplate" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.template.pathname"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.template.pathname.title"/></c:set>
						<form:input size="100" path="aggregateReportTemplatePathname" title="${title}"/>
						<form:errors path="aggregateReportTemplatePathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.sender.title"/></c:set>
						<form:input size="50" path="aggregateReportSender" title="${title}"/>
						<form:errors path="aggregateReportSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.recipients.title"/></c:set>
						<form:input size="100" path="aggregateReportRecipients" title="${title}"/>
						<form:errors path="aggregateReportRecipients" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.subject"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.subject.title"/></c:set>
						<form:input size="100" path="aggregateReportSubject" title="${title}"/>
						<form:errors path="aggregateReportSubject" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="ConditionRateReportTask" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.condrate"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.cutoff.datetime"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.cutoff.datetime.title"/></c:set>
						<form:input size="30" path="properties['ConditionRateReportTask.cutoffDateTime']" title="${title}"/>
						<form:errors path="properties['ConditionRateReportTask.cutoffDateTime']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.condrate.sampledays"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.condrate.sampledays.title"/></c:set>
						<form:input size="30" path="properties['ConditionRateReportTask.sampleDays']" title="${title}"/>
						<form:errors path="properties['ConditionRateReportTask.sampleDays']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.condrate.historydays"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.condrate.historydays.title"/></c:set>
						<form:input size="5" path="properties['ConditionRateReportTask.historyDays']" title="${title}"/>
						<form:errors path="properties['ConditionRateReportTask.historyDays']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.condrate.lowrateratio"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.condrate.lowrateratio.title"/></c:set>
						<form:input size="30" path="properties['ConditionRateReportTask.lowRateRatio']" title="${title}"/>
						<form:errors path="properties['ConditionRateReportTask.lowRateRatio']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.condrate.highrateratio"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.condrate.highrateratio.title"/></c:set>
						<form:input size="30" path="properties['ConditionRateReportTask.highRateRatio']" title="${title}"/>
						<form:errors path="properties['ConditionRateReportTask.highRateRatio']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsink"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsink.title"/></c:set>
						<form:select path="conditionRateExportSinkClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${crrDataFeedSinks}" />
						</form:select>
						<form:errors path="conditionRateExportSinkClass" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="ConditionRateReport" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.condrate.report"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.recipient.name"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.recipient.name.title"/></c:set>
						<form:input size="50" path="conditionRateReportTitle" title="${title}"/>
						<form:errors path="conditionRateReportTitle" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.template"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.template.title"/></c:set>
						<form:textarea rows="10" cols="100" path="conditionRateReportTemplate" title="${title}"/>
						<form:errors path="conditionRateReportTemplate" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.template.pathname"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.template.pathname.title"/></c:set>
						<form:input size="100" path="conditionRateReportTemplatePathname" title="${title}"/>
						<form:errors path="conditionRateReportTemplatePathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.sender.title"/></c:set>
						<form:input size="50" path="conditionRateReportSender" title="${title}"/>
						<form:errors path="conditionRateReportSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.recipients.title"/></c:set>
						<form:input size="100" path="conditionRateReportRecipients" title="${title}"/>
						<form:errors path="conditionRateReportRecipients" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.subject"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.subject.title"/></c:set>
						<form:input size="100" path="conditionRateReportSubject" title="${title}"/>
						<form:errors path="conditionRateReportSubject" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="ConditionRateExport" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.condrate.export"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.pathname"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.pathname.title"/></c:set>
						<form:input size="100" path="conditionRateExportDestinationPathname" title="${title}"/>
						<form:errors path="conditionRateExportDestinationPathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.extension"/><spring:message code="ncd.required"/></td>
					<td nowrap>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.extension.title"/></c:set>
						<form:input path="conditionRateExportDestinationExtension" title="${title}"/>
						<form:errors path="conditionRateExportDestinationExtension" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.add.timestamp"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.add.timestamp.title"/></c:set>
						<form:checkbox path="conditionRateExportAddTimestamp" value="true" title="${title}"/>
						<form:errors path="conditionRateExportAddTimestamp" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.timestamp.format"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.timestamp.format.title"/></c:set>
						<form:input size="30" path="conditionRateExportTimestampFormat" title="${title}"/>
						<form:errors path="conditionRateExportTimestampFormat" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.sender.title"/></c:set>
						<form:input size="100" path="conditionRateExportSender" title="${title}"/>
						<form:errors path="conditionRateExportSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.success.recipients"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.success.recipients.title"/></c:set>
						<form:input size="100" path="conditionRateExportSuccessAlertList" title="${title}"/>
						<form:errors path="conditionRateExportSuccessAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.error.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.error.recipients.title"/></c:set>
						<form:input size="100" path="conditionRateExportErrorAlertList" title="${title}"/>
						<form:errors path="conditionRateExportErrorAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.title"/></c:set>
						<form:select path="conditionRateExportSenderClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${crrDataFeedSenders}" />
						</form:select>
						<form:errors path="conditionRateExportSenderClass" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="ZeroCountConditionReportTask" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.zerocount"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.zerocount.period"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.zerocount.period.title"/></c:set>
						<form:input size="30" path="properties['ZeroCountConditionReportTask.period']" title="${title}"/>
						<form:errors path="properties['ZeroCountConditionReportTask.period']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.zerocount.cutoff"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.zerocount.cutoff.title"/></c:set>
						<form:input size="30" path="properties['ZeroCountConditionReportTask.cutoffDateTime']" title="${title}"/>
						<form:errors path="properties['ZeroCountConditionReportTask.cutoffDateTime']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsink"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsink.title"/></c:set>
						<form:select path="zeroCountConditionExportSinkClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${crrDataFeedSinks}" />
						</form:select>
						<form:errors path="zeroCountConditionExportSinkClass" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<p><spring:message code="ncd.pages.report.zerocount.condlist"/></p>					
						<table class="box">
							<tr>
								<td>&nbsp;</td>
								<th><spring:message code="ncd.pages.report.zerocount.condlist.application"/></th>
								<th><spring:message code="ncd.pages.report.zerocount.condlist.facility"/></th>
								<th><spring:message code="ncd.pages.report.zerocount.condlist.location"/></th>
								<th><spring:message code="ncd.pages.report.zerocount.condlist.condition"/><spring:message code="ncd.required"/></th>
							</tr>
							<tr>
								<td><input type="submit" value='<spring:message code="ncd.buttons.add"/>' name="action"></td>
								<td>
									<c:set var="title"><spring:message code="ncd.pages.report.zerocount.condlist.application.title"/></c:set>
									<form:input size="32" path="newApplication" title="${title}"/>
									<form:errors path="newApplication" cssClass="error" />
								</td>
								<td>
									<c:set var="title"><spring:message code="ncd.pages.report.zerocount.condlist.facility.title"/></c:set>
									<form:input size="32" path="newFacility" title="${title}"/>
									<form:errors path="newFacility" cssClass="error" />
								</td>
								<td>
									<c:set var="title"><spring:message code="ncd.pages.report.zerocount.condlist.location.title"/></c:set>
									<form:input size="32" path="newLocation" title="${title}"/>
									<form:errors path="newLocation" cssClass="error" />
								</td>
								<td>
									<c:set var="title"><spring:message code="ncd.pages.report.zerocount.condlist.condition.title"/></c:set>
									<form:select path="newCondition" title="${title}">
										<form:options items="${anyConditions}" />
									</form:select>
									<form:errors path="newCondition" cssClass="error" />
								</td>
							</tr>
							<c:forEach var="row" varStatus="idx" items="${getNcdReportObj.monitoredConditions}">
								<tr>
									<td><form:checkbox path="selectedMonitoredConditions" value="${row.id}"/></td>
									<td>${row.application}</td>
									<td>${row.facility}</td>
									<td>${row.location}</td>
									<td>
										<c:choose>
											<c:when test="${row.condition != null}">
												${row.condition.displayText}
											</c:when>
											<c:otherwise>
												*
											</c:otherwise>
										</c:choose>
									</td>
								</tr>
							</c:forEach>
							<td>
								<td colspan="5">
									<input type="submit" value='<spring:message code="ncd.buttons.deleteselected"/>' name="action">
								</td>
							</td>
						</table>
					</td>
				</tr>
			</table>
		</div>
		<div id="ZeroCountConditionReport" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.zerocount.report"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.recipient.name"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.recipient.name.title"/></c:set>
						<form:input size="50" path="zeroCountConditionReportTitle" title="${title}"/>
						<form:errors path="zeroCountConditionReportTitle" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.template"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.template.title"/></c:set>
						<form:textarea rows="10" cols="100" path="zeroCountConditionReportTemplate" title="${title}"/>
						<form:errors path="zeroCountConditionReportTemplate" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.template.pathname"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.template.pathname.title"/></c:set>
						<form:input size="100" path="zeroCountConditionReportTemplatePathname" title="${title}"/>
						<form:errors path="zeroCountConditionReportTemplatePathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.sender.title"/></c:set>
						<form:input size="50" path="zeroCountConditionReportSender" title="${title}"/>
						<form:errors path="zeroCountConditionReportSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.recipients.title"/></c:set>
						<form:input size="100" path="zeroCountConditionReportRecipients" title="${title}"/>
						<form:errors path="zeroCountConditionReportRecipients" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.subject"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.subject.title"/></c:set>
						<form:input size="100" path="zeroCountConditionReportSubject" title="${title}"/>
						<form:errors path="zeroCountConditionReportSubject" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="ZeroCountConditionExport" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.zerocount.export"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.pathname"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.pathname.title"/></c:set>
						<form:input size="100" path="zeroCountConditionExportDestinationPathname" title="${title}"/>
						<form:errors path="zeroCountConditionExportDestinationPathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.extension"/><spring:message code="ncd.required"/></td>
					<td nowrap>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.extension.title"/></c:set>
						<form:input path="zeroCountConditionExportDestinationExtension" title="${title}"/>
						<form:errors path="zeroCountConditionExportDestinationExtension" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.add.timestamp"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.add.timestamp.title"/></c:set>
						<form:checkbox path="zeroCountConditionExportAddTimestamp" value="true" title="${title}"/>
						<form:errors path="zeroCountConditionExportAddTimestamp" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.timestamp.format"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.timestamp.format.title"/></c:set>
						<form:input size="30" path="zeroCountConditionExportTimestampFormat" title="${title}"/>
						<form:errors path="zeroCountConditionExportTimestampFormat" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.sender.title"/></c:set>
						<form:input size="100" path="zeroCountConditionExportSender" title="${title}"/>
						<form:errors path="zeroCountConditionExportSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.success.recipients"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.success.recipients.title"/></c:set>
						<form:input size="100" path="zeroCountConditionExportSuccessAlertList" title="${title}"/>
						<form:errors path="zeroCountConditionExportSuccessAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.error.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.error.recipients.title"/></c:set>
						<form:input size="100" path="zeroCountConditionExportErrorAlertList" title="${title}"/>
						<form:errors path="zeroCountConditionExportErrorAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.title"/></c:set>
						<form:select path="zeroCountConditionExportSenderClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${crrDataFeedSenders}" />
						</form:select>
						<form:errors path="zeroCountConditionExportSenderClass" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DataSourceReportTask" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.datasource"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.datasource.period"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.datasource.period.title"/></c:set>
						<form:input size="30" path="properties['DataSourceReportTask.period']" title="${title}"/>
						<form:errors path="properties['DataSourceReportTask.period']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.datasource.cutoff"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.datasource.cutoff.title"/></c:set>
						<form:input size="30" path="properties['DataSourceReportTask.cutoffDateTime']" title="${title}"/>
						<form:errors path="properties['DataSourceReportTask.cutoffDateTime']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsink"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsink.title"/></c:set>
						<form:select path="dataSourceExportSinkClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${crrDataFeedSinks}" />
						</form:select>
						<form:errors path="dataSourceExportSinkClass" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DataSourceReport" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.datasource.report"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.recipient.name"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.recipient.name.title"/></c:set>
						<form:input size="50" path="dataSourceReportTitle" title="${title}"/>
						<form:errors path="dataSourceReportTitle" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.template"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.template.title"/></c:set>
						<form:textarea rows="10" cols="100" path="dataSourceReportTemplate" title="${title}"/>
						<form:errors path="dataSourceReportTemplate" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.template.pathname"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.template.pathname.title"/></c:set>
						<form:input size="100" path="dataSourceReportTemplatePathname" title="${title}"/>
						<form:errors path="dataSourceReportTemplatePathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.sender.title"/></c:set>
						<form:input size="50" path="dataSourceReportSender" title="${title}"/>
						<form:errors path="dataSourceReportSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.recipients.title"/></c:set>
						<form:input size="100" path="dataSourceReportRecipients" title="${title}"/>
						<form:errors path="dataSourceReportRecipients" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.aggregate.subject"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.aggregate.subject.title"/></c:set>
						<form:input size="100" path="dataSourceReportSubject" title="${title}"/>
						<form:errors path="dataSourceReportSubject" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DataSourceExport" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.datasource.export"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.pathname"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.pathname.title"/></c:set>
						<form:input size="100" path="dataSourceExportDestinationPathname" title="${title}"/>
						<form:errors path="dataSourceExportDestinationPathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.extension"/><spring:message code="ncd.required"/></td>
					<td nowrap>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.extension.title"/></c:set>
						<form:input path="dataSourceExportDestinationExtension" title="${title}"/>
						<form:errors path="dataSourceExportDestinationExtension" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.add.timestamp"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.add.timestamp.title"/></c:set>
						<form:checkbox path="dataSourceExportAddTimestamp" value="true" title="${title}"/>
						<form:errors path="dataSourceExportAddTimestamp" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.timestamp.format"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.timestamp.format.title"/></c:set>
						<form:input size="30" path="dataSourceExportTimestampFormat" title="${title}"/>
						<form:errors path="dataSourceExportTimestampFormat" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.sender.title"/></c:set>
						<form:input size="100" path="dataSourceExportSender" title="${title}"/>
						<form:errors path="dataSourceExportSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.success.recipients"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.success.recipients.title"/></c:set>
						<form:input size="100" path="dataSourceExportSuccessAlertList" title="${title}"/>
						<form:errors path="dataSourceExportSuccessAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.error.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.error.recipients.title"/></c:set>
						<form:input size="100" path="dataSourceExportErrorAlertList" title="${title}"/>
						<form:errors path="dataSourceExportErrorAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.title"/></c:set>
						<form:select path="dataSourceExportSenderClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${crrDataFeedSenders}" />
						</form:select>
						<form:errors path="dataSourceExportSenderClass" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DataExtractorFakeAll" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.extract.fakeall"/></b>
			<table class="box">
				<tr>
					<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.extract.fakeall.groups"/></td>
					<td width="<%=controlWidth%>">
						<form:input path="properties['DataExtractorFakeAll.groupCount']"/>
						<form:errors path="properties['DataExtractorFakeAll.groupCount']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.extract.fakeall.conditions"/></td>
					<td>
						<form:input path="properties['DataExtractorFakeAll.conditionCount']"/>
						<form:errors path="properties['DataExtractorFakeAll.conditionCount']" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="ExtractTaskFilter" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.export.filter"/></b>
			<table class="box">
				<tr>
					<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.export.filter.institutions"/></td>
					<td width="<%=controlWidth%>">
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.institutions.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.institutionsToSend']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.institutionsToSend']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.conditions"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.conditions.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.conditionNamesToSend']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.conditionNamesToSend']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.excluded.conditions"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.excluded.conditions.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.conditionNamesToNotSend']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.conditionNamesToNotSend']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.counties"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.counties.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.counties']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.counties']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.jurisdictions"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.jurisdictions.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.jurisdictions']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.jurisdictions']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.codes"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.codes.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.codes']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.codes']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.new.results"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.new.results.title"/></c:set>
						<c:set var="incremental" value="${getNcdReportObj.properties['DataFeedExtractorFactory.incremental']}"/>
						<c:if test="${incremental == null}">
							<c:set var="incremental" value="true"/>
						</c:if>
						<form:checkbox path="properties['DataFeedExtractorFactory.incremental']" value="${incremental}" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.incremental']" cssClass="error" />
						<spring:message code="ncd.pages.report.export.filter.new.results.last.run"/>${lastrundate}
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.recent.results"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.recent.results.title"/></c:set>
						<form:input size="6" path="properties['DataFeedExtractorFactory.recentResultInterval']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.recentResultInterval']" cssClass="error" />
						<form:select path="properties['DataFeedExtractorFactory.recentResultIntervalUnits']">
							<form:options items="${recentResultIntervalUnits}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.recentResultIntervalUnits']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.date.range"/></td>
					<td>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.start.date"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.start.date.title"/></c:set>
						<form:input size="10" path="properties['DataFeedExtractorFactory.startDate']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.startDate']" cssClass="error" />
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.start.time.title"/></c:set>
						<form:input size="10" path="properties['DataFeedExtractorFactory.startTime']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.startTime']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.end.date"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.end.date.title"/></c:set>
						<form:input size="10" path="properties['DataFeedExtractorFactory.endDate']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.endDate']" cssClass="error" />
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.end.time.title"/></c:set>
						<form:input size="10" path="properties['DataFeedExtractorFactory.endTime']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.endTime']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude"/></td>
					<td>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.condition"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.condition.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeCondition1']">
							<form:options items="${conditions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCondition1']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.codes"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.codes.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.excludeCodes1']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCodes1']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.institution"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.institution.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeInstitution1']">
							<form:options items="${institutions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeInstitution1']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude"/></td>
					<td>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.condition"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.condition.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeCondition2']">
							<form:options items="${conditions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCondition2']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.codes"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.codes.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.excludeCodes2']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCodes2']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.institution"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.institution.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeInstitution2']">
							<form:options items="${institutions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeInstitution2']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude"/></td>
					<td>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.condition"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.condition.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeCondition3']">
							<form:options items="${conditions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCondition3']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.codes"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.codes.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.excludeCodes3']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCodes3']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.institution"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.institution.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeInstitution3']">
							<form:options items="${institutions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeInstitution3']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude"/></td>
					<td>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.condition"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.condition.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeCondition4']">
							<form:options items="${conditions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCondition4']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.codes"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.codes.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.excludeCodes4']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCodes4']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.institution"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.institution.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeInstitution4']">
							<form:options items="${institutions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeInstitution4']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude"/></td>
					<td>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.condition"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.condition.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeCondition5']">
							<form:options items="${conditions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCondition5']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.codes"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.codes.title"/></c:set>
						<form:input size="<%=filterEditSize%>" path="properties['DataFeedExtractorFactory.excludeCodes5']" title="${title}"/>
						<form:errors path="properties['DataFeedExtractorFactory.excludeCodes5']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.filter.exclude.institution"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.filter.exclude.institution.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.excludeInstitution5']">
							<form:options items="${institutions}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.excludeInstitution5']" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="ExtractTask" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.export"/></b>
			<table class="box">
				<tr>
					<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.feedextractor"/></td>
					<td width="<%=controlWidth%>">
						<c:set var="title"><spring:message code="ncd.pages.report.feedextractor.title"/></c:set>
						<form:select path="properties['DataFeedExtractorFactory.class']" onchange="updateVisibility()" title="${title}">
							<form:options items="${dataFeedExtractors}" />
						</form:select>
						<form:errors path="properties['DataFeedExtractorFactory.class']" cssClass="error" />
					</td>
				</tr>
				<c:if test="${debugging}">
					<tr>
						<td nowrap><spring:message code="ncd.pages.report.export.maxrows"/></td>
						<td>
							<c:set var="title"><spring:message code="ncd.pages.report.export.maxrows.title"/></c:set>
							<form:input path="properties['DataFeedExtractorFactory.maxRows']" title="${title}"/>
							<form:errors path="properties['DataFeedExtractorFactory.maxRows']" cssClass="error" />
						</td>
					</tr>
				</c:if>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsink"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsink.title"/></c:set>
						<form:select path="extractSinkClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${dataFeedSinks}" />
						</form:select>
						<form:errors path="extractSinkClass" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.strip"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.strip.title"/></c:set>
						<form:select path="extractStripForRHITs" onchange="updateVisibility()" title="${title}">
							<form:options items="${booleans}" />
						</form:select>
						<form:errors path="extractStripForRHITs" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.columns"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.columns.title"/></c:set>
						<form:input size="100" path="properties['DataFeedSinkFactory.outputColumnsToInclude']" title="${title}"/>
						<form:errors path="properties['DataFeedSinkFactory.outputColumnsToInclude']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.pathname"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.pathname.title"/></c:set>
						<form:input size="100" path="extractDestinationPathname" title="${title}"/>
						<form:errors path="extractDestinationPathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.extension"/><spring:message code="ncd.required"/></td>
					<td nowrap>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.extension.title"/></c:set>
						<form:input path="extractDestinationExtension" title="${title}"/>
						<form:errors path="extractDestinationExtension" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.add.timestamp"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.add.timestamp.title"/></c:set>
						<form:checkbox path="extractAddTimestamp" value="true" title="${title}"/>
						<form:errors path="extractAddTimestamp" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.timestamp.format"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.timestamp.format.title"/></c:set>
						<form:input size="30" path="extractTimestampFormat" title="${title}"/>
						<form:errors path="extractTimestampFormat" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.sender.title"/></c:set>
						<form:input size="100" path="extractSender" title="${title}"/>
						<form:errors path="extractSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.success.recipients"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.success.recipients.title"/></c:set>
						<form:input size="100" path="extractSuccessAlertList" title="${title}"/>
						<form:errors path="extractSuccessAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.error.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.error.recipients.title"/></c:set>
						<form:input size="100" path="extractErrorAlertList" title="${title}"/>
						<form:errors path="extractErrorAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.title"/></c:set>
						<form:select path="extractSenderClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${dataFeedSenders}" />
						</form:select>
						<form:errors path="extractSenderClass" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DailyExtractTaskFilter" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.dailyexport.filter"/></b>
			<table class="box">
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.dailyexport.filter.days"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.dailyexport.filter.days.title"/></c:set>
						<form:input size="6" path="properties['DailyExtractorFactory.days']" title="${title}"/>
						<form:errors path="properties['DailyExtractorFactory.days']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.dailyexport.filter.reportable"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.dailyexport.filter.reportable.title"/></c:set>
						<form:select path="properties['DailyExtractorFactory.reportable']" title="${title}">
							<form:options items="${reportStatusTypes}" />
						</form:select>
						<form:errors path="properties['DailyExtractorFactory.reportable']" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DailyExtractTask" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.dailyexport"/></b>
			<table class="box">
				<c:if test="${debugging}">
					<tr>
						<td nowrap><spring:message code="ncd.pages.report.export.maxrows"/></td>
						<td>
							<c:set var="title"><spring:message code="ncd.pages.report.export.maxrows.title"/></c:set>
							<form:input path="properties['DailyExtractorFactory.maxRows']" title="${title}"/>
							<form:errors path="properties['DailyExtractorFactory.maxRows']" cssClass="error" />
						</td>
					</tr>
				</c:if>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.pathname"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.pathname.title"/></c:set>
						<form:input size="100" path="dailyExtractDestinationPathname" title="${title}"/>
						<form:errors path="dailyExtractDestinationPathname" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.extension"/><spring:message code="ncd.required"/></td>
					<td nowrap>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.extension.title"/></c:set>
						<form:input path="dailyExtractDestinationExtension" title="${title}"/>
						<form:errors path="dailyExtractDestinationExtension" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.add.timestamp"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.add.timestamp.title"/></c:set>
						<form:checkbox path="dailyExtractAddTimestamp" value="true" title="${title}"/>
						<form:errors path="dailyExtractAddTimestamp" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.output.timestamp.format"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.output.timestamp.format.title"/></c:set>
						<form:input size="30" path="dailyExtractTimestampFormat" title="${title}"/>
						<form:errors path="dailyExtractTimestampFormat" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.sender"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.sender.title"/></c:set>
						<form:input size="100" path="dailyExtractSender" title="${title}"/>
						<form:errors path="dailyExtractSender" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.success.recipients"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.success.recipients.title"/></c:set>
						<form:input size="100" path="dailyExtractSuccessAlertList" title="${title}"/>
						<form:errors path="dailyExtractSuccessAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.export.error.recipients"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.export.error.recipients.title"/></c:set>
						<form:input size="100" path="dailyExtractErrorAlertList" title="${title}"/>
						<form:errors path="dailyExtractErrorAlertList" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.title"/></c:set>
						<form:select path="dailyExtractSenderClass" onchange="updateVisibility()" title="${title}">
							<form:options items="${dataFeedSenders}" />
						</form:select>
						<form:errors path="dailyExtractSenderClass" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DataFeedSinkAccess" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.feedsink.access"/></b>
			<table class="box">
				<tr>
					<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.feedsink.access.table"/></td>
					<td width="<%=controlWidth%>">
						<c:set var="title"><spring:message code="ncd.pages.report.feedsink.access.table.title"/></c:set>
						<form:input size="30" path="properties['DataFeedSinkFactory.databaseTableName']" title="${title}"/>
						<form:errors path="properties['DataFeedSinkFactory.databaseTableName']" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsink.access.template.pathname"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsink.access.template.pathname.title"/></c:set>
						<form:input size="100" path="properties['DataFeedSinkFactory.databaseTemplatePathname']" title="${title}"/>
						<form:errors path="properties['DataFeedSinkFactory.databaseTemplatePathname']" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DataFeedSinkDelimited" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.feedsink.delimited"/></b>
			<table class="box">
				<tr>
					<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.feedsink.delimited.datetime.format"/></td>
					<td width="<%=controlWidth%>">
						<c:set var="title"><spring:message code="ncd.pages.report.feedsink.delimited.datetime.format.title"/></c:set>
						<form:input size="30" path="properties['DataFeedSinkFactory.dateTimeFormat']" title="${title}"/>
						<form:errors path="properties['DataFeedSinkFactory.dateTimeFormat']" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DataFeedSenderFTP" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.feedsender.ftp"/></b>
			<table class="box">
				<tr>
					<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.feedsender.ftp.host"/><spring:message code="ncd.required"/></td>
					<td width="<%=controlWidth%>">
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.ftp.host.title"/></c:set>
						<form:input size="30" path="ftpHost" title="${title}"/>
						<form:errors path="ftpHost" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender.ftp.port"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.ftp.port.title"/></c:set>
						<form:input path="ftpPort" title="${title}"/>
						<form:errors path="ftpPort" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender.ftp.username"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.ftp.username.title"/></c:set>
						<form:input path="ftpUsername" title="${title}"/>
						<form:errors path="ftpUsername" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender.ftp.password"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.ftp.password.title"/></c:set>
						<form:password path="ftpPassword" title="${title}" showPassword="true"/>
						<form:errors path="ftpPassword" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender.ftp.repeatpassword"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.ftp.repeatpassword.title"/></c:set>
						<form:password path="ftpPasswordRepeat" title="${title}" showPassword="true"/>
						<form:errors path="ftpPasswordRepeat" cssClass="error" />
					</td>
				</tr>
			</table>
		</div>
		<div id="DataFeedSenderSFTP" style="display: none;">
			<br/>
			<b class="boxHeader"><spring:message code="ncd.pages.report.section.feedsender.sftp"/></b>
			<table class="box">
				<tr>
					<td width="<%=labelWidth%>" nowrap><spring:message code="ncd.pages.report.feedsender.sftp.host"/><spring:message code="ncd.required"/></td>
					<td width="<%=controlWidth%>">
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.sftp.host.title"/></c:set>
						<form:input size="30" path="sftpHost" title="${title}"/>
						<form:errors path="sftpHost" cssClass="error" />
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender.sftp.port"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.sftp.port.title"/></c:set>
						<form:input path="sftpPort" title="${title}"/>
						<form:errors path="sftpPort" cssClass="error"/>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender.sftp.username"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.sftp.username.title"/></c:set>
						<form:input path="sftpUsername" title="${title}"/>
						<form:errors path="sftpUsername" cssClass="error"/>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender.sftp.password"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.sftp.password.title"/></c:set>
						<form:password path="sftpPassword" title="${title}" showPassword="true"/>
						<form:errors path="sftpPassword" cssClass="error"/>
					</td>
				</tr>
				<tr>
					<td nowrap><spring:message code="ncd.pages.report.feedsender.sftp.repeatpassword"/><spring:message code="ncd.required"/></td>
					<td>
						<c:set var="title"><spring:message code="ncd.pages.report.feedsender.sftp.repeatpassword.title"/></c:set>
						<form:password path="sftpPasswordRepeat" title="${title}" showPassword="true"/>
						<form:errors path="sftpPasswordRepeat" cssClass="error"/>
					</td>
				</tr>
			</table>
		</div>
	</div>
</form:form>
<script type="text/javascript">
updateVisibility();
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
