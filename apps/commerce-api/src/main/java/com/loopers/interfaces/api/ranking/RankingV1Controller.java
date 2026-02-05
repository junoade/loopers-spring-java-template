package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingPeriod;
import com.loopers.application.ranking.RankingQueryResponse;
import com.loopers.application.ranking.RankingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ranking")
public class RankingV1Controller {

    private final RankingQueryService rankingQueryService;

    @GetMapping
    public RankingQueryResponse getDailyRanking(
            @RequestParam(defaultValue = "DAILY", name = "period") RankingPeriod period,
            @RequestParam(required = false, name = "date") String date,
            @RequestParam(defaultValue = "20", name = "size") int size
    ) {
        return rankingQueryService.getDailyPopularProducts(period, date, size);
    }

}
