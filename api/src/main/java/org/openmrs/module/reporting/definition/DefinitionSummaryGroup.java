package org.openmrs.module.reporting.definition;

import java.util.ArrayList;
import java.util.List;


public class DefinitionSummaryGroup {
    private String groupName;
    public String getGroupName() {
        return groupName;
    }

    private List<DefinitionSummary> definitionSummaries = new ArrayList<DefinitionSummary>();

    
    public List<DefinitionSummary> getDefinitionSummaries() {
        return definitionSummaries;
    }

    public DefinitionSummaryGroup(String groupName) {
       this.groupName = groupName;
    }
    
    public void UpdateDefinitionSummary(DefinitionSummary definitionSummary)
    {
        definitionSummaries.add(definitionSummary);
    }
}
