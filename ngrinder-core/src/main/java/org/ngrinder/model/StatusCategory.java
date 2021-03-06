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
package org.ngrinder.model;

/**
 * Category of {@link Status}. This class provides the characterisic of each status.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum StatusCategory {
	/**
	 * Ready to run..
	 */
	PREPARE("blue.png", false, true),
	/**
	 * Processing.
	 */
	PROGRESSING("blue_anime.gif", true, false),
	/**
	 * Testing..
	 */
	TESTING("green_anime.gif", true, false),
	/**
	 * Finished normally.
	 */
	FINISHED("green.png", false, true),
	/**
	 * Stopped by error .
	 */
	ERROR("red.png", false, true),
	/**
	 * Stopped by user.
	 */
	STOP("grey.png", false, true);

	private final boolean stoppable;
	private final boolean deletable;
	private final String iconName; 

	StatusCategory(String iconName, boolean stoppable, boolean deletable) {
		this.iconName = iconName;
		this.stoppable = stoppable;
		this.deletable = deletable;
	}

	public boolean isStoppable() {
		return stoppable;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public String getIconName() {
		return iconName;
	}
}
