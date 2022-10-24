package com.saltlux.kbtransferdate.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
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
public class KBMetaDevEntity {

  @Id
  private String categoryCode;

  @Column(name = "agentid")
  private Integer agentId;

  @Column(name = "pdfAgentid")
  private Integer pdfAgentId;

  @Column(name = "productAgentid")
  private Integer productAgentId;

  @Column(name = "cardAgentid")
  private Integer cardAgentId;

  private String siteCode;

  private String siteName;

  private String categoryType;

  private String categoryName;

  private String siteUrl;

  private String categoryUrl;

  private String siteGroup;

  private String status;

  private Boolean isFileAgent;

  private String structureTypeCode;

  private LocalDateTime registeredDatetime;

  private String remarks;

  private Integer errorCount;

  private LocalDateTime closedDatetime;

  @Column(name = "oldAgentid")
  private Integer oldAgentId;

  @Column(name = "dataLastObjectid")
  private String dataLastObjectId;

  @Column(name = "pdfLastObjectid")
  private String pdfLastObjectId;

  @Column(name = "productLastObjectid")
  private String productLastObjectId;

  @Column(name = "cardLastObjectid")
  private String cardLastObjectId;

  private Boolean isNewProductAgent;

  public String toString() {
    return String.format(
      "AgentId - %d / SiteCode - %s / CategoryCode - %s",
      this.agentId,
      this.siteCode,
      this.categoryCode
    );
  }
}
