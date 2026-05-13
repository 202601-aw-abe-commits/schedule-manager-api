ALTER TABLE schedule_join_request
ADD COLUMN IF NOT EXISTS game_id VARCHAR(100);
