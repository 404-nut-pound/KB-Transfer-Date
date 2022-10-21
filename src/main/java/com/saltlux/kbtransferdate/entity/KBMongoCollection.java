package com.saltlux.kbtransferdate.entity;

import java.util.List;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "extraction_content")
@Getter
@Setter(value = AccessLevel.PROTECTED)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
public class KBMongoCollection {

  @Id
  private ObjectId _id;

  @Field(name = "agentid")
  private String agentId;

  private String prName;

  private String prCode;

  private String url;

  private String html;

  private String summary;

  private String keyGroup;

  private String key;

  private String value;

  private String valueTable;

  private List<String> imgLinks;

  private String createDate;

  private String startDate;

  private String seqNumber;
}
