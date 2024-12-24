package com.sixsense.newsfeed.controller;

import com.sixsense.newsfeed.dto.CreatePostRequestDto;
import com.sixsense.newsfeed.dto.PostResponseDto;
import com.sixsense.newsfeed.dto.UpdatePostRequestDto;
import com.sixsense.newsfeed.service.PostService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sixsense.newsfeed.constant.Token.AUTHORIZATION_HEADER;

@RestController
@AllArgsConstructor
@RequestMapping("/api/feeds")
public class PostApiController {
    private final PostService postService;

    // 글작성
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @Valid @RequestBody CreatePostRequestDto dto,
            @RequestHeader(AUTHORIZATION_HEADER) String accessToken) {
        PostResponseDto responseDto = postService.createPost(dto, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<Page<PostResponseDto>> findAllMyPosts(
            @RequestHeader(AUTHORIZATION_HEADER) String accessToken,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostResponseDto> allMyPosts = postService.findAllMyPosts(accessToken, pageable);
        return ResponseEntity.ok(allMyPosts);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> findAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page - 1 , size);
        Page<PostResponseDto> findAll = postService.findAll(pageable);
        return ResponseEntity.ok(findAll);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> findById(@PathVariable Long id){
        PostResponseDto postDetails = postService.findById(id);
        return new ResponseEntity<>(postDetails,HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> Update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequestDto requestDto,
            @RequestHeader(AUTHORIZATION_HEADER) String accessToken
    ){
        PostResponseDto update = postService.update(id, requestDto, accessToken);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(AUTHORIZATION_HEADER) String accessToken
    ){
        postService.delete(id,accessToken);
        return ResponseEntity.noContent().build();
    }
}
