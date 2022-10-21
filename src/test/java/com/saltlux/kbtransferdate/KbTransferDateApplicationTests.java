package com.saltlux.kbtransferdate;

import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import com.saltlux.kbtransferdate.repo.KBMetaDevQueryRepo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KbTransferDateApplicationTests {

  @Autowired
  private KBMetaDevQueryRepo kbMetaDevQueryRepo;

  @Test
  void getActivatedMetaListTest() {
    List<KBMetaDevEntity> kbMetaDevEntityList = kbMetaDevQueryRepo.getActivatedMetaList();

    System.out.println(
      "kbMetaDevEntityList.size() - " + kbMetaDevEntityList.size()
    );
  }
}
