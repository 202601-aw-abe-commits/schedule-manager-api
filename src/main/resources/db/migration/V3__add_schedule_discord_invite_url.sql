ALTER TABLE schedule_item
ADD COLUMN IF NOT EXISTS discord_invite_url VARCHAR(1000);
