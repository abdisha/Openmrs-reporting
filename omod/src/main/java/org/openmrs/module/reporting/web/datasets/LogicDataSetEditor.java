/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reporting.web.datasets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.reporting.common.LogicUtil;
import org.openmrs.module.reporting.dataset.definition.LogicDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.LogicDataSetDefinition.Column;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
public class LogicDataSetEditor {
	
	Log log = LogFactory.getLog(getClass());
	
	@RequestMapping("/module/reporting/datasets/logicDataSetEditor.form")
	public void showDataset(HttpServletRequest request, ModelMap model, @RequestParam(required = false, value = "uuid") String uuid) {
		DataSetDefinitionService svc = Context.getService(DataSetDefinitionService.class);
		LogicDataSetDefinition definition = (LogicDataSetDefinition) svc.getDefinition(uuid, LogicDataSetDefinition.class);
		
		HttpSession httpSession = request.getSession();
		if(ModuleFactory.isModuleStarted("logic")) {
			
		List<String> tokens = new ArrayList<String>(Context.getLogicService().getAllTokens());
		Collections.sort(tokens);
		
		Map<Column, Exception> logicErrors = new HashMap<Column, Exception>();
		for (Column col : definition.getColumns()) {
			try {
				LogicUtil.parse(col.getLogic());
			}
			catch (Exception ex) {
				logicErrors.put(col, ex);
			}
		}
		
		model.addAttribute("definition", definition);
		model.addAttribute("logicErrors", logicErrors);
		model.addAttribute("tokens", tokens);
		model.addAttribute("conceptNameTags", Context.getConceptService().getAllConceptNameTags());
	   }else {
		
		httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "reporting.logicModuleNotStarted.error");
	   }
	}
	
	@RequestMapping("/module/reporting/datasets/logicDataSetEditorSave.form")
	public String saveLogicDataset(ModelMap model, @RequestParam(required = false, value = "uuid") String uuid,
	                               @RequestParam(required = false, value = "name") String name,
	                               @RequestParam(required = false, value = "description") String description,
	                               WebRequest request) {
		DataSetDefinitionService svc = Context.getService(DataSetDefinitionService.class);
		LogicDataSetDefinition definition = uuid == null ? new LogicDataSetDefinition() : (LogicDataSetDefinition) svc
		        .getDefinition(uuid, LogicDataSetDefinition.class);
		
		definition.setName(name);
		definition.setDescription(description);
		
		definition.clearColumns();
		int numColumns = request.getParameterValues("columnLogic").length;
		for (int i = 0; i < numColumns; ++i) {
			String columnName = request.getParameterValues("columnName")[i];
			String columnLabel = request.getParameterValues("columnLabel")[i];
			String columnLogic = request.getParameterValues("columnLogic")[i];
			String columnFormat = request.getParameterValues("columnFormat")[i];
			if (!StringUtils.isBlank(columnName) && !StringUtils.isBlank(columnLogic))
				definition.addColumn(columnName, columnLabel, columnLogic, columnFormat);
		}
		
		try {
			boolean foundInvalidExpression = false;
			ArrayList<String> invalidTokens = null;
			//validate each logic expression
			for (Column col : definition.getColumns()) {
				if (!LogicUtil.isValidLogicExpression(col.getLogic())) {
					foundInvalidExpression = true;
					if (invalidTokens == null)
						invalidTokens = new ArrayList<String>();
					invalidTokens.add(col.getLogic());
				}
			}
			
			if (foundInvalidExpression) {
				String dynamicText = (invalidTokens.size() == 1) ? "value is" : "values are";
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "The following logic expression " + dynamicText
				        + " invalid: " + StringUtils.join(invalidTokens, ", "), WebRequest.SCOPE_SESSION);
			} else
				svc.saveDefinition(definition);
			
		}
		catch (LogicException e) {
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage(), WebRequest.SCOPE_SESSION);
			log.error(e.getMessage(), e);
		}
		
		return "redirect:logicDataSetEditor.form?uuid=" + definition.getUuid();
	}
}
