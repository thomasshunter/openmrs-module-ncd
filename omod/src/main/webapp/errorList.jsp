<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Errors" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.errorList.linkname" /></h2>

<br/>

<script type="text/javascript">

function onAdditionalInfo(additionalInfo) 
{
	alert(additionalInfo);
	return false;
}

function onReprocess(f) 
{
	if (confirm("Are you sure you want to reprocess the selected error(s)? Click OK if you are sure you want to reprocess them.")) 
	{
		return true;
	}
	else 
	{
		return false;
	}
}

function onHide(f) 
{
	if (confirm("Are you sure you want to hide the selected error(s)? Click OK if you are sure you want to hide them.")) 
	{
		return true;
	}
	else 
	{
		return false;
	}
}

function onUnhide(f) 
{
	if (confirm("Are you sure you want to unhide the selected error(s)? Click OK if you are sure you want to unhide them.")) 
	{
		return true;
	}
	else 
	{
		return false;
	}
}

function onDelete(f) 
{
	if (confirm("Are you sure you want to delete the selected error(s)? This is permanent, and cannot be undone.  Click OK if you are sure you want to delete them.")) 
	{
		return true;
	}
	else 
	{
		return false;
	}
}

</script>

<form:form commandName="getNcdErrorListObj">

	<%-- Standard filter section --%>
	<c:set var="filterSection" value="ncd.pages.errorlist.filter.title" />
	<c:set var="filterTerms" value="${getNcdErrorListObj.filter.terms}" />
	<c:set var="filterPath" value="filter.terms" />
	<c:set var="jspFilter" value="${getNcdErrorListObj.filter}" />
	<c:set var="editFilter" value="${getNcdErrorListObj.editFilter}" />
	<c:set var="editFilterDivHeight" value="175px" />
	<%@include file="listFilter.jsp" %>

	<br/>
	
	<%-- Error list section --%>
	<b class="boxHeader"><spring:message code="ncd.pages.errorlist.results.title"/></b>
	<div class="box">
		<input type="submit" value="<spring:message code="ncd.buttons.refresh"/>" name="listaction"/>
		<c:if test="${getNcdErrorListObj.previousPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.firstpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.prevpage"/>" name="listaction"/>
		</c:if>
  			<c:if test="${getNcdErrorListObj.nextPageVisible}">
			<input type="submit" value="<spring:message code="ncd.buttons.nextpage"/>" name="listaction"/>
			<input type="submit" value="<spring:message code="ncd.buttons.lastpage"/>" name="listaction"/>
		</c:if>
		<input type="submit" value="<spring:message code="ncd.buttons.selectall"/>" name="listaction"/>
		<input type="submit" value="<spring:message code="ncd.buttons.selectnone"/>" name="listaction"/>
		<openmrs:hasPrivilege privilege="Edit NCD Errors">
			<c:set var="title"><spring:message code="ncd.pages.errorlist.button.reprocess.title"/></c:set>
			<input type="submit" value="<spring:message code="ncd.pages.errorlist.button.reprocess"/>" name="listaction" title="${title}" onclick="return onReprocess()"/>
			<c:set var="title"><spring:message code="ncd.pages.errorlist.button.hide.title"/></c:set>
			<input type="submit" value="<spring:message code="ncd.pages.errorlist.button.hide"/>" name="listaction" title="${title}" onclick="return onHide()"/>
			<c:set var="title"><spring:message code="ncd.pages.errorlist.button.unhide.title"/></c:set>
			<input type="submit" value="<spring:message code="ncd.pages.errorlist.button.unhide"/>" name="listaction" title="${title}" onclick="return onUnhide()"/>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Delete NCD Errors">
			<c:set var="title"><spring:message code="ncd.pages.errorlist.button.delete.title"/></c:set>
			<input type="submit" value="<spring:message code="ncd.pages.errorlist.button.delete"/>" name="listaction" title="${title}" onclick="return onDelete()"/>
		</openmrs:hasPrivilege>
		<div class="scrollable" style="height: 380px">
			<table cellspacing="0" class="scrollable list">
			    <thead>
			    	<ncd:sortcolumns formname="forms[0]" sortkey="${getNcdErrorListObj.filter.sortKeyLabel}" 
			    		direction="${getNcdErrorListObj.filter.sortAscending}" resourcepath="${pageContext.request.contextPath}/moduleResources/ncd/">
					    <tr class="evenRow">
					    	<th> </th>
					        <th align="center" nowrap>
								<c:set var="title"><spring:message code="ncd.pages.errorlist.mpqseqnumber.title"/></c:set>
								<ncd:sortcolumn sortkey="MPQSEQNUMBER" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.errorlist.mpqseqnumber"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.errorlist.lasterrordate.title"/></c:set>
								<ncd:sortcolumn sortkey="LASTERRORDATE" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.errorlist.lasterrordate"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.errorlist.level.title"/></c:set>
								<ncd:sortcolumn sortkey="LEVEL" cssclass="sortcolumnheader" title="${title}">
					        		<spring:message code="ncd.pages.errorlist.level"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.errorlist.description.title"/></c:set>
								<ncd:sortcolumn sortkey="DESCRIPTION" cssclass="sortcolumnheader" title="${title}">
						        	<spring:message code="ncd.pages.errorlist.description"/>
			        			</ncd:sortcolumn>
				        	</th>
					        <th align="center">
								<c:set var="title"><spring:message code="ncd.pages.errorlist.additionalinfo.title"/></c:set>
								<a class="unmarked" title="${title}">
						        	<spring:message code="ncd.pages.errorlist.additionalinfo"/>
			        			</a>
				        	</th>
					        <th align="center">&nbsp;&nbsp;&nbsp;</th>
					    </tr>
					</ncd:sortcolumns>
			    </thead>
			    <tbody style="height: 350px">
			    	<c:set var="atLeastOneRow" value="false" />
					<c:forEach var="row" varStatus="idx" items="${getNcdErrorListObj.visibleRows}">
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
							<td><form:checkbox path="selectedResults" value="${row.id}"/>&nbsp;&nbsp;</td>
							<td><a href="errorDetail.form?edit=${row.id}">${row.mpqSeqNumber}</a></td>
							<td><ncd:write value="${row.lastErrorDate}" type="datetime" />&nbsp;&nbsp;</td>
							<td>${row.level}&nbsp;&nbsp;</td>
							<td>${row.description}&nbsp;&nbsp;</td>
							<td>
								<c:choose>
									<c:when test="${row.additionalInfo != null}">
										<input type="submit" value="<spring:message code="ncd.pages.errorlist.additionalinfo"/>" name="action"  onclick="return onAdditionalInfo('${row.alertFormattedAdditionalInfo}')">
									</c:when>
									<c:otherwise>
										Not available
									</c:otherwise>
								</c:choose>
							</td>
				      	    <td align="center">&nbsp;&nbsp;&nbsp;</td>
						</tr>
					</c:forEach>
					<c:if test="${atLeastOneRow == false}">
				    	<tr class="buttons" style="height: 12px">
							<td colspan="6"> </td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td> </td>
				    		<td colspan="5">
				    			<span class="error">
									<spring:message code="ncd.pages.errorlist.nomatches"/>
								</span>
				    		</td>
				    	</tr>
				    	<tr class="buttons" style="height: 12px">
							<td colspan="6"> </td>
				    	</tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
