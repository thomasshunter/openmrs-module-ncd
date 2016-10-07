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

<openmrs:require privilege="View NCD Reportable Results" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.reportableresultdetail.linkname" /></h2>

<!-- TODO: add sent in error flag support -->

<script type="text/javascript">

function onRelease(f) {

	if (confirm("Are you sure you want to release this reportable result? Click OK if you are sure you want to release it.")) {
		return true;
	}
	else {
		return false;
	}
}

function onReject(f) {

	if (confirm("Are you sure you want to reject this reportable result? Click OK if you are sure you want to reject it.")) {
		return true;
	}
	else {
		return false;
	}
}

function onSentInError(f) {

	if (confirm("Are you sure you want to mark this reportable result as sent in error? Click OK if you are sure you want to do this.")) {
		return true;
	}
	else {
		return false;
	}
}

function onNotSentInError(f) {

	if (confirm("Are you sure you want to mark this reportable result as not sent in error? Click OK if you are sure you want to do this.")) {
		return true;
	}
	else {
		return false;
	}
}

</script>

<form:form commandName="getNcdReportableResultDetailObj">
	<br/>
	<div class="scrollable" style="height:600px">
		<b class="boxHeader"><spring:message code="ncd.pages.reportableresultdetail.section.result"/></b>
		<table class="box">
		    <tbody>
		    	<tr>
		    		<td width="1">
		    			<spring:message code="ncd.pages.reportableresultdetail.id"/>
		    		</td>
		    		<td width="*">
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.id.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.id" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.tblreportableresult"/>
		    		</td>
		    		<td>
		    			<c:if test="${getNcdReportableResultDetailObj.result.previousReportableResult != null}">
							<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.tblreportableresult.title"/></c:set>
			    			<a title="${title}" href="reportableResultDetail.form?edit=${getNcdReportableResultDetailObj.result.previousReportableResult.id}">
			    				<ncd:write path="result.previousReportableResult.id" />
	    					</a>
	   					</c:if>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.conditionname"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.conditionname.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			        		<c:choose>
								<c:when test="${getNcdReportableResultDetailObj.result.manualReviewStatusType.reviewStatus == 'Hold' && getNcdReportableResultDetailObj.result.conditionName == 'Unknown'}">
									<form:select path="result.conditionName">
										<form:options items="${allConditionNames}"/>
									</form:select>
			        			</c:when>
			        			<c:otherwise>
					    			<ncd:write path="result.conditionName" />
			        			</c:otherwise>
		        			</c:choose>
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.indicatingcritic"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.indicatingcritic.title"/></c:set>
						<c:choose>
							<c:when test="${getNcdReportableResultDetailObj.result.critic != null}">
			        			<a title="${title}" class="unmarked">
				    				<ncd:write path="result.critic.name" />
		    					</a>
							</c:when>
							<c:otherwise>
								<spring:message code="ncd.pages.reportableresultdetail.indicatingcritic.notrecorded"/>
							</c:otherwise>
						</c:choose>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obraltcode"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obraltcode.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obrAltCode" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obraltcodetext"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obraltcodetext.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obrAltCodeText" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obraltcodesys"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obraltcodesys.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obrAltCodeSystem" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obxaltcode"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obxaltcode.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obxAltCode" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obxaltcodetext"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obxaltcodetext.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obxAltCodeText" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obxaltcodesys"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obxaltcodesys.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obxAltCodeSystem" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.resultSeq"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.resultSeq.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.resultSeq" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obrSetId"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obrSetId.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obrSetId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obxStartSetId"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obxStartSetId.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obxStartSetId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.obxEndSetId"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.obxEndSetId.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.obxEndSetId" />
		    			</a>
		    		</td>
		    	</tr>
<%--
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.sentinerror"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.sentInError.title"/></c:set>
		    			<input type="checkbox" name="sentInError" value="false" title="${title}" />
		    		</td>
		    	</tr>
--%>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.manualreviewstatus"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.manualreviewstatus.title"/></c:set>
			        	<a title="${title}" class="unmarked">
							<ncd:write path="result.manualReviewStatusType.reviewStatus"/>
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.releasedate"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.releasedate.title"/></c:set>
			        	<a title="${title}" class="unmarked">
							<c:choose>
								<c:when test="${getNcdReportableResultDetailObj.result.releaseDate != null}">
					    			<ncd:write value="${getNcdReportableResultDetailObj.result.releaseDate}" type="datetime" />
								</c:when>
								<c:otherwise>
									<spring:message code="ncd.pages.reportableresultdetail.releasedate.notreleased"/>
								</c:otherwise>
							</c:choose>
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.sentinerror"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.sentinerror.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write value="${getNcdReportableResultDetailObj.result.sentInError}" />
		    			</a>
		    		</td>
		    	</tr>
 			</tbody>
		</table>
		<br/>
		<b class="boxHeader"><spring:message code="ncd.pages.reportableresultdetail.section.message"/></b>
		<table class="box">
		    <tbody>
		    	<tr>
		    		<td width="1">
		    			<spring:message code="ncd.pages.reportableresultdetail.messagereceiveddatetime"/>
		    		</td>
		    		<td width="*">
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.messagereceiveddatetime.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.messageReceivedDateTime" type="datetime" />
						</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.mpqseqnumber"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.mpqseqnumber.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.mpqSeqNumber" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.tblinstitution"/>
		    		</td>
		    		<td>
		    			<c:if test="${getNcdReportableResultDetailObj.result.institution != null}">
							<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.tblinstitution.title"/></c:set>
				        	<a title="${title}" class="unmarked">
				    			<ncd:write path="result.institution.name" />
			    			</a>
			    		</c:if>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.institutionidtype"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.institutionidtype.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.institutionIdType" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.application"/>
		    		</td>
		    		<td>
		    			<c:if test="${getNcdReportableResultDetailObj.result.sendingApplication != null}">
							<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.application.title"/></c:set>
				        	<a title="${title}" class="unmarked">
				    			<ncd:write path="result.sendingApplication" />
			    			</a>
		    			</c:if>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.facility"/>
		    		</td>
		    		<td>
		    			<c:if test="${getNcdReportableResultDetailObj.result.sendingFacility != null}">
							<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.facility.title"/></c:set>
				        	<a title="${title}" class="unmarked">
				    			<ncd:write path="result.sendingFacility" />
			    			</a>
		    			</c:if>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.location"/>
		    		</td>
		    		<td>
		    			<c:if test="${getNcdReportableResultDetailObj.result.sendingLocation != null}">
							<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.location.title"/></c:set>
				        	<a title="${title}" class="unmarked">
				    			<ncd:write path="result.sendingLocation" />
			    			</a>
		    			</c:if>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.facilityid"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.facilityid.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.facilityId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.tblloinccode"/>
		    		</td>
		    		<td>
		    			<c:if test="${getNcdReportableResultDetailObj.result.code != null}">
							<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.tblloinccode.title"/></c:set>
				        	<a title="${title}" class="unmarked">
				    			<ncd:write path="result.code.code" />
			    			</a>
		    			</c:if>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.tblrawhl7"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.tblrawhl7.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.rawMessage.messageText" type="hl7" />
		    			</a>
		    		</td>
		    	</tr>
			</tbody>
		</table>
		<br/>
		<b class="boxHeader"><spring:message code="ncd.pages.reportableresultdetail.section.patient"/></b>
		<table class="box">
		    <tbody>
		    	<tr>
		    		<td width="1">
		    			<spring:message code="ncd.pages.reportableresultdetail.patientinstitutionmedicalrecordid"/>
		    		</td>
		    		<td width="*">
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientinstitutionmedicalrecordid.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientInstitutionMedicalRecordId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.globalpatientid"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.globalpatientid.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.globalPatientId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientssn"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientssn.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientSSN" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientname"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientname.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientName" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientbirth"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientbirth.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientBirth" type="date" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientrace"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientrace.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientRace" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientsex"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientsex.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientSex" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientphone"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientphone.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientPhone" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientstreet1"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientstreet1.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientStreet1" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientstreet2"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientstreet2.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientStreet2" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientcity"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientcity.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientCity" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientcounty"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientcounty.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientCounty" />
		    			</a>	
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientstate"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientstate.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientState" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientzip"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientzip.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientZip" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.patientcountry"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.patientcountry.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.patientCountry" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.inferredcounty"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.inferredcounty.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.county" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.inferredjurisdiction"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.inferredjurisdiction.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.jurisdiction" />
		    			</a>
		    		</td>
		    	</tr>
			</tbody>
		</table>
		<br/>
		<b class="boxHeader"><spring:message code="ncd.pages.reportableresultdetail.section.provider"/></b>
		<table class="box">
		    <tbody>
		    	<tr>
		    		<td width="1">
		    			<spring:message code="ncd.pages.reportableresultdetail.providername"/>
		    		</td>
		    		<td width="*">
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providername.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerName" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providernamesource"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providernamesource.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerNameSource" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providernamematched"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providernamematched.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerNameMatched" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerssn"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerssn.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerSSN" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerbirth"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerbirth.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerBirth" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerpractice"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerpractice.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerPractice" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerstreet"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerstreet.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerStreet" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providercity"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providercity.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerCity" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerstate"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerstate.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerState" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerzip"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerzip.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerZip" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providercounty"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providercounty.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerCounty" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.provideraddresssource"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.provideraddresssource.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerAddressSource" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerphone"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerphone.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerPhone" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerlocalid"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerlocalid.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerLocalId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerlocalidsource"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerlocalidsource.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerLocalIdSource" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerdeanumber"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerdeanumber.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerDEANumber" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.providerlicense"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.providerlicense.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.providerLicense" />
		    			</a>
		    		</td>
		    	</tr>
			</tbody>
		</table>
		<br/>
		<b class="boxHeader"><spring:message code="ncd.pages.reportableresultdetail.section.lab"/></b>
		<table class="box">
		    <tbody>
		    	<tr>
		    		<td width="1">
		    			<spring:message code="ncd.pages.reportableresultdetail.labname"/>
		    		</td>
		    		<td width="*">
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.labname.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.labName" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.labid"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.labid.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.labId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.labphone"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.labphone.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.labPhone" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.labstreet1"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.labstreet1.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.labStreet1" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.labstreet2"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.labstreet2.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.labStreet2" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.labcity"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.labcity.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.labCity" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.labstate"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.labstate.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.labState" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.labzip"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.labzip.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.labZip" />
		    			</a>
		    		</td>
		    	</tr>
			</tbody>
		</table>
		<br/>
		<b class="boxHeader"><spring:message code="ncd.pages.reportableresultdetail.section.test"/></b>
		<table class="box">
		    <tbody>
		    	<tr>
		    		<td width="1">
		    			<spring:message code="ncd.pages.reportableresultdetail.testid"/>
		    		</td>
		    		<td width="*">
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testid.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testname"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testname.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testName" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testcodesys"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testcodesys.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testCodeSystem" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testplacerordernum"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testplacerordernum.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testPlacerOrderNum" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testplacerordernumsource"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testplacerordernumsource.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testPlacerOrderNumSource" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testfillerordernum"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testfillerordernum.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testFillerOrderNum" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testfillerordernumsource"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testfillerordernumsource.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testFillerOrderNumSource" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testdate"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testdate.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testDate" type="datetime" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testparentplacer"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testparentplacer.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testParentPlacer" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testparentfiller"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testparentfiller.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testParentFiller" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testspecimentext"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testspecimentext.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testSpecimenText" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testdatatype"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testdatatype.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testDataType" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testnormalrange"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testnormalrange.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testNormalRange" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testabnormalflag"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testabnormalflag.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testAbnormalFlag" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testcomment"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testcomment.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testComment" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testresultid"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testresultid.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testResultId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testresultname"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testresultname.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testResultName" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testresultcodesys"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testresultcodesys.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testResultCodeSystem" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testresultsubid"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testresultsubid.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testResultSubId" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testresultcode"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testresultcode.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testResultCode" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testresultvalue"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testresultvalue.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testResultValue" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testresultunits"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testresultunits.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testResultUnits" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testpreviousdate"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testpreviousdate.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			        		<c:if test="${result.testPreviousDate != null}">
				    			<ncd:write path="result.testPreviousDate" type="datetime" />
							</c:if>
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testdatesource"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testdatesource.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testDateSource" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.reportableresultdetail.testresultstatus"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.reportableresultdetail.testresultstatus.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="result.testResultStatus" />
		    			</a>
		    		</td>
		    	</tr>
			</tbody>
		</table>
	</div>
	<hr/>
	<input type="submit" value="<spring:message code="ncd.buttons.close"/>" name="action" />
	<c:if test="${getNcdReportableResultDetailObj.result.manualReviewStatusType.reviewStatus == 'Hold'}">
		<input type="submit" value="<spring:message code="ncd.pages.reportableresultdetail.buttons.release"/>" name="action"  onclick="return onRelease()">
		<input type="submit" value="<spring:message code="ncd.pages.reportableresultdetail.buttons.reject"/>" name="action"  onclick="return onReject()">
	</c:if>
	<c:if test="${getNcdReportableResultDetailObj.result.manualReviewStatusType.reviewStatus != 'Hold'}">
		<c:if test="${getNcdReportableResultDetailObj.result.sentInError == true}">
			<input type="submit" value="<spring:message code="ncd.pages.reportableresultdetail.buttons.notsentinerror"/>" name="action"  onclick="return onNotSentInError()">
		</c:if>
		<c:if test="${getNcdReportableResultDetailObj.result.sentInError == false}">
			<input type="submit" value="<spring:message code="ncd.pages.reportableresultdetail.buttons.sentinerror"/>" name="action"  onclick="return onSentInError()">
		</c:if>
	</c:if>
	<c:if test="${getNcdReportableResultDetailObj.showNavigationButtons == true}">
		<input type="submit" value="<spring:message code="ncd.buttons.first"/>" name="action"/>
		<input type="submit" value="<spring:message code="ncd.buttons.previous"/>" name="action"/>
		<input type="submit" value="<spring:message code="ncd.buttons.next"/>" name="action"/>
		<input type="submit" value="<spring:message code="ncd.buttons.last"/>" name="action"/>
	</c:if>
</form:form>
 
<%@ include file="/WEB-INF/template/footer.jsp"%>
