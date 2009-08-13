package org.openmrs.module.report.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.module.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.cohort.definition.CohortDefinition;
import org.openmrs.module.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.cohort.definition.LocationCohortDefinition;
import org.openmrs.module.dataset.definition.CohortDataSetDefinition;
import org.openmrs.module.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.dataset.definition.DataSetDefinition;
import org.openmrs.module.evaluation.parameter.Mapped;
import org.openmrs.module.evaluation.parameter.Parameter;
import org.openmrs.module.indicator.CohortIndicator;
import org.openmrs.module.indicator.aggregation.CountAggregator;
import org.openmrs.module.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.report.ReportDefinition;
import org.openmrs.util.OpenmrsUtil;

/**
 * Mock Implementation of the ReportService API
 */
public class MockReportService extends BaseReportService implements ReportService {

	private transient Log log = LogFactory.getLog(this.getClass());
	
	private List<ReportDefinition> reportDefinitions = new ArrayList<ReportDefinition>();
	
	/**
	 * Default constructor
	 */
	public MockReportService() { 
		initializeService();
	}

	/**
	 * @see ReportService#saveReportDefinition(ReportDefinition)
	 */
	public ReportDefinition saveReportDefinition(ReportDefinition reportDefinition) throws APIException {		
		// Just assumes it's new
		reportDefinitions.add(reportDefinition);
		serializeReportDefinitions();		
		return reportDefinition;
	}
	
	/**
	 * @see ReportService#getReportDefinition(Integer)
	 */
	public ReportDefinition getReportDefinition(Integer reportDefinitionId) throws APIException {
		for (ReportDefinition reportDefinition : reportDefinitions) { 
			if (reportDefinition.getId().equals(reportDefinitionId)) { 
				return reportDefinition;				
			}
		}
		return new ReportDefinition();
	}

	/**
	 * @see ReportService#getReportDefinitionByUuid(String)
	 */
	public ReportDefinition getReportDefinitionByUuid(String uuid) throws APIException {
		for (ReportDefinition reportDefinition : reportDefinitions) { 
			if (reportDefinition.getUuid().equals(uuid)) { 
				return reportDefinition;
			}
		}
		return new ReportDefinition();
	}
	
	/**
	 * @see ReportService#getReportDefinitions()
	 */
	public List<ReportDefinition> getReportDefinitions() throws APIException {
		return getReportDefinitions(false);
	}
	
	/**
	 * @see ReportService#getReportDefinitions(boolean)
	 */
	public List<ReportDefinition> getReportDefinitions(boolean includeRetired) throws APIException {
		List<ReportDefinition> ret = new ArrayList<ReportDefinition>();
		if (reportDefinitions != null) {
			for (ReportDefinition r : reportDefinitions) {
				if (includeRetired || !r.isRetired()) {
					ret.add(r);
				}
			}
		}
		return ret;
	}
	
	/**
	 * @see ReportService#deleteReportDefinition(ReportDefinition)
	 */
	public void deleteReportDefinition(ReportDefinition reportDefinition) {
		throw new APIException("not implemented yet");
	}	
	
	/**
	 * Initializes the service by de-serializing report schemas from the filesystem.
	 * 
	 * @throws Exception
	 */
	public void initializeService() { 		
		reportDefinitions.add(this.getCohortReportDefinition());
		reportDefinitions.add(this.getIndicatorReportDefinition());
		this.serializeReportDefinitions();
	}

	
	
	
	public void deserializeReportDefinitions() { 
		try { 			
			File directory = 
				OpenmrsUtil.getDirectoryInApplicationDataDirectory("reports/schemas");
			
			// Iterate over the 
			for (String filename : directory.list()) { 			
				log.info("filename: " + filename);
				
				String contents = null;
				if (filename.endsWith(".ser")) {
					contents = OpenmrsUtil.getFileAsString(new File(directory, filename));
					log.info("Xml report schema: " + contents);
					reportDefinitions.add( deserializeReportDefinition(new File(directory, filename)) );
				}
				
			}
		} catch (Exception e) { 
			log.error("Unable to de-serialize report schemas from the fileystem", e);
		}		
	}

	public void serializeReportDefinitions() { 		
		try { 
			
			File directory = 
				OpenmrsUtil.getDirectoryInApplicationDataDirectory("reports/schemas");			
			
			for (ReportDefinition schema : reportDefinitions) { 
				serializeReportDefinition(directory, schema);
			}			
		} catch (Exception e) { 
			log.error("Unable to serialize report schemas to the filesystem", e);
		}
	}
	
	
	public void serializeReportDefinition(File directory, ReportDefinition reportDefinitionObj) throws Exception { 		
		ObjectOutputStream oos = null;
		try { 
			File dest = new File(directory, reportDefinitionObj.getName() + ".ser");			
			FileOutputStream fos = new FileOutputStream(dest);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(reportDefinitionObj);
		} catch (Exception e) { 
			log.error("Error serializing report schema", e);			
		} finally { 
			oos.flush();
			oos.close();
		}
	}
	
	public ReportDefinition deserializeReportDefinition(File reportDefinitionFile) throws Exception { 
		ObjectInputStream ois = null;
		try { 
			FileInputStream fis = new FileInputStream(reportDefinitionFile);
			ois = new ObjectInputStream(fis);
			return (ReportDefinition)ois.readObject();
		} catch (Exception e) { 
			log.error("Error deserializing report schema", e);			
		} finally { 
			ois.close();
		}
		return new ReportDefinition();
	}

	
	/**
	 * Gets a simple cohort report schema.
	 * @return
	 */
	public ReportDefinition getCohortReportDefinition() { 
		// Add a very basic cohort report to the report schemas
		AgeCohortDefinition childOnDate = new AgeCohortDefinition();
		childOnDate.setName("Child On Date Cohort Definition");
		childOnDate.setMaxAge(14);
		childOnDate.addParameter(new Parameter("effectiveDate", "Age As of Date", Date.class));		

		CohortDataSetDefinition dsd = new CohortDataSetDefinition();
		dsd.setName("# Children (As Of Date) Dataset");
		dsd.setName("This is a cohort dataset definition used to calculate the number of patients who are children on a specific date");
		dsd.addParameter(new Parameter("cd.startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("cd.endDate", "End Date", Date.class));
		dsd.addStrategy("Children at Start", new Mapped<CohortDefinition>(childOnDate, "effectiveDate=${cd.startDate}"));
		dsd.addStrategy("Children at End", new Mapped<CohortDefinition>(childOnDate, "effectiveDate=${cd.endDate}"));		

		// Create the report schema
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setId(1);
		reportDefinition.setUuid(UUID.randomUUID().toString());
		reportDefinition.setName("Simple Cohort Report");
		reportDefinition.setDescription("This is a simple report with parameters and a cohort dataset definition");
		reportDefinition.addParameter(new Parameter("report.startDate", "Report Start Date", Date.class, null, null));
		reportDefinition.addParameter(new Parameter("report.endDate", "Report End Date", Date.class, null, null));
		reportDefinition.addDataSetDefinition(new Mapped<DataSetDefinition>(dsd, "cd.startDate=${report.startDate},cd.endDate=${report.endDate}"));
	
		return reportDefinition;	
	}
	
	/**
	 * Gets a simple indicator report schema.
	 * 
	 * @return	a simple indicator report schema
	 */
	public ReportDefinition getIndicatorReportDefinition() { 
				
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setId(2);
		reportDefinition.setUuid(UUID.randomUUID().toString());
		reportDefinition.setName("Simple Indicator Report");
		reportDefinition.setDescription("This is a simple indicator report with a cohort indicator dataset definition");
		reportDefinition.addParameter(new Parameter("report.location", "Report Location", Location.class));
		reportDefinition.addParameter(new Parameter("report.reportDate", "Report Date", Date.class));

		
		
		// Add dataset definition
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Number of patients enrolled at a location by gender and age");
		dsd.addParameter(new Parameter("location", "Location", Location.class));
		dsd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		reportDefinition.addDataSetDefinition(dsd, "location=${report.location},effectiveDate=${report.reportDate}");
		
		// Add cohort definition
		LocationCohortDefinition atSite = new LocationCohortDefinition();
		atSite.addParameter(new Parameter("location", "Location: ", Location.class));

		// Add cohort indicator
		CohortIndicator indicator = new CohortIndicator();
		indicator.setName("Number of patients at a particular site");
		indicator.setAggregator(CountAggregator.class);
		indicator.setCohortDefinition(atSite, "location=${indicator.location}");
		indicator.addParameter(new Parameter("indicator.location", "Location", Location.class));
		indicator.addParameter(new Parameter("indicator.effDate", "Date", Date.class));
		indicator.setLogicCriteria(null);
		dsd.addIndicator("patientsAtSite", indicator, "indicator.location=${location},indicator.effDate=${effectiveDate}");
		
		// Defining dimensions
		CohortDefinitionDimension genderDimension = new CohortDefinitionDimension();		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setGender("M");		

		// Cohort definition
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setGender("F");
		genderDimension.addCohortDefinition("male", males, null);
		genderDimension.addCohortDefinition("female", females, null);		

		// Age dimension
		CohortDefinitionDimension ageDimension = new CohortDefinitionDimension();
		ageDimension.addParameter(new Parameter("ageDate", "ageDate", Date.class));		

		// Age (child) cohort definition
		AgeCohortDefinition adult = new AgeCohortDefinition();
		adult.setMinAge(15);
		adult.addParameter(new Parameter("effectiveDate", "Effective Date:", Date.class));
		ageDimension.addCohortDefinition("adult", adult, "effectiveDate=${ageDate}");		

		// Age (adult) cohort definition
		AgeCohortDefinition child = new AgeCohortDefinition();
		child.setMaxAge(14);
		child.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		ageDimension.addCohortDefinition("child", child, "effectiveDate=${ageDate}");
		
		// Add dimensions
		dsd.addDimension("gender", genderDimension, null);
		dsd.addDimension("age", ageDimension, "ageDate=${indicator.effDate}");				
		
		// Add columns
		dsd.addColumnSpecification("1.A", "Male Adult", Object.class, "patientsAtSite", "gender=male,age=adult");
		dsd.addColumnSpecification("1.B", "Male Child", Object.class, "patientsAtSite", "gender=male,age=child");
		dsd.addColumnSpecification("2.A", "Female Adult", Object.class, "patientsAtSite", "gender=female,age=adult");
		dsd.addColumnSpecification("2.B", "Female Child", Object.class, "patientsAtSite", "gender=female,age=child");
				
		return reportDefinition;
		
	}
	
	
	

}