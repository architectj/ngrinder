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
package net.grinder.util;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 * Grinder classpath optimization class.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class GrinderClassPathUtils {
	/**
	 * Construct classPath for grinder from given classpath string.
	 * 
	 * @param classPath
	 *            classpath string
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public static String filterClassPath(String classPath, Logger logger) {
		List<String> classPathList = new ArrayList<String>();
		for (String eachClassPath : checkNotNull(classPath).split(File.pathSeparator)) {
			String filename = FilenameUtils.getName(eachClassPath);
			if (isNotJarOrUselessJar(filename)) {
				continue;
			}

			logger.trace("classpath :" + eachClassPath);
			classPathList.add(eachClassPath);
		}
		return StringUtils.join(classPathList, File.pathSeparator);
	}
	
	private static final List<String> USEFUL_JAR_LIST = new ArrayList<String>();
	private static final List<String> USELESS_JAR_LIST = new ArrayList<String>();
	static {
		// TODO: If we have need another jar files, we should append it hear
		USEFUL_JAR_LIST.add("dns");
		USEFUL_JAR_LIST.add("grinder");
		USEFUL_JAR_LIST.add("asm");
		USEFUL_JAR_LIST.add("picocontainer");
		USEFUL_JAR_LIST.add("jython");
		USEFUL_JAR_LIST.add("slf4j-api");
		USEFUL_JAR_LIST.add("logback");
		USEFUL_JAR_LIST.add("jsr173");
		USEFUL_JAR_LIST.add("xmlbeans");
		USEFUL_JAR_LIST.add("stax-api");

		USELESS_JAR_LIST.add("ngrinder-core");
		USELESS_JAR_LIST.add("ngrinder-controller");
		USELESS_JAR_LIST.add("spring");
		
	}

	private static boolean isNotJarOrUselessJar(String jarFilename) {
		if (!"jar".equals(FilenameUtils.getExtension(jarFilename))) {
			return true;
		}
		for (String jarName : USELESS_JAR_LIST) {
			if (jarFilename.contains(jarName)) {
				return true;
			}
		}
		for (String jarName : USEFUL_JAR_LIST) {
			if (jarFilename.contains(jarName)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Construct classPath from current classLoader.
	 * 
	 * @param logger
	 *            logger
	 * @return classpath optimized for grinder.
	 */
	public static String buildClasspathBasedOnCurrentClassLoader(Logger logger) {
		URL[] urLs = ((URLClassLoader) GrinderClassPathUtils.class.getClassLoader()).getURLs();
		StringBuilder builder = new StringBuilder();
		for (URL each : urLs) {
			builder.append(each.getFile()).append(File.pathSeparator);
		}
		return GrinderClassPathUtils.filterClassPath(builder.toString(), logger);
	}
}
