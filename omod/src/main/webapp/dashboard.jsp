<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />

<openmrs:require privilege="View NCD Dashboard" otherwise="/login.htm" />

<style type="text/css">
a.unmarked {
	border-style: none;
	font-style: normal;
	text-decoration: none;
}
</style>

<script type="text/javascript">

function onDismiss(f) {

	if (f['dismissReason'].value == null || f['dismissReason'].value.length==0) { 
		if (confirm("Are you sure you want to dismiss the selected alert(s) without entering a reason? Click OK if you are sure you want to continue.")) {
			return true;
		}
		else {
			return false;
		}
	}
	else {
		return true;
	}
}

function onStackTrace(stackTrace) {
	alert(stackTrace);
	return false;
}

function onResetCounts(f) {

	if (confirm("Are you sure you want to reset the messages / segments processed counts?  This will permanently delete historical message counts from the database, and it cannot be undone.  Click OK if you are sure you want to continue.")) {
		return true;
	}
	else {
		return false;
	}
}

</script>

<meta http-equiv="refresh" content="60" > 
<h2><spring:message code="ncd.pages.dashboard.linkname" /></h2>

<br/>

<form:form commandName="getNcdDashboardObj">
	<openmrs:hasPrivilege privilege="View NCD Alerts">
		<b class="boxHeader"><spring:message code="ncd.pages.dashboard.alerts.title"/></b>
			<table class="box">
			    <thead>
				    <tr>
				    	<th>&nbsp;</th>
				        <th width="45%"><spring:message code="ncd.pages.dashboard.alerts.message"/></th>
				        <th><spring:message code="ncd.pages.dashboard.alerts.type"/></th>
				        <th><spring:message code="ncd.pages.dashboard.alerts.count"/></th>
				        <th><spring:message code="ncd.pages.dashboard.alerts.firsttime"/></th>
				        <th><spring:message code="ncd.pages.dashboard.alerts.lasttime"/></th>
				    </tr>
			    </thead>
			    <tbody>
					<c:choose>
						<c:when test="${(getNcdDashboardObj.alertSummariesSize) == 0}">
							<tr>
								<td colspan="6">
									No alerts at this time.
								</td>
							</tr>
						</c:when>
						<c:otherwise>
							<c:forEach var="row" varStatus="idx" items="${getNcdDashboardObj.visibleRows}">
								<tr> 
									<td><form:checkbox path="selectedResults" value="${row.id}"/>&nbsp;&nbsp;</td>
									<td>${row.summary}&nbsp;&nbsp;</td>
									<td>${row.alertType.alertType}&nbsp;&nbsp;</td>
									<td>${row.occurrences}&nbsp;&nbsp;</td>
									<td><ncd:write value="${row.firstDate}" type="datetime" />&nbsp;&nbsp;</td>
									<td><ncd:write value="${row.lastDate}" type="datetime" />&nbsp;&nbsp;</td>
								</tr>
							</c:forEach>
						</c:otherwise>
					</c:choose>
					<tr>
						<td colspan=6">
							<input type="submit" value="<spring:message code="ncd.buttons.refresh"/>" name="action">
							<input type="button" value="<spring:message code="ncd.pages.dashboard.alerts.history"/>" onclick="gotoURL('alertList.form')">
							<openmrs:hasPrivilege privilege="Edit NCD Alerts">
								<input type="submit" value="<spring:message code="ncd.pages.dashboard.alerts.dismiss"/>" name="action"  onclick="return onDismiss(document.forms[0])">
								Reason: 
								<form:input size="80" path="dismissReason"/>
							</openmrs:hasPrivilege>
						</td>
					</tr>
				</tbody>
			</table>
		
		<br/>
	</openmrs:hasPrivilege>

	<b class="boxHeader"><spring:message code="ncd.pages.dashboard.errors.title"/></b>
	<table class="box">
	    <thead>
		    <tr>
		        <th><spring:message code="ncd.pages.dashboard.errors.level"/></th>
		        <th><spring:message code="ncd.pages.dashboard.errors.when"/></th>
		        <th><spring:message code="ncd.pages.dashboard.errors.stack.trace"/></th>
		        <th width="75%"><spring:message code="ncd.pages.dashboard.errors.message"/></th>
		    </tr>
	    </thead>
	    <tbody>
				<c:choose>
					<c:when test="${(getNcdDashboardObj.systemActivityEventsSize) == 0}">
						<tr>
							<td colspan="4">
								No system activity to report at this time.
							</td>
						</tr>
					</c:when>
					<c:otherwise>
						<c:forEach var="row" items="${getNcdDashboardObj.systemActivityEvents}">
							<tr> 
								<td><spring:message code="${row.level}"/>&nbsp;&nbsp;</td>
								<td><ncd:write value="${row.occurred}" type="datetime" />&nbsp;&nbsp;</td>
								<td>
									<c:choose>
										<c:when test="${row.formattedStackTrace != null}">
											<input type="submit" value="<spring:message code="ncd.pages.dashboard.errors.stack.trace"/>" name="action"  onclick="return onStackTrace('${row.formattedStackTrace}')">
										</c:when>
										<c:otherwise>
											Not available
										</c:otherwise>
									</c:choose>
								</td>
								<td>${row.summary}</td>
							</tr>
						</c:forEach>
					</c:otherwise>
				</c:choose>
		</tbody>
	</table>
	
	<br/>

	<openmrs:hasPrivilege privilege="View NCD Message Counts">
		<b class="boxHeader"><spring:message code="ncd.pages.dashboard.messages.title"/></b>
		<table class="box">
		    <thead>
		        <tr>
		            <td align="right">&nbsp;</td>
					<c:forEach var="col" items="${getNcdDashboardObj.messageCountSummaries}">
						<td align="right">
							<c:choose>
								<c:when test="${col.processedDate != null}">
									<b><ncd:write value="${col.processedDate}" type="date" /></b>
								</c:when>
								<c:otherwise>
									<b><spring:message code="ncd.pages.dashboard.messages.all.dates"/></b>
								</c:otherwise>
							</c:choose>
						</td>
					</c:forEach>
		        </tr>
		    </thead>
		    <tbody>
				<tr> 
					<td>
						<c:set var="title"><spring:message code="ncd.pages.dashboard.messages.potentially.reportable.title"/></c:set>
						<a title="${title}" class="unmarked">
							<spring:message code="ncd.pages.dashboard.messages.potentially.reportable"/>
			        	</a>
					</td>
					<c:forEach var="col" items="${getNcdDashboardObj.messageCountSummaries}">
						<td align="right">
							${col.potentiallyReportable}
						</td>
					</c:forEach>
				</tr>
				<tr> 
					<td>
						<c:set var="title"><spring:message code="ncd.pages.dashboard.messages.decided.results.positive.title"/></c:set>
						<a title="${title}" class="unmarked">
							<spring:message code="ncd.pages.dashboard.messages.decided.results.positive"/>
			        	</a>
					</td>
					<c:forEach var="col" items="${getNcdDashboardObj.messageCountSummaries}">
						<td align="right">
							${col.decidedResultPositive}
						</td>
					</c:forEach>
				</tr>
				<tr> 
					<td>
						<c:set var="title"><spring:message code="ncd.pages.dashboard.messages.decided.results.negative.title"/></c:set>
						<a title="${title}" class="unmarked">
							<spring:message code="ncd.pages.dashboard.messages.decided.results.negative"/>
			        	</a>
					</td>
					<c:forEach var="col" items="${getNcdDashboardObj.messageCountSummaries}">
						<td align="right">
							${col.decidedResultNegative}
						</td>
					</c:forEach>
				</tr>
				<tr> 
					<td>
						<c:set var="title"><spring:message code="ncd.pages.dashboard.messages.critic.positive.title"/></c:set>
						<a title="${title}" class="unmarked">
							<spring:message code="ncd.pages.dashboard.messages.critic.positive"/>
			        	</a>
					</td>
					<c:forEach var="col" items="${getNcdDashboardObj.messageCountSummaries}">
						<td align="right">
							${col.criticPositive}
						</td>
					</c:forEach>
				</tr>
				<tr> 
					<td>
						<c:set var="title"><spring:message code="ncd.pages.dashboard.messages.critic.negative.title"/></c:set>
						<a title="${title}" class="unmarked">
							<spring:message code="ncd.pages.dashboard.messages.critic.negative"/>
			        	</a>
					</td>
					<c:forEach var="col" items="${getNcdDashboardObj.messageCountSummaries}">
						<td align="right">
							${col.criticNegative}
						</td>
					</c:forEach>
				</tr>
				<tr> 
					<td>
						<c:set var="title"><spring:message code="ncd.pages.dashboard.messages.indeterminate.positive.title"/></c:set>
						<a title="${title}" class="unmarked">
							<spring:message code="ncd.pages.dashboard.messages.indeterminate.positive"/>
			        	</a>
					</td>
					<c:forEach var="col" items="${getNcdDashboardObj.messageCountSummaries}">
						<td align="right">
							${col.indeterminate}
						</td>
					</c:forEach>
				</tr>
				<openmrs:hasPrivilege privilege="Reset NCD Message Counts">
					<tr>
						<td>
							<input type="submit" value="<spring:message code="ncd.pages.dashboard.messages.reset.counts"/>" name="action" onclick="return onResetCounts()">
						</td>
					</tr>
				</openmrs:hasPrivilege>
			</tbody>
		</table>
		
		<br/>
	</openmrs:hasPrivilege>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
