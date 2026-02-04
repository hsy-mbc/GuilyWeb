package org.hsy.spring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.common.enums.PostCategory;
import org.hsy.spring.dto.BoardDetailResponseDTO;
import org.hsy.spring.dto.BoardListDTO;
import org.hsy.spring.dto.BoardSaveDTO;
import org.hsy.spring.security.CustomUserDetails;
import org.hsy.spring.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardApiController {
    private final BoardService boardService;

    @GetMapping("/filter")
    public List<BoardListDTO> filterByCategory(@RequestParam(required = false) PostCategory category) {
        return boardService.getPostList(category, null);
    }

    @PostMapping("/{postNo}/like")
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable Long postNo,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> result = boardService.toggleLike(postNo, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{postNo}")
    public ResponseEntity<Map<String, String>> updatePost(@PathVariable Long postNo,
                                                          @Valid @RequestBody BoardSaveDTO dto,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        boardService.updatePost(postNo, dto, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "수정되었습니다."));
    }

    @DeleteMapping("/{postNo}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long postNo,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        boardService.deletePost(postNo, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }

}