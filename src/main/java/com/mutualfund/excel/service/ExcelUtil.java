package com.mutualfund.excel.service;

import com.mutualfund.excel.constants.AppConstants;
import com.mutualfund.excel.model.Fund;
import com.mutualfund.excel.model.FundData;
import com.mutualfund.excel.model.NavObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ExcelUtil {

    Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    Map<String, Fund> fundMap = new HashMap<>();
    public void processCategory(Workbook workbook, String categoryName, List<String> urls) {
        log.info("Processing category : {}", categoryName);
        List<Fund> fundList = populateFundList(urls);
        List<FundData> topFunds = new ArrayList<>();
        Set<String> uniqueFunds = new HashSet<>();
        findTopFunds(fundList, new ArrayList<>(), topFunds, uniqueFunds, 0);

        topFunds.sort(Comparator.comparing(FundData::getSharpe).reversed());

        Sheet sheet = workbook.createSheet(categoryName);
        for (int i = 0; i < Math.min(topFunds.size(), 10); i++) {
            FundData fundData = topFunds.get(i);
            // Print fundData to Excel sheet
            if (!Double.isNaN(fundData.getAverageReturns()) ||
                    !Double.isNaN(fundData.getStandardDeviation()) ||
                    !Double.isNaN(fundData.getSharpe())) {
                log.info("Values for scheme : {}  - avg : {}, std dev : {}, sharpe : {}", fundData.getFund().getSchemaName(),
                        fundData.getAverageReturns(), fundData.getSharpe());

                // If none of the values are NaN, create the row and set cell values
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(fundData.getFund().getSchemaName());
                row.createCell(1).setCellValue(fundData.getAverageReturns());
                row.createCell(2).setCellValue(fundData.getStandardDeviation());
                row.createCell(3).setCellValue(fundData.getSharpe());
            }
        }
    }

    private void findTopFunds(List<Fund> funds, List<Fund> currentCombination, List<FundData> topFunds, Set<String> uniqueFunds, int startIndex) {
        if (currentCombination.size() == 3) {
            // Calculate FundData for the current combination
            List<FundData> fundDataList = new ArrayList<>();
            LocalDate latestInceptionDate = getLatestInceptionDate(currentCombination);
            for (Fund fund : currentCombination) {
                FundData fundData = calculateFundData(fund, latestInceptionDate);
                fundDataList.add(fundData);
            }

            // Find the fund with the highest Sharpe ratio in the combination
            FundData bestFund = Collections.max(fundDataList, Comparator.comparing(FundData::getSharpe));
            if (uniqueFunds.add(bestFund.getFund().getSchemaName())) {
                topFunds.add(bestFund);
            }
            return;
        }

        for (int i = startIndex; i < funds.size(); i++) {
            List<Fund> newCombination = new ArrayList<>(currentCombination);
            newCombination.add(funds.get(i));
            findTopFunds(funds, newCombination, topFunds, uniqueFunds,i + 1);
        }
    }

    private LocalDate getLatestInceptionDate(List<Fund> funds) {
        return funds.stream()
                .map(fund -> LocalDate.parse(fund.getNavObjectList().getLast().getDate(), AppConstants.FORMATTER))
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
    }

    private void extracted(Workbook workbook, String categoryName, List<Fund> fundList) {
        List<FundData> bestFunds = new ArrayList<>();
        Sheet sheet = workbook.createSheet(categoryName);
        int rowNum = 0;


        for (int i = 0; i < fundList.size() - 2; i++) {
            for (int j = i + 1; j < fundList.size() - 1; j++) {
                for (int k = j + 1; k < fundList.size(); k++) {
                    // Calculate the FundData for each fund in the combination
                    LocalDate latestInceptionDate = Collections.max(Arrays.asList(
                            LocalDate.parse(fundList.get(i).getNavObjectList().getLast().getDate(), AppConstants.FORMATTER),
                            LocalDate.parse(fundList.get(j).getNavObjectList().getLast().getDate(), AppConstants.FORMATTER),
                            LocalDate.parse(fundList.get(k).getNavObjectList().getLast().getDate(), AppConstants.FORMATTER)
                    ));

                    FundData fundDataI = calculateFundData(fundList.get(i), latestInceptionDate);
                    FundData fundDataJ = calculateFundData(fundList.get(j), latestInceptionDate);
                    FundData fundDataK = calculateFundData(fundList.get(k), latestInceptionDate);

                    // Find the fund with the highest Sharpe ratio in the combination
                    FundData bestFund = Collections.max(Arrays.asList(fundDataI, fundDataJ, fundDataK),
                            Comparator.comparing(FundData::getSharpe));

                    // Add the best fund to the list
                    bestFunds.add(bestFund);
                }
            }
        }
    }

    private List<Fund> populateFundList(List<String> urls) {
        List<Fund> fundList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        for (String url : urls) {
            if(!fundMap.containsKey(url)) {
                String response = restTemplate.getForObject(url, String.class);
                JSONObject jsonObject = new JSONObject(response);
                String schemeName = jsonObject.getJSONObject("meta").getString("scheme_name");
                JSONArray dataArray = jsonObject.getJSONArray("data");

                Fund fund = new Fund();
                fund.setUrl(url);
                List<NavObject> navObjectList = new ArrayList<>();
                dataArray.forEach(
                        item -> {
                            JSONObject data = (JSONObject) item;
                            NavObject navObject = new NavObject();
                            navObject.setDate(data.getString("date"));
                            navObject.setNav(data.getString("nav"));
                            navObjectList.add(navObject);
                        }
                );
                fund.setSchemaName(schemeName);
                fund.setNavObjectList(navObjectList);
                fundMap.put(url, fund);
                fundList.add(fund);
            }
            else {
                fundList.add(fundMap.get(url));
            }
        }
        return fundList;
    }

    private FundData calculateFundData(Fund fund, LocalDate latestInceptionDate) {
        double average = calculateAverage(fund.getNavObjectList(), latestInceptionDate);
        double standardDeviation = calculateStandardDeviation(fund.getNavObjectList(), average);
        if(standardDeviation == 0) {
            log.info("std dev is 0 for funds : {}", fund.getSchemaName());
        }
        double sharpe = standardDeviation!= 0 ? (average - 7) / standardDeviation : 0;
        return new FundData(fund, average, standardDeviation, sharpe);
    }

    private double calculateStandardDeviation(List<NavObject> navs, Double averageReturn) {
        if(navs.isEmpty()) {
            log.info("nav is 0");
            return 0.0;
        }
        double sum = 0.0;
        for (NavObject nav : navs) {
            sum += Math.pow(Double.parseDouble(nav.getNav()) - averageReturn, 2);
        }
        return Math.sqrt(sum / navs.size());
    }

    private double calculateAverage(List<NavObject> navs, LocalDate latestInceptionDate) {
        List<NavObject> navsAfterDate = navs.stream()
                .filter(nav -> !LocalDate.parse(nav.getDate(), AppConstants.FORMATTER).isBefore(latestInceptionDate))
                .sorted(Comparator.comparing(NavObject::getDate))
                .toList();

        if(navsAfterDate.isEmpty()) {
            log.info("Nav avg is 0");
            return 0.0;
        }
        List<Double> rollingReturns = getDoubles(navsAfterDate);
        return rollingReturns.stream().mapToDouble(a -> a).average().orElse(0.0);
    }

    private static List<Double> getDoubles(List<NavObject> navsAfterDate) {
        Integer rollingPeriod = 246;  // 1-year rolling period for a market with 252 trading days
        List<Double> dailyReturns = new ArrayList<>();
        for (int i = 1; i < navsAfterDate.size(); i++) {
            double prevNav = Double.parseDouble(navsAfterDate.get(i - 1).getNav());
            double currNav = Double.parseDouble(navsAfterDate.get(i).getNav());
            dailyReturns.add((currNav - prevNav) / prevNav);
        }

        List<Double> rollingReturns = new ArrayList<>();
        for (int i = 0; i < dailyReturns.size() - rollingPeriod; i++) {
            double rollingReturn = 0.0;
            for (int j = i; j < i + rollingPeriod; j++) {
                rollingReturn += dailyReturns.get(j);
            }
            rollingReturns.add(rollingReturn);
        }
        return rollingReturns;
    }

    private void processCategory1(Workbook workbook, String categoryName, List<String> urls) {
        RestTemplate restTemplate = new RestTemplate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        List<Fund> fundList = new ArrayList<>();
        for (String url : urls) {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject jsonObject = new JSONObject(response);
            String schemeName = jsonObject.getJSONObject("meta").getString("scheme_name");
            JSONArray dataArray = jsonObject.getJSONArray("data");

            Fund fund = new Fund();
            List<NavObject> navObjectList = new ArrayList<>();
            dataArray.forEach(
                    item -> {
                        JSONObject data = (JSONObject) item;
                        NavObject navObject = new NavObject();
                        navObject.setDate(data.getString("date"));
                        navObject.setNav(data.getString("nav"));
                        navObjectList.add(navObject);
                    }
            );
            fund.setSchemaName(schemeName);
            fund.setNavObjectList(navObjectList);
            fundList.add(fund);

        }

        Sheet sheet = workbook.createSheet(categoryName);

        int rowNum = 0;

        for (int i = 0; i < fundList.size() - 2; i++) {
            for (int j = i + 1; j < fundList.size() - 1; j++) {
                for (int k = j + 1; k < fundList.size(); k++) {
                    LocalDate latestInceptionDate = Collections.max(Arrays.asList(
                            LocalDate.parse(fundList.get(i).getNavObjectList().getLast().getDate(), formatter),
                            LocalDate.parse(fundList.get(j).getNavObjectList().getLast().getDate(), formatter),
                            LocalDate.parse(fundList.get(k).getNavObjectList().getLast().getDate(), formatter)
                    ));

                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(fundList.get(i).getSchemaName());
                    Double average = calculateAverage(fundList.get(i).getNavObjectList(), latestInceptionDate);
                    Double standardDeviation = calculateStandardDeviation(fundList.get(i).getNavObjectList(),average);
                    Double sharpe = (average - 7)/standardDeviation;
                    row.createCell(1).setCellValue(average);
                    row.createCell(2).setCellValue(standardDeviation);
                    row.createCell(3).setCellValue(sharpe);
                    row = sheet.createRow(rowNum++);
                    average = calculateAverage(fundList.get(j).getNavObjectList(), latestInceptionDate);
                    standardDeviation = calculateStandardDeviation(fundList.get(j).getNavObjectList(),average);
                    sharpe = (average - 7)/standardDeviation;
                    row.createCell(0).setCellValue(fundList.get(j).getSchemaName());
                    row.createCell(1).setCellValue(average);
                    row.createCell(2).setCellValue(standardDeviation);
                    row.createCell(3).setCellValue(sharpe);
                    row = sheet.createRow(rowNum++);
                    average = calculateAverage(fundList.get(k).getNavObjectList(), latestInceptionDate);
                    standardDeviation = calculateStandardDeviation(fundList.get(k).getNavObjectList(),average);
                    sharpe = (average - 7)/standardDeviation;
                    row.createCell(0).setCellValue(fundList.get(k).getSchemaName());
                    row.createCell(1).setCellValue(average);
                    row.createCell(2).setCellValue(standardDeviation);
                    row.createCell(3).setCellValue(sharpe);
                    rowNum++;  // Add an empty row after each combination
                }
            }
        }

    }


}
