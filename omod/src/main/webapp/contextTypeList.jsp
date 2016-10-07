<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />

<openmrs:require privilege="View NCD NLP Contexts" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.contextTypeList.linkname" /></h2>

<br/>

<b class="boxHeader"><spring:message code="ncd.pages.contextTypeList.title"/></b>
<form method="post" class="box">
	<table>
	    <thead>
			<tr> 
				<td>&nbsp;</td>
				<td colspan="2">
					<openmrs:hasPrivilege privilege="Delete NCD NLP Contexts">
						<input type="submit" value="<spring:message code="ncd.buttons.remove"/>" name="action">
					</openmrs:hasPrivilege>
					<openmrs:hasPrivilege privilege="Add NCD NLP Contexts">
						<input type="button" value="<spring:message code="ncd.pages.contextTypeList.add"/>" onclick="gotoURL('contextTypeDetail.form?new=1')"/>
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
		        <th><spring:message code="ncd.pages.contextTypeList.name"/></th>
		        <th><spring:message code="ncd.pages.contextTypeList.contexts"/></th>
		    </tr>
	    </thead>
	    <tbody>
			<c:forEach var="row" items="${getNcdContextTypeListObj.contextTypes}">
				<tr> 
					<td><input type="checkbox" name="selectedContextTypes" value="${row.typeName}"></td>
					<td><a href="contextTypeDetail.form?edit=${row.typeName}" title="${row.description}">${row.typeName}</a></td>
					<c:set var="title"><spring:message code="ncd.pages.contextTypeList.contextslink.title"/></c:set>
					<td><a href="contextList.form?edit=${row.typeName}" title="${title}"><spring:message code="ncd.pages.contextTypeList.contextslink"/></a></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
