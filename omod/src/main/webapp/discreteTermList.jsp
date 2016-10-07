<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<!-- The DAO methods currently only require an authenticated user. We should
     probably change this to specific privileges. -->
<!--  <openmrs:require privilege="View NCD NLP Discrete Terms" otherwise="/login.htm" /> -->

<h2><spring:message code="ncd.pages.discreteTermList.linkname" /></h2>

<br/>

<b class="boxHeader"><spring:message code="ncd.pages.discreteTermList.title"/></b>
<div class="box">
	<form:form commandName="getNcdDiscreteTermListObj">
		<openmrs:hasPrivilege privilege="Delete NCD NLP Discrete Terms">
			<c:set var="title"><spring:message code="ncd.pages.discreteTermList.delete.title"/></c:set>
			<input type="submit" value="<spring:message code="ncd.pages.discreteTermList.delete"/>" name="action" title="${title}">
		</openmrs:hasPrivilege>
		<div class="scrollable" style="height: 600px">		
			<table cellspacing="0" class="scrollable">
			    <thead>
				    <tr>
						<c:set var="checktitle"><spring:message code="ncd.pages.discreteTermList.check.title"/></c:set>
						<c:set var="termtitle"><spring:message code="ncd.pages.discreteTermList.term.title"/></c:set>
						<c:set var="negtitle"><spring:message code="ncd.pages.discreteTermList.negative.title"/></c:set>
				    	<th>&nbsp;</th>
				        <th><a class="unmarked" title="${termtitle}"><spring:message code="ncd.pages.discreteTermList.term"/></a></th>
				        <th><a class="unmarked" title="${negtitle}"><spring:message code="ncd.pages.discreteTermList.negative"/></a></th>
				    </tr>
				    <tr>
				    	<th>
							<c:set var="title"><spring:message code="ncd.pages.discreteTermList.add.title"/></c:set>
							<input type="submit" value="<spring:message code="ncd.pages.discreteTermList.add"/>" name="action" title="${title}">
						</th>
				        <th>
							<c:set var="title"><spring:message code="ncd.pages.discreteTermList.newterm.title"/></c:set>
				        	<form:input path="newTerm" title="${title}" />
				        	<form:errors path="newTerm" />
				        </th>
				        <th>
							<c:set var="title"><spring:message code="ncd.pages.discreteTermList.newnegative.title"/></c:set>
				        	<form:checkbox path="newTermNegative" title="${title}" />
			        	</th>
				    </tr>
			    </thead>
			    <tbody style="height: 540px">
			    	<c:set var="atLeastOneRow" value="false" />
					<c:forEach var="row" items="${getNcdDiscreteTermListObj.terms}">
				    	<c:set var="atLeastOneRow" value="true" />
						<c:choose>
							<c:when test="${(idx.count % 2) == 0}">
								<c:set var="rowClass" value="evenRow" /> 
							</c:when>
							<c:otherwise>
								<c:set var="rowClass" value="oddRow" /> 
							</c:otherwise>
						</c:choose>
						<tr class="${rowClass}" style="height: 0px">
							<td><form:checkbox path="selectedTerms" value="${row.term}" title="${checktitle}"/></td>
							<td><a class="unmarked" title="${termtitle}">${row.term}</a></td>
							<td><a class="unmarked" title="${negtitle}">${row.negative}</a></td>
						</tr>
					</c:forEach>
					<c:if test="${atLeastOneRow == false}">
				    	<tr class="buttons" style="height: 12px">
							<td colspan="3"> </td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td> </td>
				    		<td colspan="3">
				    			<span class="error">
									<spring:message code="ncd.pages.decidedresultlist.nomatches"/>
								</span>
				    		</td>
				    	</tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</form:form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
