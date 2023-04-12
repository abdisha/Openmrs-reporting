<%@ include file="/WEB-INF/view/module/reporting/include.jsp"%> <%@ include
file="/WEB-INF/view/module/reporting/includeScripts.jsp"%>

<script type="text/javascript">

 
</script>

<c:forEach var="definitionGroup" items="${model.reportGroups}">
  <openmrs:portlet url="reportListManager" moduleId="reporting" parameters="groupName=${definitionGroup}|numOnPage=15"/> 
</c:forEach>
