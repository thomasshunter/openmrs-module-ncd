<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Institutions" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.institutionList.linkname" /></h2>

<br/>

<form:form commandName="getNcdInstitutionListObj">

	<b class="boxHeader"><spring:message code="ncd.pages.institutionList.box.title"/></b>
	<div class="box">
		<input type="button" value="<spring:message code="ncd.pages.institutionList.add"/>" name="action" onclick="gotoURL('institutionDetail.form?new=1')">
		<br/>
		<div class="scrollable" style="height: 590px">		
			<table cellspacing="0" class="scrollable list">
			    <thead>
				    <tr>
				        <th><spring:message code="ncd.pages.institutionList.headers.name"/></th>
				        <th><spring:message code="ncd.pages.institutionList.headers.description"/></th>
				    </tr>
			    </thead>
			    <tbody style="height: 560px">
					<c:forEach var="row" varStatus="idx" items="${getNcdInstitutionListObj.institutions}">
						<c:choose>
							<c:when test="${(idx.count % 2) == 0}">
								<c:set var="rowClass" value="evenRow" /> 
							</c:when>
							<c:otherwise>
								<c:set var="rowClass" value="oddRow" /> 
							</c:otherwise>
						</c:choose>
						<c:if test="${row.retired}">
							<c:set var="rowClass" value="${rowClass} retired" /> 
						</c:if>
						<tr class="${rowClass}" style="height: 0px"> 
							<td>
								<a href="institutionDetail.form?edit=${row.id}">
									${row.name}
								</a>
							</td>
							<td>
								${row.description}
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
