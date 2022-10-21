package com.saltlux.kbtransferdate;

import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import com.saltlux.kbtransferdate.entity.KBMongoCollection;
import com.saltlux.kbtransferdate.repo.KBMetaDevQueryRepo;
import com.saltlux.kbtransferdate.repo.KBMongoRepo;
import com.saltlux.kbtransferdate.util.AppUtil;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;

@SpringBootTest
class KbTransferDateApplicationTests {

  @Autowired
  private KBMetaDevQueryRepo kbMetaDevQueryRepo;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private KBMongoRepo kbMongoRepo;

  @Test
  void getActivatedMetaByAgentIdTest() {
    Optional<KBMetaDevEntity> kbMetaDevEntity = kbMetaDevQueryRepo.getActivatedMetaByAgentId(
      558894
    );

    System.out.println("kbMetaDevEntity - " + kbMetaDevEntity.hashCode());
  }

  @Test
  void getActivatedMetaListTest() {
    List<KBMetaDevEntity> kbMetaDevEntityList = kbMetaDevQueryRepo.getActivatedMetaList();

    System.out.println(
      "kbMetaDevEntityList.size() - " + kbMetaDevEntityList.size()
    );
  }

  @Test
  void findMongoCollectionByAgentId() {
    // Query query = new Query(
    //   Criteria
    //     .where("agentid")
    //     .is(558894)
    //     .and("_id")
    //     .gt(new ObjectId(AppUtil.getDateFromDateString("20221020")))
    // );
    // query.addCriteria(
    //   Criteria
    //     .where("_id")
    //     .lt(new ObjectId(AppUtil.getDateFromDateString("20221021")))
    // )
    ObjectId fromId = new ObjectId(AppUtil.getDateFromDateString("20221020"));
    ObjectId toId = new ObjectId(AppUtil.getDateFromDateString("20221021"));

    BasicQuery basicQuery = new BasicQuery(
      String.format(
        "{'agentid': %s, _id: {$gt: ObjectId(\"%s\"), $lt: ObjectId(\"%s\")}}",
        "558894",
        fromId.toString(),
        toId.toString()
      )
    );

    List<KBMongoCollection> kbMongoCollectionList = mongoTemplate.find(
      basicQuery,
      KBMongoCollection.class
    );

    // List<KBMongoCollection> kbMongoCollectionList = kbMongoRepo.findByAgentIdAndIdGtAndIdLt(
    //   "558894",
    //   new ObjectId(AppUtil.getDateFromDateString("20221020")),
    //   new ObjectId(AppUtil.getDateFromDateString("20221021"))
    // );

    System.out.println(
      "kbMongoCollectionList size - " + kbMongoCollectionList.size()
    );
  }
}
