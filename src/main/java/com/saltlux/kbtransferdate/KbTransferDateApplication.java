package com.saltlux.kbtransferdate;

import com.saltlux.kbtransferdate.service.DateService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
   *
   * 날짜는 수집 대상의 날짜, 시간은 프로그램 실행 시간을 사용
   */
  private static void setRuntimeVariables(String targetDate) {
    LocalDateTime now = LocalDateTime.now();
    LocalDate targetLocalDate = LocalDate.parse(
      targetDate,
      DateTimeFormatter.ofPattern("yyyyMMdd")
    );

    MDC.put("THREAD_WAIT_TIME_MILI", String.valueOf(THREAD_WAIT_TIME_MILI));
    MDC.put(
      "OPERATE_yyyyMM",
      targetLocalDate.format(DateTimeFormatter.ofPattern("yyyyMM"))
    );
    MDC.put(
      "OPERATE_ddHHmm",
      String.format(
        "%s%s",
        targetLocalDate.format(DateTimeFormatter.ofPattern("dd")),
        now.format(DateTimeFormatter.ofPattern("HHmm"))
      )
    );
    MDC.put(
      "OPERATE_yyyy-MM-dd",
      targetLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    );
    MDC.put("OPERATE_HH-mm", now.format(DateTimeFormatter.ofPattern("HH-mm")));
  }

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println(
        "Please insert 'target date'(yyyyMMdd) ['agentId'(6 number)]"
      );

      System.exit(0);
    }

    setRuntimeVariables(args[0]);

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

    //sercice 클래스 실행, Runnable이지만 신규 스레드로 실행하지 않고 직접 호출
    context.getBean(DateService.class).run();

    double spendTime = (System.currentTimeMillis() - startTime) / 1000.0;

    log.info(
      String.format(
        "All threads are terminated!!!\tTime spended %,.03f secs.",
        spendTime
      )
    );
  }
}
