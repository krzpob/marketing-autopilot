CREATE TABLE access_tokens (
    id            UUID         NOT NULL,
    owner_ig_id   VARCHAR(50)  NOT NULL,
    owner_username VARCHAR(100),
    token         VARCHAR(2048) NOT NULL,
    token_type    VARCHAR(20)  NOT NULL,
    expires_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL,
    refreshed_at  TIMESTAMPTZ,

    CONSTRAINT pk_access_tokens      PRIMARY KEY (id),
    CONSTRAINT uq_access_tokens_owner UNIQUE (owner_ig_id)
);

COMMENT ON TABLE  access_tokens              IS 'Instagram/Facebook OAuth access tokens';
COMMENT ON COLUMN access_tokens.token_type   IS 'SHORT_LIVED (~1h) or LONG_LIVED (~60 days)';
COMMENT ON COLUMN access_tokens.owner_ig_id  IS 'Instagram user ID of the token owner';