package com.example.demo.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.common.CommonUtil;
import com.example.demo.forms.IndexForm;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Controller
public class IndexController {

	/**
	 * 
	 * @param indexForm
	 * @param model
	 * @return
	 */
	@GetMapping("/")
	public String indexPageSend(IndexForm indexForm, Model model) {
		indexForm.setSendValue("HELLOWORLD");
		model.addAttribute("indexForm", indexForm);
		return "index2";
	}
	
	/***
	 * PDFファイルダウンロード
	 * @param indexForm
	 * @param model
	 * @return
	 * @throws IOException 
	 * @throws JRException 
	 */
	@PostMapping(value="/", params = "PdfPrint",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void printActPdf(IndexForm indexForm, HttpServletResponse response) throws IOException, JRException {
		// resources内のテンプレートファイルを指定する
		String sourceFileName = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "jasperreports/SampleJasperTemplate.jasper").getAbsolutePath();
		
		// 印刷時にJasperに受け渡すデータの作成
		List<Map<String, Object>> dataList = new ArrayList<>();
		for(int inx = 0;inx < 10;inx++) {
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("NO", inx + 1);
			dataMap.put("SEND_VALUE", indexForm.getSendValue());
			dataList.add(dataMap);
		}
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
		// ファイル名
		String fileName = "sample_" + CommonUtil.getLocalDateTimeJst("yyyyMMdd_HHmmssSSS") + ".pdf";
		// HTTPヘッダに、ダウンロードファイル名を設定
	    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	    
	    // Excelファイルの作成と、レスポンスストリームへの書き込み
	    try (ServletOutputStream stream = response.getOutputStream();
	    	 ByteArrayOutputStream writeStream = new ByteArrayOutputStream();) {
	    	
	    	// JasperReportsによる印刷
			JasperPrint jasperPrint = JasperFillManager.fillReport(sourceFileName, new HashMap<>(), beanColDataSource);
			JasperExportManager.exportReportToPdfStream(jasperPrint, writeStream);
			response.getOutputStream().write(writeStream.toByteArray());
		
			// AzureUpload
			CommonUtil.azureFilesUplad(writeStream, fileName, indexForm.getAccountName(), indexForm.getAccountKey(), indexForm.getShareName());
	    } catch(Exception ex) {
	    	ex.printStackTrace();
	    }
	}
	
	/***
	 * EXCELファイルダウンロード
	 * @param indexForm
	 * @param model
	 * @return
	 * @throws IOException 
	 */
	@PostMapping(value="/", params = "ExcelPrint",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void printActExcel(IndexForm indexForm, HttpServletResponse response) throws IOException {

		// ファイル名
		String fileName = "sample_" + CommonUtil.getLocalDateTimeJst("yyyyMMdd_HHmmssSSS") + ".xlsx";
		//HTTPヘッダに、ダウンロードファイル名を設定
	    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	    //Excelファイルの作成と、レスポンスストリームへの書き込み
	    try (ServletOutputStream stream = response.getOutputStream();
	    	 Workbook wb = new XSSFWorkbook();
	    	 ByteArrayOutputStream writeStream = new ByteArrayOutputStream();) {

		    // シートの生成
		    Sheet sh = wb.createSheet();

			sh.createRow(0).createCell(0).setCellValue("出力時間：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
			// セルに値を設定
		    for(int row = 1;row < 11;row++) {
				sh.createRow(row).createCell(0).setCellValue("NO:");
				sh.createRow(row).createCell(1).setCellValue(row);
				sh.createRow(row).createCell(2).setCellValue("VALUE:");
				sh.createRow(row).createCell(3).setCellValue(indexForm.getSendValue());
		    }
			// Excel出力
			wb.write(writeStream);
			// ResponseのStreamに書き込み
			stream.write(writeStream.toByteArray());

			
			// AzureUpload
			CommonUtil.azureFilesUplad(writeStream, fileName, indexForm.getAccountName(), indexForm.getAccountKey(), indexForm.getShareName());
	    }
	}
	

	/***
	 * EXCELファイルダウンロード（テンプレートファイルからの生成）
	 * @param indexForm
	 * @param model
	 * @return
	 * @throws IOException 
	 */
	@PostMapping(value="/", params = "ExcelPrintTemplate",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void printActExcelTemplate(IndexForm indexForm, HttpServletResponse response) throws IOException {
		// resources内のテンプレートファイルを指定する
		String sourceFileName = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "excel/test_template.xlsx").getAbsolutePath();
		
		// ファイル名
		String fileName = "sample_template_" + CommonUtil.getLocalDateTimeJst("yyyyMMdd_HHmmssSSS") + ".xlsx";
		
		//HTTPヘッダに、ダウンロードファイル名を設定
	    response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	    
	    //Excelファイルの作成と、レスポンスストリームへの書き込み
	    try (ServletOutputStream stream = response.getOutputStream();
    		 InputStream is = new ByteArrayInputStream(Files.readAllBytes(Path.of(sourceFileName)));
		     ByteArrayOutputStream writeStream = new ByteArrayOutputStream();) {
	    	Workbook wb = WorkbookFactory.create(is);
		    // シートの生成
		    Sheet sh = wb.getSheetAt(0);

			sh.getRow(0).createCell(1).setCellValue(CommonUtil.getLocalDateTimeJst("yyyy-MM-dd HH:mm:ss.SSS"));
			// セルに値を設定
			int index = 1;
		    for(int row = 3;row < 11;row++) {
		    	Row rowSet = sh.createRow(row	);
		    	rowSet.createCell(1).setCellValue("NO:");
		    	rowSet.createCell(2).setCellValue(index);
		    	rowSet.createCell(3).setCellValue("VALUE:");
		    	rowSet.createCell(4).setCellValue(indexForm.getSendValue());
				index++;
		    }
			// Excel出力
			wb.write(writeStream);
			// ResponseのStreamに書き込み
			stream.write(writeStream.toByteArray());
			// AzureUpload
			CommonUtil.azureFilesUplad(writeStream, fileName, indexForm.getAccountName(), indexForm.getAccountKey(), indexForm.getShareName());
	    }
	}
	
}
