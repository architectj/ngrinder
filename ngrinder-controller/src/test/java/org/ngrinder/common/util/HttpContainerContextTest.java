/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.common.util;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class HttpContainerContextTest extends AbstractNGrinderTransactionalTest {
	
	@Autowired
	private HttpContainerContext httpContainerContext;

	@Autowired
	private Config config;
	
	@Before
	public void setMockContext() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addHeader("User-Agent", "Win");
		SecurityContextHolderAwareRequestWrapper reqWrapper = new SecurityContextHolderAwareRequestWrapper(req, "U");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(reqWrapper));
	}

	@After
	public void resetContext() {
		RequestContextHolder.resetRequestAttributes();
	}
	

	@Test
	public void testGetCurrentRequestUrlFromUserRequest() {
		String requestUrl = httpContainerContext.getCurrentRequestUrlFromUserRequest();
		assertTrue(requestUrl.startsWith("http://"));

		String httpUrl = config.getSystemProperties().getProperty("http.url", "");
		if (StringUtils.isNotBlank(httpUrl)) {
			config.getSystemProperties().addProperty("http.url", "");
		} else {
			config.getSystemProperties().addProperty("http.url", "http://aa.com");
		}
		requestUrl = httpContainerContext.getCurrentRequestUrlFromUserRequest();
		assertTrue(requestUrl.startsWith("http://"));

		//reset the system properties.
		config.getSystemProperties().addProperty("http.url", requestUrl);
	}

	/**
	 * Test method for {@link org.ngrinder.common.util.HttpContainerContext#isUnixUser()}.
	 */
	@Test
	public void testIsUnixUser() {
		boolean isUnix = httpContainerContext.isUnixUser();
		assertTrue(!isUnix);
	}

}
