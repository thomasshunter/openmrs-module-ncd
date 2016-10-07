<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Codes" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.codeSystemList.linkname" /></h2>

<br/>

<form:form commandName="getNcdCodeSystemListObj">

	<b class="boxHeader"><spring:message code="ncd.pages.codeSystemList.box.title"/></b>
	<div class="box">
			<table cellspacing="0">
			    <thead>
			    	<tr>
			    		<td nowrap>
							<openmrs:hasPrivilege privilege="Add NCD Codes">
								<c:set var="title"><spring:message code="ncd.pages.codeSystemList.add.title"/></c:set>
								<input type="button" value="<spring:message code="ncd.pages.codeSystemList.add"/>" onclick="gotoURL('codeSystemDetail.form?new=1')" title="${title}">
							</openmrs:hasPrivilege>
			    		</td>
			    	</tr>
			    	<tr>
			    		<td>&nbsp;</td>
			    	</tr>
				    <tr>
				        <th><spring:message code="ncd.pages.codeSystemList.headers.name"/></th>
				    </tr>
			    </thead>
			    <tbody>
					<c:forEach var="row" varStatus="idx" items="${getNcdCodeSystemListObj.systems}">
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
								<a href="codeSystemDetail.form?edit=${row.id}">${row.name}</a>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
