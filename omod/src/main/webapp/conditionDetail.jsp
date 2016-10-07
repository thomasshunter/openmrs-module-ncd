<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Conditions" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.conditionlist.linkname" /></h2>

<br/>

<form:form commandName="getNcdConditionDetailObj">
	<openmrs:hasPrivilege privilege="Edit NCD Conditions">
		<input type="submit" value="<spring:message code="ncd.buttons.save"/>" name="action">
	</openmrs:hasPrivilege>
	<input type="submit" value="<spring:message code="ncd.buttons.cancel"/>" name="action">
	<br/>
	<br/>
	<b class="boxHeader"><spring:message code="ncd.pages.conditiondetail.title"/></b>
	<div class="box">
		<table>
		    <tbody>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.conditiondetail.conditionname"/>
						<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.conditiondetail.conditionname.title"/></c:set>
		    			<form:input path="condition.displayText" size="80" title="${title}" />
						<form:errors path="condition.displayText" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.conditiondetail.conditiongroup"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.conditiondetail.conditiongroup.title"/></c:set>
						<form:select path="conditionGroupName" title="${title}" items="${allConditionGroups}"/>
						<form:errors path="conditionGroupName" cssClass="error" />
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.conditiondetail.reportable"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.conditiondetail.reportable.title"/></c:set>
		    			<form:checkbox path="condition.reportable" title="${title}"/>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.conditiondetail.manualReviewRequired"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.conditiondetail.manualReviewRequired.title"/></c:set>
		    			<form:checkbox path="condition.manualReviewRequired" title="${title}"/>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.conditiondetail.reportAll"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.conditiondetail.reportAll.title"/></c:set>
		    			<form:checkbox path="condition.reportAll" title="${title}"/>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.conditiondetail.retired"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.conditiondetail.retired.title"/></c:set>
		    			<form:checkbox path="condition.retired" title="${title}"/>
		    		</td>
		    	</tr>
			</tbody>
		</table>
	</div>
	<br/>
	<openmrs:hasPrivilege privilege="Edit NCD Conditions">
		<b class="boxHeader"><spring:message code="ncd.pages.conditiondetail.addcodecondition.title"/></b>
		<div class="box">
				<spring:message code="ncd.pages.conditiondetail.newcode"/>
				<spring:message code="ncd.required"/>
				<c:set var="title"><spring:message code="ncd.pages.conditiondetail.newcode.title"/></c:set>
				<form:input path="newCode" size="10" title="${title}" />
				<form:errors path="newCode" cssClass="error" />
				
				<spring:message code="ncd.pages.conditiondetail.newcodesystem"/>
				<c:set var="title"><spring:message code="ncd.pages.conditiondetail.newcodesystem.title"/></c:set>
				<form:select path="newCodeSystem" title="${title}" items="${allCodeSystems}"/>
				<form:errors path="newCodeSystem" cssClass="error" />
	
				<spring:message code="ncd.pages.conditiondetail.newindicator"/>
				<c:set var="title"><spring:message code="ncd.pages.conditiondetail.newindicator.title"/></c:set>
				<form:input path="newIndicator" size="40" title="${title}" />
				<form:errors path="newIndicator" cssClass="error" />
				
				<input type="submit" value="<spring:message code="ncd.pages.conditiondetail.add"/>" name="action">
		</div>
		<br/>
	</openmrs:hasPrivilege>
	<b class="boxHeader"><spring:message code="ncd.pages.conditiondetail.codeconditions.title"/></b>
	<div class="box">
		<openmrs:hasPrivilege privilege="Edit NCD Conditions">
			<input type="submit" value="<spring:message code="ncd.buttons.remove"/>" name="action">
			<br/>
			<br/>
		</openmrs:hasPrivilege>
		<br/>
		<div class="scrollable" style="height: 290px">		
			<table cellspacing="0" class="scrollable">
				<thead>
					<tr>
						<th align="center">&nbsp;</th>
						<th align="center"><spring:message code="ncd.pages.conditiondetail.codeconditions.code"/></th>
						<th align="center"><spring:message code="ncd.pages.conditiondetail.codeconditions.codesystem"/></th>
						<th align="center"><spring:message code="ncd.pages.conditiondetail.codeconditions.code.displayText"/></th>
						<th align="center"><spring:message code="ncd.pages.conditiondetail.codeconditions.indicator"/></th>
					</tr>
				</thead>
				<tbody style="height: 260px">
					<c:forEach var="row" varStatus="idx" items="${getNcdConditionDetailObj.relatedCodes}">
						<c:choose>
							<c:when test="${(idx.count % 2) == 0}">
								<c:set var="rowClass" value="evenRow" /> 
							</c:when>
							<c:otherwise>
								<c:set var="rowClass" value="oddRow" /> 
							</c:otherwise>
						</c:choose>
						<tr class="${rowClass}" style="height: 0px"> 
							<td><form:checkbox path="selectedCodeConditions" value="${row.id}"/></td>
							<td>${row.code.code}</td>
							<td align="center">${row.code.codeSystem.name}</td>
							<td>${row.code.displayText}</td>
							<td>${row.conditionIndicator}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
