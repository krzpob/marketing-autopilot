ALTER TABLE monitored_profiles
    ADD COLUMN platform VARCHAR(20) NOT NULL DEFAULT 'INSTAGRAM';

ALTER TABLE monitored_profiles
    DROP CONSTRAINT uq_monitored_profiles_owner_handle;

ALTER TABLE monitored_profiles
    ADD CONSTRAINT uq_monitored_profiles_owner_platform_handle
        UNIQUE (owner_ig_id, platform, competitor_ig_handle);

COMMENT ON COLUMN monitored_profiles.platform
    IS 'Platforma źródłowa obserwacji — INSTAGRAM, FACEBOOK, GOOGLE. Ten sam handle może być obserwowany niezależnie na kilku platformach.';


ALTER TABLE monitored_hashtags
    ADD COLUMN platform VARCHAR(20) NOT NULL DEFAULT 'INSTAGRAM';

ALTER TABLE monitored_hashtags
    DROP CONSTRAINT uq_monitored_hashtags_owner_hashtag;

ALTER TABLE monitored_hashtags
    ADD CONSTRAINT uq_monitored_hashtags_owner_platform_hashtag
        UNIQUE (owner_ig_id, platform, hashtag);

COMMENT ON COLUMN monitored_hashtags.platform
    IS 'Platforma źródłowa obserwacji — INSTAGRAM, FACEBOOK, GOOGLE.';