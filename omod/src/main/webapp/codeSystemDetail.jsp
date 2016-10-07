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

<h2><spring:message code="ncd.pages.codeSystemDetail.linkname" /></h2>


<br/>

<c:set var="isNew" value="${getNcdCodeSystemDetailObj.codeSystem.id == null}" />

<form:form commandName="getNcdCodeSystemDetailObj">
	<b class="boxHeader"><spring:message code="ncd.pages.codeSystemDetail.title"/></b>
	<div class="box">
		<table>
			<thead>
				<tr>
					<td colspan="2">
						<openmrs:hasPrivilege privilege="Add NCD Codes,Edit NCD Codes">
							<input type="submit" value="<spring:message code="ncd.buttons.save"/>" name="action">
						</openmrs:hasPrivilege>
						<input type="submit" value="<spring:message code="ncd.buttons.cancel"/>" name="action">
					</td>
				</tr>
				<tr>
					<td colspan="2">
						&nbsp;
					</td>
				</tr>
			</thead>
		    <tbody>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeSystemDetail.name"/>
		    			<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeSystemDetail.name.title"/></c:set>
		    			<form:input path="codeSystem.name" title="${title}"/>
						<form:errors path="codeSystem.name" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeSystemDetail.retired"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeSystemDetail.retired.title"/></c:set>
		    			<form:checkbox path="codeSystem.retired" title="${title}"/>
		    		</td>
		    	</tr>
			</tbody>
		</table>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
