CREATE TABLE users (
    id UUID PRIMARY KEY,
    identity_subject VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_users_identity_subject
        UNIQUE (identity_subject)
);