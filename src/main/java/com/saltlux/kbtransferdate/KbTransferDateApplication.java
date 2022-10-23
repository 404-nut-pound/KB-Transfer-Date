package com.saltlux.kbtransferdate;

import com.saltlux.kbtransferdate.service.DateService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class KbTransferDateApplication {

  private static final long THREAD_WAIT_TIME_MILI = 5 * 1000;

  /**
   * 공통 변수 설정용, 여러 스레드에서 공유 가능
   * 단, 신규 스레드(runnable run, callable call, new Class() 등)에서는 사용 불가
   */
  private static void setRuntimeVariables() {
    MDC.put("THREAD_WAIT_TIME_MILI", String.valueOf(THREAD_WAIT_TIME_MILI));
    MDC.put(
      "OPERATE_yyyyMM",
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
    );
    MDC.put(
      "OPERATE_ddHHmm",
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddHHmm"))
    );
    MDC.put(
      "OPERATE_yyyy-MM-dd",
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    );
    MDC.put(
      "OPERATE_HH-mm",
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm"))
    );
  }

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println(
        "Please insert 'target date'(yyyyMMdd) ['agentId'(6 number)]"
      );

      System.exit(0);
    }

    setRuntimeVariables();

    for (int i = 0; i < args.length; i++) {
      MDC.put(String.format("arg%d", i), args[i]);
    }

    long startTime = System.currentTimeMillis();

    log.info(
      "========== Start KB Transfer by Date for '{}' ==========",
      args[0]
    );

    //spring context 실행
    ConfigurableApplicationContext context = SpringApplication.run(
      KbTransferDateApplication.class,
      args
    );

    //thread 관리자 생성
    ExecutorService executorService = Executors.newCachedThreadPool();

    //sercice 클래스 실행
    executorService.submit(context.getBean(DateService.class));

    executorService.shutdown();

    //service 클래스가 종료될 때까지 대기
    while (
      !executorService.awaitTermination(
        THREAD_WAIT_TIME_MILI,
        TimeUnit.MILLISECONDS
      )
    ) {
      log.info(
        "Wait for threads are terminated for {} miliseconds. Remaining thread count - {}",
        THREAD_WAIT_TIME_MILI,
        Thread.activeCount()
      );
    }

    double spendTime = (System.currentTimeMillis() - startTime) / 1000.0;

    log.info(
      String.format(
        "All threads are terminated!!!\tTime spended %,.03f secs.",
        spendTime
      )
    );
  }
}
