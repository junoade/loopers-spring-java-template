package com.loopers.ranking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 랭킹 키를 정의합니다.
 */
public final class RankingKey {

    /**
     * 일별 랭킹 레디스키를 정의합니다.
     * ranking:all:{yyyyMMdd}
     * @param date
     * @return
     */
    public static String dailyAll(LocalDate date) {
        return "ranking:all:" + date.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

}
