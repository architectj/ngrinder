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

package org.ngrinder.infra.plugin;

import static org.ngrinder.common.util.NoOp.noOp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.model.Home;
import org.ngrinder.extension.OnControllerLifeCycleRunnable;
import org.ngrinder.infra.annotation.RuntimeOnlyComponent;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.service.IPerfTestService;
import org.ngrinder.service.IUserService;
import org.ngrinder.user.service.UserService;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.context.ServletContextAware;

import com.atlassian.plugin.DefaultModuleDescriptorFactory;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.main.AtlassianPlugins;
import com.atlassian.plugin.main.PluginsConfiguration;
import com.atlassian.plugin.main.PluginsConfigurationBuilder;
import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;

/**
 * Plugin manager which is responsible to initialize the plugin infra.<br/>
 * It is built on atlassian plugin framework.
 * 
 * @see https://developer.atlassian.com/display/PLUGINFRAMEWORK/Plugin+Framework
 * @author JunHo Yoon
 * @since 3.0
 */
@RuntimeOnlyComponent
public class PluginManager implements ServletContextAware, NGrinderConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

	private AtlassianPlugins plugins;
	private ServletContext servletContext;

	@Autowired
	private Config config;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private UserService userService;

	/**
	 * Initialize plugin component.
	 */
	@PostConstruct
	public void init() {
		// In case of test mode, no plugin is supported.
		if (isPluginSupportEnabled()) {
			initPluginFramework();
		}
	}

	/**
	 * Initialize Plugin Framework.
	 * 
	 */
	public void initPluginFramework() {
		CoreLogger.LOGGER.info("Initializing Plugin System");
		// Determine which packages to expose to plugins
		DefaultPackageScannerConfiguration scannerConfig = new DefaultPackageScannerConfiguration();
		scannerConfig.setServletContext(servletContext);

		// Expose current packages to the plugins
		scannerConfig.getPackageIncludes().add("org.ngrinder.*");
		scannerConfig.getPackageIncludes().add("org.apache.*");
		scannerConfig.getPackageIncludes().add("org.slf4j.*");
		scannerConfig.getPackageIncludes().add("net.grinder.*");
		scannerConfig.getPackageIncludes().add("javax.servlet.*");
		scannerConfig.getPackageIncludes().add("org.springframework.security.*");
		// Determine which module descriptors, or extension points, to expose.
		DefaultModuleDescriptorFactory modules = new DefaultModuleDescriptorFactory(new DefaultHostContainer());
		initPluginDescriptor(modules, NGRINDER_DEFAULT_PACKAGE);

		// Determine which service objects to expose to plugins
		HostComponentProvider host = new HostComponentProvider() {
			public void provide(ComponentRegistrar reg) {
				reg.register(AuthenticationManager.class).forInstance(authenticationManager);
				reg.register(IUserService.class).forInstance(userService);
				reg.register(IPerfTestService.class).forInstance(perfTestService);
			}
		};
		Home home = config.getHome();

		// Construct the configuration
		PluginsConfiguration config = new PluginsConfigurationBuilder().pluginDirectory(home.getPluginsDirectory())
				.packageScannerConfiguration(scannerConfig).hotDeployPollingFrequency(PLUGIN_UPDATE_FREQUENCY, TimeUnit.SECONDS)
				.hostComponentProvider(host).moduleDescriptorFactory(modules).build();

		// Start the plugin framework
		this.plugins = new AtlassianPlugins(config);
		addPluginUpdateEvent(this);
		plugins.start();
		CoreLogger.LOGGER.info("Plugin System is started.");
	}

	/**
	 * Plugin Framework start listener.
	 * 
	 * @param event
	 *            event
	 */
	@PluginEventListener
	public void onPluginFrameworkStart(PluginFrameworkStartedEvent event) {
		for (OnControllerLifeCycleRunnable runnable : plugins.getPluginAccessor().getEnabledModulesByClass(
				OnControllerLifeCycleRunnable.class)) {
			String ip = "";
			try {
				InetAddress addr = InetAddress.getLocalHost();
				ip = addr.getHostAddress();
			} catch (UnknownHostException e) {
				noOp();
			}
			runnable.start(ip, this.config.getVesion());
		}
	}

	/**
	 * Plugin Framework shutdown listener.
	 * 
	 * @param event
	 *            event
	 */
	@PluginEventListener
	public void onPluginFrameworkShutdown(PluginFrameworkShutdownEvent event) {
		for (OnControllerLifeCycleRunnable runnable : plugins.getPluginAccessor().getEnabledModulesByClass(
				OnControllerLifeCycleRunnable.class)) {
			String ip = "";
			try {
				InetAddress addr = InetAddress.getLocalHost();
				ip = addr.getHostAddress();
			} catch (UnknownHostException e) {
				noOp();
			}
			runnable.finish(ip, this.config.getVesion());
		}
	}

	/**
	 * Check if plugin support is enabled.
	 * 
	 * @return true if plugin is supported.
	 * 
	 */
	protected boolean isPluginSupportEnabled() {
		return config.isPluginSupported();
	}

	/**
	 * Collect all plugin descriptors by scanning.
	 * 
	 * @param modules
	 *            module factory
	 * @param packagename
	 *            the package name from which scan is done.
	 */
	@SuppressWarnings("rawtypes")
	protected void initPluginDescriptor(DefaultModuleDescriptorFactory modules, String packagename) {
		final Reflections reflections = new Reflections(packagename);
		Set<Class<? extends AbstractModuleDescriptor>> pluginDescriptors = reflections.getSubTypesOf(AbstractModuleDescriptor.class);

		for (Class<? extends AbstractModuleDescriptor> pluginDescriptor : pluginDescriptors) {
			PluginDescriptor pluginDescriptorAnnotation = pluginDescriptor.getAnnotation(PluginDescriptor.class);
			if (pluginDescriptorAnnotation == null) {
				LOGGER.error("plugin descriptor " + pluginDescriptor.getName() + " doesn't have PluginDescriptor annotation. Skip..");
			} else if (StringUtils.isEmpty(pluginDescriptorAnnotation.value())) {
				LOGGER.error("plugin descriptor " + pluginDescriptor.getName() + " doesn't have corresponding plugin key. Skip..");
			} else {
				modules.addModuleDescriptor(pluginDescriptorAnnotation.value(), pluginDescriptor);
				LOGGER.info("plugin descriptor {} with {} is initiated.", pluginDescriptor.getName(), pluginDescriptorAnnotation.value());
			}
		}
	}

	/**
	 * Get plugins by module descriptor class.
	 * 
	 * @param <M>
	 *            module type
	 * @param moduleClass
	 *            model type class
	 * @return plugin list
	 */
	public <M> List<M> getEnabledModulesByClass(Class<M> moduleClass) {
		return getEnabledModulesByClass(moduleClass, null);
	}

	/**
	 * Get plugins by module descriptor class.<br/>
	 * This method puts the given default plugin at a head of returned plugin
	 * list.
	 * 
	 * @param <M>
	 *            module type
	 * @param moduleClass
	 *            module class
	 * @param defaultPlugin
	 *            default plugin
	 * @return plugin list
	 */
	public <M> List<M> getEnabledModulesByClass(Class<M> moduleClass, M defaultPlugin) {
		ArrayList<M> pluginClasses = new ArrayList<M>();
		if (defaultPlugin != null) {
			pluginClasses.add(defaultPlugin);
		}
		if (plugins == null) {
			return pluginClasses;
		}
		pluginClasses.addAll(plugins.getPluginAccessor().getEnabledModulesByClass(moduleClass));
		return pluginClasses;
	}

	/**
	 * Stop plugin framework.
	 */
	@PreDestroy
	public void destroy() {
		plugins.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.context.ServletContextAware#setServletContext
	 * (javax.servlet. ServletContext)
	 */
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Add plugin update event.
	 * 
	 * @param listener
	 *            any listener.
	 */
	public void addPluginUpdateEvent(Object listener) {
		this.plugins.getPluginEventManager().register(listener);
	}

}
