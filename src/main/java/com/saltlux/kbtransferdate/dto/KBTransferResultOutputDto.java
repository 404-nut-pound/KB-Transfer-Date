package com.saltlux.kbtransferdate.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KBTransferResultOutputDto {

  private String productName;

  private String siteCode;

  private String categoryCode;

  private String summary;

  private String crwalDate;

  private String prCode;

  private String url;

  private String _id;

  private String key;

  private String keyPath;

  private String value;

  public String toString() {
    return String.format(
      "SiteCode - %s / CategoryCode - %s",
      this.siteCode,
      this.categoryCode
    );
  }
}
