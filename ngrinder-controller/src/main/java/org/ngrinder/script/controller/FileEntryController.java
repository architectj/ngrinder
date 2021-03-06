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
package org.ngrinder.script.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.ScriptValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * FileEntry manipulation controller.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Controller
@RequestMapping("/script")
public class FileEntryController extends NGrinderBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryController.class);

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private ScriptValidationService scriptValidationService;

	/**
	 * Validate the script.
	 * 
	 * @param user
	 * @param scriptEntry
	 * @return
	 */
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	public @ResponseBody
	String validate(User user, FileEntry scriptEntry,
					@RequestParam(value = "hostString", required = false) String hostString) {
		return scriptValidationService.validateScript(user, scriptEntry, false, hostString);
	}

	/**
	 * Get the list of file entries for the given user.
	 * 
	 * @param user
	 *            current user
	 * @param path
	 *            path looking for.
	 * @param model
	 *            model.
	 * @return script/scriptList
	 */
	@RequestMapping({ "/list/**", "" })
	public String get(User user, @RemainedPath String path, ModelMap model) { // "fileName"

		List<FileEntry> files = fileEntryService.getFileEntries(user, path, null);
		Collections.sort(files, new Comparator<FileEntry>() {
			@Override
			public int compare(FileEntry o1, FileEntry o2) {
				if (o1.getFileType() == FileType.DIR && o2.getFileType() != FileType.DIR) {
					return -1;
				}
				return (o1.getFileName().compareTo(o2.getFileName()));
			}

		});
		model.addAttribute("files", files);
		model.addAttribute("currentPath", path);
		model.addAttribute("svnUrl", fileEntryService.getSvnUrl(user, path));
		return "script/scriptList";
	}

	/**
	 * Add a folder on the given path.
	 * 
	 * @param user
	 *            current user
	 * @param path
	 *            path in which folder will be added
	 * @param folderName
	 *            folderName
	 * @param model
	 *            model.
	 * @return redirect:/script/list/${path}
	 */
	@RequestMapping(value = "/create/**", params = "type=folder", method = RequestMethod.POST)
	public String addFolder(User user, @RemainedPath String path, @RequestParam("folderName") String folderName,
					ModelMap model) { // "fileName"
		try {
			fileEntryService.addFolder(user, path, StringUtils.trimToEmpty(folderName), "");
		} catch (Exception e) {
			return "error/errors";
		}
		return "redirect:/script/list/" + path;
	}

	/**
	 * Provide new file creation form data.
	 * 
	 * @param user
	 *            current user
	 * @param path
	 *            path in which a file will be added
	 * @param testUrl
	 *            url the script may uses
	 * @param fileName
	 *            fileName
	 * @param scriptType
	 *            Type of script. optional
	 * @param model
	 *            model.
	 * @return redirect:/script/list/${path}
	 */
	@RequestMapping(value = "/create/**", params = "type=script", method = RequestMethod.POST)
	public String getCreateForm(User user, @RemainedPath String path,
					@RequestParam(value = "testUrl", required = false) String testUrl,
					@RequestParam("fileName") String fileName,
					@RequestParam(value = "scriptType", required = false) String scriptType, 
					@RequestParam(value = "createLibAndResource", defaultValue = "false") boolean createLibAndResoure,
					ModelMap model) {
		fileName = StringUtils.trimToEmpty(fileName);
		if (StringUtils.isBlank(testUrl)) {
			testUrl = "http://sample.com";
		}
		model.addAttribute("createLibAndResource", createLibAndResoure);
		if (fileEntryService.hasFileEntry(user, path + "/" + fileName)) {
			model.addAttribute("file", fileEntryService.getFileEntry(user, path + "/" + fileName));
		} else {
			model.addAttribute("file", fileEntryService.prepareNewEntry(user, path, fileName, testUrl));
		}
		return "script/scriptEditor";
	}

	/**
	 * Get the details of given path.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            user
	 * @param revision
	 *            revision. -1 if HEAD
	 * @param model
	 *            model
	 * @return script/scriptEditor
	 */
	@RequestMapping("/detail/**")
	public String getDetail(User user, @RemainedPath String path,
					@RequestParam(value = "r", required = false) Long revision, ModelMap model) {
		FileEntry script = fileEntryService.getFileEntry(user, path, revision);
		if (script == null || !script.getFileType().isEditable()) {
			LOG.error("Error while getting file detail on {}. the file does not exist or not editable", path);
			return "redirect:/script/list";
		}
		model.addAttribute("file", script);
		model.addAttribute("ownerId", user.getUserId());
		String targetHosts = script.getProperties().get("targetHosts");
		if (StringUtils.isNotBlank(targetHosts)) {
			model.addAttribute("targetHosts", targetHosts);
		}
		return "script/scriptEditor";
	}

	/**
	 * Download file entry of given path.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            user
	 * @param response
	 *            response
	 */
	@RequestMapping("/download/**")
	public void download(User user, @RemainedPath String path, HttpServletResponse response) {
		FileEntry fileEntry = fileEntryService.getFileEntry(user, path);
		if (fileEntry == null) {
			LOG.error("{} requested to download not existing file entity {}", user.getUserId(), path);
			return;
		}
		response.reset();
		try {
			response.addHeader(
							"Content-Disposition",
							"attachment;filename="
											+ java.net.URLEncoder.encode(FilenameUtils.getName(fileEntry.getPath()),
															"utf8"));
		} catch (UnsupportedEncodingException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		response.setContentType("application/octet-stream; charset=UTF-8");
		response.addHeader("Content-Length", "" + fileEntry.getFileSize());
		byte[] buffer = new byte[4096];
		ByteArrayInputStream fis = null;
		OutputStream toClient = null;
		try {
			fis = new ByteArrayInputStream(fileEntry.getContentBytes());
			toClient = new BufferedOutputStream(response.getOutputStream());
			int readLength;
			while (((readLength = fis.read(buffer)) != -1)) {
				toClient.write(buffer, 0, readLength);
			}
		} catch (IOException e) {
			throw new NGrinderRuntimeException("error while download file", e);
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(toClient);
		}
	}

	/**
	 * Search files on the query.
	 * 
	 * @param user
	 *            user
	 * @param query
	 *            query string
	 * @param model
	 *            model
	 * 
	 * @return script/scriptList
	 */
	@RequestMapping(value = "/search/**")
	public String searchFileEntity(User user, @RequestParam(required = true, value = "query") final String query,
					ModelMap model) {
		Collection<FileEntry> searchResult = Collections2.filter(fileEntryService.getAllFileEntries(user),
						new Predicate<FileEntry>() {
							@Override
							public boolean apply(FileEntry input) {
								return input.getPath().contains(query);
							}
						});
		model.addAttribute("files", searchResult);
		model.addAttribute("currentPath", "");
		return "script/scriptList";
	}

	/**
	 * Save fileEntry and return the the path.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            path to which this will forward.
	 * @param fileEntry
	 *            file to be saved
	 * @param model
	 *            model
	 * @return script/scriptList
	 */
	@RequestMapping(value = "/save/**", method = RequestMethod.POST)
	public String saveFileEntry(User user, @RemainedPath String path, FileEntry fileEntry,
					@RequestParam("targetHosts") String targetHosts,
					@RequestParam(value = "createLibAndResource", defaultValue = "false") boolean createLibAndResource,
					ModelMap model) {
		if (StringUtils.isNotBlank(targetHosts)) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("targetHosts", StringUtils.trim(targetHosts));
			fileEntry.setProperties(map);
		}
		
		fileEntryService.save(user, fileEntry);
		
		String basePath = FilenameUtils.getPath(fileEntry.getPath());
		if (createLibAndResource) {
			fileEntryService.addFolder(user, basePath, "lib", getMessages("script.commit.libfolder"));
			fileEntryService.addFolder(user, basePath, "resources", getMessages("script.commit.resourcefolder"));
		}
		return "redirect:/script/list/" + basePath;
	}

	/**
	 * Upload files.
	 * 
	 * @param user
	 *            path
	 * @param path
	 *            path
	 * @param fileEntry
	 *            fileEntry
	 * @param file
	 *            multipart file
	 * @param model
	 *            model
	 * @return script/scriptList
	 */
	@RequestMapping(value = "/upload/**", method = RequestMethod.POST)
	public String uploadFiles(User user, @RemainedPath String path, @RequestParam("description") String description,
					@RequestParam("uploadFile") MultipartFile file, ModelMap model) {
		try {
			FileEntry fileEntry = new FileEntry();
			if (fileEntry.getFileType().isEditable()) {
				fileEntry.setContent(new String(file.getBytes()));
			} else {
				fileEntry.setContentBytes(file.getBytes());
			}
			fileEntry.setDescription(description);
			fileEntry.setPath(FilenameUtils.separatorsToUnix(FilenameUtils.concat(path, file.getOriginalFilename())));
			fileEntryService.save(user, fileEntry);
			return "redirect:/script/list/" + path;
		} catch (IOException e) {
			LOG.error("Error while getting file content:" + e.getMessage(), e);
			throw new NGrinderRuntimeException("Error while getting file content:" + e.getMessage(), e);
		}
	}

	/**
	 * Delete files on the given path.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            base path
	 * @param filesString
	 *            file list delimited by ","
	 * @param model
	 *            model
	 * @return redirect:/script/list/${path}
	 */
	@RequestMapping(value = "/delete/**", method = RequestMethod.POST)
	public @ResponseBody
	String delete(User user, @RemainedPath String path, @RequestParam("filesString") String filesString, ModelMap model) {
		String[] files = filesString.split(",");
		fileEntryService.delete(user, path, files);
		Map<String, Object> rtnMap = new HashMap<String, Object>(1);
		rtnMap.put(JSON_SUCCESS, true);
		return toJson(rtnMap);
	}

}
