<%--
/**
 * Copyright (c) 2000-2015 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@page import="com.liferay.portal.model.Company"%>
<%@ include file="/html/portlet/portal_settings/init.jsp" %>


<%
boolean googleAuthEnabled = PrefsPropsUtil.getBoolean(company.getCompanyId(), "google.auth.enabled", true);
String googleAPIKey = PrefsPropsUtil.getString(company.getCompanyId(), "google.api.key");
String googleAPISecret = PrefsPropsUtil.getString(company.getCompanyId(), "google.api.secret");
String googleCallbackURL = PrefsPropsUtil.getString(company.getCompanyId(), "google.api.callback.url");
%>

<aui:fieldset>
	<aui:input label="enabled" name='<%= "settings--google.auth.enabled--" %>' type="checkbox" value="<%= googleAuthEnabled %>" />

	<aui:input cssClass="lfr-input-text-container" label="API Key" name='<%= "settings--google.api.key--" %>' type="text" value="<%= googleAPIKey %>" />

	<aui:input cssClass="lfr-input-text-container" label="API Secret" name='<%= "settings--google.api.secret--" %>' type="text" value="<%= googleAPISecret %>" />
	
	<aui:input cssClass="lfr-input-text-container"  style="width: 50%;" label="Redirect URL" name='<%= "settings--google.api.callback.url--" %>' type="text" value="<%= googleCallbackURL %>" />
</aui:fieldset>

