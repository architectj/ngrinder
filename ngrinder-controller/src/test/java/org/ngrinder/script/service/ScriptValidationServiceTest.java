package org.ngrinder.script.service;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import net.grinder.engine.agent.LocalScriptTestDriveService;
import net.grinder.engine.common.EngineException;
import net.grinder.util.Directory.DirectoryException;
import net.grinder.util.thread.Condition;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.init.ClassPathInit;
import org.ngrinder.infra.init.DBInit;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.repository.MockFileEntityRepsotory;
import org.ngrinder.script.util.CompressionUtil;
import org.ngrinder.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

public class ScriptValidationServiceTest extends AbstractNGrinderTransactionalTest {
	Logger m_logger = LoggerFactory.getLogger(ScriptValidationServiceTest.class);
	@Autowired
	private LocalScriptTestDriveService validationService;

	@Autowired
	private ScriptValidationService scriptValidationService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	public MockFileEntityRepsotory repo;

	@Autowired
	public IUserService userService;

	@Autowired
	public DBInit dbinit;

	@Autowired
	public ClassPathInit classPathInit;
	/**
	 * Locate dumped user1 repo into tempdir
	 * 
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {
		CompressionUtil compressUtil = new CompressionUtil();

		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		compressUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
		repo.setUserRepository(new File(file, getTestUser().getUserId()));
		
	}

	@Test
	public void testValidation() throws EngineException, DirectoryException, IOException {
		File file = new ClassPathResource("/validation/script.py").getFile();
		Condition m_eventSync = new Condition();
		File log = validationService.doValidate(file.getParentFile(), file, m_eventSync, true, "");
		assertThat(log.length(), notNullValue());
	}

	@Test(timeout = 30000)
	public void testInfiniteScriptValidation() throws EngineException, DirectoryException, IOException {
		String script = IOUtils.toString(new ClassPathResource("/validation/script.py").getInputStream());
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/script.py");
		fileEntry.setContent(script);
		String validateScript = scriptValidationService.validateScript(getTestUser(), fileEntry, false, "");
		//assertThat(validateScript, containsString("Validation should be performed within 10sec. Stop it forcely"));
	}

	@Test
	public void testNormalScriptValidation() throws EngineException, DirectoryException, IOException {
		String script = IOUtils.toString(new ClassPathResource("/validation/script2.py").getInputStream());
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/script2.py");
		fileEntry.setContent(script);
		String validateScript = scriptValidationService.validateScript(getTestUser(), fileEntry, false, "");
		assertThat(validateScript, not(containsString("Validation should be performed within 10sec. Stop it forcely")));
		assertThat(validateScript.length(), lessThan(10000));
	}

	@Test
	public void testScriptValidationWithSvnScript() throws EngineException, DirectoryException, IOException {
		String script = IOUtils.toString(new ClassPathResource("/validation/script2.py").getInputStream());
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/script2.py");
		fileEntry.setContent(script);
		fileEntryService.save(getTestUser(), fileEntry);
		fileEntry.setContent("");
		String validateScript = scriptValidationService.validateScript(getTestUser(), fileEntry, true, "");
		assertThat(validateScript, not(containsString("Validation should be performed within 10sec. Stop it forcely")));
		assertThat(validateScript.length(), lessThan(10000));
	}
}
