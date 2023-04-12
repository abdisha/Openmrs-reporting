/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reporting.web.controller.portlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.definition.DefinitionSummary;
import org.openmrs.module.reporting.definition.DefinitionSummaryGroup;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;

public class ReportListPortletController extends ReportingPortletController {

	@Override
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		super.populateModel(request, model);
		String[] reportName;
		List<String> reportGroups = new ArrayList<String>();
		List<DefinitionSummary> definitionSummaries = Context.getService(ReportDefinitionService.class)
				.getAllDefinitionSummaries(false);

		List<DefinitionSummaryGroup> definitionSummaryGroups = new ArrayList<DefinitionSummaryGroup>();
		DefinitionSummaryGroup group;
		boolean foundGroup = false;
		
		
		for (DefinitionSummary definitionSummary : definitionSummaries) {
			for (DefinitionSummaryGroup definitionSummaryGroup : definitionSummaryGroups) {
				if (definitionSummaryGroup.getGroupName().contains(definitionSummary.getName().split("-")[0])) {
					foundGroup = true;
					definitionSummaryGroup.UpdateDefinitionSummary(definitionSummary);
					continue;
				}

			}

			if (!foundGroup) {
				group = new DefinitionSummaryGroup(definitionSummary.getName().split("-")[0]);
				group.UpdateDefinitionSummary(definitionSummary);
				definitionSummaryGroups.add(group);
			}
			reportName=definitionSummary.getName().split("-");

			definitionSummary.setName(reportName.length>1?reportName[1]:reportName[0]);
			foundGroup =false;
		}

		for (DefinitionSummaryGroup definitionSummaryGroup : definitionSummaryGroups) {
			reportGroups.add(definitionSummaryGroup.getGroupName());
			model.put(definitionSummaryGroup.getGroupName(), definitionSummaryGroup.getDefinitionSummaries());
		}
		model.put("reportGroups", reportGroups);
	}

}
