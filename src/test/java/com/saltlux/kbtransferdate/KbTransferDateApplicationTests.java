package com.saltlux.kbtransferdate;

import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import com.saltlux.kbtransferdate.entity.KBMongoCollection;
import com.saltlux.kbtransferdate.repo.KBMetaDevQueryRepo;
import com.saltlux.kbtransferdate.repo.KBMongoRepoImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KbTransferDateApplicationTests {

  @Autowired
  private KBMetaDevQueryRepo kbMetaDevQueryRepo;

  @Autowired(required = true)
  private KBMongoRepoImpl kbMongoRepoImpl;

  @Test
  void getActivatedMetaByAgentIdTest() {
    Optional<KBMetaDevEntity> kbMetaDevEntity = kbMetaDevQueryRepo.getActivatedMetaByAgentId(
      558894
    );

    System.out.println("kbMetaDevEntity - " + kbMetaDevEntity.get().toString());
  }

  @Test
  void getActivatedMetaListAllTest() {
    List<KBMetaDevEntity> kbMetaDevEntityList = kbMetaDevQueryRepo.getActivatedMetaListAll();

    System.out.println(
      "kbMetaDevEntityList.size() - " + kbMetaDevEntityList.size()
    );
  }

  @Test
  void findMongoCollectionByAgentId() {
    // ObjectId fromId = new ObjectId(AppUtil.getDateFromDateString("20221020"));
    // ObjectId toId = new ObjectId(AppUtil.getDateFromDateString("20221021"));

    //using jpa - failed
    // List<KBMongoCollection> kbMongoCollectionList = kbMongoRepo.findByAgentIdAndIdGtAndIdLt(
    //   "558894",
    //   fromId,
    //   toId
    // );

    //using Query Criteria - failed
    // Query query = new Query(
    //   Criteria
    //     .where("agentid")
    //     .is(558894)
    //     .and("_id")
    //     .gt(fromId)
    // );
    // query.addCriteria(
    //   Criteria
    //     .where("_id")
    //     .lt(toId)
    // )

    //using BasicQuery Criteria - successed
    // BasicQuery basicQuery = new BasicQuery(
    //   String.format(
    //     "{'agentid': %s, _id: {$gt: ObjectId(\"%s\"), $lt: ObjectId(\"%s\")}}",
    //     "558894",
    //     fromId.toString(),
    //     toId.toString()
    //   )
    // );
    //
    // List<KBMongoCollection> kbMongoCollectionList = mongoTemplate.find(
    //   basicQuery,
    //   KBMongoCollection.class
    // );

    List<KBMongoCollection> kbMongoCollectionList = kbMongoRepoImpl.getKBMongoCollectionListByAgentIdAndCreateDateBetween(
      "20221020",
      558894
    );

    System.out.println(
      "kbMongoCollectionList size - " + kbMongoCollectionList.size()
    );
  }
}
