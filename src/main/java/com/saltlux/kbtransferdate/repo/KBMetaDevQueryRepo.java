package com.saltlux.kbtransferdate.repo;

import static com.saltlux.kbtransferdate.entity.QKBMetaDevEntity.kBMetaDevEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KBMetaDevQueryRepo {

  private final JPAQueryFactory queryFactory;

  public List<KBMetaDevEntity> getActivatedMetaList() {
    return queryFactory
      .selectFrom(kBMetaDevEntity)
      .where(kBMetaDevEntity.status.eq("1"))
      .fetch();
  }
}
