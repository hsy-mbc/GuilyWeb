package org.hsy.spring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hsy.spring.dto.BoardDetailResponseDTO;
import org.hsy.spring.dto.BoardWriteDTO;
import org.hsy.spring.security.CustomUserDetails;
import org.hsy.spring.service.BoardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/board")
    public String board(Model model) {
        model.addAttribute("posts", boardService.getPostList(null));

        return "board";
    }

    @GetMapping("/board/write")
    public String boardWrite(Model model) {
        return "board_write";
    }

    @GetMapping("/board/{postNo}")
    public String boardDetail(@PathVariable Long postNo, Model model) {
        BoardDetailResponseDTO post = boardService.getPostDetail(postNo);

        model.addAttribute("post", post);

        return "board_detail";
    }

    @PostMapping("/api/post")
    public String writePost(@Valid BoardWriteDTO dto,
                            @AuthenticationPrincipal CustomUserDetails userDetails){
        boardService.savePost(dto, userDetails.getUsername());

        return "redirect:/board";
    }

}