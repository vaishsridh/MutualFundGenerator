package com.mutualfund.excel.controller;


import com.mutualfund.excel.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        Workbook workbook = excelService.generateExcel();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Combinations_fin.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
