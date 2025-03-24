package com.everyplaceinkorea.epik_boot3_api.repository.feed;

import com.everyplaceinkorea.epik_boot3_api.entity.feed.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {
  List<Feed> findAllByMemberId(Long memberId);
  // 전제조회
  @Query("SELECT f FROM Feed f WHERE (:lastId IS NULL OR f.id > :lastId) ORDER BY f.id ASC")
  List<Feed> findFeedsByLastId(@Param("lastId") Long lastId, Pageable pageable);

  List<Feed> findAllByCategoryId(Long categoryId);
}
