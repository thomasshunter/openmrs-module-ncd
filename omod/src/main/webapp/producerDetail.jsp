<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD HL7 Producers" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.producerDetail.title" /></h2>

<br/>

<c:set var="isNew" value="${getNcdProducerDetailObj.producer.id == null}" />

<form:form commandName="getNcdProducerDetailObj">
	<b class="boxHeader"><spring:message code="ncd.pages.producerDetail.title"/></b>
	<div class="box">
		<table>
			<thead>
				<tr>
					<td colspan="2">
						<openmrs:hasPrivilege privilege="Add NCD HL7 Producers,Edit NCD HL7 Producers">
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
		    			<spring:message code="ncd.pages.producerDetail.applicationname"/>
		    			<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.applicationname.title"/></c:set>
		    			<form:input path="producer.applicationname" title="${title}"/>
						<form:errors path="producer.applicationname" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.producerDetail.facilityname"/>
		    			<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.facilityname.title"/></c:set>
		    			<form:input path="producer.facilityname" title="${title}"/>
						<form:errors path="producer.facilityname" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.producerDetail.locationname"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.locationname.title"/></c:set>
		    			<form:input path="producer.locationname" title="${title}"/>
						<form:errors path="producer.locationname" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.producerDetail.description"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.description.title"/></c:set>
		    			<form:input path="producer.description" title="${title}"/>
						<form:errors path="producer.description" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.producerDetail.institution"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.institution.title"/></c:set>
		    			<form:select path="institutionId" title="${title}">
			    			<form:options items="${allInstitutions}" itemValue="id" itemLabel="name" />
		    			</form:select>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.producerDetail.excluded"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.excluded.title"/></c:set>
		    			<form:checkbox path="producer.excluded" title="${title}"/>
						<form:errors path="producer.excluded" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.producerDetail.reportall"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.reportall.title"/></c:set>
		    			<form:checkbox path="producer.reportall" title="${title}"/>
						<form:errors path="producer.reportall" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.producerDetail.ignored"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.ignored.title"/></c:set>
		    			<form:checkbox path="producer.ignored" title="${title}"/>
						<form:errors path="producer.ignored" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.producerDetail.retired"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.producerDetail.retired.title"/></c:set>
		    			<form:checkbox path="producer.retired" title="${title}"/>
						<form:errors path="producer.retired" cssClass="error" />
		    		</td>
		    	</tr>
			</tbody>
		</table>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
