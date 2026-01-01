package com.loopers.batch.job.rankWeekly.step.dto;

public record WeeklyAggRow (
        String yearWeek, // e.g. 2026-W01
        Long productId,
        long viewCount,
        long likeCount,
        long orderCount
) { }
