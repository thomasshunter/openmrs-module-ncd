<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="openmrs" uri="/WEB-INF/taglibs/openmrs.tld" %>
<%@ taglib prefix="ncd"     uri="resources/ncd.tld" %>
<%@ taglib prefix="spring"  uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form"    uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core"  %>


<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Codes" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.codeDetail.linkname" /></h2>


<br/>

<c:set var="isNew" value="${getNcdCodeDetailObj.code.id == null}" />

<form:form commandName="getNcdCodeDetailObj">
	<b class="boxHeader"><spring:message code="ncd.pages.codeDetail.title"/></b>
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
		    			<spring:message code="ncd.pages.codeDetail.code"/>
		    			<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeDetail.code.title"/></c:set>
		    			<form:input path="code.code" title="${title}"/>
						<form:errors path="code.code" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeDetail.codeSystem"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeDetail.codeSystem.title"/></c:set>
		    			<form:select path="codeSystemId" title="${title}">
							<form:options items="${allCodeSystems}" itemValue="id" itemLabel="name" />
		    			</form:select>
						<form:errors path="codeSystemId" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeDetail.codeType"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeDetail.codeType.title"/></c:set>
		    			<form:select path="codeTypeId" title="${title}">
							<form:options items="${allCodeTypes}" itemValue="id" itemLabel="name" />
		    			</form:select>
						<form:errors path="codeTypeId" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeDetail.displayText"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeDetail.displayText.title"/></c:set>
		    			<form:input path="code.displayText" title="${title}"/>
						<form:errors path="code.displayText" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeDetail.scaleType"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeDetail.scaleType.title"/></c:set>
		    			<form:select path="code.scaleType" items="${allScaleTypes}" title="${title}" />
						<form:errors path="code.scaleType" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeDetail.reportAll"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeDetail.reportAll.title"/></c:set>
		    			<form:checkbox path="code.reportAll" title="${title}"/>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeDetail.retired"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeDetail.retired.title"/></c:set>
		    			<form:checkbox path="code.retired" title="${title}"/>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.codeDetail.conditions"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.codeDetail.conditions.title"/></c:set>
		    			<a class="unmarked" title="${title}">${mappedConditionNames}</a>
		    		</td>
		    	</tr>
			</tbody>
		</table>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
