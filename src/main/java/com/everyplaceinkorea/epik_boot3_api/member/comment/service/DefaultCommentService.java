package com.everyplaceinkorea.epik_boot3_api.member.comment.service;

import com.everyplaceinkorea.epik_boot3_api.entity.comment.FeedComment;
import com.everyplaceinkorea.epik_boot3_api.entity.feed.Feed;
import com.everyplaceinkorea.epik_boot3_api.entity.member.Member;
import com.everyplaceinkorea.epik_boot3_api.member.comment.dto.CommentCreateDto;
import com.everyplaceinkorea.epik_boot3_api.member.comment.dto.CommentResponseDto;
import com.everyplaceinkorea.epik_boot3_api.member.comment.dto.CommentUpdateDto;
import com.everyplaceinkorea.epik_boot3_api.repository.Member.MemberRepository;
import com.everyplaceinkorea.epik_boot3_api.repository.comment.FeedCommentRepository;
import com.everyplaceinkorea.epik_boot3_api.repository.feed.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultCommentService implements CommentService{

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public Long create(Long feedId, Long memberId, CommentCreateDto commentCreateDto) {
        // 댓글에는 어느 회원이 어느 피드에 댓글을 어떠한 댓글을 작성했는지가 기록이 되어야함
        //일단 어느 회원
        Member member = memberRepository.findById(memberId).orElseThrow();
        Feed feed = feedRepository.findById(feedId).orElseThrow();

        FeedComment feedComment = FeedComment.builder()
                .content(commentCreateDto.getContent())
                .feed(feed)
                .member(member)
                .build();

        feedCommentRepository.save(feedComment);

        return null;
    }

    // 피드 댓글 전체 조회
    @Override
    public List<CommentResponseDto> getList(Long feedId) {
        // 댓글 테이블에서 피드번호에 해당하는 모든 댓글 조회
        List<FeedComment> allByFeedId = feedCommentRepository.findAllByFeedId(feedId);

        // 여기 뽑아온걸
        List<CommentResponseDto> commentDtos = allByFeedId
                .stream()
                .map(comment -> {
                    CommentResponseDto commentDto = modelMapper.map(comment, CommentResponseDto.class);
                    commentDto.setId(comment.getId());
                    commentDto.setWriter(comment.getMember().getNickname());
                    commentDto.setContent(comment.getContent());
                    commentDto.setWriteDate(comment.getWriteDate());
                    return commentDto;
                }).toList();

        return commentDtos;
    }

    @Transactional
    @Override
    public void update(Long commentId, CommentUpdateDto updateDto) {
        // 댓글 수정
        FeedComment feedComment = feedCommentRepository.findById(commentId).orElseThrow();
        feedComment.update(updateDto);
    }

    @Transactional
    @Override
    public void delete(Long commentId) {
        FeedComment feedComment = feedCommentRepository.findById(commentId).orElseThrow();
        feedComment.delete();
    }
}
