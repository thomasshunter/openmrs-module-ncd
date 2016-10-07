<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />

<openmrs:require privilege="View NCD Critics" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.criticdetail.linkname" /></h2>

<br/>

<b class="boxHeader"><spring:message code="ncd.pages.criticdetail.title" /></b>
<form:form commandName="getNcdCriticDetailObj" cssClass="box">
	<form:hidden path="newConcept" />
	<openmrs:hasPrivilege privilege="Add NCD Critics,Edit NCD Critics">
		<input type="submit" value="<spring:message code="ncd.buttons.save"/>" name="action">
	</openmrs:hasPrivilege>
	<input type="submit" value="<spring:message code="ncd.buttons.cancel"/>" name="action">
	<table>
	    <tbody>
	    	<tr>
	    		<td colspan="2">
	    			&nbsp;
				</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.conceptname"/>
	    			<spring:message htmlEscape="false" code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.conceptName.title"/></c:set>
	    			<form:input path="concept.conceptName" title="${title}" />
					<form:errors path="concept.conceptName" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.code"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.code.title"/></c:set>
	    			<form:input path="concept.code" size="50" title="${title}" />
					<form:errors path="concept.code" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.condition"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.conditionName.title"/></c:set>
	    			<form:select path="conditionName" title="${title}">
						<form:options items="${conditionnames}" />
	    			</form:select>
					<form:errors path="conditionName" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.namecode"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.nameCode.title"/></c:set>
	    			<form:input path="concept.nameCode" title="${title}" />
					<form:errors path="concept.nameCode" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.absolutes"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.absolutes.title"/></c:set>
	    			<form:input path="concept.absolutes" size="100" title="${title}"/>
					<form:errors path="concept.absolutes" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.absExcept"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.absExcept.title"/></c:set>
	    			<form:input path="concept.absExcept" size="100" title="${title}" />
					<form:errors path="concept.absExcept" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.altnum"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.altnum.title"/></c:set>
	    			<form:input path="concept.altnum" title="${title}" />
					<form:errors path="concept.altnum" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.preConcept"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.preConcept.title"/></c:set>
	    			<form:input path="concept.preConcept" size="100" title="${title}" />
					<form:errors path="concept.preConcept" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.postConcept"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.postConcept.title"/></c:set>
	    			<form:input path="concept.postConcept" size="100" title="${title}" />
					<form:errors path="concept.postConcept" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.altCon1"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.altCon1.title"/></c:set>
	    			<form:input path="concept.altCon1" size="100" title="${title}" />
					<form:errors path="concept.altCon1" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.altCon2"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.altCon2.title"/></c:set>
	    			<form:input path="concept.altCon2" size="100" title="${title}" />
					<form:errors path="concept.altCon2" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.altCon3"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.altCon3.title"/></c:set>
	    			<form:input path="concept.altCon3" size="100" title="${title}" />
					<form:errors path="concept.altCon3" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.altExcept"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.altExcept.title"/></c:set>
	    			<form:input path="concept.altExcept" size="100" title="${title}" />
					<form:errors path="concept.altExcept" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.negExceptCon"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.negExceptCon.title"/></c:set>
	    			<form:input path="concept.negExceptCon" size="100" title="${title}" />
					<form:errors path="concept.negExceptCon" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.negExNeg"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.negExNeg.title"/></c:set>
	    			<form:input path="concept.negExNeg" size="100" title="${title}" />
					<form:errors path="concept.negExNeg" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.onOff"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.onOff.title"/></c:set>
	    			<form:input path="concept.onOff" title="${title}" />
					<form:errors path="concept.onOff" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.reportExtraction"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.reportExtraction.title"/></c:set>
	    			<form:input path="concept.reportExtraction" title="${title}" />
					<form:errors path="concept.reportExtraction" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.sectionExtraction"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.sectionExtraction.title"/></c:set>
	    			<form:input path="concept.sectionExtraction" title="${title}" />
					<form:errors path="concept.sectionExtraction" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.sectionStart"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.sectionStart.title"/></c:set>
	    			<form:input path="concept.sectionStart" size="100" title="${title}" />
					<form:errors path="concept.sectionStart" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.sectionEnd"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.sectionEnd.title"/></c:set>
	    			<form:input path="concept.sectionEnd" size="100" title="${title}" />
					<form:errors path="concept.sectionEnd" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.specialNegs"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.specialNegs.title"/></c:set>
	    			<form:input path="concept.specialNegs" size="100" title="${title}" />
					<form:errors path="concept.specialNegs" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.smallWindowNegs"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.smallWindowNegs.title"/></c:set>
	    			<form:input path="concept.smallWindowNegs" size="100" title="${title}" />
					<form:errors path="concept.smallWindowNegs" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.absoluteNegs"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.absoluteNegs.title"/></c:set>
	    			<form:input path="concept.absoluteNegs" size="100" title="${title}" />
					<form:errors path="concept.absoluteNegs" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.contexts"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.contexts.title"/></c:set>
	    			<form:checkboxes path="contexts" items="${allcontexts}" title="${title}" />
					<form:errors path="contexts" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.paragraph"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.paragraph.title"/></c:set>
	    			<form:input path="concept.paragraph" title="${title}" /> 
					<form:errors path="concept.paragraph" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.criticdetail.negationgroup"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.criticdetail.negationGroup.title"/></c:set>
	    			<form:input path="concept.negationGroup" title="${title}" /> 
					<form:errors path="concept.negationGroup" cssClass="error" />
	    		</td>
	    	</tr>
			<tr>
				<td colspan="2">
					<%@include file="requiredLegend.jsp" %>
				</td>
			</tr>
		</tbody>
	</table>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
