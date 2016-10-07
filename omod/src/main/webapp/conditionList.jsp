<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Conditions" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.conditionlist.linkname" /></h2>

<script type="text/javascript">

function onDeleteConditions(f) {

	if (confirm("Deleting a condition will also delete any related CodeConditions, and cannot be undone.  Click OK if you are sure you want to continue.")) {
		return true;
	}
	else {
		return false;
	}
}

function onDeleteGroups(f) {

	if (confirm("Deleting a condition group will also delete any related conditions and their related CodeConditions, and cannot be undone.  Click OK if you are sure you want to continue.")) {
		return true;
	}
	else {
		return false;
	}
}

</script>

<br/>

<form method="post">
	<table>
		<tbody>
			<tr>
				<td width="50%" valign="top">
					<b class="boxHeader"><spring:message code="ncd.pages.conditionList.conditions.title"/></b>
					<div class="box">
						<div class="scrollable" style="height: 600px">		
							<table cellspacing="0" class="scrollable">
							    <thead>
									<tr> 
										<td>&nbsp;</td>
										<td nowrap colspan="2">
											<%--
											<openmrs:hasPrivilege privilege="Delete NCD Conditions">
												<input type="submit" value="<spring:message code="ncd.buttons.remove"/>" name="condaction" onclick="return onDeleteConditions()">
											</openmrs:hasPrivilege>
											--%>
											<openmrs:hasPrivilege privilege="Add NCD Conditions">
												<input type="button" value="<spring:message code="ncd.pages.conditionList.condition.add"/>" onclick='gotoURL("conditionDetail.form?new=1")'>
											</openmrs:hasPrivilege>
										</td>
									</tr>
									<tr>
										<td colspan="3">
											&nbsp;
										</td>
									</tr>
								    <tr>
								    	<th>&nbsp;</th>
								        <th><spring:message code="ncd.pages.conditionList.condname"/></th>
								        <th><spring:message code="ncd.pages.conditionList.retired"/></th>
								    </tr>
							    </thead>
							    <tbody style="height: 530px">
									<c:forEach var="row" varStatus="idx" items="${getNcdConditionListObj.conditions}">
										<c:choose>
											<c:when test="${(idx.count % 2) == 0}">
												<c:set var="rowClass" value="evenRow" /> 
											</c:when>
											<c:otherwise>
												<c:set var="rowClass" value="oddRow" /> 
											</c:otherwise>
										</c:choose>
										<c:choose>
											<c:when test="${row.retired==true}">
												<c:set var="retired" value="Y"/>
											</c:when>
											<c:otherwise>
												<c:set var="retired" value="N"/>
											</c:otherwise>
										</c:choose>
										<tr class="${rowClass}" style="height: 0px"> 
											<td><input type="checkbox" name="selectedConditions" value="${row.id}"></td>
											<td><a href="conditionDetail.form?edit=${row.id}">${row.displayText}</a></td>
											<td align="center">${retired}</td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</div>
					</div>
				</td>
				<td width="50%" valign="top">
					<b class="boxHeader"><spring:message code="ncd.pages.conditionList.groups.title"/></b>
					<div class="box">
						<div class="scrollable" style="height: 600px">		
							<table cellspacing="0" class="scrollable">
							    <thead>
									<tr> 
										<td>&nbsp;</td>
										<td nowrap colspan="2">
											<%--
											<openmrs:hasPrivilege privilege="Delete NCD Conditions">
												<input type="submit" value="<spring:message code="ncd.buttons.remove"/>" name="groupaction" onclick="return onDeleteGroups()">
											</openmrs:hasPrivilege>
											--%>
											<openmrs:hasPrivilege privilege="Add NCD Conditions">
												<input type="button" value="<spring:message code="ncd.pages.conditionList.group.add"/>" onclick='gotoURL("conditionGroupDetail.form?new=1")'>
											</openmrs:hasPrivilege>
										</td>
									</tr>
									<tr>
										<td colspan="3">
											&nbsp;
										</td>
									</tr>
								    <tr>
								    	<th>&nbsp;</th>
								        <th><spring:message code="ncd.pages.conditionList.groupname"/></th>
								        <th><spring:message code="ncd.pages.conditionList.retired"/></th>
								    </tr>
							    </thead>
							    <tbody style="height: 530px">
									<c:forEach var="row" varStatus="idx" items="${getNcdConditionListObj.groups}">
										<c:choose>
											<c:when test="${(idx.count % 2) == 0}">
												<c:set var="rowClass" value="evenRow" /> 
											</c:when>
											<c:otherwise>
												<c:set var="rowClass" value="oddRow" /> 
											</c:otherwise>
										</c:choose>
										<c:choose>
											<c:when test="${row.retired==true}">
												<c:set var="retired" value="Y"/>
											</c:when>
											<c:otherwise>
												<c:set var="retired" value="N"/>
											</c:otherwise>
										</c:choose>
										<tr class="${rowClass}" style="height: 0px"> 
											<td><input type="checkbox" name="selectedGroups" value="${row.id}"></td>
											<td><a href="conditionGroupDetail.form?edit=${row.id}">${row.displayText}</a></td>
											<td align="center">${retired}</td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</div>
					</div>
				</td>
			</tr>
		</tbody>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
