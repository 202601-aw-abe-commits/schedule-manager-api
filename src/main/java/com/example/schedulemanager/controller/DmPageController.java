package com.example.schedulemanager.controller;

import com.example.schedulemanager.model.AppUser;
import com.example.schedulemanager.model.DmConversation;
import com.example.schedulemanager.service.DmService;
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
public class DmPageController {
    private final UserAccountService userAccountService;
    private final LabelColorService labelColorService;
    private final DmService dmService;

    public DmPageController(UserAccountService userAccountService, LabelColorService labelColorService, DmService dmService) {
        this.userAccountService = userAccountService;
        this.labelColorService = labelColorService;
        this.dmService = dmService;
    }

    @GetMapping("/dm")
    public String dm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("currentDisplayName", user.getDisplayName());
        model.addAttribute("labelColorStyle", labelColorService.toInlineStyle(user.getId()));
        return "dm";
    }

    @GetMapping("/dm/conversations/{id}")
    public String dmConversation(
            @PathVariable("id") Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userAccountService.getByUsername(userDetails.getUsername());
        DmConversation conversation = dmService.listConversations(user.getId()).stream()
                .filter(row -> row.getId() != null && row.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "会話が見つかりません。"));
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("currentDisplayName", user.getDisplayName());
        model.addAttribute("labelColorStyle", labelColorService.toInlineStyle(user.getId()));
        model.addAttribute("conversationId", conversation.getId());
        model.addAttribute("partnerDisplayName", conversation.getPartnerDisplayName());
        model.addAttribute("partnerUsername", conversation.getPartnerUsername());
        return "dm-chat";
    }
}
