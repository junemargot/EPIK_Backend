package com.everyplaceinkorea.epik_boot3_api.member.feed.controller;

import com.everyplaceinkorea.epik_boot3_api.member.feed.dto.FeedCreateDto;
import com.everyplaceinkorea.epik_boot3_api.member.feed.dto.FeedUpdateDto;
import com.everyplaceinkorea.epik_boot3_api.member.feed.sesrvice.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 회원 피드 요청 api
 */

@RestController
@RequestMapping("feed")
@RequiredArgsConstructor
@Slf4j
public class FeedController {

    private final FeedService feedService;

    // 피드 등록
    @PostMapping
    public ResponseEntity<Long> create(@RequestPart("request") FeedCreateDto feedCreateDto,
                                       @RequestPart MultipartFile[] files) {
        log.info("Create feed: {}", feedCreateDto.toString());
        log.info("File size: {}", files.length);
        // 파일 리스트 순회
        for (MultipartFile file : files) {
            // 파일명 출력
            String fileName = file.getOriginalFilename();
            log.info("fileName : {}", fileName);
        }

//        return null;
        return ResponseEntity.status(HttpStatus.OK)
                .body(feedService.create(feedCreateDto, files));
    }

    // 피드 삭제(soft)
    @DeleteMapping("{feedId}")
    public ResponseEntity<Void> delete(@PathVariable("feedId") Long id) {
        feedService.delete(id);
        return ResponseEntity.noContent().build();
    }


    // 피드 수정
    @PatchMapping("{feedId}")
    public ResponseEntity<Void> update(@PathVariable("feedId") Long id, FeedUpdateDto feedUpdateDto) {
        feedService.update(id, feedUpdateDto);
        return ResponseEntity.noContent().build();
    }

    // 좋아요 완료
    @PostMapping("{feedId}/like")
    public ResponseEntity<Void> like(@PathVariable Long feedId) {
        feedService.likeFeed(feedId);
        return ResponseEntity.noContent().build();
    }

    // 좋아요 취소
    @DeleteMapping("{feedId}/like")
    public ResponseEntity<Void> unlike(@PathVariable Long feedId) {
        feedService.unLikeFeed(feedId);
        return ResponseEntity.noContent().build();
    }

    // 마이 피드 조회
}
