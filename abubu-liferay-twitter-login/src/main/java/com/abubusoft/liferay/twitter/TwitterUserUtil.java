package com.abubusoft.liferay.twitter;

import java.util.Map;

public abstract class TwitterUserUtil {

	public static TwitterUser convert(Map<String, Object> data)
	{
		String[] names = data.get("name").toString().split("\\s+");
		
		TwitterUser user=new TwitterUser();
		user.setId(data.get("id").toString());
		user.setFirstName(names[0]);
		user.setLastName(names[1]);
		
		if (data.containsKey("emailAddress"))
		{
			user.setEmailAddress(data.get("emailAddress").toString());
		} else {
			String email=System.currentTimeMillis()+"-twitter@insiel.it";
			user.setEmailAddress(email);
		}
		
		return user;
	}
}
