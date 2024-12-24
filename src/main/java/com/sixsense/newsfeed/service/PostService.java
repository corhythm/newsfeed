package com.sixsense.newsfeed.service;

import com.sixsense.newsfeed.config.jwt.TokenProvider;
import com.sixsense.newsfeed.domain.Post;
import com.sixsense.newsfeed.domain.User;
import com.sixsense.newsfeed.dto.CreatePostRequestDto;
import com.sixsense.newsfeed.dto.PostResponseDto;
import com.sixsense.newsfeed.dto.UpdatePostRequestDto;
import com.sixsense.newsfeed.error.ErrorCode;
import com.sixsense.newsfeed.error.exception.base.AccessDeniedException;
import com.sixsense.newsfeed.error.exception.base.NotFoundException;
import com.sixsense.newsfeed.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final PostRepository postRepository;

    @Transactional
    // 게시글 생성
    public PostResponseDto createPost(CreatePostRequestDto dto, String token) {
        Long userId = tokenProvider.getUserId(token);
        User currentUser = userService.getById(userId);

        // 게시글 생성
        Post createdPost = postRepository.save(dto.toEntity(currentUser));
        return PostResponseDto.fromEntity(createdPost);
    }

    public Page<PostResponseDto> findAllMyPosts(String token, Pageable pageable) {
        Long userId = tokenProvider.getUserId(token);

        return postRepository.findAllByUserId(userId, pageable)
                .map(PostResponseDto::fromEntity);
    }

    public Page<PostResponseDto> findAll(Pageable pageable) {

        return postRepository.findAllByOrderByUpdatedAtDesc(pageable)
                .map(PostResponseDto::fromEntity);
    }

    public PostResponseDto findById(Long id) {
        Post findPost = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));
        return PostResponseDto.fromEntity(findPost);
    }

    public PostResponseDto update(Long id,
                                  UpdatePostRequestDto requestDto,
                                  String token) {

        Long userId = tokenProvider.getUserId(token);
//        User currentUser = userService.getById(userId);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(ErrorCode.POST_ACCESS_DENIED);
        }

        post.update(requestDto.getContent(), requestDto.getImgUrl());

        postRepository.save(post);

        return new PostResponseDto(
                post.getId(),
                post.getUser().getId(),         // User ID
                post.getUser().getName(),
                post.getContent(),
                post.getImgUrl(),
                post.getUpdatedAt()
        );
    }

    public void delete(Long id, String token) {
        Long userId = tokenProvider.getUserId(token);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(ErrorCode.POST_ACCESS_DENIED);
        }

        post.setIsDeleted(true);

        postRepository.save(post);
    }

}
