<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="ncd"     uri="resources/ncd.tld" %>
<%@ taglib prefix="openmrs" uri="/WEB-INF/taglibs/openmrs.tld" %>
<%@ taglib prefix="ncd"     uri="resources/ncd.tld" %>


<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Codes" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.codeList.linkname" /></h2>

<form:form commandName="getNcdCodeListObj">

	<%-- Standard filter section --%>
	<c:set var="filterSection" value="ncd.pages.codeList.filter.title" />
	<c:set var="filterTerms" value="${getNcdCodeListObj.filter.terms}" />
	<c:set var="filterPath" value="filter.terms" />
	<c:set var="jspFilter" value="${getNcdCodeListObj.filter}" />
	<c:set var="editFilter" value="${getNcdCodeListObj.editFilter}" />
	<c:set var="editFilterDivHeight" value="160px" />
	<%@include file="listFilter.jsp" %>

	<br/>
	
	<%-- Result list section --%>
	<b class="boxHeader"><spring:message code="ncd.pages.codeList.results.title"/></b>
	<div class="box">
		<input type="submit" value="<spring:message code="ncd.buttons.refresh"/>" name="listaction"/>
		<c:if test="${getNcdCodeListObj.previousPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.firstpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.prevpage"/>" name="listaction"/>
		</c:if>
  			<c:if test="${getNcdCodeListObj.nextPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.nextpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.lastpage"/>" name="listaction"/>
		</c:if>
		<openmrs:hasPrivilege privilege="Add NCD Codes">
			<input type="button" value="<spring:message code="ncd.pages.codeList.add"/>" name="listaction" onclick="gotoURL('codeDetail.form?new=1')" />
		</openmrs:hasPrivilege>
		<div class="scrollable" style="height: 390px">		
			<table cellspacing="0" class="scrollable list">
			    <thead>
			    	<ncd:sortcolumns formname="forms[0]" sortkey="${getNcdCodeListObj.filter.sortKeyLabel}" 
			    		direction="${getNcdCodeListObj.filter.sortAscending}" resourcepath="${pageContext.request.contextPath}/moduleResources/ncd/">
					    <tr class="evenRow">
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.codeList.code.title"/></c:set>
								<ncd:sortcolumn sortkey="CODE" cssclass="sortcolumnheader" title="${title}">
					        		<spring:message code="ncd.pages.codeList.code"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.codeList.codeSystem.title"/></c:set>
								<ncd:sortcolumn sortkey="CODESYSTEM" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.codeList.codeSystem"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.codeList.codeType.title"/></c:set>
								<ncd:sortcolumn sortkey="CODETYPE" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.codeList.codeType"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.codeList.displayText.title"/></c:set>
								<ncd:sortcolumn sortkey="DISPLAYTEXT" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.codeList.displayText"/>
			        			</ncd:sortcolumn>
				        	</th>
					    </tr>
					</ncd:sortcolumns>
			    </thead>
			    <tbody style="height: 360px">
			    	<c:set var="atLeastOneRow" value="false" />
					<c:forEach var="row" varStatus="idx" items="${getNcdCodeListObj.visibleRows}">
				    	<c:set var="atLeastOneRow" value="true" />
						<c:choose>
							<c:when test="${(idx.count % 2) == 0}">
								<c:set var="rowClass" value="evenRow" /> 
							</c:when>
							<c:otherwise>
								<c:set var="rowClass" value="oddRow" /> 
							</c:otherwise>
						</c:choose>
						<c:if test="${row.retired}">
							<c:set var="rowClass" value="${rowClass} retired" /> 
						</c:if>
						<tr class="${rowClass}" style="height: 0px"> 
							<td><a href="codeDetail.form?edit=${row.id}">${row.code}</a></td>
							<td>${row.codeSystem.name}</td>
							<td>${row.codeType.name}</td>
							<td>${row.displayText}</td>
						</tr>
					</c:forEach>
					<c:if test="${atLeastOneRow == false}">
				    	<tr class="buttons" style="height: 12px">
							<td colspan="5"> </td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td> </td>
				    		<td colspan="4">
				    			<span class="error">
									<spring:message code="ncd.pages.codeList.nomatches"/>
								</span>
				    		</td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td colspan="5"> </td>
				    	</tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
