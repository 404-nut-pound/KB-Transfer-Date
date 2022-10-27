package com.saltlux.kbtransferdate.worker;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.saltlux.kbtransferdate.dto.KBTransferProductOutputDto;
import com.saltlux.kbtransferdate.dto.KBTransferResultOutputDto;
import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import com.saltlux.kbtransferdate.entity.KBMongoCollection;
import com.saltlux.kbtransferdate.repo.KBMongoRepoImpl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Getter
@Builder
@Slf4j
public class DateWorker
  implements Callable<Map<String, List<KBTransferProductOutputDto>>> {

  private String targetDate;
  private String resultOutputPath;
  private String resultOutputFileName;
  private String operateFullYearMonth;
  private String operateDateHourMinute;
  private String operateDateHyphen;
  private String operateTimeHyphen;

  private List<KBMetaDevEntity> kbMetaDevEntityList;

  private KBMongoRepoImpl kbMongoRepoImpl;

  @Override
  public Map<String, List<KBTransferProductOutputDto>> call() {
    if (targetDate == null) {
      log.error("targetDate is null!");

      return null;
    }

    if (resultOutputPath == null) {
      log.error("resultOutputPath is null!");

      return null;
    }

    if (resultOutputFileName == null) {
      log.error("resultOutputFileName is null!");

      return null;
    }

    if (operateFullYearMonth == null) {
      log.error("operateFullYearMonth is null!");

      return null;
    }

    if (operateDateHourMinute == null) {
      log.error("operateDateHourMinute is null!");

      return null;
    }

    if (operateDateHyphen == null) {
      log.error("operateDateHyphen is null!");

      return null;
    }

    if (operateTimeHyphen == null) {
      log.error("operateTimeHyphen is null!");

      return null;
    }

    if (kbMetaDevEntityList == null || kbMetaDevEntityList.size() == 0) {
      log.error("kbMetaDevEntityList is null!");

      return null;
    }

    log.info(
      "DateWorker starts with list size - {}",
      kbMetaDevEntityList.size()
    );

    final JsonMapper jsonMapper = JsonMapper.builder().build();

    //productList.json 생성용 목록
    Map<String, List<KBTransferProductOutputDto>> productOutputDtoBySiteCodeMap = new ConcurrentHashMap<>();

    try {
      for (KBMetaDevEntity kbMetaDevEntity : kbMetaDevEntityList) {
        List<KBMongoCollection> kbMongoCollectionList = kbMongoRepoImpl.getKBMongoCollectionListByAgentIdAndCreateDateBetween(
          targetDate,
          kbMetaDevEntity.getAgentId()
        );

        if (kbMongoCollectionList.size() == 0) {
          log.error(
            "Cannot find crawled data. Target Date - {} / {}",
            targetDate,
            kbMetaDevEntity.toString()
          );

          continue;
        }

        log.info(
          "Target Date - {} / {} / Crawled Data size - {}",
          targetDate,
          kbMetaDevEntity.toString(),
          kbMongoCollectionList.size()
        );

        List<KBTransferResultOutputDto> resultOutputDtoList = new ArrayList<KBTransferResultOutputDto>();

        for (KBMongoCollection kbMongoCollection : kbMongoCollectionList) {
          //pr_name과 key가 빈 값이 아닌 항목만 저장
          if (
            StringUtils.hasText(kbMongoCollection.getPrName()) &&
            StringUtils.hasText(kbMongoCollection.getKey())
          ) {
            resultOutputDtoList.add(
              KBTransferResultOutputDto
                .builder()
                .productName(kbMongoCollection.getPrName())
                .siteCode(kbMetaDevEntity.getSiteCode())
                .categoryCode(kbMetaDevEntity.getCategoryCode())
                .summary(kbMongoCollection.getSummary())
                .crawlDate(kbMongoCollection.getCreateDate())
                .prCode(kbMongoCollection.getPrCode())
                .url(kbMongoCollection.getUrl())
                .valueTable(kbMongoCollection.getValueTable())
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
        }

        //result.json 파일 생성 시작
        //포맷 - /data/kb_guest/makeup/file/json/yyyyMM/ddHHmm/siteCode/categoryCode/json/
        //예시 - /data/kb_guest/makeup/file/json/202210/120005/002/C10227/json/
        //날짜는 대상 일자, 시간은 프로그램 실행 시점의 값이 입력 됨
        final String formattedResultOutputPath = String.format(
          resultOutputPath,
          operateFullYearMonth,
          operateDateHourMinute,
          kbMetaDevEntity.getSiteCode(),
          kbMetaDevEntity.getCategoryCode()
        );

        //포맷 - yyyy-MM-dd_HH-mm_siteCode_categoryCode_result.json
        //예시 - 2022-10-21_00-05_002_C10227_result.json
        //날짜는 대상 일자, 시간은 프로그램 실행 시점의 값이 입력 됨
        final String formattedResultOutputFileName = String.format(
          resultOutputFileName,
          operateDateHyphen,
          operateTimeHyphen,
          kbMetaDevEntity.getSiteCode(),
          kbMetaDevEntity.getCategoryCode()
        );

        try {
          if (!Files.isDirectory(Paths.get(formattedResultOutputPath))) {
            Files.createDirectories(Paths.get(formattedResultOutputPath));
          }

          Path path = Paths.get(
            String.format(
              "%s/%s",
              formattedResultOutputPath,
              formattedResultOutputFileName
            )
          );

          Files.deleteIfExists(path);
          Files.write(
            path,
            jsonMapper
              .writerWithDefaultPrettyPrinter()
              .writeValueAsBytes(resultOutputDtoList)
          );
        } catch (IOException ioe) {
          log.error(
            "Error with write result json. Target Date - {} / {}",
            targetDate,
            kbMetaDevEntity.toString()
          );
          log.error(ioe.getMessage(), ioe);
        }
        //result.json 파일 생성 종료

        //카테고리 코드 별 중복 제거한 상품 이름 목록 저장
        List<KBTransferProductOutputDto> productOutputDtoList = productOutputDtoBySiteCodeMap.getOrDefault(
          kbMetaDevEntity.getSiteCode(),
          new ArrayList<>()
        );

        //pr_name이 빈 값이 아닌 항목만 저장
        Set<String> productNameSet = kbMongoCollectionList
          .stream()
          .filter(
            kbMongoCollection ->
              StringUtils.hasText(kbMongoCollection.getPrName())
          )
          .map(KBMongoCollection::getPrName)
          .collect(Collectors.toSet());

        productOutputDtoList.add(
          KBTransferProductOutputDto
            .builder()
            .siteCode(kbMetaDevEntity.getSiteCode())
            .categoryCode(kbMetaDevEntity.getCategoryCode())
            .categoryName(kbMetaDevEntity.getCategoryName())
            .categoryType(kbMetaDevEntity.getCategoryType())
            .count(productNameSet.size())
            .productName(productNameSet)
            .build()
        );

        productOutputDtoBySiteCodeMap.put(
          kbMetaDevEntity.getSiteCode(),
          productOutputDtoList
        );
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    log.info("DateWorker ends with list size - {}", kbMetaDevEntityList.size());

    return productOutputDtoBySiteCodeMap;
  }
}
