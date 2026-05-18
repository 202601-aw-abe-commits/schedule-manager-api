const app = document.querySelector(".app-shell");
const currentUsername = app.dataset.currentUsername;
const searchKeyword = (app.dataset.searchKeyword || "").trim();

const threadFirstPostRecruitmentLimitInput = document.getElementById("threadFirstPostRecruitmentLimit");
const threadGameTitleSuggestions = document.getElementById("threadGameTitleSuggestions");
const threadSearchSuggestions = document.getElementById("threadSearchSuggestions");
const boardMessage = document.getElementById("boardMessage");

const openCreateThreadCardButton = document.getElementById("openCreateThreadCard");
const openThreadListCardButton = document.getElementById("openThreadListCard");
const threadListSection = document.getElementById("threadListSection");
const postEditorSection = document.getElementById("postEditorSection");
const postListSection = document.getElementById("postListSection");

const gameCardList = document.getElementById("gameCardList");
const selectedThreadLabel = document.getElementById("selectedThreadLabel");
const postSummaryList = document.getElementById("postSummaryList");
const postDetailEmpty = document.getElementById("postDetailEmpty");
const postDetailPanel = document.getElementById("postDetailPanel");
const postDetailMeta = document.getElementById("postDetailMeta");
const postDetailBody = document.getElementById("postDetailBody");
const postOwnerActions = document.getElementById("postOwnerActions");
const postEditButton = document.getElementById("postEditButton");
const postDeleteButton = document.getElementById("postDeleteButton");
const postEditForm = document.getElementById("postEditForm");
const editPostBodyInput = document.getElementById("editPostBody");
const editPostScheduleDateInput = document.getElementById("editPostScheduleDate");
const editPostStartTimeInput = document.getElementById("editPostStartTime");
const editPostRankBandInput = document.getElementById("editPostRankBand");
const editPostRecruitmentLimitInput = document.getElementById("editPostRecruitmentLimit");
const postUpdateButton = document.getElementById("postUpdateButton");
const postEditCancelButton = document.getElementById("postEditCancelButton");
const joinRequestForm = document.getElementById("joinRequestForm");
const joinRequestCommentInput = document.getElementById("joinRequestComment");
const joinRequestSubmitButton = document.getElementById("joinRequestSubmitButton");
const boardPendingJoinSection = document.getElementById("boardPendingJoinSection");
const boardPendingJoinList = document.getElementById("boardPendingJoinList");
const boardParticipantList = document.getElementById("boardParticipantList");
const boardDiscordSection = document.getElementById("boardDiscordSection");
const boardDiscordInviteLink = document.getElementById("boardDiscordInviteLink");
const boardDiscordOwnerForm = document.getElementById("boardDiscordOwnerForm");
const boardDiscordInviteUrlInput = document.getElementById("boardDiscordInviteUrl");
const boardDiscordSaveButton = document.getElementById("boardDiscordSaveButton");

const state = {
    threads: [],
    threadTitles: [],
    selectedGameTitle: "",
    selectedPosts: [],
    selectedPost: null
};

const POPULAR_GAME_TITLES = [
    "Apex Legends",
    "VALORANT",
    "Overwatch 2",
    "Monster Hunter Wilds",
    "Fortnite",
    "League of Legends",
    "Minecraft",
    "Call of Duty"
];
const GAME_TITLE_HISTORY_KEY = "board_game_title_history";
const GAME_TITLE_HISTORY_LIMIT = 20;

if (openThreadListCardButton) {
    openThreadListCardButton.addEventListener("click", () => setBoardMode("list"));
}

if (joinRequestForm) {
    joinRequestForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!state.selectedPost || !state.selectedPost.id) {
            return;
        }
        try {
            joinRequestSubmitButton.disabled = true;
            await fetchJson(`/api/board/posts/${state.selectedPost.id}/join-requests`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ comment: joinRequestCommentInput.value })
            });
            boardMessage.style.color = "#087057";
            boardMessage.textContent = "参加希望を送信しました。";
            await refreshSelectedPost();
        } catch (error) {
            boardMessage.style.color = "#be2f2f";
            boardMessage.textContent = error.message;
        } finally {
            joinRequestSubmitButton.disabled = false;
        }
    });
}

async function loadThreads() {
    const query = searchKeyword ? `?keyword=${encodeURIComponent(searchKeyword)}` : "";
    const threads = await fetchJson(`/api/board/threads${query}`);
    state.threads = Array.isArray(threads) ? threads : [];
    state.threadTitles = state.threads.map((thread) => normalizeTitle(thread.gameTitle)).filter((title) => title);
    refreshGameTitleSuggestions();
    renderGameCards();
}

function renderGameCards() {
    gameCardList.innerHTML = "";
    const groups = new Map();
    state.threads.forEach((thread) => {
        const title = normalizeTitle(thread.gameTitle);
        if (!title) {
            return;
        }
        if (!groups.has(title)) {
            groups.set(title, []);
        }
        groups.get(title).push(thread);
    });

    if (groups.size === 0) {
        gameCardList.innerHTML = "<p class=\"help-text\">該当するゲームがありません。</p>";
        return;
    }

    Array.from(groups.entries()).forEach(([title, threads]) => {
        const totalPosts = threads.reduce((sum, thread) => sum + Number(thread.postCount ?? 0), 0);
        const button = document.createElement("button");
        button.type = "button";
        button.className = "board-game-card";
        const decoratedTitle = window.GameBadge ? window.GameBadge.withBadge(title) : title;
        button.innerHTML = `<h4>${escapeHtml(decoratedTitle)}</h4><p class=\"help-text\">スレッド: ${threads.length} / 投稿: ${totalPosts}</p>`;
        button.addEventListener("click", async () => {
            await selectGameTitle(title);
        });
        gameCardList.appendChild(button);
    });
}

async function selectGameTitle(gameTitle) {
    state.selectedGameTitle = normalizeTitle(gameTitle);
    selectedThreadLabel.textContent = `${state.selectedGameTitle} の投稿一覧`;
    state.selectedPost = null;
    postDetailPanel.hidden = true;
    postDetailEmpty.hidden = false;
    boardPendingJoinList.innerHTML = "";
    boardParticipantList.innerHTML = "";
    await loadPostsByGameTitle(state.selectedGameTitle);
}

async function loadPostsByGameTitle(gameTitle) {
    const targetThreads = state.threads.filter((thread) => normalizeTitle(thread.gameTitle) === gameTitle);
    if (targetThreads.length === 0) {
        state.selectedPosts = [];
        renderPostSummaries();
        return;
    }

    const postLists = await Promise.all(targetThreads.map((thread) => fetchJson(`/api/board/threads/${thread.id}/posts`)));
    const merged = [];
    postLists.forEach((posts, index) => {
        const thread = targetThreads[index];
        (Array.isArray(posts) ? posts : []).forEach((post) => {
            merged.push({ ...post, threadId: thread.id, threadTitle: thread.gameTitle });
        });
    });
    merged.sort((a, b) => {
        const aKey = `${a.createdAt || ""}`;
        const bKey = `${b.createdAt || ""}`;
        if (aKey === bKey) {
            return Number(b.id) - Number(a.id);
        }
        return bKey.localeCompare(aKey);
    });
    state.selectedPosts = merged;
    renderPostSummaries();
}

function renderPostSummaries() {
    postSummaryList.innerHTML = "";
    if (!Array.isArray(state.selectedPosts) || state.selectedPosts.length === 0) {
        postSummaryList.innerHTML = "<li class=\"friend-card-empty\">このゲームの投稿はまだありません。</li>";
        return;
    }

    state.selectedPosts.forEach((post) => {
        const li = document.createElement("li");
        li.className = "board-post-summary-item";
        const button = document.createElement("button");
        button.type = "button";
        button.className = "board-post-summary-button";
        const authorName = post.authorDisplayName || post.authorUsername || "不明";
        const dateText = formatBoardDate(post.scheduleDate);
        const timeText = post.startTime ? String(post.startTime).slice(0, 5) : "未指定";
        const limit = post.recruitmentLimit ? `${post.recruitmentLimit}人` : "指定なし";
        button.innerHTML = `
            <div class="board-post-summary-date">${escapeHtml(dateText)} ${escapeHtml(timeText)}</div>
            <div class="board-post-summary-limit">募集人数: ${escapeHtml(limit)}</div>
            <div class="board-post-summary-author">作成者: ${escapeHtml(authorName)}</div>
        `;
        button.addEventListener("click", async () => {
            await selectPost(post);
        });
        li.appendChild(button);

        if (post.authorUserId && post.authorUsername !== currentUsername) {
            const reportButton = document.createElement("button");
            reportButton.type = "button";
            reportButton.className = "secondary";
            reportButton.textContent = "通報";
            reportButton.addEventListener("click", async () => {
                await reportUser(post.authorUserId, "BOARD_POST", post.id);
            });
            li.appendChild(reportButton);
        }
        postSummaryList.appendChild(li);
    });
}

async function selectPost(post) {
    state.selectedPost = post;
    hidePostEditForm();
    postDetailBody.textContent = post.body || "";
    const authorName = post.authorDisplayName || post.authorUsername || "不明";
    const dateText = post.scheduleDate || "未指定";
    const timeText = post.startTime ? String(post.startTime).slice(0, 5) : "未指定";
    const deviceText = toBoardDeviceLabel(post.deviceType);
    const limit = post.recruitmentLimit ? `${post.recruitmentLimit}人` : "指定なし";
    const rankText = post.rankBand ? post.rankBand : "未指定";
    postDetailMeta.textContent = `作成者: ${authorName} / 予定日: ${dateText} / 時間: ${timeText} / デバイス: ${deviceText} / ランク: ${rankText} / 募集人数: ${limit}`;
    const isOwnPost = post.authorUsername && post.authorUsername === currentUsername;
    if (postOwnerActions) {
        postOwnerActions.hidden = !isOwnPost;
    }
    renderParticipationPanel(post, isOwnPost);
    postDetailPanel.hidden = false;
    postDetailEmpty.hidden = true;
}

if (postEditButton) {
    postEditButton.addEventListener("click", () => {
        if (!state.selectedPost || !state.selectedPost.id) {
            return;
        }
        const post = state.selectedPost;
        if (!post.authorUsername || post.authorUsername !== currentUsername) {
            boardMessage.style.color = "#be2f2f";
            boardMessage.textContent = "自分の投稿のみ編集できます。";
            return;
        }
        showPostEditForm(post);
    });
}

if (postEditForm) {
    postEditForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!state.selectedPost || !state.selectedPost.id) {
            return;
        }
        const post = state.selectedPost;
        if (!post.authorUsername || post.authorUsername !== currentUsername) {
            boardMessage.style.color = "#be2f2f";
            boardMessage.textContent = "自分の投稿のみ編集できます。";
            return;
        }
        const payload = {
            body: editPostBodyInput.value,
            scheduleDate: normalizeOptionalText(editPostScheduleDateInput.value),
            startTime: normalizeOptionalText(editPostStartTimeInput.value),
            rankBand: normalizeOptionalText(editPostRankBandInput.value),
            recruitmentLimit: parseIntOrNull(editPostRecruitmentLimitInput.value)
        };

        try {
            postUpdateButton.disabled = true;
            const updated = await fetchJson(`/api/board/posts/${post.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            boardMessage.style.color = "#087057";
            boardMessage.textContent = "投稿を編集しました。";
            hidePostEditForm();
            await loadThreads();
            await selectGameTitle(post.threadTitle || state.selectedGameTitle);
            if (updated && updated.id) {
                const refreshed = state.selectedPosts.find((row) => Number(row.id) === Number(updated.id));
                if (refreshed) {
                    await selectPost(refreshed);
                }
            }
        } catch (error) {
            boardMessage.style.color = "#be2f2f";
            boardMessage.textContent = error.message;
        } finally {
            postUpdateButton.disabled = false;
        }
    });
}

if (postEditCancelButton) {
    postEditCancelButton.addEventListener("click", () => {
        hidePostEditForm();
    });
}

if (postDeleteButton) {
    postDeleteButton.addEventListener("click", async () => {
        if (!state.selectedPost || !state.selectedPost.id) {
            return;
        }
        const post = state.selectedPost;
        if (!post.authorUsername || post.authorUsername !== currentUsername) {
            boardMessage.style.color = "#be2f2f";
            boardMessage.textContent = "自分の投稿のみ削除できます。";
            return;
        }
        const confirmed = window.confirm("この投稿を削除しますか？");
        if (!confirmed) {
            return;
        }
        try {
            await fetchJson(`/api/board/posts/${post.id}`, { method: "DELETE" });
            boardMessage.style.color = "#087057";
            boardMessage.textContent = "投稿を削除しました。";
            state.selectedPost = null;
            postDetailPanel.hidden = true;
            postDetailEmpty.hidden = false;
            boardPendingJoinList.innerHTML = "";
            boardParticipantList.innerHTML = "";
            await loadThreads();
            await selectGameTitle(post.threadTitle || state.selectedGameTitle);
        } catch (error) {
            boardMessage.style.color = "#be2f2f";
            boardMessage.textContent = error.message;
        }
    });
}

if (boardDiscordOwnerForm) {
    boardDiscordOwnerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!state.selectedPost || !state.selectedPost.id) {
            return;
        }
        try {
            boardDiscordSaveButton.disabled = true;
            await fetchJson(`/api/board/posts/${state.selectedPost.id}/discord-invite`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ discordInviteUrl: normalizeOptionalText(boardDiscordInviteUrlInput.value) })
            });
            boardMessage.style.color = "#087057";
            boardMessage.textContent = "Discord招待URLを更新しました。";
            await refreshSelectedPost();
        } catch (error) {
            boardMessage.style.color = "#be2f2f";
            boardMessage.textContent = error.message;
        } finally {
            boardDiscordSaveButton.disabled = false;
        }
    });
}

function refreshGameTitleSuggestions() {
    if (!threadGameTitleSuggestions && !threadSearchSuggestions) {
        return;
    }

    const merged = [
        ...POPULAR_GAME_TITLES,
        ...loadGameTitleHistory(),
        ...state.threadTitles
    ];
    const normalized = Array.from(new Set(merged.map((title) => normalizeTitle(title)).filter((title) => title)));
    if (threadGameTitleSuggestions) {
        threadGameTitleSuggestions.innerHTML = "";
    }
    if (threadSearchSuggestions) {
        threadSearchSuggestions.innerHTML = "";
    }
    normalized.slice(0, 50).forEach((title) => {
        if (threadGameTitleSuggestions) {
            const threadOption = document.createElement("option");
            threadOption.value = title;
            threadGameTitleSuggestions.appendChild(threadOption);
        }
        if (threadSearchSuggestions) {
            const searchOption = document.createElement("option");
            searchOption.value = title;
            threadSearchSuggestions.appendChild(searchOption);
        }
    });
}

function loadGameTitleHistory() {
    try {
        const raw = localStorage.getItem(GAME_TITLE_HISTORY_KEY);
        if (!raw) {
            return [];
        }
        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed)) {
            return [];
        }
        return parsed.map((title) => normalizeTitle(title)).filter((title) => title);
    } catch (error) {
        return [];
    }
}

function pushGameTitleHistory(title) {
    const normalizedTitle = normalizeTitle(title);
    if (!normalizedTitle) {
        return;
    }

    const next = [normalizedTitle, ...loadGameTitleHistory().filter((item) => item !== normalizedTitle)]
        .slice(0, GAME_TITLE_HISTORY_LIMIT);
    try {
        localStorage.setItem(GAME_TITLE_HISTORY_KEY, JSON.stringify(next));
    } catch (error) {
        // Ignore storage errors and continue without persistence.
    }
}

function parseIntOrNull(value) {
    if (value == null || value.trim() === "") {
        return null;
    }
    return Number.parseInt(value, 10);
}

function normalizeOptionalText(value) {
    const normalized = normalizeTitle(value);
    return normalized === "" ? null : normalized;
}

function renderParticipationPanel(post, isOwnPost) {
    const joined = post.joinedByCurrentUser === true;
    const status = String(post.joinRequestStatusForCurrentUser || "");
    const closed = post.recruitmentClosed === true;

    if (joinRequestCommentInput) {
        joinRequestCommentInput.value = "";
    }
    if (joinRequestForm) {
        joinRequestForm.hidden = isOwnPost;
    }
    if (joinRequestCommentInput) {
        joinRequestCommentInput.disabled = isOwnPost || joined || status === "PENDING" || closed;
        if (isOwnPost) {
            joinRequestCommentInput.placeholder = "自分の募集には参加希望を送信できません";
        } else if (joined) {
            joinRequestCommentInput.placeholder = "参加承認済みです";
        } else if (status === "PENDING") {
            joinRequestCommentInput.placeholder = "参加希望は承認待ちです";
        } else if (closed) {
            joinRequestCommentInput.placeholder = "この募集は締め切られています";
        } else {
            joinRequestCommentInput.placeholder = "例: 参加希望です。21:00から入れます。";
        }
    }
    if (joinRequestSubmitButton) {
        joinRequestSubmitButton.disabled = isOwnPost || joined || status === "PENDING" || closed;
    }

    if (boardPendingJoinSection) {
        boardPendingJoinSection.hidden = !isOwnPost;
    }
    if (boardPendingJoinList) {
        boardPendingJoinList.innerHTML = "";
        const pending = Array.isArray(post.pendingJoinRequests) ? post.pendingJoinRequests : [];
        if (isOwnPost && pending.length === 0) {
            boardPendingJoinList.innerHTML = "<li>承認待ちの参加希望はありません。</li>";
        }
        pending.forEach((request) => {
            const li = document.createElement("li");
            const name = request.requesterDisplayName || request.requesterUsername || "不明";
            const text = document.createElement("span");
            text.textContent = `${name}: ${request.comment || ""}`;
            const approveButton = document.createElement("button");
            approveButton.type = "button";
            approveButton.className = "primary";
            approveButton.textContent = "了承";
            approveButton.addEventListener("click", async () => {
                await decideJoinRequest(post.id, request.id, true);
            });
            const rejectButton = document.createElement("button");
            rejectButton.type = "button";
            rejectButton.className = "secondary";
            rejectButton.textContent = "見送り";
            rejectButton.addEventListener("click", async () => {
                await decideJoinRequest(post.id, request.id, false);
            });
            li.append(text, approveButton, rejectButton);
            boardPendingJoinList.appendChild(li);
        });
    }

    if (boardParticipantList) {
        boardParticipantList.innerHTML = "";
        const participants = Array.isArray(post.participants) ? post.participants : [];
        if (participants.length === 0) {
            boardParticipantList.innerHTML = "<li>参加メンバーはまだいません。</li>";
        }
        participants.forEach((user) => {
            const li = document.createElement("li");
            li.textContent = `${user.displayName || user.username || "不明"} (@${user.username || ""})`;
            boardParticipantList.appendChild(li);
        });
    }

    const canViewDiscord = Boolean(post.discordInviteUrl) && (isOwnPost || joined);
    if (boardDiscordSection) {
        boardDiscordSection.hidden = !canViewDiscord;
    }
    if (boardDiscordInviteLink && canViewDiscord) {
        boardDiscordInviteLink.href = post.discordInviteUrl;
    }

    if (boardDiscordOwnerForm) {
        boardDiscordOwnerForm.hidden = !isOwnPost;
    }
    if (boardDiscordInviteUrlInput && isOwnPost) {
        boardDiscordInviteUrlInput.value = post.discordInviteUrl || "";
    }
}

async function decideJoinRequest(postId, joinRequestId, approve) {
    try {
        await fetchJson(`/api/board/posts/${postId}/join-requests/${joinRequestId}/${approve ? "approve" : "reject"}`, { method: "POST" });
        boardMessage.style.color = "#087057";
        boardMessage.textContent = approve ? "参加希望を了承しました。" : "参加希望を見送りました。";
        await refreshSelectedPost();
    } catch (error) {
        boardMessage.style.color = "#be2f2f";
        boardMessage.textContent = error.message;
    }
}

async function refreshSelectedPost() {
    if (!state.selectedPost || !state.selectedPost.id) {
        return;
    }
    const post = state.selectedPost;
    await loadThreads();
    await selectGameTitle(post.threadTitle || state.selectedGameTitle);
    const refreshed = state.selectedPosts.find((row) => Number(row.id) === Number(post.id));
    if (refreshed) {
        await selectPost(refreshed);
    }
}

function showPostEditForm(post) {
    if (!postEditForm) {
        return;
    }
    editPostBodyInput.value = post.body || "";
    editPostScheduleDateInput.value = post.scheduleDate || "";
    editPostStartTimeInput.value = post.startTime ? String(post.startTime).slice(0, 5) : "";
    editPostRankBandInput.value = post.rankBand || "";
    editPostRecruitmentLimitInput.value = post.recruitmentLimit == null ? "" : String(post.recruitmentLimit);
    postEditForm.hidden = false;
}

function hidePostEditForm() {
    if (!postEditForm) {
        return;
    }
    postEditForm.hidden = true;
}

function normalizeTitle(value) {
    if (value == null) {
        return "";
    }
    return String(value).trim();
}

function toBoardDeviceLabel(value) {
    const normalized = normalizeTitle(value).toUpperCase();
    if (normalized === "CONSOLE") {
        return "家庭用ゲーム機";
    }
    return "PC";
}

function formatBoardDate(value) {
    if (!value) {
        return "日付未指定";
    }
    const text = String(value);
    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(text);
    if (!match) {
        return text;
    }
    const month = Number(match[2]);
    const day = Number(match[3]);
    return `${month}月${day}日`;
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");
    const data = isJson ? await response.json() : null;

    if (!response.ok) {
        const message = data && data.message ? data.message : "通信に失敗しました。";
        throw new Error(message);
    }
    return data;
}

function setBoardMode(mode) {
    const showList = mode === "list";
    if (threadListSection) {
        threadListSection.hidden = !showList;
    }
    if (postEditorSection) {
        postEditorSection.hidden = !showList;
    }
    if (postListSection) {
        postListSection.hidden = !showList;
    }
}

async function reportUser(targetUserId, sourceType, sourceId) {
    const categoryInput = window.prompt(
        "通報カテゴリを入力してください: HARASSMENT / HATE_SPEECH / SPAM / SEXUAL / VIOLENCE / OTHER"
    );
    if (!categoryInput) {
        return;
    }
    const category = String(categoryInput).trim().toUpperCase();
    const note = window.prompt("備考を入力してください（5文字以上）");
    if (!note) {
        return;
    }
    try {
        await fetchJson("/api/reports", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                targetUserId,
                sourceType,
                sourceId,
                category,
                note
            })
        });
        boardMessage.style.color = "#087057";
        boardMessage.textContent = "通報を送信しました。";
    } catch (error) {
        boardMessage.style.color = "#be2f2f";
        boardMessage.textContent = error.message;
    }
}

async function initializeBoard() {
    setBoardMode(searchKeyword ? "list" : "menu");
    await loadThreads();
}

initializeBoard();

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
