CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL PRIMARY KEY,
    username  VARCHAR(250) NOT NULL
);

CREATE TABLE IF NOT EXISTS reviews (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id    BIGINT REFERENCES users (id) ON DELETE SET NULL,
    event_id     BIGINT NOT NULL,
    title        VARCHAR(120),
    content      VARCHAR(10000) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date TIMESTAMP WITHOUT TIME ZONE,
    mark         BIGINT NOT NULL,
    likes        BIGINT,
    dislikes     BIGINT
);

CREATE TABLE IF NOT EXISTS opinions
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evaluator_id BIGINT,
    review_id    BIGINT REFERENCES reviews (id) ON DELETE CASCADE,
    label        BIGINT NOT NULL,
    CONSTRAINT uq_evaluator_review UNIQUE (evaluator_id, review_id)
);