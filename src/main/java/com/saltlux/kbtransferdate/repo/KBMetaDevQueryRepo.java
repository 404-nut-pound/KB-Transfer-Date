package com.saltlux.kbtransferdate.repo;

import static com.saltlux.kbtransferdate.entity.QKBMetaDevEntity.kBMetaDevEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KBMetaDevQueryRepo {

  private final JPAQueryFactory queryFactory;

  /**
   * AgentId를 기준으로 활성화 된 메타 데이터 조회
   * @param agentId
   * @return
   */
  public Optional<KBMetaDevEntity> getActivatedMetaByAgentId(
    final int agentId
  ) {
    return Optional.ofNullable(
      queryFactory
        .selectFrom(kBMetaDevEntity)
        .where(
          kBMetaDevEntity.status.eq("1"),
          kBMetaDevEntity.agentId.eq(agentId)
        )
        .fetchOne()
    );
  }

  /**
   * 활성화 된 메타 데이터 목록 조회
   * @return
   */
  public List<KBMetaDevEntity> getActivatedMetaListAll() {
    return queryFactory
      .selectFrom(kBMetaDevEntity)
      .where(kBMetaDevEntity.status.eq("1"))
      .fetch();
  }
}
