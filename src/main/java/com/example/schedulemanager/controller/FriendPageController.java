package com.example.schedulemanager.controller;

import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.service.FriendshipService;
import com.example.schedulemanager.service.LabelColorService;
import com.example.schedulemanager.service.UserAccountService;
import java.util.NoSuchElementException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class FriendPageController {
    private final UserAccountService userAccountService;
    private final LabelColorService labelColorService;
    private final FriendshipService friendshipService;

    public FriendPageController(
            UserAccountService userAccountService,
            LabelColorService labelColorService,
            FriendshipService friendshipService) {
        this.userAccountService = userAccountService;
        this.labelColorService = labelColorService;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/friends")
    public String friends(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        applyCommonModel(model, userDetails);
        return "friends";
    }

    @GetMapping("/friends/search")
    public String friendSearch(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        applyCommonModel(model, userDetails);
        return "friends-search";
    }

    @GetMapping("/friends/list")
    public String friendList(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        applyCommonModel(model, userDetails);
        return "friends-list";
    }

    @GetMapping("/friends/requests")
    public String friendRequests(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        applyCommonModel(model, userDetails);
        return "friends-requests";
    }

    private void applyCommonModel(Model model, UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("currentDisplayName", user.getDisplayName());
        model.addAttribute("labelColorStyle", labelColorService.toInlineStyle(user.getId()));
    }

    @GetMapping("/friends/profile/{username}")
    public String friendProfile(
            @PathVariable("username") String username,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser viewer = userAccountService.getByUsername(userDetails.getUsername());
        AppUser target;
        try {
            target = userAccountService.getByUsername(username);
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "指定したユーザーが見つかりません。");
        }

        boolean isSelf = viewer.getId().equals(target.getId());
        boolean isFriend = friendshipService.areFriendsOrSelf(viewer.getId(), target.getId()) && !isSelf;
        boolean canSendRequest = !isSelf && !isFriend && !friendshipService.hasAnyRelationship(viewer.getId(), target.getId());

        model.addAttribute("currentUsername", viewer.getUsername());
        model.addAttribute("currentDisplayName", viewer.getDisplayName());
        model.addAttribute("labelColorStyle", labelColorService.toInlineStyle(viewer.getId()));
        model.addAttribute("friend", target);
        model.addAttribute("isSelf", isSelf);
        model.addAttribute("isFriend", isFriend);
        model.addAttribute("canSendRequest", canSendRequest);
        return "friend-profile";
    }
}
