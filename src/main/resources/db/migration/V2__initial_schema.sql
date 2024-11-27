ALTER TABLE reviews ADD likes BIGINT;
ALTER TABLE reviews ADD dislikes BIGINT;

CREATE TABLE IF NOT EXISTS opinions
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evaluator_id BIGINT,
    review_id    BIGINT REFERENCES reviews (id) ON DELETE CASCADE,
    label        BIGINT NOT NULL,
    CONSTRAINT uq_evaluator_review UNIQUE (evaluator_id, review_id)
);