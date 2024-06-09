package com.mutualfund.excel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundData {
    private Fund fund;
    private Double averageReturns;
    private Double standardDeviation;
    private Double sharpe;
    private LocalDate calcInceptionDate;
}
