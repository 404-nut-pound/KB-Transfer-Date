package com.saltlux.kbtransferdate.worker;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.saltlux.kbtransferdate.dto.KBTransferOutputDto;
import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import com.saltlux.kbtransferdate.entity.KBMongoCollection;
import com.saltlux.kbtransferdate.repo.KBMongoRepoImpl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Builder
@Slf4j
public class DateWorker implements Runnable {

  private String targetDate;
  private String outputPath;
  private String outputFileName;
  private String operateFullYearMonth;
  private String operateDateHourMinute;
  private String operateDateHyphen;
  private String operateTimeHyphen;

  private List<KBMetaDevEntity> kbMetaDevEntityList;

  private KBMongoRepoImpl kbMongoRepoImpl;

  @Builder.Default
  private JsonMapper jsonMapper = JsonMapper.builder().build();

  @Override
  public void run() {
    if (outputPath == null) {
      log.error("outputPath is null!");

      return;
    }

    if (outputFileName == null) {
      log.error("outputFileName is null!");

      return;
    }

    if (operateFullYearMonth == null) {
      log.error("operateFullYearMonth is null!");

      return;
    }

    if (operateDateHourMinute == null) {
      log.error("operateDateHourMinute is null!");

      return;
    }

    if (operateDateHyphen == null) {
      log.error("operateDateHyphen is null!");

      return;
    }

    if (operateTimeHyphen == null) {
      log.error("operateTimeHyphen is null!");

      return;
    }

    if (kbMetaDevEntityList == null || kbMetaDevEntityList.size() == 0) {
      log.error("kbMetaDevEntityList is null!");

      return;
    }

    log.info(
      "DateWorker starts with list size - {}",
      kbMetaDevEntityList.size()
    );

    try {
      for (KBMetaDevEntity kbMetaDevEntity : kbMetaDevEntityList) {
        List<KBMongoCollection> kbMongoCollectionList = kbMongoRepoImpl.getKBMongoCollectionListByAgentIdAndCreateDateBetween(
          targetDate,
          kbMetaDevEntity.getAgentId()
        );

        if (kbMongoCollectionList.size() == 0) {
          log.error(
            "Cannot find crawled data Target Date - {} / AgentId - {} / SiteCode - {} / CategoryCode - {}",
            targetDate,
            kbMetaDevEntity.getAgentId(),
            kbMetaDevEntity.getSiteCode(),
            kbMetaDevEntity.getCategoryCode()
          );

          return;
        }

        log.info(
          "Target Date - {} / AgentId - {} / SiteCode - {} / CategoryCode - {} / Crawled Data size - {}",
          targetDate,
          kbMetaDevEntity.getAgentId(),
          kbMetaDevEntity.getSiteCode(),
          kbMetaDevEntity.getCategoryCode(),
          kbMongoCollectionList.size()
        );

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
              ._id(
                String.format("%s_000", kbMongoCollection.get_id().toString())
              )
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
              .writeValueAsBytes(kbTransferOutputDtoList)
          );
        } catch (IOException ioe) {
          log.error(
            "Error with write file - Target Date - {} / AgentId - {} / SiteCode - {} / CategoryCode - {}",
            targetDate,
            kbMetaDevEntity.getAgentId(),
            kbMetaDevEntity.getSiteCode(),
            kbMetaDevEntity.getCategoryCode()
          );
          log.error(ioe.getMessage(), ioe);
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    log.info("DateWorker ends with list size - {}", kbMetaDevEntityList.size());
  }
}
