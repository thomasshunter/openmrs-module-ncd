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

<openmrs:require privilege="View NCD Reportable Results" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.reportableresultlist.linkname" /></h2>

<br/>

<c:set var="batchConfirmMsg">
<spring:message code="ncd.pages.reportableresultlist.batch.confirm" />
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
		document.getElementById("getNcdReportableResultListObj").submit();
	}
}
</script>

<form:form commandName="getNcdReportableResultListObj">

	<%-- Standard filter section --%>
	<c:set var="filterSection" value="ncd.pages.reportableresultlist.filter.title" />
	<c:set var="filterTerms" value="${getNcdReportableResultListObj.filter.terms}" />
	<c:set var="filterPath" value="filter.terms" />
	<c:set var="jspFilter" value="${getNcdReportableResultListObj.filter}" />
	<c:set var="editFilter" value="${getNcdReportableResultListObj.editFilter}" />
	<c:set var="editFilterDivHeight" value="525px" />
	<%@include file="listFilter.jsp" %>

	<br/>
	
	<div id="batchDialog" style="display:none">
		<input type="hidden" id="batchaction" name="batchaction" value=""/>
		<a name="batchDialogAnchor"></a>
		<b class="boxHeader"><spring:message code="ncd.pages.reportableresultlist.batch.title"/></b>
		<div class="box">
			<table>
				<tbody>
					<tr>
						<td colspan="2">
							<spring:message code="ncd.pages.reportableresultlist.batch.desc"/>
						</td>
					</tr>
					<openmrs:hasPrivilege privilege="Edit NCD Reportable Results">
						<tr>
							<td>
								<spring:message code="ncd.pages.reportableresultlist.batch.sentinerror"/>
							</td>
							<td>
								<c:set var="title"><spring:message code="ncd.pages.reportableresultlist.batch.sentinerror.title"/></c:set>
								<form:select path="batchSentInErrorOp" title="${title}">
									<form:options items="${sentInErrorOps}" />
								</form:select>
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

	<%-- Result list section --%>
	<b class="boxHeader"><spring:message code="ncd.pages.reportableresultlist.results.title"/></b>
	<div class="box">
		<input type="submit" value="<spring:message code="ncd.buttons.refresh"/>" name="listaction"/>
		<c:if test="${getNcdReportableResultListObj.previousPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.firstpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.prevpage"/>" name="listaction"/>
		</c:if>
  			<c:if test="${getNcdReportableResultListObj.nextPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.nextpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.lastpage"/>" name="listaction"/>
		</c:if>
		<input type="submit" value="<spring:message code="ncd.buttons.selectall"/>" name="listaction"/>
		<input type="submit" value="<spring:message code="ncd.buttons.selectnone"/>" name="listaction"/>
		<c:set var="title"><spring:message code="ncd.buttons.editselected.title"/></c:set>
		<input type="button" value="<spring:message code="ncd.buttons.editselected"/>" onclick="showBatchDialog()" title="${title}"/>
		<input type="button" value="<spring:message code="ncd.buttons.export"/>" onclick="gotoURL('reportableResultList.form?export=1')"/>
		<div class="scrollable" style="height: 380px">		
			<table cellspacing="0" class="scrollable list">
			    <thead>
			    	<ncd:sortcolumns formname="forms[0]" sortkey="${getNcdReportableResultListObj.filter.sortKeyLabel}" 
			    		direction="${getNcdReportableResultListObj.filter.sortAscending}" resourcepath="${pageContext.request.contextPath}/moduleResources/ncd/">
					    <tr class="evenRow">
					    	<th> </th>
					        <th align="center" valign="bottom">
								<c:set var="title"><spring:message code="ncd.pages.reportableresultlist.messagereceiveddatetime.title"/></c:set>
								<ncd:sortcolumn sortkey="DATETIMERCVD" cssclass="sortcolumnheader" title="${title}">
					        		<spring:message code="ncd.pages.reportableresultlist.messagereceiveddatetime"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center" valign="bottom">
								<c:set var="title"><spring:message code="ncd.pages.reportableresultlist.mpqseqnumber.title"/></c:set>
					        	<ncd:sortcolumn sortkey="MPQSEQNUMBER" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.reportableresultlist.mpqseqnumber"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center" valign="bottom">
								<c:set var="title"><spring:message code="ncd.pages.reportableresultlist.loinc.title"/></c:set>
					        	<ncd:sortcolumn sortkey="CODE" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.reportableresultlist.loinc"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center" valign="bottom">
								<c:set var="title"><spring:message code="ncd.pages.reportableresultlist.conditionname.title"/></c:set>
					        	<ncd:sortcolumn sortkey="CONDITION" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.reportableresultlist.conditionname"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center" valign="bottom">
								<c:set var="title"><spring:message code="ncd.pages.reportableresultlist.critic.title"/></c:set>
					        	<ncd:sortcolumn sortkey="CRITIC" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.reportableresultlist.critic"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center" valign="bottom">&nbsp;&nbsp;&nbsp;</th>
					    </tr>
				    </ncd:sortcolumns>
			    </thead>
			    <tbody style="height: 350px">
			    	<c:set var="atLeastOneRow" value="false" />
					<c:forEach var="row" varStatus="idx" items="${getNcdReportableResultListObj.visibleRows}">
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
							<td><form:checkbox path="selectedResults" value="${row.id}"/></td>
							<td><a href="reportableResultDetail.form?edit=${row.id}&shownav=1"><ncd:write value="${row.messageReceivedDateTime}" type="datetime"/></a></td>
							<td>${row.mpqSeqNumber}</td>
							<td>${row.codeDisplay}</td>
							<td>${row.conditionName}</td>
							<td>${row.criticDisplay}</td>
				      	  <td align="center">&nbsp;&nbsp;&nbsp;</td>
						</tr>
					</c:forEach>
					<c:if test="${atLeastOneRow == false}">
				    	<tr class="buttons" style="height: 12px">
							<td colspan="6"> </td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td> </td>
				    		<td colspan="5">
				    			<span class="error">
									<spring:message code="ncd.pages.reportedresultlist.nomatches"/>
								</span>
				    		</td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td colspan="6"> </td>
				    	</tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
