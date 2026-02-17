package org.hsy.spring.controller.api;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.CommentResponseDTO;
import org.hsy.spring.dto.CommentSaveDTO;
import org.hsy.spring.security.CustomUserDetails;
import org.hsy.spring.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentApiController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDTO> saveComment(@RequestBody CommentSaveDTO dto,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponseDTO savedComment = commentService.saveComment(dto, userDetails.getUsername());
        return ResponseEntity.ok(savedComment);
    }

}
