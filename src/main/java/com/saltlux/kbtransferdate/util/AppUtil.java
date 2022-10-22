package com.saltlux.kbtransferdate.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppUtil {

  /**
   * 날짜 형식의 String을 Date 객체로 변환
   * @param date
   * @return
   */
  public static Date getDateFromDateString(final String date) {
    return Date.from(
      LocalDate
        .parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))
        .atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant()
    );
  }

  /**
   * LocalDate 객체를 Date 객체로 변환
   * @param localDate
   * @return
   */
  public static Date getDateFromLocalDate(final LocalDate localDate) {
    return getDateFromDateString(
      localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    );
  }

  private static final int AVAILABLE_PROCESSORS = Runtime
    .getRuntime()
    .availableProcessors();

  /**
   * 프로세서(스레드) 기준으로 List를 분할
   *
   * @param <T>
   * @return
   * @throws IOException
   */
  public static <T> List<List<T>> getBlockedList(List<T> t) {
    List<List<T>> blockedPathList = new ArrayList<>(AVAILABLE_PROCESSORS);

    try {
      int blockSize = 0, blockIndex = 0, repeatLimit = AVAILABLE_PROCESSORS >
        t.size()
        ? t.size()
        : AVAILABLE_PROCESSORS;

      //프로세서 수 보다 파일 개수가 적을 때 blockSize 는 ceil 처리로 무조건 1 이상이기 때문에 index 오류 발생
      //분모를 파일 개수로 설정
      blockSize = (int) Math.ceil((double) t.size() / repeatLimit);

      log.info(
        "target files - {} / repeat limit - {} / block size - {}",
        t.size(),
        repeatLimit,
        blockSize
      );

      for (
        blockIndex = 0;
        blockIndex < repeatLimit - 1 &&
        ((blockIndex + 1) * blockSize) < t.size();
        blockIndex++
      ) {
        //반복문으로 각 blockSize만큼 subList하여 추가
        blockedPathList.add(
          t.subList(blockIndex * blockSize, (blockIndex + 1) * blockSize)
        );
      }

      //반복문 종료 후 남은 요소를 추가
      blockedPathList.add(t.subList(blockIndex * blockSize, t.size()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return blockedPathList;
  }
}
