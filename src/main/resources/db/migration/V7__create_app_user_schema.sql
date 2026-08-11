CREATE TABLE servicemind.app_users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    role            VARCHAR(16) NOT NULL,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_app_users_role
        CHECK (
            role IN (
                'ADMIN',
                'AGENT'
            )
        )
);

CREATE UNIQUE INDEX uk_app_users_username_ci
    ON servicemind.app_users (
        LOWER(username)
    );

CREATE INDEX idx_app_users_role
    ON servicemind.app_users (role);

CREATE INDEX idx_app_users_enabled
    ON servicemind.app_users (enabled);