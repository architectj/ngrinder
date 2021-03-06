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
package net.grinder.engine.communication;

import net.grinder.communication.Address;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.messages.console.AgentAddress;

/**
 * NGrinder message to send a log to console.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class LogReportGrinderMessage implements AddressAwareMessage {
	private static final long serialVersionUID = 1274708100107820158L;
	private AgentAddress m_processAddress;
	private final byte[] logs;
	private final String testId;

	/**
	 * Constructor.
	 * @param testId corresponding testid 
	 * @param logs compressed log byte array
	 * @param agentAddress agent address
	 */
	public LogReportGrinderMessage(String testId, byte[] logs, AgentAddress agentAddress) {
		this.testId = testId;
		this.logs = logs;
		setAddress(agentAddress);
	}

	@Override
	public void setAddress(Address address) {
		m_processAddress = (AgentAddress) address;
	}

	public AgentAddress getAddress() {
		return m_processAddress;
	}

	public byte[] getLogs() {
		return logs;
	}

	public String getTestId() {
		return testId;
	}

}
