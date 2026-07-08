CREATE TABLE competitor.monitored_hashtag_projection (
    id            UUID         NOT NULL,
    owner_ig_id   VARCHAR(50)  NOT NULL,
    hashtag       VARCHAR(100) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT true,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_monitored_hashtag_projection
        PRIMARY KEY (id),
    CONSTRAINT uq_monitored_hashtag_projection
        UNIQUE (owner_ig_id, hashtag)
);

CREATE INDEX idx_monitored_hashtag_projection_hashtag
    ON competitor.monitored_hashtag_projection (hashtag)
    WHERE active = true;

COMMENT ON TABLE competitor.monitored_hashtag_projection
    IS 'Lokalna projekcja MonitoredHashtag z data-collector — budowana z eventów Kafka, tylko do odczytu';

CREATE TABLE competitor.monitored_profile_projection (
    id                    UUID         NOT NULL,
    owner_ig_id           VARCHAR(50)  NOT NULL,
    competitor_ig_handle  VARCHAR(100) NOT NULL,
    active                BOOLEAN      NOT NULL DEFAULT true,
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_monitored_profile_projection
        PRIMARY KEY (id),
    CONSTRAINT uq_monitored_profile_projection
        UNIQUE (owner_ig_id, competitor_ig_handle)
);

CREATE INDEX idx_monitored_profile_projection_handle
    ON competitor.monitored_profile_projection (competitor_ig_handle)
    WHERE active = true;

COMMENT ON TABLE competitor.monitored_profile_projection
    IS 'Lokalna projekcja MonitoredProfile z data-collector — budowana z eventów Kafka, tylko do odczytu';