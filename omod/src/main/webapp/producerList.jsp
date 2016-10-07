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

<h2><spring:message code="ncd.pages.producerList.linkname" /></h2>

<br/>

<form:form commandName="getNcdProducerListObj">

	<b class="boxHeader"><spring:message code="ncd.pages.producerList.box.title"/></b>
	<div class="box">
		<input type="submit" value="<spring:message code="ncd.buttons.refresh"/>" name="listaction"/>
		<c:if test="${getNcdProducerListObj.previousPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.firstpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.prevpage"/>" name="listaction"/>
		</c:if>
		<c:if test="${getNcdProducerListObj.nextPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.nextpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.lastpage"/>" name="listaction"/>
		</c:if>
		<input type="button" value="<spring:message code="ncd.pages.producerList.add"/>" name="action" onclick="gotoURL('producerDetail.form?new=1')">
		<br/>
		<div class="scrollable" style="height: 590px">		
			<table cellspacing="0" class="scrollable list">
			    <thead>
			    	<ncd:sortcolumns formname="forms[0]" sortkey="${getNcdProducerListObj.filter.sortKeyLabel}" 
			    		direction="${getNcdProducerListObj.filter.sortAscending}" resourcepath="${pageContext.request.contextPath}/moduleResources/ncd/">
					    <tr>
					        <th>
								<c:set var="title"><spring:message code="ncd.pages.producerList.headers.applicationname.title"/></c:set>
								<ncd:sortcolumn sortkey="APPLICATION" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.producerList.headers.applicationname"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th>
								<c:set var="title"><spring:message code="ncd.pages.producerList.headers.facilityname.title"/></c:set>
								<ncd:sortcolumn sortkey="FACILITY" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.producerList.headers.facilityname"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th>
								<c:set var="title"><spring:message code="ncd.pages.producerList.headers.locationname.title"/></c:set>
								<ncd:sortcolumn sortkey="LOCATION" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.producerList.headers.locationname"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th>
								<c:set var="title"><spring:message code="ncd.pages.producerList.headers.description.title"/></c:set>
								<ncd:sortcolumn sortkey="DESCRIPTION" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.producerList.headers.description"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th>
								<c:set var="title"><spring:message code="ncd.pages.producerList.headers.institution.name.title"/></c:set>
								<ncd:sortcolumn sortkey="INSTITUTION" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.producerList.headers.institution.name"/>
			        			</ncd:sortcolumn>
				        	</th>
					    </tr>
				    </ncd:sortcolumns>
			    </thead>
			    <tbody style="height: 560px">
					<c:forEach var="row" varStatus="idx" items="${getNcdProducerListObj.visibleRows}">
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
							<td>
								<a href="producerDetail.form?edit=${row.id}">
									${row.applicationname}
								</a>
							</td>
							<td>
								${row.facilityname}
							</td>
							<td>
								${row.locationname}
							</td>
							<td>
								${row.description}
							</td>
							<td>
								${row.institution.name}
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
