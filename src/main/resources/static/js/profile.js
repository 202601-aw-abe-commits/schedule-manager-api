const profileForm = document.getElementById("profileForm");
const displayNameInput = document.getElementById("profileDisplayName");
const imageUrlInput = document.getElementById("profileImageUrl");
const bioInput = document.getElementById("profileBio");
const profileMessage = document.getElementById("profileMessage");
const profileImagePreview = document.getElementById("profileImagePreview");
const profilePreviewName = document.getElementById("profilePreviewName");
const profilePreviewBio = document.getElementById("profilePreviewBio");

profileForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    profileMessage.textContent = "";

    const payload = {
        displayName: displayNameInput.value,
        profileImageUrl: imageUrlInput.value || null,
        profileBio: bioInput.value || null
    };

    try {
        const user = await fetchJson("/api/me", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        renderProfile(user);
        profileMessage.style.color = "#087057";
        profileMessage.textContent = "プロフィールを更新しました。";
    } catch (error) {
        profileMessage.style.color = "#be2f2f";
        profileMessage.textContent = error.message;
    }
});

imageUrlInput.addEventListener("input", () => {
    renderPreviewImage(imageUrlInput.value);
});

displayNameInput.addEventListener("input", () => {
    profilePreviewName.textContent = displayNameInput.value.trim() || "表示名";
});

bioInput.addEventListener("input", () => {
    profilePreviewBio.textContent = bioInput.value.trim() || "自己紹介はまだありません。";
});

async function loadProfile() {
    try {
        const user = await fetchJson("/api/me");
        renderProfile(user);
    } catch (error) {
        profileMessage.style.color = "#be2f2f";
        profileMessage.textContent = error.message;
    }
}

function renderProfile(user) {
    displayNameInput.value = user.displayName || "";
    imageUrlInput.value = user.profileImageUrl || "";
    bioInput.value = user.profileBio || "";
    profilePreviewName.textContent = user.displayName || "表示名";
    profilePreviewBio.textContent = user.profileBio || "自己紹介はまだありません。";
    renderPreviewImage(user.profileImageUrl);
}

function renderPreviewImage(imageUrl) {
    const url = (imageUrl || "").trim();
    if (!url) {
        profileImagePreview.src = "/img/default-profile.svg";
        return;
    }
    profileImagePreview.src = url;
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

loadProfile();
