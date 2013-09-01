/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.reporting.web.reports.renderers;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.DelimitedTextReportRenderer;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.TsvReportRenderer;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.service.ReportService;

import org.openmrs.web.WebConstants;

@Controller
public class DelimitedTextReportRendererFormController {

	protected static Log log = LogFactory.getLog(DelimitedTextReportRendererFormController.class);

	/**
	 * Default Constructor
	 */
	public DelimitedTextReportRendererFormController() { }

	/**
	 *  prepares a new form for the a DelimitedReportRenderer
	 */
	@RequestMapping("/module/reporting/reports/renderers/delimitedTextReportRenderer")
	public void delimitedTextReportRenderer(ModelMap model, 
								@RequestParam(required=false, value="reportDesignUuid") String reportDesignUuid, 
								@RequestParam(required=false, value="reportDefinitionUuid") String reportDefinitionUuid,
								@RequestParam(required=true,  value="type") Class<? extends DelimitedTextReportRenderer> type,
								@RequestParam(required=false, value="successUrl") String successUrl) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException, InstantiationException, ClassNotFoundException {
		
		ReportService rs = Context.getService(ReportService.class); 
		Map<String, String> configurableProperties = new LinkedHashMap<String, String>();
		ReportDesign design = null;
		if (StringUtils.isNotEmpty(reportDesignUuid)) {
			design = rs.getReportDesignByUuid(reportDesignUuid);
		} else {
			design = new ReportDesign();
			design.setRendererType(type);
			if (StringUtils.isNotEmpty(reportDefinitionUuid)) {
				design.setReportDefinition(Context.getService(ReportDefinitionService.class).getDefinitionByUuid(reportDefinitionUuid));
			}

		}
		
		DelimitedTextReportRenderer rendererType = null;
		if ( TsvReportRenderer.class.isAssignableFrom( design.getRendererType() ) ) {
			rendererType = new TsvReportRenderer();
		} else if ( CsvReportRenderer.class.isAssignableFrom( design.getRendererType() ) ) {
			rendererType = new CsvReportRenderer();
		}
		
		configurableProperties.put("filenameExtension", design.getPropertyValue("filenameExtension", rendererType.getFilenameExtension()));
		configurableProperties.put("beforeColumnDelimiter", 
				rendererType.escapeDelimiter(design.getPropertyValue("beforeColumnDelimiter", rendererType.getBeforeColumnDelimiter())));
		configurableProperties.put("afterColumnDelimiter",  
				rendererType.escapeDelimiter(design.getPropertyValue("afterColumnDelimiter", rendererType.getAfterColumnDelimiter())));
		configurableProperties.put("beforeRowDelimiter",    
				rendererType.escapeDelimiter(design.getPropertyValue("beforeRowDelimiter", rendererType.getBeforeRowDelimiter())));
		configurableProperties.put("afterRowDelimiter",     
				rendererType.escapeDelimiter(design.getPropertyValue("afterRowDelimiter", rendererType.getAfterRowDelimiter())));
		
		String pathToRemove = "/" + WebConstants.WEBAPP_NAME;
    	if (StringUtils.isEmpty(successUrl)) {
    		successUrl = "/module/reporting/reports/manageReportDesigns.form";
    	}
    	else if (successUrl.startsWith(pathToRemove)) {
    		successUrl = successUrl.substring(pathToRemove.length());
    	}
		model.addAttribute("design", design );
		model.addAttribute("configurableProperties", configurableProperties);
		model.addAttribute("successUrl", successUrl);
		model.addAttribute("cancelUrl",  successUrl);

	}

	/**
	 * Saves report design
	 */
	@RequestMapping("/module/reporting/reports/renderers/saveDelimitedTextReportDesign")
	public String saveDelimitedTextReportDesign(ModelMap model, HttpServletRequest request,
					@RequestParam(required=false, value="uuid") String uuid,
					@RequestParam(required=true,  value="name") String name,
					@RequestParam(required=false, value="description") String description,
					@RequestParam(required=true,  value="reportDefinition") String reportDefinitionUuid,
					@RequestParam(required=true,  value="rendererType") Class<? extends DelimitedTextReportRenderer> rendererType,
					@RequestParam(required=false, value="filenameExtension") String filenameExtension,
					@RequestParam(required=false, value="beforeColumnDelimiter") String beforeColumnDelimiter,
					@RequestParam(required=false, value="afterColumnDelimiter") String afterColumnDelimiter,
					@RequestParam(required=false, value="beforeRowDelimiter") String beforeRowDelimiter,
					@RequestParam(required=false, value="afterRowDelimiter") String afterRowDelimiter,
					@RequestParam(required=true,  value="successUrl") String successUrl
	){
		ReportService rs = Context.getService(ReportService.class);
		ReportDesign design = null;
		Properties delimiters = new Properties();

		if (StringUtils.isNotEmpty(uuid)) {
			design = rs.getReportDesignByUuid(uuid);
		}
		if (design == null) {
			design = new ReportDesign();
			design.setRendererType(rendererType);
		}
				
		design.setName(name);
		design.setDescription(description);
		design.setReportDefinition(Context.getService(ReportDefinitionService.class).getDefinitionByUuid(reportDefinitionUuid));
		
		DelimitedTextReportRenderer renderer = null;
		if ( TsvReportRenderer.class.isAssignableFrom( design.getRendererType() ) ) {
			renderer = new TsvReportRenderer();
		} else if ( CsvReportRenderer.class.isAssignableFrom( design.getRendererType() ) ) {
			renderer = new CsvReportRenderer();
		}
		
		if ( !filenameExtension.equals(StringEscapeUtils.escapeJava(renderer.getFilenameExtension()))) {
			delimiters.put("filenameExtension", StringEscapeUtils.escapeJava(filenameExtension));
		}
		
		if ( !beforeColumnDelimiter.equals(StringEscapeUtils.escapeJava(renderer.getBeforeColumnDelimiter()))) {
			delimiters.put("beforeColumnDelimiter", StringEscapeUtils.escapeJava(beforeColumnDelimiter));
		}
		
		if ( !afterColumnDelimiter.equals(StringEscapeUtils.escapeJava(renderer.getAfterColumnDelimiter()))) {
			delimiters.put("afterColumnDelimiter", StringEscapeUtils.escapeJava(afterColumnDelimiter));
		}
		
		if ( !beforeRowDelimiter.equals(StringEscapeUtils.escapeJava(renderer.getBeforeRowDelimiter()))) {
			delimiters.put("beforeRowDelimiter", StringEscapeUtils.escapeJava(beforeRowDelimiter));
		}
		
		if ( !afterRowDelimiter.equals(StringEscapeUtils.escapeJava(renderer.getAfterRowDelimiter()))) {
			delimiters.put("afterRowDelimiter", StringEscapeUtils.escapeJava(afterRowDelimiter));
		}
		
		if ( !delimiters.isEmpty()) {
			design.setProperties(delimiters);
		}
		
		String pathToRemove = "/" + WebConstants.WEBAPP_NAME;
    	if (StringUtils.isEmpty(successUrl)) {
    		successUrl = "/module/reporting/reports/manageReportDesigns.form";
    	}
    	else if (successUrl.startsWith(pathToRemove)) {
    		successUrl = successUrl.substring(pathToRemove.length());
    	}
    	design = rs.saveReportDesign(design);
    	return "redirect:" + successUrl;
	}
}