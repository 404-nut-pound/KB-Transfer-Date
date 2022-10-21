package com.saltlux.kbtransferdate.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
public class JpaConfig {

  @PersistenceContext
  private EntityManager em;

  /**
   * Spring Bean으로 등록해서 다른 repo에서 호출해서 사용
   * @param em
   * @return
   */
  @Bean
  public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(em);
  }
}
