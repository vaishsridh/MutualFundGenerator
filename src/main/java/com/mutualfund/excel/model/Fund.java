package com.mutualfund.excel.model;


import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fund {
    String schemaName;
    String url;
    List<NavObject> navObjectList;
    LocalDate inceptionDate;
}
