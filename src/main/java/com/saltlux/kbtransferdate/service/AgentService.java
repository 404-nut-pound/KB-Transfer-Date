package com.saltlux.kbtransferdate.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.saltlux.kbtransferdate.dto.KBTransferOutputDto;
import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import com.saltlux.kbtransferdate.entity.KBMongoCollection;
import com.saltlux.kbtransferdate.repo.KBMetaDevQueryRepo;
import com.saltlux.kbtransferdate.repo.KBMongoRepoImpl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 입력받는 명령어
 * <p/>
 * 0 - agent<p/>
 * 1 - date(yyyyMMdd)<p/>
 * 2 - agentId(int)<p/>
 */
@Component
@Slf4j
public class AgentService implements Runnable {

  @Value("${com.saltlux.kb-transfer-date.output-path}")
  private String outputPath;

  @Value("${com.saltlux.kb-transfer-date.output-file-name}")
  private String outputFileName;

  private String operateFullYearMonth = MDC
    .get("OPERATE_FULLYEAR_MONTH")
    .toString();
  private String operateDateHourMinute = MDC
    .get("OPERATE_DATE_HOUR_MINUTE")
    .toString();
  private String operateDateHyphen = MDC.get("OPERATE_DATE_HYPHEN").toString();
  private String operateTimeHyphen = MDC
    .get("OPERATE_TIME_HOUR_MINUTE_HYPHEN")
    .toString();

  private String targetDate = null;

  private Integer targetAgentId = null;

  private JsonMapper jsonMapper = JsonMapper.builder().build();

  @Autowired
  private KBMetaDevQueryRepo kbMetaDevQueryRepo;

  @Autowired
  private KBMongoRepoImpl kbMongoRepoImpl;

  public AgentService() {
    Object targetDate = MDC.get("arg1");

    if (
      targetDate != null &&
      Pattern
        .compile("\\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])")
        .matcher(targetDate.toString())
        .matches()
    ) {
      this.targetDate = targetDate.toString();
    } else {
      log.info(
        "Target Date is null or not formatted with LocalDate(yyyyMMdd)."
      );
    }

    Object targetAgentId = MDC.get("arg2");

    if (
      targetAgentId != null &&
      Pattern.compile("\\d{6}").matcher(targetAgentId.toString()).matches()
    ) {
      this.targetAgentId = Integer.parseInt(targetAgentId.toString());
    } else {
      log.info("Target AgentId is null or not number.");
    }
  }

  @Override
  public void run() {
    if (targetDate == null) {
      log.info("Target Date is null!");

      return;
    }

    if (targetAgentId == null) {
      log.info("Target AgentId is null!");

      return;
    }

    log.info("AgentService starts with [{} / {}]", targetDate, targetAgentId);

    Optional<KBMetaDevEntity> optionalKBMetaDevEntity = kbMetaDevQueryRepo.getActivatedMetaByAgentId(
      targetAgentId
    );

    if (!optionalKBMetaDevEntity.isPresent()) {
      log.error("Cannot find metadata by AgentId - {}", targetAgentId);
    }

    KBMetaDevEntity kbMetaDevEntity = optionalKBMetaDevEntity.get();

    log.info(
      "Selected Meta AgentId - {} / SiteCode - {} / CategoryCode - {}",
      kbMetaDevEntity.getAgentId(),
      kbMetaDevEntity.getSiteCode(),
      kbMetaDevEntity.getCategoryCode()
    );

    List<KBMongoCollection> kbMongoCollectionList = kbMongoRepoImpl.getKBMongoCollectionListByAgentIdAndCreateDateBetween(
      targetAgentId,
      targetDate
    );

    if (kbMongoCollectionList.size() == 0) {
      log.info("Cannot find crawl data by AgentId - {}", targetAgentId);

      return;
    }

    log.info("kbMongoCollectionList size - {}", kbMongoCollectionList.size());

    ArrayList<KBTransferOutputDto> kbTransferOutputDtoList = new ArrayList<KBTransferOutputDto>(
      kbMongoCollectionList.size()
    );

    for (KBMongoCollection kbMongoCollection : kbMongoCollectionList) {
      kbTransferOutputDtoList.add(
        KBTransferOutputDto
          .builder()
          .productName(kbMongoCollection.getPrName())
          .siteCode(kbMetaDevEntity.getSiteCode())
          .categoryCode(kbMetaDevEntity.getCategoryCode())
          .summary(kbMongoCollection.getSummary())
          .crwalDate(kbMongoCollection.getCreateDate())
          .prCode(kbMongoCollection.getPrCode())
          .url(kbMongoCollection.getUrl())
          ._id(String.format("%s_000", kbMongoCollection.get_id().toString()))
          .key(kbMongoCollection.getKey())
          .keyPath(
            String.format(
              "%s#@#%s",
              kbMongoCollection.getKeyGroup(),
              kbMongoCollection.getKey()
            )
          )
          .value(kbMongoCollection.getValue())
          .build()
      );
    }

    //포맷 - /data/kb_guest/makeup/file/json/%s/%s/%s/%s/json/
    //예시 - /data/kb_guest/makeup/file/json/202210/120005/002/C10227/json/
    final String formattedOutputPath = String.format(
      outputPath,
      operateFullYearMonth,
      operateDateHourMinute,
      kbMetaDevEntity.getSiteCode(),
      kbMetaDevEntity.getCategoryCode()
    );

    //포맷 - %s_%s_%s_%s_result.json
    //예시 - 2022-10-21_00-05_002_C10227_result.json
    final String formattedOutputFileName = String.format(
      outputFileName,
      operateDateHyphen,
      operateTimeHyphen,
      kbMetaDevEntity.getSiteCode(),
      kbMetaDevEntity.getCategoryCode()
    );

    try {
      Path path = Paths.get(
        String.format("%s/%s", formattedOutputPath, formattedOutputFileName)
      );

      if (!Files.isDirectory(path)) {
        Files.createDirectories(path);
      }

      Files.deleteIfExists(path);
      Files.write(
        path,
        jsonMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsBytes(kbTransferOutputDtoList),
        StandardOpenOption.CREATE
      );
    } catch (IOException e) {
      log.error(
        "Error with write file - AgentId - {} / SiteCode - {} / CategoryCode - {}\n{}",
        kbMetaDevEntity.getAgentId(),
        kbMetaDevEntity.getSiteCode(),
        kbMetaDevEntity.getCategoryCode()
      );
      log.error(e.getMessage(), e);
    }

    log.info("AgentService ends with [{} / {}]", targetDate, targetAgentId);
  }
}
