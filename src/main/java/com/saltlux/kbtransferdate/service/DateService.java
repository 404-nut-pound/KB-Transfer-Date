package com.saltlux.kbtransferdate.service;

import com.saltlux.kbtransferdate.entity.KBMetaDevEntity;
import com.saltlux.kbtransferdate.repo.KBMetaDevQueryRepo;
import com.saltlux.kbtransferdate.repo.KBMongoRepoImpl;
import com.saltlux.kbtransferdate.util.AppUtil;
import com.saltlux.kbtransferdate.worker.DateWorker;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

  private String targetDate = null;

  private Integer targetAgentId = null;

  @Value("${com.saltlux.kb-transfer-date.output-path}")
  private String outputPath;

  @Value("${com.saltlux.kb-transfer-date.output-file-name}")
  private String outputFileName;

  private final long threadWaitTimeMili = Long.parseLong(
    MDC.get("THREAD_WAIT_TIME_MILI").toString()
  );
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

  @Autowired
  private KBMetaDevQueryRepo kbMetaDevQueryRepo;

  @Autowired
  private KBMongoRepoImpl kbMongoRepoImpl;

  public DateService() {
    Object targetDate = MDC.get("arg0");

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

    Object targetAgentId = MDC.get("arg1");

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
      log.error("Target Date is null!");

      return;
    }

    log.info("DateService starts with [{}]", targetDate);

    try {
      if (targetAgentId == null) {
        //전체 AgentId를 대상으로 동작
        List<KBMetaDevEntity> kbMetaDevEntityList = kbMetaDevQueryRepo.getActivatedMetaListAll();

        //실행 환경의 thread 수에 따라 분할한 목록
        List<List<KBMetaDevEntity>> blockedKBMetaDevEntityList = AppUtil.getBlockedList(
          kbMetaDevEntityList
        );

        //thread 관리자 생성
        ExecutorService executorService = Executors.newFixedThreadPool(
          blockedKBMetaDevEntityList.size()
        );

        //실행 환경의 thread 수 만큼 worker 실행
        for (List<KBMetaDevEntity> kbMetaDevEntitiyList : blockedKBMetaDevEntityList) {
          executorService.submit(
            DateWorker
              .builder()
              .targetDate(targetDate)
              .outputPath(outputPath)
              .outputFileName(outputFileName)
              .operateFullYearMonth(operateFullYearMonth)
              .operateDateHourMinute(operateDateHourMinute)
              .operateDateHyphen(operateDateHyphen)
              .operateTimeHyphen(operateTimeHyphen)
              .kbMetaDevEntityList(kbMetaDevEntitiyList)
              .kbMongoRepoImpl(kbMongoRepoImpl)
              .build()
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
      } else {
        //특정 AgentId를 대상으로 동작
        Optional<KBMetaDevEntity> optionalKBMetaDevEntity = kbMetaDevQueryRepo.getActivatedMetaByAgentId(
          targetAgentId
        );

        if (!optionalKBMetaDevEntity.isPresent()) {
          log.error("Cannot find metadata by AgentId - {}", targetAgentId);

          return;
        }

        KBMetaDevEntity kbMetaDevEntity = optionalKBMetaDevEntity.get();

        //단일 대상이므로 Thread 처리하지 않고 직업 호출
        DateWorker
          .builder()
          .targetDate(targetDate)
          .outputPath(outputPath)
          .outputFileName(outputFileName)
          .operateFullYearMonth(operateFullYearMonth)
          .operateDateHourMinute(operateDateHourMinute)
          .operateDateHyphen(operateDateHyphen)
          .operateTimeHyphen(operateTimeHyphen)
          .kbMetaDevEntityList(Arrays.asList(kbMetaDevEntity))
          .kbMongoRepoImpl(kbMongoRepoImpl)
          .build()
          .run();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    log.info("DateService ends with [{}]", targetDate);
  }
}
