<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Critics" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.criticlist.linkname" /></h2>

<br/>

<b class="boxHeader"><spring:message code="ncd.pages.criticlist.title"/></b>
<div class="box">
	<form method="POST" action="conceptImport.form" enctype="multipart/form-data">
		<c:set var="title"><spring:message code="ncd.pages.criticlist.export.title"/></c:set>
		<input type="button" value="<spring:message code="ncd.pages.criticlist.export"/>" name="action" title="${title}" onclick="gotoURL('criticList.form?export=1')">
		<openmrs:hasPrivilege privilege="Add NCD Critics,Edit NCD Critics">
			<c:set var="title"><spring:message code="ncd.pages.criticlist.import.title"/></c:set>
			<input type="file" name="file"/>
			<input type="submit" value="<spring:message code="ncd.pages.criticlist.import"/>" name="action" title="${title}">
		</openmrs:hasPrivilege>
	</form>
	<form:form commandName="getNcdCriticListObj">
		<openmrs:hasPrivilege privilege="Delete NCD Critics">
			<c:set var="title"><spring:message code="ncd.pages.criticlist.delete.title"/></c:set>
			<input type="submit" value="<spring:message code="ncd.pages.criticlist.delete"/>" name="action" title="${title}">
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Add NCD Critics">
			<c:set var="title"><spring:message code="ncd.pages.criticlist.add.title"/></c:set>
			<input type="button" value="<spring:message code="ncd.pages.criticlist.add"/>" onclick="gotoURL('criticDetail.form?new=1')" title="${title}">
		</openmrs:hasPrivilege>
		<div class="scrollable" style="height: 450px">		
			<table cellspacing="0" class="scrollable">
			    <thead>
				    <tr>
				    	<th>&nbsp;</th>
				        <th style="width: 400px"><spring:message code="ncd.pages.criticlist.name"/></th>
				    </tr>
			    </thead>
			    <tbody style="height: 400px">
			    	<c:set var="atLeastOneRow" value="false" />
					<c:forEach var="row" items="${getNcdCriticListObj.concepts}">
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
							<td><form:checkbox path="selectedConcepts" value="${row.conceptName}"/></td>
							<td><a href="criticDetail.form?edit=${row.conceptName}">${row.conceptName}</a></td>
						</tr>
					</c:forEach>
					<c:if test="${atLeastOneRow == false}">
				    	<tr class="buttons" style="height: 12px">
							<td colspan="2"> </td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td> </td>
				    		<td colspan="2">
				    			<span class="error">
									<spring:message code="ncd.pages.decidedresultlist.nomatches"/>
								</span>
				    		</td>
				    	</tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</form:form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
