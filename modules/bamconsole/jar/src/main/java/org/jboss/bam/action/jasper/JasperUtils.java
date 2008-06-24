package org.jboss.bam.action.jasper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import net.sf.jasperreports.engine.query.JRJpaQueryExecuterFactory;

import org.hibernate.Session;
import org.jboss.bam.report.Report;
import org.jboss.bam.util.Resource;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.log.Log;

@Name("JasperUtils")
public class JasperUtils {

	private static final String EXT_SRC = "jrxml";
	private static final String EXT_COMPILED = "jasper";
	private static final String EXT_PDF = "pdf";
	private static final String EXT_XML = "xml";
	private static final String EXT_XLS = "xls";
	private static final String EXT_RTF = "rtf";
	private static final String EXT_CSV = "csv";
	private static final String EXT_ODT = "odt";

	@Logger
	Log log;

	@In(required = false)
	private long reportId;

	@In("entityManager")
	private EntityManager em;

	public void setReportId(long reportId) {
		this.reportId = reportId;
	}

	public long getReportId() {
		return this.reportId;
	}

	private String reportName;

	private void setReportName(String reportName) {
		this.reportName = reportName;
	}

	private String getReportName() {
		return this.reportName;
	}

	private String reportSummary;

	private void setReportSummary(String reportSummary) {
		this.reportSummary = reportSummary;
	}

	private String getReportSummary() {
		return this.reportSummary;
	}

	private String reportDescription;

	private void setReportDescription(String reportDescription) {
		this.reportDescription = reportDescription;
	}

	private String getReportDescription() {
		return this.reportDescription;
	}

	/**
	 * Return the Jasper source file from the stream
	 * 
	 * @return File Resource
	 */
	public Resource getFile() {
		return new Resource() {
			Report reportFile = getReportById(getReportId());

			public Object getContentType() {
				return "application/jrxml";
			}

			public byte[] getContent() throws IOException {
				return reportFile.getReportFile();
			}

			public String getFileName() {
				return reportFile.getId() + "." + JasperUtils.EXT_SRC;
			}

			public Disposition getDisposition() {
				return Disposition.ATTACHMENT;
			}
		};
	}

	/**
	 * Return the Jasper compiled source file from the stream
	 * 
	 * @return File Resource
	 */
	public Resource getCompiledFile() {
		return new Resource() {
			Report reportFile = getReportById(getReportId());

			public Object getContentType() {
				return "application/jasper";
			}

			public byte[] getContent() throws IOException {
				ByteArrayInputStream bais = new ByteArrayInputStream(reportFile
				    .getReportFile());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					JasperCompileManager.compileReportToStream(bais, baos);
				} catch (JRException ex) {
					log.debug("Error compiling report", ex);
				}
				return baos.toByteArray();
			}

			public String getFileName() {
				return reportFile.getId() + "." + JasperUtils.EXT_COMPILED;
			}

			public Disposition getDisposition() {
				return Disposition.ATTACHMENT;
			}
		};
	}

	/**
	 * Return the exported jasper report in pdf format from the stream
	 * 
	 * @return File Resource
	 */
	public Resource getPdfFile() {
		return new Resource() {
			Report reportFile = getReportById(getReportId());

			public Object getContentType() {
				return "application/pdf";
			}

			public byte[] getContent() throws IOException {
				ByteArrayInputStream reportFileInputStream = new ByteArrayInputStream(
				    reportFile.getReportFile());
				ByteArrayOutputStream compiledFileOutputStream = new ByteArrayOutputStream();
				ByteArrayOutputStream filledStream = new ByteArrayOutputStream();
				ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
				Session session = ManagedJbpmContext.instance().getSession();
				Map<String, Object> parameters = getParameters(session);
				try {
					Connection connection = getConnection();
					JasperCompileManager.compileReportToStream(reportFileInputStream,
					    compiledFileOutputStream);
					byte[] compiledReport = compiledFileOutputStream.toByteArray();
					ByteArrayInputStream compiledBytes = new ByteArrayInputStream(
					    compiledReport);
					JasperFillManager.fillReportToStream(compiledBytes, filledStream,
					    parameters, connection);
					byte[] filledBytes = filledStream.toByteArray();
					ByteArrayInputStream filledInputStream = new ByteArrayInputStream(
					    filledBytes);
					JasperExportManager.exportReportToPdfStream(filledInputStream,
					    pdfStream);
				} catch (JRException ex) {
					log.debug("Error compiling report", ex);
					ex.printStackTrace();
				} catch (SQLException ex) {
					log.debug("Error acquiring SQL Exception", ex);
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					log.debug("Class Not Found", ex);
					ex.printStackTrace();
				}
				return pdfStream.toByteArray();
			}

			public String getFileName() {
				return reportFile.getId() + "." + JasperUtils.EXT_PDF;
			}

			public Disposition getDisposition() {
				return Disposition.ATTACHMENT;
			}
		};
	}

	/**
	 * Return the exported jasper report in xml format from the stream
	 * 
	 * @return File Resource
	 */
	public Resource getXmlFile() {
		return new Resource() {
			Report reportFile = getReportById(getReportId());

			public Object getContentType() {
				return "application/xml";
			}

			public byte[] getContent() throws IOException {
				ByteArrayInputStream reportFileInputStream = new ByteArrayInputStream(
				    reportFile.getReportFile());
				ByteArrayOutputStream compiledFileOutputStream = new ByteArrayOutputStream();
				ByteArrayOutputStream filledStream = new ByteArrayOutputStream();
				ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
				Session session = ManagedJbpmContext.instance().getSession();
				Map<String, Object> parameters = getParameters(session);
				try {
					Connection connection = getConnection();
					JasperCompileManager.compileReportToStream(reportFileInputStream,
					    compiledFileOutputStream);
					byte[] compiledReport = compiledFileOutputStream.toByteArray();
					ByteArrayInputStream compiledBytes = new ByteArrayInputStream(
					    compiledReport);
					JasperFillManager.fillReportToStream(compiledBytes, filledStream,
					    parameters, connection);
					byte[] filledBytes = filledStream.toByteArray();
					ByteArrayInputStream filledInputStream = new ByteArrayInputStream(
					    filledBytes);
					JasperExportManager.exportReportToXmlStream(filledInputStream,
					    pdfStream);
				} catch (JRException ex) {
					log.debug("Error compiling report", ex);
					ex.printStackTrace();
				} catch (SQLException ex) {
					log.debug("Error acquiring SQL Exception", ex);
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					log.debug("Class Not Found", ex);
					ex.printStackTrace();
				}
				return pdfStream.toByteArray();
			}

			public String getFileName() {
				return reportFile.getId() + "." + JasperUtils.EXT_XML;
			}

			public Disposition getDisposition() {
				return Disposition.ATTACHMENT;
			}
		};
	}

	/**
	 * Return the exported jasper report in xls format from the stream
	 * 
	 * @return File Resource
	 */
	public Resource getXlsFile() {
		return new Resource() {
			Report reportFile = getReportById(getReportId());

			public Object getContentType() {
				return "application/xls";
			}

			public byte[] getContent() throws IOException {
				ByteArrayInputStream reportFileInputStream = new ByteArrayInputStream(
				    reportFile.getReportFile());
				ByteArrayOutputStream compiledFileOutputStream = new ByteArrayOutputStream();
				ByteArrayOutputStream filledStream = new ByteArrayOutputStream();
				ByteArrayOutputStream xlsStream = new ByteArrayOutputStream();
				Session session = ManagedJbpmContext.instance().getSession();
				Map<String, Object> parameters = getParameters(session);
				try {
					Connection connection = getConnection();
					JasperCompileManager.compileReportToStream(reportFileInputStream,
					    compiledFileOutputStream);
					byte[] compiledReport = compiledFileOutputStream.toByteArray();
					ByteArrayInputStream compiledBytes = new ByteArrayInputStream(
					    compiledReport);
					JasperFillManager.fillReportToStream(compiledBytes, filledStream,
					    parameters, connection);
					byte[] filledBytes = filledStream.toByteArray();
					ByteArrayInputStream filledInputStream = new ByteArrayInputStream(
					    filledBytes);
					JRXlsExporter exporter = new JRXlsExporter();
					Map<String, String> dateFormats = new HashMap<String, String>();
					dateFormats.put("EEE, MMM d, yyyy", "ddd, mmm d, yyyy");
					exporter.setParameter(JRExporterParameter.INPUT_STREAM,
					    filledInputStream);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, xlsStream);
					exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET,
					    Boolean.TRUE);
					exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE,
					    Boolean.TRUE);
					exporter.setParameter(JRXlsExporterParameter.FORMAT_PATTERNS_MAP,
					    dateFormats);
					exporter.exportReport();
				} catch (JRException ex) {
					log.debug("Error compiling report", ex);
					ex.printStackTrace();
				} catch (SQLException ex) {
					log.debug("Error acquiring SQL Exception", ex);
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					log.debug("Class Not Found", ex);
					ex.printStackTrace();
				}
				return xlsStream.toByteArray();
			}

			public String getFileName() {
				return reportFile.getId() + "." + JasperUtils.EXT_XLS;
			}

			public Disposition getDisposition() {
				return Disposition.ATTACHMENT;
			}
		};
	}

	/**
	 * Return the exported jasper report in rtf format from the stream
	 * 
	 * @return File Resource
	 */
	public Resource getRtfFile() {
		return new Resource() {
			Report reportFile = getReportById(getReportId());

			public Object getContentType() {
				return "application/rtf";
			}

			public byte[] getContent() throws IOException {
				ByteArrayInputStream reportFileInputStream = new ByteArrayInputStream(
				    reportFile.getReportFile());
				ByteArrayOutputStream compiledFileOutputStream = new ByteArrayOutputStream();
				ByteArrayOutputStream filledStream = new ByteArrayOutputStream();
				ByteArrayOutputStream rtfStream = new ByteArrayOutputStream();
				Session session = ManagedJbpmContext.instance().getSession();
				Map<String, Object> parameters = getParameters(session);
				try {
					Connection connection = getConnection();
					JasperCompileManager.compileReportToStream(reportFileInputStream,
					    compiledFileOutputStream);
					byte[] compiledReport = compiledFileOutputStream.toByteArray();
					ByteArrayInputStream compiledBytes = new ByteArrayInputStream(
					    compiledReport);
					JasperFillManager.fillReportToStream(compiledBytes, filledStream,
					    parameters, connection);
					byte[] filledBytes = filledStream.toByteArray();
					ByteArrayInputStream filledInputStream = new ByteArrayInputStream(
					    filledBytes);
					JRRtfExporter exporter = new JRRtfExporter();
					exporter.setParameter(JRExporterParameter.INPUT_STREAM,
					    filledInputStream);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, rtfStream);
					exporter.exportReport();
				} catch (JRException ex) {
					log.debug("Error compiling report", ex);
					ex.printStackTrace();
				} catch (SQLException ex) {
					log.debug("Error acquiring SQL Exception", ex);
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					log.debug("Class Not Found", ex);
					ex.printStackTrace();
				}
				return rtfStream.toByteArray();
			}

			public String getFileName() {
				return reportFile.getId() + "." + JasperUtils.EXT_RTF;
			}

			public Disposition getDisposition() {
				return Disposition.ATTACHMENT;
			}
		};
	}

	/**
	 * Return the exported jasper report in csv format from the stream
	 * 
	 * @return File Resource
	 */
	public Resource getCsvFile() {
		return new Resource() {
			Report reportFile = getReportById(getReportId());

			public Object getContentType() {
				return "application/csv";
			}

			public byte[] getContent() throws IOException {
				ByteArrayInputStream reportFileInputStream = new ByteArrayInputStream(
				    reportFile.getReportFile());
				ByteArrayOutputStream compiledFileOutputStream = new ByteArrayOutputStream();
				ByteArrayOutputStream filledStream = new ByteArrayOutputStream();
				ByteArrayOutputStream csvStream = new ByteArrayOutputStream();
				Session session = ManagedJbpmContext.instance().getSession();
				Map<String, Object> parameters = getParameters(session);
				try {
					Connection connection = getConnection();
					JasperCompileManager.compileReportToStream(reportFileInputStream,
					    compiledFileOutputStream);
					byte[] compiledReport = compiledFileOutputStream.toByteArray();
					ByteArrayInputStream compiledBytes = new ByteArrayInputStream(
					    compiledReport);
					JasperFillManager.fillReportToStream(compiledBytes, filledStream,
					    parameters, connection);
					byte[] filledBytes = filledStream.toByteArray();
					ByteArrayInputStream filledInputStream = new ByteArrayInputStream(
					    filledBytes);
					JRCsvExporter exporter = new JRCsvExporter();
					exporter.setParameter(JRExporterParameter.INPUT_STREAM,
					    filledInputStream);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, csvStream);
					exporter.exportReport();
				} catch (JRException ex) {
					log.debug("Error compiling report", ex);
					ex.printStackTrace();
				} catch (SQLException ex) {
					log.debug("Error acquiring SQL Exception", ex);
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					log.debug("Class Not Found", ex);
					ex.printStackTrace();
				}
				return csvStream.toByteArray();
			}

			public String getFileName() {
				return reportFile.getId() + "." + JasperUtils.EXT_CSV;
			}

			public Disposition getDisposition() {
				return Disposition.ATTACHMENT;
			}
		};
	}

	/**
	 * Return the exported jasper report in odt format from the stream
	 * 
	 * @return File Resource
	 */
	public Resource getOdtFile() {
		return new Resource() {
			Report reportFile = getReportById(getReportId());

			public Object getContentType() {
				return "application/csv";
			}

			public byte[] getContent() throws IOException {
				ByteArrayInputStream reportFileInputStream = new ByteArrayInputStream(
				    reportFile.getReportFile());
				ByteArrayOutputStream compiledFileOutputStream = new ByteArrayOutputStream();
				ByteArrayOutputStream filledStream = new ByteArrayOutputStream();
				ByteArrayOutputStream odtStream = new ByteArrayOutputStream();
				Session session = ManagedJbpmContext.instance().getSession();
				Map<String, Object> parameters = getParameters(session);
				try {
					Connection connection = getConnection();
					JasperCompileManager.compileReportToStream(reportFileInputStream,
					    compiledFileOutputStream);
					byte[] compiledReport = compiledFileOutputStream.toByteArray();
					ByteArrayInputStream compiledBytes = new ByteArrayInputStream(
					    compiledReport);
					JasperFillManager.fillReportToStream(compiledBytes, filledStream,
					    parameters, connection);
					byte[] filledBytes = filledStream.toByteArray();
					ByteArrayInputStream filledInputStream = new ByteArrayInputStream(
					    filledBytes);
					JROdtExporter exporter = new JROdtExporter();
					exporter.setParameter(JRExporterParameter.INPUT_STREAM,
					    filledInputStream);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, odtStream);
					exporter.exportReport();
				} catch (JRException ex) {
					log.debug("Error compiling report", ex);
					ex.printStackTrace();
				} catch (SQLException ex) {
					log.debug("Error acquiring SQL Exception", ex);
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					log.debug("Class Not Found", ex);
					ex.printStackTrace();
				}
				return odtStream.toByteArray();
			}

			public String getFileName() {
				return reportFile.getId() + "." + JasperUtils.EXT_ODT;
			}

			public Disposition getDisposition() {
				return Disposition.ATTACHMENT;
			}
		};
	}

	private Map<String, Object> getParameters(Session session) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		/* Expose the hibernate session to Jasper */
		parameters.put(JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION, session);
		/* Expose the entity manager to Jasper */
		parameters.put(JRJpaQueryExecuterFactory.PARAMETER_JPA_ENTITY_MANAGER, em);
		parameters.put("ReportTitle", this.getReportName());
		parameters.put("ReportSummary", this.getReportSummary());
		parameters.put("ReportDescription", this.getReportDescription());
		return parameters;
	}

	private Connection getConnection() throws ClassNotFoundException,
	    SQLException {
		return ManagedJbpmContext.instance().getSession().connection();
	}

	@Transactional
	private Report getReportById(long reportId) {
		Report report = null;
		javax.persistence.Query query = em
		    .createQuery("select report from Report report where report.id = :reportId");
		query.setParameter("reportId", reportId);
		report = (Report) query.getSingleResult();
		this.setReportName(report.getName());
		this.setReportSummary(report.getSummary());
		this.setReportDescription(report.getDescription());
		return report;
	}
}
