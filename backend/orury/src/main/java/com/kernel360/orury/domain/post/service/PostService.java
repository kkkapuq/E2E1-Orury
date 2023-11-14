package com.kernel360.orury.domain.post.service;

import com.kernel360.orury.domain.board.db.BoardRepository;
import com.kernel360.orury.domain.post.db.PostEntity;
import com.kernel360.orury.domain.post.db.PostRepository;
import com.kernel360.orury.domain.post.model.PostDto;
import com.kernel360.orury.domain.post.model.PostRequest;
import com.kernel360.orury.domain.user.db.UserEntity;
import com.kernel360.orury.domain.user.db.UserRepository;
import com.kernel360.orury.global.common.Api;
import com.kernel360.orury.global.common.Pagination;
import com.kernel360.orury.global.constants.Constant;
import com.kernel360.orury.global.message.errors.ErrorMessages;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final PostConverter postConverter;
    private final UserRepository userRepository;

    public PostDto createPost(
            PostRequest postRequest,
            String userEmail
    ) {
        var user = userRepository.findByEmailAddr(userEmail)
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.THERE_IS_NO_USER.getMessage() + userEmail));

        var boardEntity = boardRepository.findById(postRequest.getBoardId())
                .orElseThrow(() -> new RuntimeException(ErrorMessages.THERE_IS_NO_BOARD.getMessage()));

        var entity = PostEntity.builder()
                .postTitle(postRequest.getPostTitle())
                .postContent(postRequest.getPostContent())
                .userId(user.getId())
                .board(boardEntity)
                .thumbnailUrl(postRequest.getPostImageList().isEmpty() ? null : postRequest.getPostImageList().get(0))
                .images(postRequest.getPostImageList().isEmpty() ? null : String.join(",", postRequest.getPostImageList()))
                .createdBy(Constant.ADMIN.getMessage())
                .createdAt(LocalDateTime.now())
                .updatedBy(Constant.ADMIN.getMessage())
                .updatedAt(LocalDateTime.now())
                .build();
        var saveEntity = postRepository.save(entity);

        return postConverter.toDto(saveEntity);
    }

    public PostDto getPost(Long id) {
        Optional<PostEntity> postEntityOptional = postRepository.findById(id);
        PostEntity post = postEntityOptional.orElseThrow(
                () -> new RuntimeException(ErrorMessages.THERE_IS_NO_POST.getMessage() + id));
        return postConverter.toDto(post);
    }

    public PostDto updatePost(
            PostRequest postRequest
    ) {
        Long postId = postRequest.getId();
        var postEntityOptional = postRepository.findById(postId);
        var entity = postEntityOptional.orElseThrow(
                () -> new RuntimeException(ErrorMessages.THERE_IS_NO_POST.getMessage() + postId));
        var dto = postConverter.toDto(entity);
        dto.setPostTitle(postRequest.getPostTitle());
        dto.setPostContent(postRequest.getPostContent());
        dto.setUpdatedBy(Constant.ADMIN.getMessage());
        dto.setUpdatedAt(LocalDateTime.now());
        var saveEntity = postConverter.toEntity(dto);
        postRepository.save(saveEntity);
        return dto;
    }

    public void deletePost(
            Long id
    ) {
        postRepository.deleteById(id);
    }

    public Api<List<PostDto>> getPostList(Pageable pageable) {

        var entityList = postRepository.findAll(pageable);

        var pagination = Pagination.builder()
                .page(entityList.getNumber())
                .currentElements(entityList.getNumberOfElements())
                .size(entityList.getSize())
                .totalElements(entityList.getTotalElements())
                .totalPage(entityList.getTotalPages())
                .build();

        var dtoList = entityList.stream()
                .map(postConverter::toDto)
                .toList();

        return Api.<List<PostDto>>builder()
                .body(dtoList)
                .pagination(pagination)
                .build()
                ;
    }

    public boolean isWriter(String userEmail, Long postId) {
        PostEntity post = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException(ErrorMessages.THERE_IS_NO_POST.getMessage() + postId)
        );

        UserEntity user = userRepository.findByEmailAddr(userEmail).orElseThrow(
                () -> new RuntimeException(ErrorMessages.THERE_IS_NO_USER.getMessage() + userEmail)
        );

        return Objects.equals(user.getId(), post.getUserId());

    }
}