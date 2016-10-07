<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="View NCD Conditions" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.conditionlist.linkname" /></h2>

<br/>

<b class="boxHeader"><spring:message code="ncd.pages.conditiongroupdetail.title"/></b>
<form:form commandName="getNcdConditionGroupDetailObj" cssClass="box">
	<table>
	    <tbody>
	    	<tr>
	    		<td colspan="2">
	    			<openmrs:hasPrivilege privilege="Edit NCD Conditions">
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
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.conditiongroupdetail.displayText"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.conditiongroupdetail.displayText.title"/></c:set>
	    			<form:input path="displayText" size="80" title="${title}"/>
					<form:errors path="displayText" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.conditiongroupdetail.displayOrder"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.conditiongroupdetail.displayOrder.title"/></c:set>
	    			<form:input path="displayOrder" title="${title}"/>
					<form:errors path="displayOrder" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.conditiongroupdetail.retired"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.conditiongroupdetail.retired.title"/></c:set>
	    			<form:checkbox path="retired" title="${title}"/>
					<form:errors path="retired" cssClass="error" />
	    		</td>
	    	</tr>
		</tbody>
	</table>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
