package com.mutualfund.excel.service;

import com.mutualfund.excel.constants.AppConstants;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExcelService {

    @Autowired
    ExcelUtil excelUtil;

    public Workbook generateExcel() {
        Workbook workbook = new XSSFWorkbook();

        excelUtil.processCategory(workbook, "Flexi Cap", AppConstants.FLEXI_CAP_URLS);
        excelUtil.processCategory(workbook, "Small Cap", AppConstants.SMALL_CAP_URLS);
        excelUtil.processCategory(workbook, "Mid Cap", AppConstants.MID_CAP_URLS);

        return workbook;
    }

}
