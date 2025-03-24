package com.everyplaceinkorea.epik_boot3_api.admin.contents.musical.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MusicalResponseDto {
    private Long id; // 등록된 뮤지컬의 식별 번호
    private String title; // 제목
    private String content; // 내용
    private String writer; // 작성자
    private String address; // 주소
    private String venue; // 장소
    private LocalDate startDate; // 시작일
    private LocalDate endDate; // 종료일
    private String saveImageName; // 저장된 이미지 경로
    private String runningTime; // 관람시간
    private String ageRestriction; // 관람 연령
    private List<MusicalTicketPriceDto> ticketPrices; // 티켓 금액
    private List<MusicalTicketOfficeDto> ticketOffices; // 티켓 예매처
    private LocalDateTime writeDate; // 등록일

}

/*
* 응답 데이터 예시
* {
    "id": 93,
    "title": "saveAll테스트",
    "content": "This is a description of the musical.",
    "writer": "admin2",
    "address": "1234 Theater St.",
    "venue": "",
    "startDate": "2024-12-01",
    "endDate": "2024-12-31",
    "imgSrc": "C:\\upload\\2024\\11\\13\\f295a4aa-efe4-46d8-8773-419564b4ba9b_동글이.jpg",
    "runningTime": 120,
    "ageRestriction": "All Ages",
    "ticketPrices": [
        {
            "id": 181,
            "seat": "saveAll테스트",
            "price": 17778,
            "musicalId": 93
        },
        {
            "id": 182,
            "seat": "saveAll테스트",
            "price": 17778,
            "musicalId": 93
        }
    ],
    "ticketOffices": [
        {
            "id": 205,
            "name": "17778",
            "link": "https://ticketmaster.com/example/saveAll테스트",
            "musicalId": 93
        },
        {
            "id": 206,
            "name": "17778",
            "link": "https://interpark.com/example/saveAll테스트",
            "musicalId": 93
        }
    ],
    "writeDate": "2024-11-13T00:52:15.755066"
}
*
* */