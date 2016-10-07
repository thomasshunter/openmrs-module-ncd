<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD NLP Contexts" otherwise="/login.htm" />

<form:form commandName="getNcdContextListObj">

	<h2>
		<spring:message code="ncd.pages.contextList.title" />
		<ncd:write path="contextTypeName"/>
	</h2>
	
	<br/>
	
	<openmrs:hasPrivilege privilege="Add NCD NLP Contexts">
		<b class="boxHeader"><spring:message code="ncd.pages.contextList.addcontext.title"/></b>
		<div class="box">
			<spring:message code="ncd.pages.contextList.new.contextValue"/>
			<c:set var="title"><spring:message code="ncd.pages.contextList.new.contextValue.title"/></c:set>
			<form:input path="newContextValue" title="${title}"/>
			<form:errors path="newContextValue" cssClass="error" />
			<spring:message code="ncd.pages.contextList.new.contextGroup"/>
			<c:set var="title"><spring:message code="ncd.pages.contextList.new.contextGroup.title"/></c:set>
			<form:select path="newContextGroup" items="${allContextGroups}" title="${title}"/>
			<form:errors path="newContextGroup" cssClass="error" />
			<input type="submit" value="<spring:message code="ncd.pages.contextList.add"/>" name="action">
		</div>
		<br/>
	</openmrs:hasPrivilege>
	
	<b class="boxHeader"><spring:message code="ncd.pages.contextList.box.title"/></b>
	<div class="box">
		<input type="submit" value="<spring:message code="ncd.buttons.cancel"/>" name="action">
		<openmrs:hasPrivilege privilege="Delete NCD NLP Contexts">
			<input type="submit" value="<spring:message code="ncd.buttons.remove"/>" name="action">
		</openmrs:hasPrivilege>
		<br/>
		<br/>
		<div class="scrollable" style="height: 490px">		
			<table cellspacing="0" class="scrollable">
			    <thead>
				    <tr>
				    	<th>&nbsp;</th>
				        <th><spring:message code="ncd.pages.contextList.headers.contextValue"/></th>
				        <th><spring:message code="ncd.pages.contextList.headers.contextGroup"/></th>
				    </tr>
			    </thead>
			    <tbody style="height: 460px">
					<c:forEach var="row" varStatus="idx" items="${getNcdContextListObj.contexts}">
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
								<form:checkbox path="selectedContexts" value="${row.id}"/>
							</td>
							<td>[${row.contextValue}]</td>
							<td>[${row.contextGroup}]</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
