/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.abubusoft.liferay.linkedin;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.scribejava.apis.LinkedInApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.struts.BaseStrutsAction;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

public class LinkedinOAuth extends BaseStrutsAction {

	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(WebKeys.THEME_DISPLAY);
		
		long companyId=themeDisplay.getCompanyId();
		
		//String linkedinAuthURL = PropsUtil.get("linkedin.api.auth.url");
		String linkedinApiKey=PrefsPropsUtil.getString(companyId, "linkedin.api.key");
		String linkedinApiSecret=PrefsPropsUtil.getString(companyId,"linkedin.api.secret");
		String linkedinCallbackURL=PrefsPropsUtil.getString(companyId,"linkedin.api.callback.url");

		String cmd = ParamUtil.getString(request, Constants.CMD);

		if (cmd.equals("login")) {
			final OAuth20Service service = new ServiceBuilder()
					.apiKey(linkedinApiKey).apiSecret(linkedinApiSecret)
					.scope("r_basicprofile,r_emailaddress")
					// replace with desired scope
					.callback(linkedinCallbackURL).state("some_params")
					.build(LinkedInApi20.instance());

			// Obtain the Request Token
			String url = service.getAuthorizationUrl();

			response.sendRedirect(url);
		} else if (cmd.equals("token")) {
			final OAuth20Service service = new ServiceBuilder()
					.apiKey(linkedinApiKey).apiSecret(linkedinApiSecret)
					.scope("r_basicprofile,r_emailaddress")
					// replace with desired scope
					.callback(linkedinCallbackURL).state("some_params")
					.build(LinkedInApi20.instance());

			String code = ParamUtil.getString(request, "code");

			final OAuth2AccessToken accessToken = service.getAccessToken(code);
			HttpSession session = request.getSession();

			Map<String, Object> linkedinData = getLinkedinData(service,
					accessToken);

			User user = getOrCreateUser(themeDisplay.getCompanyId(),
					linkedinData);

			if (user != null) {
				session.setAttribute(LinkedinConstants.LINKEDIN_ID_LOGIN,
						user.getUserId());

				sendLoginRedirect(request, response);

				return null;
			}

			session.setAttribute(LinkedinConstants.LINKEDIN_LOGIN_PENDING,
					Boolean.TRUE);

			sendCreateAccountRedirect(request, response, linkedinData);
		}

		return null;
	}

	protected void sendLoginRedirect(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(WebKeys.THEME_DISPLAY);

		PortletURL portletURL = PortletURLFactoryUtil.create(request,
				PortletKeys.FAST_LOGIN, themeDisplay.getPlid(),
				PortletRequest.RENDER_PHASE);

		portletURL.setWindowState(LiferayWindowState.POP_UP);
		portletURL.setParameter("struts_action", "/login/login_redirect");

		response.sendRedirect(portletURL.toString());
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getLinkedinData(OAuth20Service service,
			OAuth2AccessToken accessToken) throws Exception {
		String verifyCredentialsURL = PropsUtil
				.get("linkedin.api.verify.credentials.url");

		OAuthRequest authrequest = new OAuthRequest(Verb.GET,
				verifyCredentialsURL, service);

		service.signRequest(accessToken, authrequest);

		String responseData = authrequest.send().getBody();

		Map<String, Object> jsonResponseMap = new Gson().fromJson(responseData,
				Map.class);

		return jsonResponseMap;
	}

	protected void sendCreateAccountRedirect(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> data)
			throws Exception {
		// String[] names = data.get("name").toString().split("\\s+");
		String[] names = { data.get("firstName").toString(),
				data.get("lastName").toString() };

		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(WebKeys.THEME_DISPLAY);

		PortletURL redirectURL = PortletURLFactoryUtil.create(request,
				PortletKeys.FAST_LOGIN, themeDisplay.getPlid(),
				PortletRequest.RENDER_PHASE);
		redirectURL.setParameter("struts_action", "/login/login_redirect");
		redirectURL.setParameter("anonymousUser", Boolean.FALSE.toString());

		redirectURL.setPortletMode(PortletMode.VIEW);
		redirectURL.setWindowState(LiferayWindowState.POP_UP);

		PortletURL portletURL = PortletURLFactoryUtil.create(request,
				PortletKeys.LOGIN, themeDisplay.getPlid(),
				PortletRequest.RENDER_PHASE);
		portletURL.setParameter("saveLastPath", Boolean.FALSE.toString());
		portletURL.setParameter("struts_action", "/login/create_account");
		portletURL.setParameter("redirect", redirectURL.toString());
		portletURL.setParameter("linkedinId", data.get("id").toString());
		portletURL.setParameter("screenName",
				(names[0] + "." + names[1] + ".linkedin").toLowerCase());
		portletURL.setParameter("firstName", names[0]);
		portletURL.setParameter("lastName", names[1]);
		portletURL.setParameter("emailAddress", data.get("emailAddress")
				.toString());

		portletURL.setPortletMode(PortletMode.VIEW);
		portletURL.setWindowState(LiferayWindowState.POP_UP);

		response.sendRedirect(portletURL.toString());
	}

	protected User getOrCreateUser(long companyId,
			Map<String, Object> jsonResponse) throws Exception {

		LinkedinUser linkedinUser = LinkedinUserUtil.convert(jsonResponse);
		User user = getUser(companyId, jsonResponse);

		if (user == null) {
			user = findUserByEmail(companyId, linkedinUser);
			// se non esiste dobbiamo refgistrarlo
			if (user==null)
			{
				user = addUser(companyId, linkedinUser);
			} else {
				user=updateUser(companyId, linkedinUser, user);
			}
			return user;
		}

		return user;
	}

	protected User findUserByEmail(long companyId, LinkedinUser linkedinUser)
			throws Exception {
		User user = UserLocalServiceUtil.fetchUserByEmailAddress(companyId,
				linkedinUser.getEmailAddress());

		return user;
	}
	
	protected User updateUser(long companyId, LinkedinUser linkedinUser, User user)
			throws Exception {		
		user = UserLocalServiceUtil.updateUser(user);
		
		user = UserLocalServiceUtil.updateLastLogin(user.getUserId(),
				user.getLoginIP());

		user = UserLocalServiceUtil
				.updatePasswordReset(user.getUserId(), false);

		user = UserLocalServiceUtil.updateEmailAddressVerified(
				user.getUserId(), false);
		
		ExpandoValueLocalServiceUtil.addValue(companyId, User.class.getName(),
				ExpandoTableConstants.DEFAULT_TABLE_NAME,
				LinkedinConstants.LINKEDIN_ID_COLUMN_NAME, user.getUserId(),
				linkedinUser.getId());
		
		// TODO da vedere se rimettere o meno
		//UserLocalServiceUtil.updateAgreedToTermsOfUse(user.getUserId(), true);
		//UserLocalServiceUtil.updateReminderQuery(user.getUserId(), "Digit password", "password");

		return user;

	}
	
	

	protected User addUser(long companyId, LinkedinUser linkedinUser)
			throws Exception {

		long facebookId = 0;
		long creatorUserId = 0;
		boolean autoPassword = true;
		String password1 = StringPool.BLANK;
		String password2 = StringPool.BLANK;
		boolean autoScreenName = true;
		String screenName = StringPool.BLANK;
		String emailAddress = linkedinUser.getEmailAddress();
		String openId = StringPool.BLANK;
		Locale locale = LocaleUtil.getDefault();
		String firstName = linkedinUser.getFirstName();
		String middleName = StringPool.BLANK;
		String lastName = linkedinUser.getLastName();
		boolean sendEmail = false;
		int prefixId = 0;
		int suffixId = 0;
		boolean male = true;
		int birthdayMonth = Calendar.JANUARY;
		int birthdayDay = 1;
		int birthdayYear = 1970;
		String jobTitle = StringPool.BLANK;
		long[] groupIds = null;
		long[] organizationIds = null;
		long[] roleIds = null;
		long[] userGroupIds = null;

		ServiceContext serviceContext = new ServiceContext();

		User user = UserLocalServiceUtil.addUser(creatorUserId, companyId,
				autoPassword, password1, password2, autoScreenName, screenName,
				emailAddress, facebookId, openId, locale, firstName,
				middleName, lastName, prefixId, suffixId, male, birthdayMonth,
				birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds,
				roleIds, userGroupIds, sendEmail, serviceContext);

		user = UserLocalServiceUtil.updateLastLogin(user.getUserId(),
				user.getLoginIP());

		user = UserLocalServiceUtil
				.updatePasswordReset(user.getUserId(), false);

		user = UserLocalServiceUtil.updateEmailAddressVerified(
				user.getUserId(), false);

		ExpandoValueLocalServiceUtil.addValue(companyId, User.class.getName(),
				ExpandoTableConstants.DEFAULT_TABLE_NAME,
				LinkedinConstants.LINKEDIN_ID_COLUMN_NAME, user.getUserId(),
				linkedinUser.getId());
		
		// TODO da vedere se rimettere o customizzare  
		//UserLocalServiceUtil.updateAgreedToTermsOfUse(user.getUserId(), true);
		//UserLocalServiceUtil.updateReminderQuery(user.getUserId(), "Digit password", "password");

		return user;

	}

	protected User getUser(long companyId, Map<String, Object> jsonResponse)
			throws Exception {
		ExpandoTable expandoTable = ExpandoTableLocalServiceUtil.getTable(
				companyId, User.class.getName(),
				ExpandoTableConstants.DEFAULT_TABLE_NAME);

		ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.getColumn(
				expandoTable.getTableId(),
				LinkedinConstants.LINKEDIN_ID_COLUMN_NAME);

		String linkedinId = jsonResponse.get("id").toString();

		List<ExpandoValue> expandoValues = ExpandoValueLocalServiceUtil
				.getColumnValues(expandoColumn.getCompanyId(),
						User.class.getName(),
						ExpandoTableConstants.DEFAULT_TABLE_NAME,
						LinkedinConstants.LINKEDIN_ID_COLUMN_NAME, linkedinId,
						QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		int usersCount = expandoValues.size();

		User user = null;

		if (usersCount == 1) {
			ExpandoValue expandoValue = expandoValues.get(0);

			long userId = expandoValue.getClassPK();

			user = UserLocalServiceUtil.getUser(userId);
		} else if (usersCount > 1) {
			throw new Exception(
					"There is more than 1 user with the same Linkedin Id");
		}

		return user;
	}

}
