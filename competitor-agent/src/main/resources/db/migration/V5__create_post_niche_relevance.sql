CREATE TABLE competitor.post_niche_relevance (
    id                UUID             NOT NULL,
    ig_media_id       VARCHAR(50)      NOT NULL,
    source_type       VARCHAR(20)      NOT NULL,
    owner_ig_id       VARCHAR(50)      NOT NULL,

    matched_hashtags  TEXT[],
    weight            DOUBLE PRECISION NOT NULL,

    computed_at       TIMESTAMPTZ      NOT NULL DEFAULT now(),

    CONSTRAINT pk_post_niche_relevance
        PRIMARY KEY (id),
    CONSTRAINT uq_post_niche_relevance
        UNIQUE (ig_media_id, source_type, owner_ig_id)
);

CREATE INDEX idx_post_niche_relevance_owner
    ON competitor.post_niche_relevance (owner_ig_id, weight DESC);

COMMENT ON TABLE competitor.post_niche_relevance
    IS 'Waga trafności posta dla niszy danego fotografa — liczona na podstawie overlapu hashtagów';
COMMENT ON COLUMN competitor.post_niche_relevance.weight
    IS 'matchedHashtags.size() / liczba hashtagów aktywnie obserwowanych przez fotografa';