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

<h2><spring:message code="ncd.pages.institutionDetail.linkname" /></h2>

<br/>

<c:set var="isNew" value="${getNcdInstitutionDetailObj.id == null}" />

<form:form commandName="getNcdInstitutionDetailObj">
	<b class="boxHeader"><spring:message code="ncd.pages.institutionDetail.title"/></b>
	<div class="box">
		<table>
			<thead>
				<tr>
					<td colspan="2">
						<openmrs:hasPrivilege privilege="Add NCD Institutions,Edit NCD Institutions">
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
		    			<spring:message code="ncd.pages.institutionDetail.name"/>
		    			<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.name.title"/></c:set>
		    			<form:input path="name" size="40" title="${title}"/>
						<form:errors path="name" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.description"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.description.title"/></c:set>
		    			<form:input path="description" size="80" title="${title}"/>
						<form:errors path="description" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.addressline1"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.addressline1.title"/></c:set>
		    			<form:input path="addressline1" size="40" title="${title}"/>
						<form:errors path="addressline1" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.addressline2"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.addressline2.title"/></c:set>
		    			<form:input path="addressline2" size="40" title="${title}"/>
						<form:errors path="addressline2" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.city"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.city.title"/></c:set>
		    			<form:input path="city" size="40" title="${title}"/>
						<form:errors path="city" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.state"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.state.title"/></c:set>
		    			<form:input path="state" size="40" title="${title}"/>
						<form:errors path="state" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.zip"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.zip.title"/></c:set>
		    			<form:input path="zip" size="10" title="${title}"/>
						<form:errors path="zip" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.phone"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.phone.title"/></c:set>
		    			<form:input path="phone" size="10" title="${title}"/>
						<form:errors path="phone" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.www"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.www.title"/></c:set>
		    			<form:input path="www" size="80" title="${title}"/>
						<form:errors path="www" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.institutionDetail.retired"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.institutionDetail.retired.title"/></c:set>
		    			<form:checkbox path="retired" title="${title}"/>
						<form:errors path="retired" cssClass="error" />
		    		</td>
		    	</tr>
			</tbody>
		</table>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
