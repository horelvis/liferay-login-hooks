<%@ page import="com.liferay.portal.util.PortalUtil" %>
<%@ include file="/html/portlet/login/init.jsp" %>

<% boolean twitterAuthEnabled = PrefsPropsUtil.getBoolean(company.getCompanyId(), "twitter.auth.enabled", true); %>

<c:if test="<%= twitterAuthEnabled %>">

<%
String twitterAuthURL = PortalUtil.getPathContext() + "/c/portal/twitter_login?cmd=login";
String taglibOpenTwitterLoginWindow = "javascript:var twitterLoginWindow = window.open('" + twitterAuthURL.toString() + "', 'facebook', 'align=center,directories=no,height=560,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no,width=1000'); void(''); twitterLoginWindow.focus();";
%>

<liferay-ui:icon
	src="/html/portlet/login/navigation/twitter.png"
	message="twitter"
	
	url="<%= taglibOpenTwitterLoginWindow %>"
/>

</c:if>