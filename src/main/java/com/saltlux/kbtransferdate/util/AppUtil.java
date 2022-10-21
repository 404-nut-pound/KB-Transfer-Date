package com.saltlux.kbtransferdate.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
}
