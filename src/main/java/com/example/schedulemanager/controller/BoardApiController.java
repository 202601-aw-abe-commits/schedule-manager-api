package com.example.schedulemanager.controller;

import com.example.schedulemanager.dto.BoardRecruitmentCreateRequest;
import com.example.schedulemanager.dto.BoardRecruitmentUpdateRequest;
import com.example.schedulemanager.dto.BoardJoinRequestCreateRequest;
import com.example.schedulemanager.dto.BoardDiscordInviteRequest;
import com.example.schedulemanager.dto.BoardPostInterestCreateRequest;
import com.example.schedulemanager.dto.BoardThreadCreateRequest;
import com.example.schedulemanager.model.BoardPost;
import com.example.schedulemanager.model.BoardPostInterest;
import com.example.schedulemanager.model.BoardThread;
import com.example.schedulemanager.service.BoardService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/board")
public class BoardApiController {
    private final BoardService boardService;

    public BoardApiController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/threads")
    public List<BoardThread> listThreads(@RequestParam(value = "keyword", required = false) String keyword) {
        return boardService.listThreads(keyword);
    }

    @PostMapping("/threads")
    public ResponseEntity<BoardThread> createThread(
            @RequestBody BoardThreadCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardService.createThread(request, userDetails.getUsername()));
    }

    @GetMapping("/threads/{threadId}/posts")
    public List<BoardPost> listPosts(
            @PathVariable("threadId") Long threadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return boardService.listPosts(threadId, userDetails.getUsername());
    }

    @PostMapping("/threads/{threadId}/posts")
    public ResponseEntity<BoardPost> createPost(
            @PathVariable("threadId") Long threadId,
            @RequestBody BoardRecruitmentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardService.createRecruitment(threadId, request, userDetails.getUsername()));
    }

    @PutMapping("/posts/{postId}")
    public BoardPost updatePost(
            @PathVariable("postId") Long postId,
            @RequestBody BoardRecruitmentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return boardService.updateRecruitment(postId, request, userDetails.getUsername());
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boardService.deleteRecruitment(postId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/join-requests")
    public ResponseEntity<Void> joinPost(
            @PathVariable("postId") Long postId,
            @RequestBody(required = false) BoardJoinRequestCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        boardService.joinPost(postId, userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/join-requests/{joinRequestId}/approve")
    public ResponseEntity<Void> approvePostJoinRequest(
            @PathVariable("postId") Long postId,
            @PathVariable("joinRequestId") Long joinRequestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boardService.decidePostJoinRequest(postId, joinRequestId, true, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/join-requests/{joinRequestId}/reject")
    public ResponseEntity<Void> rejectPostJoinRequest(
            @PathVariable("postId") Long postId,
            @PathVariable("joinRequestId") Long joinRequestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boardService.decidePostJoinRequest(postId, joinRequestId, false, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/posts/{postId}/discord-invite")
    public BoardPost updateDiscordInvite(
            @PathVariable("postId") Long postId,
            @RequestBody(required = false) BoardDiscordInviteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return boardService.updatePostDiscordInvite(postId, userDetails.getUsername(), request);
    }

    @GetMapping("/posts/{postId}/interests")
    public List<BoardPostInterest> listInterests(@PathVariable("postId") Long postId) {
        return boardService.listInterests(postId);
    }

    @PostMapping("/posts/{postId}/interests")
    public ResponseEntity<BoardPostInterest> createInterest(
            @PathVariable("postId") Long postId,
            @RequestBody BoardPostInterestCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardService.createInterest(postId, request, userDetails.getUsername()));
    }
}
