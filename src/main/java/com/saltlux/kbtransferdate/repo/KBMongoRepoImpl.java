package com.saltlux.kbtransferdate.repo;

import com.saltlux.kbtransferdate.entity.KBMongoCollection;
import java.util.List;

public abstract class KBMongoRepoImpl implements KBMongoRepo {

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
    final String agentId,
    final String date
  ) {
    return null;
  }
}
