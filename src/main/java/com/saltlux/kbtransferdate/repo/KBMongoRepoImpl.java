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
   * @param agentId
   * @param date
   * @return
   */
  public List<KBMongoCollection> getKBMongoCollectionListByAgentIdAndCreateDateBetween(
    final int agentId,
    final String date
  ) {
    LocalDate fromDate = LocalDate.parse(
      date,
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
    );

    return mongoTemplate.find(basicQuery, KBMongoCollection.class);
  }
}
