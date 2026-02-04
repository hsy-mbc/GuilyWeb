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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/board")
    public String board(@RequestParam(required = false) String category,
                        @RequestParam(required = false) String keyword,
                        Model model) {
        PostCategory postCategory = null;
        if (category != null && !category.isEmpty()) {
            try {
                postCategory = PostCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                //
            }
        }

        List<BoardListDTO> posts = boardService.getPostList(postCategory, keyword);

        model.addAttribute("posts", posts);
        model.addAttribute("currentCategory", category);
        model.addAttribute("keyword", keyword);

        return "board";
    }

    @GetMapping("/board/write")
    public String boardWrite(Model model) {
        return "board_write";
    }

    @GetMapping("/board/{postNo}")
    public String boardDetail(@PathVariable Long postNo, Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        BoardDetailResponseDTO post = boardService.getPostDetail(postNo);

        boolean isLiked = false;
        if (userDetails != null) {
            isLiked = boardService.isUserLikedPost(postNo, userDetails.getUsername());
        }

        model.addAttribute("post", post);
        model.addAttribute("isLiked", isLiked);

        return "board_detail";
    }

    @GetMapping("/board/edit/{postNo}")
    public String editForm(@PathVariable Long postNo,
                           Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        BoardDetailResponseDTO post = boardService.getPostDetail(postNo);

        if (!post.getAuthorId().equals(userDetails.getUsername())) {
            throw new RuntimeException("권한이 없습니다.");
        }

        model.addAttribute("post", post);
        return "board_edit";
    }

    @PostMapping("/api/post")
    public String writePost(@Valid BoardSaveDTO dto,
                            @AuthenticationPrincipal CustomUserDetails userDetails){
        boardService.savePost(dto, userDetails.getUsername());

        return "redirect:/board";
    }

}