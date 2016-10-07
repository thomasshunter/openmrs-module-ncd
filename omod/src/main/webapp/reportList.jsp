<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />

<openmrs:require privilege="View NCD Scheduled Reports" otherwise="/login.htm" />

<script type="text/javascript">

function onDelete(f) {

	if (confirm("Are you sure you want to delete the selected report(s)? Click OK if you are sure you want to continue.")) {
		return true;
	}
	else {
		return false;
	}
}

</script>

<h2><spring:message code="ncd.pages.reportlist.linkname" /></h2>

<br/>

<form:form commandName="getNcdReportListObj"> 
	<b class="boxHeader"><spring:message code="ncd.pages.reportlist.list.title"/></b>
	<table>
	    <thead>
		    <tr>
		        <th>&nbsp;</th>
		        <th><spring:message code="ncd.pages.reportlist.list.name"/></th>
		        <th><spring:message code="ncd.pages.reportlist.list.type"/></th>
		        <th><spring:message code="ncd.pages.reportlist.list.status"/></th>
		    </tr>
	    </thead>
	    <tbody>
			<c:forEach var="summary" items="${getNcdReportListObj.reportSummaries}" varStatus="idx">
				<tr> 
					<td valign="top">
						<form:checkbox path="reportSummaries[${idx.index}].selected"/>
					</td>
					<td valign="top">
						<a href="report.form?reportId=${summary.id}">
							${summary.name}
						</a>&nbsp;&nbsp;
					</td>
					<td valign="top"><spring:message code="${summary.type}"/>&nbsp;&nbsp;</td>
					<td valign="top"><spring:message code="${summary.enabled}"/>&nbsp;&nbsp;</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<input type="submit" value="<spring:message code="ncd.pages.reportlist.list.buttons.selectall"/>" name="action">
	<input type="submit" value="<spring:message code="ncd.pages.reportlist.list.buttons.selectnone"/>" name="action">
	<openmrs:hasPrivilege privilege="Add NCD Scheduled Reports">
		<input type="button" value="<spring:message code="ncd.pages.reportlist.list.buttons.new"/>" onclick="gotoURL('report.form?new=1')">
		<input type="submit" value="<spring:message code="ncd.pages.reportlist.list.buttons.copy"/>" name="action">
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Delete NCD Scheduled Reports">
		<input type="submit" value="<spring:message code="ncd.pages.reportlist.list.buttons.delete"/>" name="action" onclick="return onDelete()">
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Edit NCD Scheduled Reports">
		<input type="submit" value="<spring:message code="ncd.pages.reportlist.list.buttons.disable"/>" name="action">
		<input type="submit" value="<spring:message code="ncd.pages.reportlist.list.buttons.enable"/>" name="action">
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View NCD Scheduled Reports">
		<input type="submit" value="<spring:message code="ncd.pages.reportlist.list.buttons.runnow"/>" name="action">
	</openmrs:hasPrivilege>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
