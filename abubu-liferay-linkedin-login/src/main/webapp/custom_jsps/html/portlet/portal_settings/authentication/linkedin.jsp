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

<%@ include file="/html/portlet/portal_settings/init.jsp" %>

<%
boolean linkedinAuthEnabled = PrefsPropsUtil.getBoolean(company.getCompanyId(), "linkedin.auth.enabled", true);
String linkedinAPIKey = PrefsPropsUtil.getString(company.getCompanyId(), "linkedin.api.key");
String linkedinAPISecret = PrefsPropsUtil.getString(company.getCompanyId(), "linkedin.api.secret");
String linkedinCallbackURL = PrefsPropsUtil.getString(company.getCompanyId(), "linkedin.api.callback.url");
%>

<aui:fieldset>
	<aui:input label="enabled" name='<%= "settings--linkedin.auth.enabled--" %>' type="checkbox" value="<%= linkedinAuthEnabled %>" />

	<aui:input cssClass="lfr-input-text-container" label="API Key" name='<%= "settings--linkedin.api.key--" %>' type="text" value="<%= linkedinAPIKey %>" />

	<aui:input cssClass="lfr-input-text-container" label="API Secret" name='<%= "settings--linkedin.api.secret--" %>' type="text" value="<%= linkedinAPISecret %>" />
	
	<aui:input cssClass="lfr-input-text-container"   style="width: 50%;"  label="Redirect URL" name='<%= "settings--linkedin.api.callback.url--" %>' type="text" value="<%= linkedinCallbackURL %>" />
</aui:fieldset>