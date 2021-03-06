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
package net.grinder;

/**
 * The Class GrinderConstants.
 */
public abstract class GrinderConstants {

	/** The Constant P_PROCESS. */
	public static final String P_PROCESS = "process";

	/** The Constant P_THREAD. */
	public static final String P_THREAD = "thread";

	/** Agent daemon retry interval. */
	public static final long AGENT_RETRY_INTERVAL = 1000;

	/** Agent controller HeartBeat interval. */
	public static final long AGENT_CONTROLLER_HEARTBEAT_INTERVAL = 1000;

	/**
	 * Agent controller daemon retry interval.
	 */
	protected static final long AGENT_CONTROLLER_RETRY_INTERVAL = 2000;

	public static final int AGENT_CONTROLLER_FANOUT_STREAM_THREAD_COUNT = 3;
	public static final int AGENT_FANOUT_STREAM_THREAD_COUNT = 3;
	public static final int AGENT_HEARTBEAT_INTERVAL = 1000;
	public static final int AGENT_HEARTBEAT_DELAY = 1000;
	
	
}
