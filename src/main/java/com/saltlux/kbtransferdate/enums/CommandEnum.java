package com.saltlux.kbtransferdate.enums;

import com.saltlux.kbtransferdate.service.AgentService;
import com.saltlux.kbtransferdate.service.DateService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandEnum {
  AGENT(
    "일자/에이전트별 생성",
    3,
    "agent date(yyyyMMdd) agentId",
    AgentService.class
  ),
  DATE("일자별 생성", 2, "date date(yyyyMMdd)", DateService.class);

  private final String descript;

  private final int minArgsLength;

  private final String commandFormat;

  private final Class<? extends Runnable> serviceClass;

  public static List<String> getCommandNameList() {
    return Stream
      .of(CommandEnum.values())
      .map(Enum::name)
      .collect(Collectors.toList());
  }

  public static List<String> getCommandList() {
    return Stream
      .of(CommandEnum.values())
      .map(
        commandEnum ->
          String.format("%s(%s)", commandEnum.name(), commandEnum.getDescript())
      )
      .collect(Collectors.toList());
  }
}
