CREATE TABLE competitor.hashtag_collected_posts (
    id              UUID         NOT NULL,
    ig_media_id     VARCHAR(50)  NOT NULL,
    hashtag         VARCHAR(100) NOT NULL,
    ig_hashtag_id   VARCHAR(50)  NOT NULL,

    media_type      VARCHAR(20)  NOT NULL,
    permalink       TEXT,
    caption         TEXT,
    hashtags        TEXT[],

    like_count      BIGINT       NOT NULL DEFAULT 0,
    comments_count  INT          NOT NULL DEFAULT 0,

    published_at    TIMESTAMPTZ  NOT NULL,
    collected_at    TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_hashtag_collected_posts
        PRIMARY KEY (id),
    CONSTRAINT uq_hashtag_collected_posts_media_hashtag
        UNIQUE (ig_media_id, hashtag)
);

CREATE INDEX idx_hashtag_collected_posts_hashtag
    ON competitor.hashtag_collected_posts (hashtag);
CREATE INDEX idx_hashtag_collected_posts_published_at
    ON competitor.hashtag_collected_posts (published_at DESC);

COMMENT ON TABLE competitor.hashtag_collected_posts
    IS 'Posty zebrane przez wyszukiwanie po hashtagu — bez znanego autora (Business Discovery/hashtag search tego nie zwraca)';