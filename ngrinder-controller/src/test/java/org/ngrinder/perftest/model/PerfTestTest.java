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
package org.ngrinder.perftest.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.model.PerfTest;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class PerfTestTest {

	@Test
	public void testGetTargetHostIp() {
		PerfTest test = new PerfTest();
		test.setTargetHosts("aaa.com:1.1.1.1");
		List<String> ipList = test.getTargetHostIP();
		assertThat(ipList.get(0), is("1.1.1.1"));

		test.setTargetHosts(":1.1.1.1");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(0), is("1.1.1.1"));

		test.setTargetHosts("1.1.1.1");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(0), is("1.1.1.1"));

		// multiple hosts
		test.setTargetHosts("aaa.com:1.1.1.1,aaabb.com:1.1.1.2");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(1), is("1.1.1.2"));

		test.setTargetHosts("aaa.com:1.1.1.1,:1.1.1.2");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(1), is("1.1.1.2"));

		test.setTargetHosts("aaa.com:1.1.1.1,1.1.1.2");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(1), is("1.1.1.2"));

	}

	@Test
	public void testAddProgressMessage() {
		PerfTest test = new PerfTest();
		for (int i = 0; i < 1000; i++) {
			test.setLastProgressMessage("HELLO");
			assertThat(test.getProgressMessage().length(),
							lessThan(NGrinderConstants.MAX_STACKTRACE_STRING_SIZE));
		}
	}
	
	
}
