package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingQueryService;
import com.loopers.ranking.DailyRankingResponse;
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
    public DailyRankingResponse getDailyRanking(
            @RequestParam(required = false, name = "date") String date,
            @RequestParam(defaultValue = "20", name = "size") int size
    ) {
        return rankingQueryService.getDailyPopularProducts(date, size);
    }

}
