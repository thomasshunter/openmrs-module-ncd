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

<openmrs:require privilege="View NCD Decided Results" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.decidedresultlist.linkname" /></h2>

<br/>

<c:set var="batchConfirmMsg">
<spring:message code="ncd.pages.decidedresultlist.batch.confirm" />
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
		document.getElementById("getNcdDecidedResultListObj").submit();
	}
}
</script>

<form:form commandName="getNcdDecidedResultListObj">

	<%-- Standard filter section --%>
	<c:set var="filterSection" value="ncd.pages.decidedresultlist.filter.title" />
	<c:set var="filterTerms" value="${getNcdDecidedResultListObj.filter.terms}" />
	<c:set var="filterPath" value="filter.terms" />
	<c:set var="jspFilter" value="${getNcdDecidedResultListObj.filter}" />
	<c:set var="editFilter" value="${getNcdDecidedResultListObj.editFilter}" />
	<c:set var="editFilterDivHeight" value="530px" />
	<%@include file="listFilter.jsp" %>
	
	<br/>
	
	<div id="batchDialog" style="display:none">
		<input type="hidden" id="batchaction" name="batchaction" value=""/>
		<a name="batchDialogAnchor"></a>
		<b class="boxHeader"><spring:message code="ncd.pages.decidedresultlist.batch.title"/></b>
		<div class="box">
			<table>
				<tbody>
					<tr>
						<td colspan="2">
							<spring:message code="ncd.pages.decidedresultlist.batch.desc"/>
						</td>
					</tr>
					<openmrs:hasPrivilege privilege="Edit NCD Decided Results">
						<tr>
							<td>
								<spring:message code="ncd.pages.decidedresultlist.batch.reportable"/>
							</td>
							<td>
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.batch.reportable.title"/></c:set>
								<form:select path="batchReportableOp" title="${title}">
									<form:options items="${reportableOps}" />
								</form:select>
							</td>
						</tr>
						<tr>
							<td>
								<spring:message code="ncd.pages.decidedresultlist.batch.condition"/>
							</td>
							<td>
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.batch.condition.title"/></c:set>
								<form:select path="batchConditionOp" title="${title}">
									<form:options items="${conditionOps}" />
								</form:select>
							</td>
						</tr>
					</openmrs:hasPrivilege>
					<openmrs:hasPrivilege privilege="Delete NCD Decided Results">
						<tr>
							<td>
								<spring:message code="ncd.pages.decidedresultlist.batch.remove"/>
							</td>
							<td>
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.batch.remove.title"/></c:set>
								<form:checkbox path="batchRemoveOp" title="${title}" />
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

	<b class="boxHeader"><spring:message code="ncd.pages.decidedresultlist.results.title"/></b>
	<div class="box">
		<input type="submit" value="<spring:message code="ncd.buttons.refresh"/>" name="listaction"/>
		<c:if test="${getNcdDecidedResultListObj.previousPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.firstpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.prevpage"/>" name="listaction"/>
		</c:if>
  			<c:if test="${getNcdDecidedResultListObj.nextPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.nextpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.lastpage"/>" name="listaction"/>
		</c:if>
		<input type="submit" value="<spring:message code="ncd.buttons.selectall"/>" name="listaction"/>
		<input type="submit" value="<spring:message code="ncd.buttons.selectnone"/>" name="listaction"/>
		<c:set var="title"><spring:message code="ncd.buttons.editselected.title"/></c:set>
		<input type="button" value="<spring:message code="ncd.buttons.editselected"/>" onclick="showBatchDialog()" title="${title}"/>
		<openmrs:hasPrivilege privilege="Add NCD Decided Results">
			<input type="button" value="<spring:message code="ncd.pages.decidedresultlist.results.add"/>" onclick="gotoURL('decidedResultDetail.form?new=1')"/>
		</openmrs:hasPrivilege>
		<div class="scrollable" style="height: 380px">		
			<table cellspacing="0" class="scrollable list">
			    <thead>
			    	<ncd:sortcolumns formname="forms[0]" sortkey="${getNcdDecidedResultListObj.filter.sortKeyLabel}" 
			    		direction="${getNcdDecidedResultListObj.filter.sortAscending}" resourcepath="${pageContext.request.contextPath}/moduleResources/ncd/">
					    <tr class="evenRow">
					    	<th nowrap="nowrap"> </th>
					        <th nowrap="nowrap">
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.list.dateclassified.title"/></c:set>
								<ncd:sortcolumn sortkey="DATECLASSIFIED" cssclass="sortcolumnheader" title="${title}">
					        		<spring:message code="ncd.pages.decidedresultlist.dateclassified"/>
					        	</ncd:sortcolumn>
					        </th>
					        <th nowrap="nowrap">
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.list.classifiedbywhom.title"/></c:set>
								<ncd:sortcolumn sortkey="CLASSIFIEDBY" cssclass="sortcolumnheader" title="${title}">
					        		<spring:message code="ncd.pages.decidedresultlist.classifiedbywhom"/>
					        	</ncd:sortcolumn>
				        	</th>
					        <th nowrap="nowrap">
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.list.reportable.title"/></c:set>
								<ncd:sortcolumn sortkey="REPORT" cssclass="sortcolumnheader" title="${title}">
					    	    	<spring:message code="ncd.pages.decidedresultlist.reportable"/>
					        	</ncd:sortcolumn>
				        	</th>
					        <th nowrap="nowrap">
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.list.loinccode.title"/></c:set>
								<ncd:sortcolumn sortkey="LOINCCODE" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.decidedresultlist.loinccode"/>
					        	</ncd:sortcolumn>
				        	</th>
					        <th nowrap="nowrap">
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.list.code.title"/></c:set>
								<ncd:sortcolumn sortkey="CODE" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.decidedresultlist.code"/>
					        	</ncd:sortcolumn>
				        	</th>
					        <th nowrap="nowrap">
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.list.codesys.title"/></c:set>
								<ncd:sortcolumn sortkey="CODESYSTEM" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.decidedresultlist.codesys"/>
					        	</ncd:sortcolumn>
				        	</th>
					        <th nowrap="nowrap">
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.list.codetext.title"/></c:set>
								<ncd:sortcolumn sortkey="CODETEXT" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.decidedresultlist.codetext"/>
					        	</ncd:sortcolumn>
				        	</th>
					        <th nowrap="nowrap">
								<c:set var="title"><spring:message code="ncd.pages.decidedresultlist.list.resultvalue.title"/></c:set>
								<ncd:sortcolumn sortkey="RESULTVALUE" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.decidedresultlist.resultvalue"/>
					        	</ncd:sortcolumn>
				        	</th>
					    </tr>
					</ncd:sortcolumns>
			    </thead>
			    <tbody style="height: 330px">
			    	<c:set var="atLeastOneRow" value="false" />
					<c:forEach var="row" varStatus="idx" items="${getNcdDecidedResultListObj.visibleRows}">
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
							<td>
								<form:checkbox path="selectedResults" value="${row.id}"/>
							</td>
							<td>
								<a href="decidedResultDetail.form?edit=${row.id}">
									<ncd:write value="${row.dateClassified}" type="date" />
								</a>
							</td>
							<td>${row.classifiedByWhom}</td>
							<td>${row.reportable}</td>
							<td>${row.loincCode}</td>
							<td>${row.obr}</td>
							<td>${row.obrCodeSystem}</td>
							<td>${row.obrText}</td>
							<td>${row.resultValue}</td>
						</tr>
						<tr class="${rowClass}" style="height: 0px" >
							<td> </td>
							<td> </td>
							<td> </td>
							<td> </td>
							<td> </td>
							<td>${row.obx}</td>
							<td>${row.obxCodeSystem}</td>
							<td>${row.obxText}</td>
							<td> </td>
						</tr>
					</c:forEach>
					<c:if test="${atLeastOneRow == false}">
				    	<tr class="buttons" style="height: 12px">
							<td colspan="13"> </td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td> </td>
				    		<td colspan="12">
				    			<span class="error">
									<spring:message code="ncd.pages.decidedresultlist.nomatches"/>
								</span>
				    		</td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td colspan="13"> </td>
				    	</tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
