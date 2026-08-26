ALTER TABLE enquiries ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE enquiries ADD COLUMN notification_sent_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE enquiries ADD COLUMN review_token_hash VARCHAR(64);
ALTER TABLE enquiries ADD COLUMN review_token_expires_at TIMESTAMP WITH TIME ZONE;

