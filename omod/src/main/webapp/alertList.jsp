<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Alerts" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.alertList.linkname" /></h2>

<br/>

<c:set var="batchConfirmMsg">
<spring:message code="ncd.pages.alertlist.batch.confirm" />
</c:set>

<script type="text/javascript">

function showBatchDialog() {
	show("batchDialog");
	gotoURL("#batchDialogAnchor");
}

function hideBatchDialog() {
	hide("batchDialog");
}

function executeBatch() {

	// are you sure
	var clickedOk = window.confirm("${batchConfirmMsg}");

	if (clickedOk == true) {
	
		// post batchaction
		document.getElementById("batchaction").value="apply";
		document.getElementById("getNcdAlertListObj").submit();
	}
}
</script>

<form:form commandName="getNcdAlertListObj">

	<%-- Standard filter section --%>
	<c:set var="filterSection" value="ncd.pages.alertlist.filter.title" />
	<c:set var="filterTerms" value="${getNcdAlertListObj.filter.terms}" />
	<c:set var="filterPath" value="filter.terms" />
	<c:set var="jspFilter" value="${getNcdAlertListObj.filter}" />
	<c:set var="editFilter" value="${getNcdAlertListObj.editFilter}" />
	<c:set var="editFilterDivHeight" value="175px" />
	<%@include file="listFilter.jsp" %>

	<br/>
	
	<div id="batchDialog" style="display:none">
		<input type="hidden" id="batchaction" name="batchaction" value=""/>
		<a name="batchDialogAnchor"></a>
		<b class="boxHeader"><spring:message code="ncd.pages.alertlist.batch.title"/></b>
		<div class="box">
			<table>
				<tbody>
					<tr>
						<td colspan="2">
							<spring:message code="ncd.pages.alertlist.batch.desc"/>
						</td>
					</tr>
					<openmrs:hasPrivilege privilege="Edit NCD Alerts">
						<tr>
							<td>
								<spring:message code="ncd.pages.alertlist.batch.dismissed"/>
							</td>
							<td>
								<c:set var="title"><spring:message code="ncd.pages.alertlist.batch.dismissed.title"/></c:set>
								<form:select path="batchDismissedOp" title="${title}">
									<form:options items="${dismissedOps}" />
								</form:select>
							</td>
						</tr>
						<tr>
							<td>
								<spring:message code="ncd.pages.alertlist.batch.dismissed.reason"/>
							</td>
							<td>
								<c:set var="title"><spring:message code="ncd.pages.alertlist.batch.dismissed.reason.title"/></c:set>
								<form:input size="80" path="batchReason" title="${title}"/>
							</td>
						</tr>
					</openmrs:hasPrivilege>
					<tr class="buttons">
						<td colspan="2">
							<input type="button" value="<spring:message code="ncd.buttons.apply"/>" onclick="executeBatch()" />
							<input type="button" value="Cancel" onclick="hideBatchDialog()" />
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		<br/>
	</div>

	<%-- Alert list section --%>
	<b class="boxHeader"><spring:message code="ncd.pages.alertlist.results.title"/></b>
	<div class="box">
		<input type="submit" value="<spring:message code="ncd.buttons.refresh"/>" name="listaction"/>
		<c:if test="${getNcdAlertListObj.previousPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.firstpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.prevpage"/>" name="listaction"/>
		</c:if>
  			<c:if test="${getNcdAlertListObj.nextPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.nextpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.lastpage"/>" name="listaction"/>
		</c:if>
		<input type="submit" value="<spring:message code="ncd.buttons.selectall"/>" name="listaction"/>
		<input type="submit" value="<spring:message code="ncd.buttons.selectnone"/>" name="listaction"/>
		<c:set var="title"><spring:message code="ncd.buttons.editselected.title"/></c:set>
		<input type="button" value="<spring:message code="ncd.buttons.editselected"/>" onclick="showBatchDialog()" title="${title}"/>
		<div class="scrollable" style="height: 380px">		
			<table cellspacing="0" class="scrollable list">
			    <thead>
			    	<ncd:sortcolumns formname="forms[0]" sortkey="${getNcdAlertListObj.filter.sortKeyLabel}" 
			    		direction="${getNcdAlertListObj.filter.sortAscending}" resourcepath="${pageContext.request.contextPath}/moduleResources/ncd/">
					    <tr class="evenRow">
					    	<th> </th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.alertlist.message.title"/></c:set>
								<ncd:sortcolumn sortkey="MESSAGE" cssclass="sortcolumnheader" title="${title}">
					        		<spring:message code="ncd.pages.alertlist.message"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.alertlist.type.title"/></c:set>
								<ncd:sortcolumn sortkey="ALERTTYPE" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.alertlist.type"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.alertlist.count.title"/></c:set>
								<ncd:sortcolumn sortkey="OCCURRENCES" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.alertlist.count"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.alertlist.firsttime.title"/></c:set>
								<ncd:sortcolumn sortkey="FIRSTOCCURRED" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.alertlist.firsttime"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.alertlist.lasttime.title"/></c:set>
								<ncd:sortcolumn sortkey="LASTOCCURRED" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.alertlist.lasttime"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.alertlist.dismisseddate.title"/></c:set>
								<ncd:sortcolumn sortkey="DISMISSEDDATE" cssclass="sortcolumnheader" title="${title}">
					        		<spring:message code="ncd.pages.alertlist.dismisseddate"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.alertlist.dismisseduser.title"/></c:set>
								<ncd:sortcolumn sortkey="DISMISSEDUSER" cssclass="sortcolumnheader" title="${title}">
					        		<spring:message code="ncd.pages.alertlist.dismisseduser"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.alertlist.dismissedreason.title"/></c:set>
								<ncd:sortcolumn sortkey="DISMISSEDREASON" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.alertlist.dismissedreason"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">&nbsp;&nbsp;&nbsp;</th>
				    	</tr>
				    </ncd:sortcolumns>
			    </thead>
			    <tbody style="height: 350px">
			    	<c:set var="atLeastOneRow" value="false" />
					<c:forEach var="row" varStatus="idx" items="${getNcdAlertListObj.visibleRows}">
				    	<c:set var="atLeastOneRow" value="true" />
						<c:choose>
							<c:when test="${(idx.count % 2) == 0}">
								<c:set var="rowClass" value="evenRow" /> 
							</c:when>
							<c:otherwise>
								<c:set var="rowClass" value="oddRow" /> 
							</c:otherwise>
						</c:choose>
						<tr class="${rowClass}" style="height: 0px"> 
							<td><form:checkbox path="selectedResults" value="${row.id}"/>&nbsp;&nbsp;</td>
							<td>${row.summary}&nbsp;&nbsp;</td>
							<td>${row.alertType.alertType}&nbsp;&nbsp;</td>
							<td>${row.occurrences}&nbsp;&nbsp;</td>
							<td><ncd:write value="${row.firstDate}" type="datetime" />&nbsp;&nbsp;</td>
							<td><ncd:write value="${row.lastDate}" type="datetime" />&nbsp;&nbsp;</td>
							<c:choose>
								<c:when test="${row.dismissedDate != null}">
									<td><ncd:write value="${row.dismissedDate}" type="datetime" />&nbsp;&nbsp;</td>
								</c:when>
								<c:otherwise>
									<td>&nbsp;</td>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${row.dismissedUser != null}">
									<td>${row.displayDismissedUserName}&nbsp;&nbsp;</td>
								</c:when>
								<c:otherwise>
									<td>&nbsp;</td>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${row.dismissedReason != null}">
									<td>${row.dismissedReason}&nbsp;&nbsp;</td>
								</c:when>
								<c:otherwise>
									<td>&nbsp;</td>
								</c:otherwise>
							</c:choose>
				      	    <td align="center">&nbsp;&nbsp;&nbsp;</td>
						</tr>
					</c:forEach>
					<c:if test="${atLeastOneRow == false}">
				    	<tr class="buttons" style="height: 12px">
							<td colspan="10"> </td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td> </td>
				    		<td colspan="9">
				    			<span class="error">
									<spring:message code="ncd.pages.alertlist.nomatches"/>
								</span>
				    		</td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td colspan="10"> </td>
				    	</tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
