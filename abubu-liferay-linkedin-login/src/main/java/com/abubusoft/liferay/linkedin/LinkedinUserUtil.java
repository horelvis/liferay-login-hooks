package com.abubusoft.liferay.linkedin;

import java.util.Map;

public abstract class LinkedinUserUtil {

	public static LinkedinUser convert(Map<String, Object> data)
	{
		LinkedinUser user=new LinkedinUser();
		user.setId(data.get("id").toString());
		user.setFirstName(data.get("firstName").toString());
		user.setLastName(data.get("lastName").toString());
		user.setEmailAddress(data.get("emailAddress").toString());
		
		return user;
	}
}
