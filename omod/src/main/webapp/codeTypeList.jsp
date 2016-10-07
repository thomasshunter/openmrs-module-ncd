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

<h2><spring:message code="ncd.pages.codeTypeList.linkname" /></h2>

<br/>

<form:form commandName="getNcdCodeTypeListObj">

	<b class="boxHeader"><spring:message code="ncd.pages.codeTypeList.box.title"/></b>
	<div class="box">
		<input type="button" value="<spring:message code="ncd.pages.codeTypeList.add"/>" name="action" onclick="gotoURL('codeTypeDetail.form?new=1')">
		<br/>
		<br/>
		<table cellspacing="0">
		    <thead>
			    <tr>
			        <th><spring:message code="ncd.pages.codeTypeList.headers.name"/></th>
			        <th><spring:message code="ncd.pages.codeTypeList.headers.displayTextKey"/></th>
			        <th><spring:message code="ncd.pages.codeTypeList.headers.retired"/></th>
			    </tr>
		    </thead>
		    <tbody>
				<c:forEach var="row" varStatus="idx" items="${getNcdCodeTypeListObj}">
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
							<a href="codeTypeDetail.form?edit=${row.id}">
								${row.name}
							</a>
						</td>
						<td>
							${row.displayTextKey}
						</td>
						<td>
							${row.retired}
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
