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

package com.abubusoft.liferay.twitter;

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

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
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
import com.liferay.portal.kernel.util.Validator;
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

/**
 * @author Sergio González
 */
public class TwitterOAuth extends BaseStrutsAction {

	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(WebKeys.THEME_DISPLAY);

		String cmd = ParamUtil.getString(request, Constants.CMD);
		
		long companyId=themeDisplay.getCompanyId();
		
		String twitterApiKey=PrefsPropsUtil.getString(companyId, "twitter.api.key");
		String twitterApiSecret=PrefsPropsUtil.getString(companyId,"twitter.api.secret");
		String twitterCallbackURL=PrefsPropsUtil.getString(companyId,"twitter.api.callback.url");

		if (cmd.equals("login")) {

			final OAuth10aService service = new ServiceBuilder()
					.apiKey(twitterApiKey).apiSecret(twitterApiSecret)
					.callback(twitterCallbackURL).build(TwitterApi.instance());

			// Obtain the Request Token
			final OAuth1RequestToken requestToken = service.getRequestToken();
			String url = service.getAuthorizationUrl(requestToken);

			response.sendRedirect(url);
		} else if (cmd.equals("token")) {

			HttpSession session = request.getSession();

			String oauthVerifier = ParamUtil.getString(request,
					"oauth_verifier");
			String oauthToken = ParamUtil.getString(request, "oauth_token");

			if (Validator.isNull(oauthVerifier) || Validator.isNull(oauthToken)) {
				return null;
			}
		

			final OAuth10aService service = new ServiceBuilder()
					.apiKey(twitterApiKey).apiSecret(twitterApiSecret)
					.callback(twitterCallbackURL).build(TwitterApi.instance());

			final OAuth1RequestToken requestToken = new OAuth1RequestToken(
					oauthToken, twitterApiSecret);
			final OAuth1AccessToken accessToken = service.getAccessToken(
					requestToken, oauthVerifier);

			Map<String, Object> twitterData = getTwitterData(service,
					accessToken);
			
			User user = getOrCreateUser(themeDisplay.getCompanyId(), twitterData);

			if (user != null) {
				session.setAttribute(TwitterConstants.TWITTER_ID_LOGIN,
						user.getUserId());

				sendLoginRedirect(request, response);

				return null;
			}

			session.setAttribute(TwitterConstants.TWITTER_LOGIN_PENDING,
					Boolean.TRUE);

			sendCreateAccountRedirect(request, response, twitterData);
		}

		return null;
	}
	
	protected void sendLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

		PortletURL portletURL = PortletURLFactoryUtil.create(request, PortletKeys.FAST_LOGIN, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);

		portletURL.setWindowState(LiferayWindowState.POP_UP);
		portletURL.setParameter("struts_action", "/login/login_redirect");

		response.sendRedirect(portletURL.toString());
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getTwitterData(OAuth10aService service,
			OAuth1AccessToken accessToken) throws Exception {
		String verifyCredentialsURL = PropsUtil
				.get("twitter.api.verify.credentials.url");

		OAuthRequest authrequest = new OAuthRequest(Verb.GET,
				verifyCredentialsURL, service);

		service.signRequest(accessToken, authrequest);

		String responseData = authrequest.send().getBody();

		Map<String, Object> jsonResponseMap = new Gson().fromJson(responseData,
				Map.class);

		return jsonResponseMap;
	}
	
	protected void sendCreateAccountRedirect(HttpServletRequest request, HttpServletResponse response, Map<String, Object> data) throws Exception {					
		String[] names = data.get("name").toString().split("\\s+");
		
		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		
		PortletURL redirectURL = PortletURLFactoryUtil.create(request, PortletKeys.FAST_LOGIN, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
		redirectURL.setParameter("struts_action", "/login/login_redirect");
		redirectURL.setParameter("anonymousUser", Boolean.FALSE.toString());
		redirectURL.setPortletMode(PortletMode.VIEW);
		redirectURL.setWindowState(LiferayWindowState.POP_UP);
		
		PortletURL portletURL = PortletURLFactoryUtil.create(request, PortletKeys.LOGIN, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
		portletURL.setParameter("saveLastPath", Boolean.FALSE.toString());
		portletURL.setParameter("struts_action", "/login/create_account");
		portletURL.setParameter("redirect", redirectURL.toString());
		portletURL.setParameter(TwitterConstants.TWITTER_ID_COLUMN_NAME, data.get("id").toString());
		portletURL.setParameter("screenName", data.get("screen_name").toString());
		portletURL.setParameter("firstName", names[0]);
		portletURL.setParameter("lastName", names[1]);
		portletURL.setPortletMode(PortletMode.VIEW);
		portletURL.setWindowState(LiferayWindowState.POP_UP);

		response.sendRedirect(portletURL.toString());
	}

	protected User getUser(long companyId, Map<String, Object> jsonResponse)
			throws Exception {
		ExpandoTable expandoTable = ExpandoTableLocalServiceUtil.getTable(
				companyId, User.class.getName(),
				ExpandoTableConstants.DEFAULT_TABLE_NAME);

		ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.getColumn(
				expandoTable.getTableId(), TwitterConstants.TWITTER_ID_COLUMN_NAME);

		String twitterId = jsonResponse.get("id").toString();

		List<ExpandoValue> expandoValues = ExpandoValueLocalServiceUtil
				.getColumnValues(expandoColumn.getCompanyId(),
						User.class.getName(),
						ExpandoTableConstants.DEFAULT_TABLE_NAME, TwitterConstants.TWITTER_ID_COLUMN_NAME,
						twitterId, QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		int usersCount = expandoValues.size();

		User user = null;

		if (usersCount == 1) {
			ExpandoValue expandoValue = expandoValues.get(0);

			long userId = expandoValue.getClassPK();

			user = UserLocalServiceUtil.getUser(userId);
		} else if (usersCount > 1) {
			throw new Exception(
					"There is more than 1 user with the same Twitter Id");
		}

		return user;
	}

	protected User getOrCreateUser(long companyId,
			Map<String, Object> jsonResponse) throws Exception {

		TwitterUser twitterUser = TwitterUserUtil.convert(jsonResponse);
		User user = getUser(companyId, jsonResponse);

		if (user == null) {
			user = findUserByEmail(companyId, twitterUser);
			// se non esiste dobbiamo refgistrarlo
			if (user==null)
			{
				user = addUser(companyId, twitterUser);
			} else {
				user=updateUser(companyId, twitterUser, user);
			}
			return user;
		}

		return user;
	}
	
	protected User updateUser(long companyId, TwitterUser twitterUser, User user)
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
				TwitterConstants.TWITTER_ID_COLUMN_NAME, user.getUserId(),
				twitterUser.getId());

		return user;

	}
	
	protected User addUser(long companyId, TwitterUser twitterUser)
			throws Exception {

		long facebookId = 0;
		long creatorUserId = 0;
		boolean autoPassword = true;
		String password1 = StringPool.BLANK;
		String password2 = StringPool.BLANK;
		boolean autoScreenName = true;
		String screenName = StringPool.BLANK;
		String emailAddress = twitterUser.getEmailAddress();
		String openId = StringPool.BLANK;
		Locale locale = LocaleUtil.getDefault();
		String firstName = twitterUser.getFirstName();
		String middleName = StringPool.BLANK;
		String lastName = twitterUser.getLastName();
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
				TwitterConstants.TWITTER_ID_COLUMN_NAME, user.getUserId(),
				twitterUser.getId());
		
		//TODO da vedere se rimettere o meno
		//UserLocalServiceUtil.updateAgreedToTermsOfUse(user.getUserId(), true);
		//UserLocalServiceUtil.updateReminderQuery(user.getUserId(), "Digit password", "password");

		return user;

	}
	
	protected User findUserByEmail(long companyId, TwitterUser twitterUser)
			throws Exception {
		User user = UserLocalServiceUtil.fetchUserByEmailAddress(companyId,
				twitterUser.getEmailAddress());

		return user;
	}

}
