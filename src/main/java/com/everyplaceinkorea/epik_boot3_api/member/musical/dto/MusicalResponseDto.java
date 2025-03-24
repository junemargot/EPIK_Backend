package com.everyplaceinkorea.epik_boot3_api.member.musical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MusicalResponseDto {
    // 번호
    private Long id;
    // 제목
    private String title;
    // 시작일
    private LocalDate startDate;
    // 종료일
    private LocalDate endDate;
    // 장소
    private String venue;
    // 사진
    private String saveImageName;
}
