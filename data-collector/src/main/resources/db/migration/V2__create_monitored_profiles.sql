CREATE TABLE monitored_profiles (
    id                    UUID         NOT NULL,
    owner_ig_id           VARCHAR(50)  NOT NULL,
    competitor_ig_handle  VARCHAR(100) NOT NULL,
    active                BOOLEAN      NOT NULL DEFAULT true,
    created_at            TIMESTAMPTZ  NOT NULL,
    last_collected_at     TIMESTAMPTZ,

    CONSTRAINT pk_monitored_profiles
        PRIMARY KEY (id),
    CONSTRAINT uq_monitored_profiles_owner_handle
        UNIQUE (owner_ig_id, competitor_ig_handle)
);

COMMENT ON TABLE  monitored_profiles                      IS 'Competitor profiles monitored by each photographer';
COMMENT ON COLUMN monitored_profiles.owner_ig_id          IS 'Instagram ID of the photographer (token owner)';
COMMENT ON COLUMN monitored_profiles.competitor_ig_handle IS 'Instagram handle of the competitor to monitor';

CREATE INDEX idx_monitored_profiles_owner
    ON monitored_profiles (owner_ig_id);

CREATE INDEX idx_monitored_profiles_active
    ON monitored_profiles (active)
    WHERE active = true;