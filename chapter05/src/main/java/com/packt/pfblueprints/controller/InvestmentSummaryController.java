package com.packt.pfblueprints.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.packt.pfblueprints.dao.InvestmentSummaryDAO;
import com.packt.pfblueprints.model.AccountSummary;
import com.packt.pfblueprints.model.InvestmentSummary;


@ManagedBean
@ViewScoped
public class InvestmentSummaryController implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<InvestmentSummary> investmentsInfo=new ArrayList<InvestmentSummary>();
	private InvestmentSummary investmentobj=new InvestmentSummary();
	private CartesianChartModel linearModel; 
	
	InvestmentSummaryDAO dao = new InvestmentSummaryDAO();
	
	@PostConstruct
	public void init() { 
		
		investmentsInfo=dao.getAllInvestments();
		createLinearModel();
		
	}
	
	public String storeSelectedInvestornum(){
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put("investmentNumber", investmentobj.getInvestmentNumber());
		return "transactionsummary.xhtml?faces-redirect=true";
	}
	
	public String displayAllInvestments(){
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put("accountNumber", "");
		return "investmentsummary.xhtml?faces-redirect=true";
	}

	 private void createLinearModel() {  
	        linearModel = new CartesianChartModel(); 
	        
	        for(InvestmentSummary obj:investmentsInfo){
	        	 LineChartSeries series = new LineChartSeries();
	        	 series.setLabel(obj.getFundname());  
	        	 
	        	 series.set("MarketValue1", obj.getMarketValue1());
	        	 series.set("MarketValue2", obj.getMarketValue2());
	        	 series.set("MarketValue3", obj.getMarketValue3());
	        	 series.set("MarketValue4", obj.getMarketValue4());
	        	 series.set("MarketValue5", obj.getMarketValue5());
	        	 
	        	 linearModel.addSeries(series);
	        }
	         
	    }
	 
	 public void preProcessPDF(Object document) throws IOException, BadElementException, DocumentException {  
		    Document pdf = (Document) document;  
		    pdf.open();  
		    pdf.setPageSize(PageSize.A3);  
		  
		    ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();  
		    String logo = servletContext.getRealPath("") + File.separator +"resources" + File.separator + "images" + File.separator +"logo" + File.separator + "logo.png";  
		    Image image=Image.getInstance(logo);
		    image.scaleAbsolute(100f, 50f);
		    pdf.add(image); 
		    // add a couple of blank lines
	        pdf.add( Chunk.NEWLINE );
	        pdf.add( Chunk.NEWLINE );
		    Font fontbold = FontFactory.getFont("Times-Roman", 16, Font.BOLD);
		    fontbold.setColor(55, 55, 55);;
		    pdf.add(new Paragraph("Investment Summary",fontbold));
		    // add a couple of blank lines
		    pdf.add( Chunk.NEWLINE );
		    pdf.add( Chunk.NEWLINE );
		}  
		
		public void postProcessPDF(Object document) throws IOException, BadElementException, DocumentException {  
			 Document pdf = (Document) document; 
			 pdf.add( Chunk.NEWLINE );
			 Font fontbold = FontFactory.getFont("Times-Roman", 14, Font.BOLD);
			 pdf.add(new Paragraph("Disclaimer",fontbold));
			 pdf.add( Chunk.NEWLINE );
			 pdf.add(new Paragraph("The information contained in this website is for information purposes only, and does not constitute, nor is it intended to constitute, the provision of financial product advice."));
			 pdf.add(new Paragraph("This website is intended to track the investor account summary information,investments and transaction in a partcular period of time. "));
		}  
		
		
		public void postProcessXLS(Object document) {  
		    HSSFWorkbook wb = (HSSFWorkbook) document;  
		    HSSFSheet sheet = wb.getSheetAt(0);  
		    HSSFRow header = sheet.getRow(0);  
		      
		    HSSFCellStyle cellStyle = wb.createCellStyle();    
		    cellStyle.setFillForegroundColor(HSSFColor.GREEN.index);  
		    cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
		      
		    for(int i=0; i < header.getPhysicalNumberOfCells();i++) {  
		        HSSFCell cell = header.getCell(i);  
		          
		        cell.setCellStyle(cellStyle);  
		    }  
		    
		    Row row=sheet.createRow((short)sheet.getLastRowNum()+3);
		    Cell cellDisclaimer = row.createCell(0);
		    HSSFFont customFont= wb.createFont();
		    customFont.setFontHeightInPoints((short)10);
		    customFont.setFontName("Arial");
		    customFont.setColor(IndexedColors.BLACK.getIndex());
		    customFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		    customFont.setItalic(true);
		    
		    cellDisclaimer.setCellValue("Disclaimer");
		    HSSFCellStyle cellStyleDisclaimer = wb.createCellStyle();
		    cellStyleDisclaimer.setFont(customFont);
		    cellDisclaimer.setCellStyle(cellStyleDisclaimer);
		    
		    Row row1=sheet.createRow(sheet.getLastRowNum()+2);
		    Cell cellDisclaimerContent1 = row1.createCell(0);
		    cellDisclaimerContent1.setCellValue("The information contained in this website is for information purposes only, and does not constitute, nor is it intended to constitute, the provision of financial product advice.");
		    
		    Row row2=sheet.createRow(sheet.getLastRowNum()+1);
		    Cell cellDisclaimerContent2 = row2.createCell(0);
		    cellDisclaimerContent2.setCellValue("This website is intended to track the investor account summary information,investments and transaction in a partcular period of time. ");
		    
		}  
	 
	public List<InvestmentSummary> getInvestmentsInfo() {
		return investmentsInfo;
	}

	public void setInvestmentsInfo(List<InvestmentSummary> investmentsInfo) {
		this.investmentsInfo = investmentsInfo;
	}

	public InvestmentSummary getInvestmentobj() {
		return investmentobj;
	}

	public void setInvestmentobj(InvestmentSummary investmentobj) {
		this.investmentobj = investmentobj;
	}

	public CartesianChartModel getLinearModel() {
		return linearModel;
	}

	public void setLinearModel(CartesianChartModel linearModel) {
		this.linearModel = linearModel;
	}
	
	
	

}
