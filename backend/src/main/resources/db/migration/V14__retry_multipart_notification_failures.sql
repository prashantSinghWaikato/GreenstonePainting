UPDATE notification_deliveries
SET status = 'PENDING',
    attempt_count = 0,
    last_attempt_at = NULL,
    error_message = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'FAILED'
  AND error_message LIKE 'Not in multipart mode%';
