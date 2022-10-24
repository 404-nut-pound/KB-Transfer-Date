package com.saltlux.kbtransferdate.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KBTransferProductOutputDto {

  private String siteCode;

  private String categoryCode;

  private String categoryName;

  private String categoryType;

  private int count;

  private Set<String> productName;
}
