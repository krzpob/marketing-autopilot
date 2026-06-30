CREATE TABLE monitored_hashtags (
    id                UUID         NOT NULL,
    owner_ig_id       VARCHAR(50)  NOT NULL,
    hashtag           VARCHAR(100) NOT NULL,
    active            BOOLEAN      NOT NULL DEFAULT true,
    created_at        TIMESTAMPTZ  NOT NULL,
    last_collected_at TIMESTAMPTZ,

    CONSTRAINT pk_monitored_hashtags
        PRIMARY KEY (id),
    CONSTRAINT uq_monitored_hashtags_owner_hashtag
        UNIQUE (owner_ig_id, hashtag)
);

COMMENT ON TABLE  monitored_hashtags             IS 'Hashtagi obserwowane przez fotografów';
COMMENT ON COLUMN monitored_hashtags.owner_ig_id IS 'Instagram ID fotografa (właściciel tokenu)';
COMMENT ON COLUMN monitored_hashtags.hashtag     IS 'Hashtag bez znaku #, np. fotografia';

CREATE INDEX idx_monitored_hashtags_active
    ON monitored_hashtags (active)
    WHERE active = true;