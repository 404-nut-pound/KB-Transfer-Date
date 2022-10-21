package com.saltlux.kbtransferdate.service;

import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
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

  private String targetDate = null;

  private int targetAgentId = 0;

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

    if (targetAgentId == 0) {
      log.info("Target AgentId is null!");

      return;
    }

    log.info("AgentService starts with [{} / {}]", targetDate, targetAgentId);

    log.info("AgentService ends with [{} / {}]", targetDate, targetAgentId);
  }
}
