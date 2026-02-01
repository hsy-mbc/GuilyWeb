package org.hsy.spring.controller;

import lombok.RequiredArgsConstructor;
import org.hsy.spring.common.enums.PostCategory;
import org.hsy.spring.dto.BoardListDTO;
import org.hsy.spring.security.CustomUserDetails;
import org.hsy.spring.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardApiController {
    private final BoardService boardService;

    @GetMapping("/filter")
    public List<BoardListDTO> filterByCategory(@RequestParam(required = false) PostCategory category) {
        return boardService.getPostList(category);
    }

    @PostMapping("/{postNo}/like")
    public ResponseEntity<Integer> likePost(@PathVariable Long postNo,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        int updatedLikeCount = boardService.toggleLike(postNo, userDetails.getUsername());
        return ResponseEntity.ok(updatedLikeCount);
    }

}