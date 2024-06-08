package com.mutualfund.excel.controller;

import com.mutualfund.excel.constants.AppConstants;

import com.mutualfund.excel.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    private static final String[] COLUMNs = {"Scheme Name", "Date"};

    @GetMapping("/download-flexi-combination")
    public void downloadFlexiCombinationExcel(HttpServletResponse response) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        for (String s : AppConstants.FLEXICAP_LIST) {
            Sheet sheet = workbook.createSheet(s);
            int rowNum = 0;
            for (int i = 0; i < AppConstants.FLEXICAP_LIST.size(); i++) {
                if (!AppConstants.FLEXICAP_LIST.get(i).equals(s)) continue;
                for (int j = i + 1; j < AppConstants.FLEXICAP_LIST.size(); j++) {
                    for (int k = j + 1; k < AppConstants.FLEXICAP_LIST.size(); k++) {
                        sheet.createRow(rowNum++).createCell(0).setCellValue(AppConstants.FLEXICAP_LIST.get(i));
                        sheet.createRow(rowNum++).createCell(0).setCellValue(AppConstants.FLEXICAP_LIST.get(j));
                        sheet.createRow(rowNum++).createCell(0).setCellValue(AppConstants.FLEXICAP_LIST.get(k));
                        rowNum++;  // Add an empty row after each combination
                    }
                }
            }
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=Combinations.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        Workbook workbook = excelService.generateExcel();

       // response.setContentType("application/octet-stream");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Combinations_fin.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    @GetMapping(value = "/download-get-date", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> downloadDate(HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=mutual_fund_data.xlsx");

        return ResponseEntity.ok(out -> {
            Workbook workbook = new XSSFWorkbook();
            CreationHelper createHelper = workbook.getCreationHelper();

            Sheet sheet = workbook.createSheet("Mutual Fund Data");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < COLUMNs.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(COLUMNs[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (String url : AppConstants.API_URLs) {
                RestTemplate restTemplate = new RestTemplate();
                String result = restTemplate.getForObject(url, String.class);
                // Parse the JSON response
                // Extract the scheme_name and the date of the last data object
                // Write the data into the Excel file
                Row row = sheet.createRow(rowNum++);
                JSONObject jsonObject = new JSONObject(result);
                String schemeName = jsonObject.getJSONObject("meta").getString("scheme_name");
                String date = jsonObject.getJSONArray("data").getJSONObject(jsonObject.getJSONArray("data").length() - 1).getString("date");

                row.createCell(0).setCellValue(schemeName);
                row.createCell(1).setCellValue(date);

            }

            workbook.write(out);
        });
    }
}
