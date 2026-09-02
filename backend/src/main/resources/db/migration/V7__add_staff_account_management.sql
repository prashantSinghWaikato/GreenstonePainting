ALTER TABLE admin_users DROP CONSTRAINT chk_admin_user_role;

UPDATE admin_users SET role = 'OWNER' WHERE role = 'ADMIN';

ALTER TABLE admin_users
    ADD CONSTRAINT chk_admin_user_role CHECK (role IN ('OWNER', 'STAFF'));

ALTER TABLE admin_users ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE admin_users ADD COLUMN last_login_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE admin_users ADD COLUMN password_changed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE admin_users ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE admin_users ADD COLUMN locked_until TIMESTAMP WITH TIME ZONE;

ALTER TABLE admin_users
    ADD CONSTRAINT chk_admin_user_failed_attempts CHECK (failed_login_attempts >= 0);

CREATE TABLE admin_account_activities (
    id UUID PRIMARY KEY,
    actor_admin_id UUID NOT NULL,
    target_admin_id UUID NOT NULL,
    activity_type VARCHAR(40) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_admin_account_activity_actor
        FOREIGN KEY (actor_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_admin_account_activity_target
        FOREIGN KEY (target_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_admin_account_activity_type
        CHECK (activity_type IN ('ACCOUNT_CREATED', 'ACCOUNT_ACTIVATED', 'ACCOUNT_DEACTIVATED', 'PASSWORD_CHANGED'))
);

CREATE INDEX idx_admin_account_activities_created
    ON admin_account_activities (created_at DESC);

CREATE INDEX idx_admin_account_activities_target
    ON admin_account_activities (target_admin_id, created_at DESC);
