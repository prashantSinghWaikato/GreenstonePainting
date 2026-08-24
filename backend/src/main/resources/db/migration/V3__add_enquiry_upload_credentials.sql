ALTER TABLE enquiries ADD COLUMN upload_token_hash VARCHAR(64);
ALTER TABLE enquiries ADD COLUMN upload_token_expires_at TIMESTAMP WITH TIME ZONE;
