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
package org.ngrinder.chart.repository;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;
import org.ngrinder.monitor.controller.domain.MonitorRecorder;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * Monitor data repository.
 * 
 * @author Tobi
 */
@Component
public class MonitorDataRepository implements MonitorRecorder {

	@Autowired
	private SystemMonitorRepository systemMonitorRepository;
	
	@Override
	public void before() {
		// do nothing
	}

	@Override
	public void recoderSystemInfo(String key, SystemInfo systemInfo, MonitorAgentInfo agentInfo) {
		checkNotNull(systemInfo);
		SystemDataModel systemDataModel = new SystemDataModel(systemInfo);
		systemDataModel.setKey(key);
		systemDataModel.setIp(agentInfo.getIp());
		systemDataModel.setPort(agentInfo.getPort());
		systemMonitorRepository.save(systemDataModel);

	}

	@Override
	public void after() {
		// do nothing
	}

}
