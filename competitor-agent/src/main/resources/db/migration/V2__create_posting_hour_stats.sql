CREATE TABLE competitor.posting_hour_stats (
    id                    UUID             NOT NULL,
    competitor_username   VARCHAR(100)     NOT NULL,
    media_type            VARCHAR(20)      NOT NULL,
    hour_of_day           SMALLINT         NOT NULL,

    avg_engagement_rate   DOUBLE PRECISION NOT NULL DEFAULT 0,
    post_count            INT              NOT NULL DEFAULT 0,
    total_like_count      BIGINT           NOT NULL DEFAULT 0,
    total_comments_count  BIGINT           NOT NULL DEFAULT 0,

    last_updated_at       TIMESTAMPTZ      NOT NULL DEFAULT now(),

    CONSTRAINT pk_posting_hour_stats
        PRIMARY KEY (id),
    CONSTRAINT uq_posting_hour_stats
        UNIQUE (competitor_username, media_type, hour_of_day),
    CONSTRAINT chk_hour_of_day
        CHECK (hour_of_day BETWEEN 0 AND 23)
);

CREATE INDEX idx_posting_hour_stats_competitor
    ON competitor.posting_hour_stats (competitor_username);

COMMENT ON TABLE competitor.posting_hour_stats
    IS 'Statystyki zaangażowania per godzina publikacji, per konkurent, per typ mediów';
COMMENT ON COLUMN competitor.posting_hour_stats.hour_of_day
    IS 'Godzina publikacji w UTC (0-23)';