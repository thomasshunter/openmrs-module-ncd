<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.openmrs.module.ncd.utilities.DateUtilities" %>
<%@ page import="org.openmrs.module.ncd.database.DecidedResult" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />

<openmrs:require privilege="View NCD Decided Results" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.decidedresultdetail.linkname" /></h2>

<br/>

<c:set var="isNew" value="${getNcdDecidedResultDetailObj.result.id == null}" />

<b class="boxHeader"><spring:message code="ncd.pages.decidedresultdetail.title"/></b>
<form:form cssClass="box" commandName="getNcdDecidedResultDetailObj">
	<table>
		<thead>
			<tr>
				<td colspan="2">
					<openmrs:hasPrivilege privilege="Add NCD Decided Results,Edit NCD Decided Results">
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
			<c:if test="${!isNew}">
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.decidedresultdetail.id"/>
		    		</td>
		    		<td>
		    			<ncd:write path="result.id" />
		    		</td>
		    	</tr>
	    	</c:if>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.reportable"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.reportable.title"/></c:set>
	    			<form:select path="result.reportable" title="${title}">
						<form:options items="${reportableStates}" />
	    			</form:select>
					<form:errors path="result.reportable" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.datatype"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.datatype.title"/></c:set>
	    			<form:select path="result.dataType" title="${title}">
						<form:options items="${datatypes}" />
	    			</form:select>
					<form:errors path="result.dataType" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.disposition"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.disposition.title"/></c:set>
	    			<form:input path="result.disposition" title="${title}"/>
					<form:errors path="result.disposition" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.resultcode"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.resultcode.title"/></c:set>
	    			<form:input path="result.resultCode" title="${title}"/>
					<form:errors path="result.resultCode" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.resultvalue"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.resultvalue.title"/></c:set>
	    			<form:textarea rows="10" cols="100" path="result.resultValue" title="${title}"/>
					<form:errors path="result.resultValue" cssClass="error" />
	    		</td>
	    	</tr>
			<c:if test="${! isNew}">
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.decidedresultdetail.resultcount"/>
		    			<spring:message code="ncd.required"/>
		    		</td>
		    		<td>
		    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.resultcount.title"/></c:set>
		    			<form:input path="result.resultCount" title="${title}"/>
						<form:errors path="result.resultCount" cssClass="error" />
		    		</td>
		    	</tr>
	    	</c:if>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.dateadded"/>
	    		</td>
	    		<td>
					<c:if test="${isNew}">
		    			<spring:message code="ncd.datetimenow"/>
			    	</c:if>
					<c:if test="${! isNew}">
		    			<ncd:write path="result.dateAdded" type="datetime" />
			    	</c:if>
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.dateclassified"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.dateclassified.title"/></c:set>
	    			<form:input size="30" path="result.dateClassified" title="${title}"/>
					<form:errors path="result.dateClassified" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.classifiedbywhom"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.classifiedbywhom.title"/></c:set>
	    			<form:input size="30" path="result.classifiedByWhom" title="${title}"/>
					<form:errors path="result.classifiedByWhom" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.conditionname"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.conditionname.title"/></c:set>
	    			<form:select path="result.conditionName" title="${title}">
						<form:options items="${conditionnames}" />
	    			</form:select>
					<form:errors path="result.conditionName" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.lastmodified"/>
	    		</td>
	    		<td>
	    			<c:choose>
	    				<c:when test="${isNew}">
			    			<spring:message code="ncd.datetimenow"/>
	    				</c:when>
		    			<c:otherwise>
		    				<ncd:write path="result.lastModified" type="datetime"/>
		    			</c:otherwise>
	    			</c:choose>
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.mpqsequencenumber"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.mpqsequencenumber.title"/></c:set>
	    			<form:input path="result.mpqSequenceNumber" title="${title}"/>
					<form:errors path="result.mpqSequenceNumber" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.obr"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.obr.title"/></c:set>
	    			<form:input path="result.obr" title="${title}"/>
					<form:errors path="result.obr" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.obrcodesys"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.obrcodesys.title"/></c:set>
	    			<form:input path="result.obrCodeSystem" title="${title}"/>
					<form:errors path="result.obrCodeSystem" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.obrtext"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.obrtext.title"/></c:set>
	    			<form:textarea rows="10" cols="100" path="result.obrText" title="${title}"/>
					<form:errors path="result.obrText" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.obx"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.obx.title"/></c:set>
	    			<form:input path="result.obx" title="${title}"/>
					<form:errors path="result.obx" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.obxcodesys"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.obxcodesys.title"/></c:set>
	    			<form:input path="result.obxCodeSystem" title="${title}"/>
					<form:errors path="result.obxCodeSystem" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.obxtext"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.obxtext.title"/></c:set>
	    			<form:textarea rows="10" cols="100" path="result.obxText" title="${title}"/>
					<form:errors path="result.obxText" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.nte"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.nte.title"/></c:set>
	    			<form:textarea rows="10" cols="100" path="result.nte" title="${title}"/>
					<form:errors path="result.nte" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.loinccode"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.loinccode.title"/></c:set>
	    			<form:input path="result.loincCode" title="${title}"/>
					<input type="submit" value="<spring:message code="ncd.pages.decidedresultdetail.buttons.getloinc"/>" name="action">
					<ncd:write path="codeName" />
					<form:errors path="result.loincCode" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.decidedresultdetail.rawmessage"/>
	    		</td>
	    		<td>
					<c:set var="title"><spring:message code="ncd.pages.decidedresultdetail.rawmessage.title"/></c:set>
		        	<a title="${title}" class="unmarked">
		        		<c:choose>
		        			<c:when test="${getNcdDecidedResultDetailObj.result.rawMessage != null}">
				    			<ncd:write path="result.rawMessage.messageText" type="hl7" />
		        			</c:when>
		        			<c:otherwise>
		        				<spring:message code="ncd.pages.decidedresultdetail.rawmessage.na"/>
		        			</c:otherwise>
		        		</c:choose>
	    			</a>
	    		</td>
	    	</tr>
			<tr>
				<td colspan="2">
					&nbsp;
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<%@include file="requiredLegend.jsp" %>
					<br/>
					<openmrs:hasPrivilege privilege="Add NCD Decided Results,Edit NCD Decided Results">
						<input type="submit" value="<spring:message code="ncd.buttons.save"/>" name="action">
					</openmrs:hasPrivilege>
					<input type="submit" value="<spring:message code="ncd.buttons.cancel"/>" name="action">
				</td>
			</tr>
		</tbody>
	</table>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
