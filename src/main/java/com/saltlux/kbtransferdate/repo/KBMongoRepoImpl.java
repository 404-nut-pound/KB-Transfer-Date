package com.saltlux.kbtransferdate.repo;

import com.saltlux.kbtransferdate.entity.KBMongoCollection;
import com.saltlux.kbtransferdate.util.AppUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KBMongoRepoImpl {

  private final MongoTemplate mongoTemplate;

  /**
   * AgentId와 날짜 문자열(yyyyMMdd)를 기준으로 MongoDB Collection 조회
   * 입력한 date를 기준으로 다음 날까지 입력된 데이터를 조회함
   * ex) 20221020 -> 20221020 ~ 20221021 사이에 생성된 데이터
   *
   * @param targetDate
   * @param agentId
   * @return
   */
  public List<KBMongoCollection> getKBMongoCollectionListByAgentIdAndCreateDateBetween(
    final String targetDate,
    final int agentId
  ) {
    LocalDate fromDate = LocalDate.parse(
      targetDate,
      DateTimeFormatter.ofPattern("yyyyMMdd")
    );
    LocalDate toDate = fromDate.plusDays(1);

    BasicQuery basicQuery = new BasicQuery(
      String.format(
        "{'agentid': %d, _id: {$gte: ObjectId(\"%s\"), $lt: ObjectId(\"%s\")}}",
        agentId,
        new ObjectId(AppUtil.getDateFromLocalDate(fromDate)),
        new ObjectId(AppUtil.getDateFromLocalDate(toDate))
      )
      // 기존 조회 조건인 create_time으로 구성 시
      // 조회 timeout 발생함
      // String.format(
      //   "{'agentid': %d, create_date: {$gte: \"%s 00:00:00\", $lt: \"%s 00:00:00\"}}",
      //   agentId,
      //   fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
      //   toDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      // )
    );

    return mongoTemplate.find(basicQuery, KBMongoCollection.class);
  }
}
