package com.abubusoft.liferay.twitter;

import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.struts.StrutsPortletAction;

public class CustomStrutsAction extends BaseStrutsPortletAction {

	public String render(StrutsPortletAction originalStrutsPortletAction,
			PortletConfig portletConfig, RenderRequest renderRequest,
			RenderResponse renderResponse) throws Exception {
/*
		CustomJavaClass customJavaClass = new CustomJavaClass();
		String someResult = customJavaClass.execute();
		renderRequest.setAttribute("someResult", someResult);
*/
		return originalStrutsPortletAction.render(null, portletConfig,
				renderRequest, renderResponse);
	}
}