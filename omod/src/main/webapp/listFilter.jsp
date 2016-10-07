<%@ page language="java" %>
<%@ include file="/WEB-INF/template/include.jsp"%>

<%--

View or edit a list filter.
  Request parameters in:
	commandName	The name of the spring command object.
	path		The spring path within the command object of the filter
				terms array.

- Emits two DIVs, one containing a read-only display of the filter showing
  only elements of the filter which are "set", and one containing controls
  to edit the filter. Initially only the read-only display DIV is visible.
- When the user clicks the "Edit" button in the read-only display DIV, the
  read-only display DIV is hidden and the edit DIV is shown.
- When the user clicks the "Apply" button in the edit DIV, the enclosing
  form is submitted. The Apply button submits an action named "filteraction"

Usage:

<c:set var="filterSection" value="ncd.pages.decidedresultlist.filter.title" />
<c:set var="filterTerms" value="${getNcdDecidedResultListObj.filter.terms}" />
<c:set var="filterPath" value="filter.terms" />
<c:set var="jspFilter" value="${getNcdDecidedResultListObj.filter}" />
<c:set var="editFilter" value="getNcdDecidedResultListObj.editFilter" />
<c:set var="editFilterDivHeight" value="465px" />
<%@include file="listFilter.jsp" %>

--%>

<script type="text/javascript">

function viewFilter() {
	hide("filterEdit");
	show("filterView");
}

function editFilter() {
	hide("filterView");
	show("filterEdit");
}

function onApply(f) {

	var maxRowsEntered = parseInt(document.getElementById("filter.maxRows").value);
	
	if (maxRowsEntered > 1000) {
	
		if (confirm("Large values can be very slow. Click OK if you are sure you want to continue.")) {
			return true;
		}
		else {
			return false;
		}
	}

	return true;
}

</script>

<div id="filterView" style="display:none">
	<b class="boxHeader"><spring:message code="${filterSection}"/></b>
	<div class="box">
		<ul>
			<c:forEach var="row" varStatus="idx" items="${filterTerms}">
				<c:set var="termPath" value="${filterPath}[${idx.index}]" />
				<c:if test="${row.set}">
					<li>
					<spring:message code="${row.editNameKey}"/>
					<c:choose>
						<c:when test="${row.type == 'Boolean'}">
							= ${row.value}
						</c:when>
						<c:when test="${row.type == 'DateRange'}">
							<c:if test="${not empty row.low}">
								<spring:message code="ncd.filter.onorafter"/>
								${row.low}
								<c:if test="${not empty row.high}">
									and
								</c:if>
							</c:if>
							<c:if test="${not empty row.high}">
								<spring:message code="ncd.filter.onorbefore"/>
								${row.high}
							</c:if>
						</c:when>
						<c:when test="${row.type == 'DateTimeRange'}">
							<c:if test="${not empty row.low}">
								<spring:message code="ncd.filter.onorafter"/>
								${row.low}
								<c:if test="${not empty row.high}">
									and
								</c:if>
							</c:if>
							<c:if test="${not empty row.high}">
								<spring:message code="ncd.filter.onorbefore"/>
								${row.high}
							</c:if>
						</c:when>
						<c:when test="${row.type == 'Int'}">
							= ${row.value}
						</c:when>
						<c:when test="${row.type == 'List'}">
							= ${row.value}
						</c:when>
						<c:when test="${row.type == 'OpCondition'}">
							${row.op} ${row.value}
						</c:when>
						<c:when test="${row.type == 'OpLookup'}">
							${row.op} ${row.valueDescription}
						</c:when>
						<c:when test="${row.type == 'OpString'}">
							${row.op} ${row.value}
						</c:when>
						<c:when test="${row.type == 'OpLoinc'}">
							${row.op} ${row.value}
						</c:when>
						<c:when test="${row.type == 'OpLong'}">
							${row.op} ${row.value}
						</c:when>
						<c:when test="${row.type == 'String'}">
							= ${row.value}
						</c:when>
						<c:when test="${row.type == 'StringRange'}">
							<c:if test="${not empty row.low}">
								<spring:message code="ncd.filter.atleast"/>
								${row.low}
								<c:if test="${not empty row.high}">
									and
								</c:if>
							</c:if>
							<c:if test="${not empty row.high}">
								<spring:message code="ncd.filter.atmost"/>
								${row.high}
							</c:if>
						</c:when>
						<c:when test="${row.type == 'Reportable'}">
							= ${row.value}
						</c:when>
						<c:otherwise>
							SearchTerm type "${row.type}" is not yet implemented.
						</c:otherwise>
					</c:choose>
				</c:if>
			</c:forEach>
			<li/>
			<spring:message code="ncd.filter.rowsperpage"/>
			= ${jspFilter.rowsPerPage}
			<li/>
			<spring:message code="ncd.filter.maxrows"/>
			= ${jspFilter.maxRows}
		</ul>
		<input type="button" value="<spring:message code="ncd.buttons.change"/>" onclick="editFilter()" /> 
	</div>
</div>
<div id="filterEdit" style="display:none">
	<b class="boxHeader"><spring:message code="${filterSection}"/></b>
	<div class="box">
		<div class="scrollable" style="height:${editFilterDivHeight}">
			<table cellspacing="0" class="scrollable">
				<tbody>
					<c:forEach var="row" varStatus="idx" items="${filterTerms}">
						<c:set var="termPath" value="${filterPath}[${idx.index}]" />
						<c:if test="${row.visible || jspFilter.showAll}">
							<tr>
								<td width="1%">
									<c:set var="title"><spring:message code="ncd.filter.visible.title"/></c:set>
									<form:checkbox path="${termPath}.visible" title="${title}"/>
								</td>
								<td width="1%" class="nowrap">
									<spring:message code="${row.editNameKey}"/>
								</td>
								<c:choose>
									<c:when test="${row.type == 'Boolean'}">
										<td width="1%">
											=
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:select path="${termPath}.value" title="${title}">
												<form:options items="${booleans}" />
											</form:select>
										</td>
									</c:when>
									<c:when test="${row.type == 'DateRange'}">
										<td width="1%" class="nowrap">
											<spring:message code="ncd.filter.onorafter"/>
											<c:set var="title"><spring:message code="${row.editNameKey}.low.title"/></c:set>
											<form:input path="${termPath}.low" title="${title}" />
										</td>
										<td width="1%" class="nowrap">
											<spring:message code="ncd.filter.onorbefore"/>
											<c:set var="title"><spring:message code="${row.editNameKey}.high.title"/></c:set>
											<form:input path="${termPath}.high" title="${title}" />
										</td>
									</c:when>
									<c:when test="${row.type == 'DateTimeRange'}">
										<td width="1%" class="nowrap">
											<spring:message code="ncd.filter.onorafter"/>
											<c:set var="title"><spring:message code="${row.editNameKey}.low.title"/></c:set>
											<form:input path="${termPath}.low" title="${title}" />
										</td>
										<td width="1%" class="nowrap">
											<spring:message code="ncd.filter.onorbefore"/>
											<c:set var="title"><spring:message code="${row.editNameKey}.high.title"/></c:set>
											<form:input path="${termPath}.high" title="${title}" />
										</td>
									</c:when>
									<c:when test="${row.type == 'Int'}">
										<td width="1%">
											=
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:input path="${termPath}.value" title="${title}" />
										</td>
									</c:when>
									<c:when test="${row.type == 'List'}">
										<td width="1%">
											=
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:select path="${termPath}.value" title="${title}">
												<form:options items="${requestScope[row.listName]}" />
											</form:select>
										</td>
									</c:when>
									<c:when test="${row.type == 'OpCondition'}">
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.op.title"/></c:set>
											<form:select path="${termPath}.op" title="${title}">
												<form:options items="${equalOperators}" />
											</form:select>
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:select path="${termPath}.value" title="${title}">
												<form:options items="${conditionNames}" />
											</form:select>
										</td>
									</c:when>
									<c:when test="${row.type == 'OpList'}">
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.op.title"/></c:set>
											<form:select path="${termPath}.op" title="${title}">
												<form:options items="${requestScope[row.opListName]}" />
											</form:select>
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:select path="${termPath}.value" title="${title}">
												<form:options items="${requestScope[row.valueListName]}" />
											</form:select>
										</td>
									</c:when>
									<c:when test="${row.type == 'OpLookup'}">
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.op.title"/></c:set>
											<form:select path="${termPath}.op" title="${title}">
												<form:options items="${equalOperators}" />
											</form:select>
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:select path="${termPath}.value" title="${title}">
												<form:options items="${row.options}" itemValue="first" itemLabel="second" />
											</form:select>
										</td>
									</c:when>
									<c:when test="${row.type == 'OpLoinc'}">
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.op.title"/></c:set>
											<form:select path="${termPath}.op" title="${title}">
												<form:options items="${loincOperators}" />
											</form:select>
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:input path="${termPath}.value" title="${title}" />
										</td>
									</c:when>
									<c:when test="${row.type == 'OpString'}">
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.op.title"/></c:set>
											<form:select path="${termPath}.op" title="${title}">
												<form:options items="${stringOperators}" />
											</form:select>
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:input path="${termPath}.value" title="${title}" />
										</td>
									</c:when>
									<c:when test="${row.type == 'OpLong'}">
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.op.title"/></c:set>
											<form:select path="${termPath}.op" title="${title}">
												<form:options items="${numericOperators}" />
											</form:select>
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:input path="${termPath}.value" title="${title}" />
										</td>
									</c:when>
									<c:when test="${row.type == 'String'}">
										<td colspan="2" width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:input path="${termPath}.value" title="${title}" />
										</td>
									</c:when>
									<c:when test="${row.type == 'StringRange'}">
										<td width="1%" class="nowrap">
											<spring:message code="ncd.filter.atleast"/>
											<c:set var="title"><spring:message code="${row.editNameKey}.low.title"/></c:set>
											<form:input path="${termPath}.low" title="${title}" />
										</td>
										<td width="1%" class="nowrap">
											<spring:message code="ncd.filter.atmost"/>
											<c:set var="title"><spring:message code="${row.editNameKey}.high.title"/></c:set>
											<form:input path="${termPath}.high" title="${title}" />
										</td>
									</c:when>
									<c:when test="${row.type == 'Reportable'}">
										<td width="1%">
											=
										</td>
										<td width="1%">
											<c:set var="title"><spring:message code="${row.editNameKey}.value.title"/></c:set>
											<form:select path="${termPath}.value" title="${title}">
												<form:options items="${reportableOps}" />
											</form:select>
										</td>
									</c:when>
									<c:otherwise>
										<td colspan="2" width="1%">
											SearchTerm type "${row.type}" is not yet implemented.
										</td>
									</c:otherwise>
								</c:choose>
								<td width="*">
									<form:errors path="${termPath}" cssClass="error" />
								</td>
							</tr>
						</c:if>
					</c:forEach>
					<tr>
						<td width="1%" class="nowrap">&nbsp;</td>
						<td width="1%" class="nowrap">
							<spring:message code="ncd.filter.rowsperpage"/>
						</td>
						<td colspan="2">
							<c:set var="title"><spring:message code="ncd.filter.rowsperpage.title"/></c:set>
							<form:input path="filter.rowsPerPage" title="${title}" />
						</td>
						<td width="*">
							<form:errors path="filter.rowsPerPage" cssClass="error" />
						</td>
					</tr>
					<tr>
						<td width="1%" class="nowrap">&nbsp;</td>
						<td width="1%" class="nowrap">
							<spring:message code="ncd.filter.maxrows"/>
						</td>
						<td colspan="2">
							<c:set var="title"><spring:message code="ncd.filter.maxrows.title"/></c:set>
							<form:input path="filter.maxRows" title="${title}" />
						</td>
						<td width="*">
							<form:errors path="filter.maxRows" cssClass="error" />
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		<hr/>
		<input type="submit" value="<spring:message code="ncd.buttons.apply"/>" name="filteraction" onclick="return onApply()" />
		<input type="submit" value="<spring:message code="ncd.buttons.clear"/>" name="filteraction" />
		<input type="submit" value="<spring:message code="ncd.buttons.default"/>" name="filteraction" />
		<input type="button" value="<spring:message code="ncd.buttons.cancel"/>" onclick="viewFilter()" />

		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

		<input type="submit" value="<spring:message code="ncd.filter.buttons.updatevisible"/>" name="filteraction" />
		<c:choose>
			<c:when test="${jspFilter.showAll == false}">
				<input type="submit" value="<spring:message code="ncd.filter.buttons.showall"/>" name="filteraction" />
			</c:when>
			<c:when test="${jspFilter.showAll}">
				<input type="submit" value="<spring:message code="ncd.filter.buttons.showvisible"/>" name="filteraction" />
			</c:when>
		</c:choose>
		<input type="submit" value="<spring:message code="ncd.filter.buttons.checkall"/>" name="filteraction" />
		<input type="submit" value="<spring:message code="ncd.filter.buttons.uncheckall"/>" name="filteraction" />
	</div>
</div>

<br/>

<%--
The "apply" submit button cannot just hide filterEdit and show filterView,
because the filter might have errors to display. I also want the page
to stay in the same view when it's refreshed.  
--%>

<c:if test="${editFilter == true}">
<script type="text/javascript">
editFilter();
</script>
</c:if>

<c:if test="${editFilter == false}">
<script type="text/javascript">
viewFilter();
</script>
</c:if>
