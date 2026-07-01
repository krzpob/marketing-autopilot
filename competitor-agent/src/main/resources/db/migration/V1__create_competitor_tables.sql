-- ── competitor_posts ──────────────────────────────────────────────────────────
CREATE TABLE competitor.competitor_posts (
    id                        UUID        NOT NULL,
    ig_media_id               VARCHAR(50) NOT NULL,
    shortcode                 VARCHAR(50),
    competitor_username       VARCHAR(100) NOT NULL,
    owner_ig_id               VARCHAR(50)  NOT NULL,

    media_type                VARCHAR(20)  NOT NULL,
    caption                   TEXT,
    hashtags                  TEXT[],
    mentions                  TEXT[],
    media_url                 TEXT,

    like_count                BIGINT       NOT NULL DEFAULT 0,
    comments_count            INT          NOT NULL DEFAULT 0,
    follower_count_at_collection BIGINT    NOT NULL DEFAULT 0,

    embedding                 vector(1536),

    published_at              TIMESTAMPTZ  NOT NULL,
    collected_at              TIMESTAMPTZ  NOT NULL,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_competitor_posts PRIMARY KEY (id),
    CONSTRAINT uq_competitor_posts_ig_media_id UNIQUE (ig_media_id)
);

CREATE INDEX idx_competitor_posts_username
    ON competitor.competitor_posts (competitor_username);
CREATE INDEX idx_competitor_posts_published_at
    ON competitor.competitor_posts (published_at DESC);
CREATE INDEX idx_competitor_posts_embedding
    ON competitor.competitor_posts USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

COMMENT ON TABLE competitor.competitor_posts IS 'Posty konkurentów zebrane przez data-collector';
COMMENT ON COLUMN competitor.competitor_posts.embedding IS 'OpenAI text-embedding-3-small (1536d) — caption + hashtagi';
COMMENT ON COLUMN competitor.competitor_posts.follower_count_at_collection IS 'Przybliżenie — stan konta w chwili pobrania danych';

-- ── competitor_profiles ───────────────────────────────────────────────────────
CREATE TABLE competitor.competitor_profiles (
    id                          UUID         NOT NULL,
    ig_id                       VARCHAR(50),
    username                    VARCHAR(100) NOT NULL,
    follower_count              BIGINT       NOT NULL DEFAULT 0,
    media_count                 INT          NOT NULL DEFAULT 0,
    biography                   TEXT,

    rolling_avg_engagement_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    rolling_window_size         INT          NOT NULL DEFAULT 30,
    rolling_avg_updated_at      TIMESTAMPTZ,

    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_competitor_profiles PRIMARY KEY (id),
    CONSTRAINT uq_competitor_profiles_username UNIQUE (username)
);

COMMENT ON TABLE competitor.competitor_profiles IS 'Snapshot profilu konkurenta z agregowanym ER';
COMMENT ON COLUMN competitor.competitor_profiles.rolling_avg_engagement_rate IS 'Średnia krocząca ER z ostatnich rolling_window_size postów';

-- ── engagement_analyses ───────────────────────────────────────────────────────
CREATE TABLE competitor.engagement_analyses (
    id                    UUID             NOT NULL,
    ig_media_id           VARCHAR(50)      NOT NULL,
    competitor_username   VARCHAR(100)     NOT NULL,

    engagement_rate       DOUBLE PRECISION NOT NULL,
    delta_vs_rolling_avg  DOUBLE PRECISION NOT NULL,
    level                 VARCHAR(20)      NOT NULL,

    analyzed_at           TIMESTAMPTZ      NOT NULL DEFAULT now(),

    CONSTRAINT pk_engagement_analyses PRIMARY KEY (id),
    CONSTRAINT uq_engagement_analyses_media UNIQUE (ig_media_id),
    CONSTRAINT fk_engagement_analyses_post
        FOREIGN KEY (ig_media_id)
        REFERENCES competitor.competitor_posts (ig_media_id)
);

CREATE INDEX idx_engagement_analyses_username
    ON competitor.engagement_analyses (competitor_username);
CREATE INDEX idx_engagement_analyses_level
    ON competitor.engagement_analyses (level);

-- ── hashtag_performances ──────────────────────────────────────────────────────
CREATE TABLE competitor.hashtag_performances (
    id                      UUID             NOT NULL,
    hashtag                 VARCHAR(100)     NOT NULL,
    ig_hashtag_id           VARCHAR(50)      NOT NULL,

    top_media_count         INT              NOT NULL DEFAULT 0,
    avg_like_count          BIGINT           NOT NULL DEFAULT 0,
    avg_comments_count      INT              NOT NULL DEFAULT 0,

    trend                   VARCHAR(20)      NOT NULL,
    trend_score             DOUBLE PRECISION NOT NULL DEFAULT 0,

    collected_at            TIMESTAMPTZ      NOT NULL DEFAULT now(),
    previous_collected_at   TIMESTAMPTZ,

    CONSTRAINT pk_hashtag_performances PRIMARY KEY (id)
);

CREATE INDEX idx_hashtag_performances_hashtag
    ON competitor.hashtag_performances (hashtag);
CREATE INDEX idx_hashtag_performances_collected_at
    ON competitor.hashtag_performances (collected_at DESC);

COMMENT ON TABLE competitor.hashtag_performances IS 'Wydajność hashtagów w niszy — jeden rekord per kolekcja';

-- ── analysis_results ──────────────────────────────────────────────────────────
CREATE TABLE competitor.analysis_results (
    id                    UUID         NOT NULL,
    trigger_event_id      VARCHAR(100) NOT NULL,
    competitor_username   VARCHAR(100),
    analysis_type         VARCHAR(30)  NOT NULL,

    top_hashtags          TEXT[],
    optimal_posting_hour  VARCHAR(5),
    status                VARCHAR(20)  NOT NULL,

    engagement_analysis_id UUID,
    hashtag_performance_id UUID,

    analyzed_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_analysis_results PRIMARY KEY (id),
    CONSTRAINT fk_analysis_results_engagement
        FOREIGN KEY (engagement_analysis_id)
        REFERENCES competitor.engagement_analyses (id),
    CONSTRAINT fk_analysis_results_hashtag
        FOREIGN KEY (hashtag_performance_id)
        REFERENCES competitor.hashtag_performances (id)
);

CREATE INDEX idx_analysis_results_competitor
    ON competitor.analysis_results (competitor_username);
CREATE INDEX idx_analysis_results_type
    ON competitor.analysis_results (analysis_type);
CREATE INDEX idx_analysis_results_analyzed_at
    ON competitor.analysis_results (analyzed_at DESC);

COMMENT ON TABLE competitor.analysis_results IS 'Wyniki analiz per uruchomienie — historia do uczenia się';