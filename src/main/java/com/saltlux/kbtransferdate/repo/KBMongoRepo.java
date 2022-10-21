package com.saltlux.kbtransferdate.repo;

import com.saltlux.kbtransferdate.entity.KBMongoCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KBMongoRepo
  extends MongoRepository<KBMongoCollection, String> {}
