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

<h2><spring:message code="ncd.pages.codeTypeDetail.linkname" /></h2>


<br/>

<c:set var="isNew" value="${getNcdCodeTypeDetailObj.id == null}" />

<form:form commandName="getNcdCodeTypeDetailObj">
	<b class="boxHeader"><spring:message code="ncd.pages.codeTypeDetail.title"/></b>
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
		    			<spring:message code="ncd.pages.codeTypeDetail.name"/>
		    			<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeTypeDetail.name.title"/></c:set>
		    			<form:input path="name" title="${title}"/>
						<form:errors path="name" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeTypeDetail.displayTextKey"/>
		    			<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeTypeDetail.displayTextKey.title"/></c:set>
		    			<form:input path="displayTextKey" size="40" title="${title}"/>
						<form:errors path="displayTextKey" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeTypeDetail.retired"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeTypeDetail.retired.title"/></c:set>
		    			<form:checkbox path="retired" title="${title}"/>
		    		</td>
		    	</tr>
			</tbody>
		</table>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
