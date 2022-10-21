package com.saltlux.kbtransferdate;

import com.saltlux.kbtransferdate.enums.CommandEnum;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

  private static final List<String> commandNameList = CommandEnum.getCommandNameList();
  private static final List<String> commandList = CommandEnum.getCommandList();

  /**
   * 공통 변수 설정용, 여러 스레드에서 공유 가능
   */
  private static void setRuntimeVariables() {
    MDC.put("THREAD_WAIT_TIME_MILI", String.valueOf(THREAD_WAIT_TIME_MILI));
    MDC.put(
      "OPERATE_FULLYEAR_MONTH",
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
    );
    MDC.put(
      "OPERATE_DATE_HOUR_MINUTE",
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddHHmm"))
    );
    MDC.put(
      "OPERATE_DATE_HYPHEN",
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    );
    MDC.put(
      "OPERATE_TIME_HOUR_MINUTE_HYPHEN",
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm"))
    );
  }

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println(
        String.format("Please insert command - %s", commandList)
      );

      System.exit(0);
    }

    if (!commandNameList.contains(args[0].toUpperCase())) {
      System.out.println(
        String.format("Please check command list. - %s", commandList)
      );

      System.exit(0);
    }

    CommandEnum commandEnum = CommandEnum.valueOf(args[0].toUpperCase());

    if (args.length < commandEnum.getMinArgsLength()) {
      System.out.println(
        String.format(
          "Please insert correct commands - [%s]",
          commandEnum.getCommandFormat()
        )
      );

      System.exit(0);
    }

    setRuntimeVariables();

    for (int i = 0; i < args.length; i++) {
      MDC.put(String.format("arg%d", i), args[i]);
    }

    long startTime = System.currentTimeMillis();

    log.info("========== Start nia_corpus_tools for '{}' ==========", args[0]);

    //spring context 실행
    ConfigurableApplicationContext context = SpringApplication.run(
      KbTransferDateApplication.class,
      args
    );

    //thread 관리자 생성
    ExecutorService executorService = Executors.newCachedThreadPool();

    //CommandEnum에 할당된 sercice 클래스 실행
    executorService.submit(context.getBean(commandEnum.getServiceClass()));

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
