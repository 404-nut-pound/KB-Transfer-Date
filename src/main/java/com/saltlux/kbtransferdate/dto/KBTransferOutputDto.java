package com.saltlux.kbtransferdate.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class KBTransferOutputDto {

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
}
