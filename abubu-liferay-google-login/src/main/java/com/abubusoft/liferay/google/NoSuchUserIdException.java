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

package com.abubusoft.liferay.google;

import com.liferay.portal.PortalException;

/**
 * @author Sergio González
 */
public class NoSuchUserIdException extends PortalException {

	public NoSuchUserIdException() {
		super();
	}

	public NoSuchUserIdException(String msg) {
		super(msg);
	}

	public NoSuchUserIdException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public NoSuchUserIdException(Throwable cause) {
		super(cause);
	}

}