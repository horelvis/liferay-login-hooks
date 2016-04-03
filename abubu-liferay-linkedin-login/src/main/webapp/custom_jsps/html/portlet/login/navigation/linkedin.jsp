<%@ page import="com.liferay.portal.util.PortalUtil" %>
<%@ include file="/html/portlet/login/init.jsp" %>

<% boolean linkedinAuthEnabled = PrefsPropsUtil.getBoolean(company.getCompanyId(), "linkedin.auth.enabled", true); %>
<c:if test="<%= linkedinAuthEnabled %>">

<%
String linkedinAuthURL = PortalUtil.getPathContext() + "/c/portal/linkedin_login?cmd=login";

String taglibOpenLinkedinLoginWindow = "javascript:var linkedinLoginWindow = window.open('" + linkedinAuthURL.toString() + "', 'facebook', 'align=center,directories=no,height=560,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no,width=1000'); void(''); linkedinLoginWindow.focus();";
%>

<liferay-ui:icon
	src="/html/portlet/login/navigation/linkedin.png"
	message="linkedin"
	url="<%= taglibOpenLinkedinLoginWindow %>"
/>

</c:if>