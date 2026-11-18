-- Party Playlist: схема базы данных.
-- Запуск: psql -U postgres -d party_playlist -f schema.sql
-- Тестовые данные лежат отдельно, в data.sql.

DROP TABLE IF EXISTS votes, tracks, party_members, parties, genres, users CASCADE;

-- ---------- users ----------------------------------------------------
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL UNIQUE,
    password_hash VARCHAR(64)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- ---------- genres ---------------------------------------------------
CREATE TABLE genres (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ---------- parties --------------------------------------------------
CREATE TABLE parties (
    id                        BIGSERIAL    PRIMARY KEY,
    host_id                   BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title                     VARCHAR(100) NOT NULL,
    join_code                 VARCHAR(8)   NOT NULL UNIQUE,
    phase                     VARCHAR(10)  NOT NULL DEFAULT 'WARMUP'
                                  CHECK (phase IN ('WARMUP', 'PEAK', 'SLOW')),
    status                    VARCHAR(10)  NOT NULL DEFAULT 'DRAFT'
                                  CHECK (status IN ('DRAFT', 'LIVE', 'FINISHED')),
    max_suggestions_per_guest SMALLINT     NOT NULL DEFAULT 5
                                  CHECK (max_suggestions_per_guest BETWEEN 1 AND 50),
    created_at                TIMESTAMP    NOT NULL DEFAULT now()
);

-- ---------- party_members --------------------------------------------
CREATE TABLE party_members (
    party_id  BIGINT      NOT NULL REFERENCES parties (id) ON DELETE CASCADE,
    user_id   BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role      VARCHAR(10) NOT NULL CHECK (role IN ('HOST', 'GUEST')),
    joined_at TIMESTAMP   NOT NULL DEFAULT now(),
    PRIMARY KEY (party_id, user_id)
);

-- ---------- tracks ---------------------------------------------------
CREATE TABLE tracks (
    id           BIGSERIAL    PRIMARY KEY,
    party_id     BIGINT       NOT NULL REFERENCES parties (id) ON DELETE CASCADE,
    suggested_by BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    artist       VARCHAR(200) NOT NULL,
    genre_id     INTEGER      REFERENCES genres (id) ON DELETE SET NULL,
    energy       SMALLINT     NOT NULL CHECK (energy BETWEEN 1 AND 5),
    source_type  VARCHAR(10)  NOT NULL CHECK (source_type IN ('FILE', 'YOUTUBE')),
    audio_path   VARCHAR(255),
    youtube_url  VARCHAR(500),
    cover_path   VARCHAR(255),
    status       VARCHAR(10)  NOT NULL DEFAULT 'QUEUED'
                     CHECK (status IN ('QUEUED', 'PLAYING', 'PLAYED', 'REJECTED')),
    added_at     TIMESTAMP    NOT NULL DEFAULT now(),
    played_at    TIMESTAMP,
    CONSTRAINT tracks_source_chk CHECK (
        (source_type = 'FILE'    AND audio_path  IS NOT NULL AND youtube_url IS NULL) OR
        (source_type = 'YOUTUBE' AND youtube_url IS NOT NULL AND audio_path  IS NULL)
    )
);

-- ---------- votes ----------------------------------------------------
CREATE TABLE votes (
    user_id    BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    track_id   BIGINT    NOT NULL REFERENCES tracks (id) ON DELETE CASCADE,
    value      SMALLINT  NOT NULL CHECK (value IN (-1, 1)),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, track_id)
);

CREATE INDEX idx_parties_host ON parties (host_id);
CREATE INDEX idx_tracks_party_status ON tracks (party_id, status);

-- ---------- справочник жанров ----------------------------------------
-- Не тестовые данные: без них форма предложения трека остаётся без списка жанров.
INSERT INTO genres (name) VALUES
    ('Поп'), ('Рок'), ('Хип-хоп'), ('Электроника'), ('R&B'), ('Инди'), ('Джаз'), ('Другое');
