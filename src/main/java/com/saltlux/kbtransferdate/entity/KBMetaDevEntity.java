package com.saltlux.kbtransferdate.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "kb_meta_dev")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter(value = AccessLevel.PROTECTED)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
public class KBMetaDevEntity {

  @Id
  private String categoryCode;

  private int agentId;

  private int pdfAgentid;

  private int productAgentid;

  private int cardAgentid;

  private String siteCode;

  private String siteName;

  private String categoryType;

  private String categoryName;

  private String siteUrl;

  private String categoryUrl;

  private String siteGroup;

  private String status;

  private int isFileAgent;

  private String structureTypeCode;

  private LocalDateTime registeredDatetime;

  private String remarks;

  private int errorCount;

  private LocalDateTime closedDatetime;

  private int oldAgentid;

  private String dataLastObjectid;

  private String pdfLastObjectid;

  private String productLastObjectid;

  private String cardLastObjectid;

  private int isNewProductAgent;
}
