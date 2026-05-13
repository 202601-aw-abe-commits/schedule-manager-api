INSERT INTO app_user (
    username,
    password_hash,
    email,
    total_points,
    display_name,
    profile_icon_color,
    enabled
)
VALUES (
    'b-boy49',
    '$2y$10$bccIE5cdr.UEM8t8LmjAiO.SINceQrfXAkQuLfjBtSIrhWz/NvK/2',
    'bboy49@example.local',
    0,
    'b-boy49',
    '#BFD6FF',
    TRUE
)
ON CONFLICT (username)
DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    email = EXCLUDED.email,
    display_name = EXCLUDED.display_name,
    profile_icon_color = EXCLUDED.profile_icon_color,
    enabled = EXCLUDED.enabled;
