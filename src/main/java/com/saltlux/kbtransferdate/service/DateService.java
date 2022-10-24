package com.saltlux.kbtransferdate.service;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.saltlux.kbtransferdate.dto.KBTransferProductOutputDto;
import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import com.saltlux.kbtransferdate.repo.KBMetaDevQueryRepo;
import com.saltlux.kbtransferdate.repo.KBMongoRepoImpl;
import com.saltlux.kbtransferdate.util.AppUtil;
import com.saltlux.kbtransferdate.worker.DateWorker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 입력받는 명령어
 * <p/>
 * 0 - date<p/>
 * 1 - date(yyyyMMdd)
 */
@Component
@Slf4j
public class DateService implements Runnable {

  private Object targetDate = MDC.get("arg0"), targetAgentId = MDC.get("arg1");

  @Value("${com.saltlux.kb-transfer-date.result-output-path}")
  private String resultOutputPath;

  @Value("${com.saltlux.kb-transfer-date.result-output-file-name}")
  private String resultOutputFileName;

  @Value("${com.saltlux.kb-transfer-date.product-output-path}")
  private String productOutputPath;

  @Value("${com.saltlux.kb-transfer-date.product-output-file-name}")
  private String productOutputFileName;

  private final long threadWaitTimeMili = Long.parseLong(
    MDC.get("THREAD_WAIT_TIME_MILI").toString()
  );
  private String operateFullYearMonth = MDC.get("OPERATE_yyyyMM").toString();
  private String operateDateHourMinute = MDC.get("OPERATE_ddHHmm").toString();
  private String operateDateHyphen = MDC.get("OPERATE_yyyy-MM-dd").toString();
  private String operateTimeHyphen = MDC.get("OPERATE_HH-mm").toString();

  @Autowired
  private KBMetaDevQueryRepo kbMetaDevQueryRepo;

  @Autowired
  private KBMongoRepoImpl kbMongoRepoImpl;

  @Override
  public void run() {
    String targetDateStr = null;
    Integer targetAgentIdInt = 0;

    if (
      targetDate != null &&
      Pattern
        .compile("\\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])")
        .matcher(targetDate.toString())
        .matches()
    ) {
      targetDateStr = targetDate.toString();
    } else {
      log.error(
        "Target Date is null or not formatted with LocalDate(yyyyMMdd)."
      );

      return;
    }

    if (
      targetAgentId != null &&
      Pattern.compile("\\d{6}").matcher(targetAgentId.toString()).matches()
    ) {
      targetAgentIdInt = Integer.parseInt(targetAgentId.toString());
    } else {
      log.info("Target AgentId is null or not number.");
    }

    log.info("DateService starts with {}", targetDateStr);

    final DefaultPrettyPrinter defaultPrettyPrinter = new DefaultPrettyPrinter();
    defaultPrettyPrinter.indentArraysWith(
      DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
    );

    final JsonMapper jsonMapper = JsonMapper
      .builder()
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .defaultPrettyPrinter(defaultPrettyPrinter)
      .build();

    //thread 관리자 생성
    ExecutorService executorService = Executors.newCachedThreadPool();

    List<Future<Map<String, List<KBTransferProductOutputDto>>>> futureList = new ArrayList<>();

    try {
      if (targetAgentIdInt == 0) {
        //전체 AgentId를 대상으로 동작
        List<KBMetaDevEntity> kbMetaDevEntityList = kbMetaDevQueryRepo.getActivatedMetaListAll();

        //실행 환경의 thread 수에 따라 분할한 목록
        List<List<KBMetaDevEntity>> blockedKBMetaDevEntityList = AppUtil.getBlockedList(
          kbMetaDevEntityList
        );

        //실행 환경의 thread 수 만큼 worker 실행
        for (List<KBMetaDevEntity> kbMetaDevEntitiyList : blockedKBMetaDevEntityList) {
          futureList.add(
            executorService.submit(
              DateWorker
                .builder()
                .targetDate(targetDateStr)
                .resultOutputPath(resultOutputPath)
                .resultOutputFileName(resultOutputFileName)
                .operateFullYearMonth(operateFullYearMonth)
                .operateDateHourMinute(operateDateHourMinute)
                .operateDateHyphen(operateDateHyphen)
                .operateTimeHyphen(operateTimeHyphen)
                .kbMetaDevEntityList(kbMetaDevEntitiyList)
                .kbMongoRepoImpl(kbMongoRepoImpl)
                .build()
            )
          );
        }
      } else {
        log.info("Target AgentId - {}", targetAgentIdInt);

        //특정 AgentId를 대상으로 동작
        Optional<KBMetaDevEntity> optionalKBMetaDevEntity = kbMetaDevQueryRepo.getActivatedMetaByAgentId(
          targetAgentIdInt
        );

        if (!optionalKBMetaDevEntity.isPresent()) {
          log.error("Cannot find metadata by AgentId - {}", targetAgentIdInt);

          return;
        }

        KBMetaDevEntity kbMetaDevEntity = optionalKBMetaDevEntity.get();

        futureList.add(
          executorService.submit(
            DateWorker
              .builder()
              .targetDate(targetDateStr)
              .resultOutputPath(resultOutputPath)
              .resultOutputFileName(resultOutputFileName)
              .operateFullYearMonth(operateFullYearMonth)
              .operateDateHourMinute(operateDateHourMinute)
              .operateDateHyphen(operateDateHyphen)
              .operateTimeHyphen(operateTimeHyphen)
              .kbMetaDevEntityList(Arrays.asList(kbMetaDevEntity))
              .kbMongoRepoImpl(kbMongoRepoImpl)
              .build()
          )
        );
      }

      executorService.shutdown();

      while (
        !executorService.awaitTermination(
          threadWaitTimeMili,
          TimeUnit.MILLISECONDS
        )
      ) {
        //worker 클래스가 종료될 때까지 대기
      }

      //productList 병합 및 생성
      log.info("DateService starts writing productList.");

      //siteCode 별로 생성된 productOutputDtoList를 병합 처리
      Map<String, List<KBTransferProductOutputDto>> finalProductOutputDtoMap = new ConcurrentHashMap<>();

      for (Future<Map<String, List<KBTransferProductOutputDto>>> future : futureList) {
        Map<String, List<KBTransferProductOutputDto>> productOutputDtoMap = future.get();

        productOutputDtoMap.forEach(
          (siteCode, productOutputDtoList) ->
            finalProductOutputDtoMap.merge(
              siteCode,
              productOutputDtoList,
              (finalList, eachList) -> {
                finalList.addAll(eachList);

                return finalList;
              }
            )
        );
      }

      //productList.json 파일 생성 시작
      finalProductOutputDtoMap.forEach(
        (siteCode, productOutputDtoList) -> {
          //포맷 - /data/kb_guest/makeup/file/json/yyyyMM/ddHHmm/siteCode/
          //예시 - /data/kb_guest/makeup/file/json/202210/120005/002/
          //날짜와 시간은 프로그램 실행 시점의 값이 입력 됨
          final String formattedProductOutputPath = String.format(
            productOutputPath,
            operateFullYearMonth,
            operateDateHourMinute,
            siteCode
          );

          //포맷 - yyyy-MM-dd_HH-mm_siteCode_productList.json
          //예시 - 2022-10-21_00-05_002_productList.json
          //날짜와 시간은 프로그램 실행 시점의 값이 입력 됨
          final String formattedProductOutputFileName = String.format(
            productOutputFileName,
            operateDateHyphen,
            operateTimeHyphen,
            siteCode
          );

          try {
            if (!Files.isDirectory(Paths.get(formattedProductOutputPath))) {
              Files.createDirectories(Paths.get(formattedProductOutputPath));
            }

            Path path = Paths.get(
              String.format(
                "%s/%s",
                formattedProductOutputPath,
                formattedProductOutputFileName
              )
            );

            Files.deleteIfExists(path);
            Files.write(
              path,
              jsonMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(productOutputDtoList)
            );
          } catch (IOException ioe) {
            log.error(
              "Error with write result json. Target Date - {} / SiteCode {}",
              targetDate,
              siteCode
            );
            log.error(ioe.getMessage(), ioe);
          }
        }
      );

      log.info("DateService ends writing productList.");
      //productList.json 파일 생성 종료
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    log.info("DateService ends with {}", targetDate);
  }
}
