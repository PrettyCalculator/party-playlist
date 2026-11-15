-- Схема БД Party Playlist (PostgreSQL)
-- Запуск: psql -U postgres -d party_playlist -f src/main/resources/db/schema.sql

DROP TABLE IF EXISTS votes, party_tracks, party_members, parties, tracks, genres, users CASCADE;

-- Пользователи
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL UNIQUE,
    password_hash VARCHAR(60)  NOT NULL,              -- BCrypt-хэш всегда 60 символов
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- Справочник жанров (O2M: жанр -> треки)
CREATE TABLE genres (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Каталог треков. Музыка добавляется либо файлом, либо ссылкой на YouTube.
CREATE TABLE tracks (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    artist       VARCHAR(200) NOT NULL,
    genre_id     INT          REFERENCES genres (id) ON DELETE SET NULL,
    energy       SMALLINT     NOT NULL CHECK (energy BETWEEN 1 AND 5),
    duration_sec INT          CHECK (duration_sec > 0),
    source_type  VARCHAR(10)  NOT NULL CHECK (source_type IN ('FILE', 'YOUTUBE')),
    audio_path   VARCHAR(255),                         -- путь к файлу в хранилище (для FILE)
    youtube_url  VARCHAR(500),                         -- ссылка на YouTube (для YOUTUBE)
    cover_path   VARCHAR(255),                         -- обложка, необязательна
    uploaded_by  BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    -- ровно один источник, соответствующий типу
    CONSTRAINT tracks_source_chk CHECK (
        (source_type = 'FILE'    AND audio_path  IS NOT NULL AND youtube_url IS NULL) OR
        (source_type = 'YOUTUBE' AND youtube_url IS NOT NULL AND audio_path  IS NULL)
    )
);

-- Вечеринки (O2M: хост -> вечеринки)
CREATE TABLE parties (
    id                        BIGSERIAL PRIMARY KEY,
    host_id                   BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title                     VARCHAR(100) NOT NULL,
    join_code                 VARCHAR(8)   NOT NULL UNIQUE,
    -- фаза: WARMUP = разминка, PEAK = пик, SLOW = медляк
    phase                     VARCHAR(10)  NOT NULL DEFAULT 'WARMUP'
                              CHECK (phase IN ('WARMUP', 'PEAK', 'SLOW')),
    status                    VARCHAR(10)  NOT NULL DEFAULT 'DRAFT'
                              CHECK (status IN ('DRAFT', 'LIVE', 'FINISHED')),
    -- правила комнаты
    max_suggestions_per_guest SMALLINT     NOT NULL DEFAULT 5 CHECK (max_suggestions_per_guest BETWEEN 1 AND 50),
    no_artist_repeat          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMP    NOT NULL DEFAULT now()
);

-- Участники вечеринки (M2M: пользователи <-> вечеринки)
CREATE TABLE party_members (
    party_id  BIGINT      NOT NULL REFERENCES parties (id) ON DELETE CASCADE,
    user_id   BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role      VARCHAR(10) NOT NULL CHECK (role IN ('HOST', 'GUEST')),
    joined_at TIMESTAMP   NOT NULL DEFAULT now(),
    PRIMARY KEY (party_id, user_id)
);

-- Треки в очереди конкретной вечеринки (O2M: вечеринка -> треки в очереди)
CREATE TABLE party_tracks (
    id           BIGSERIAL PRIMARY KEY,
    party_id     BIGINT      NOT NULL REFERENCES parties (id) ON DELETE CASCADE,
    track_id     BIGINT      NOT NULL REFERENCES tracks (id) ON DELETE CASCADE,
    suggested_by BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status       VARCHAR(10) NOT NULL DEFAULT 'QUEUED'
                 CHECK (status IN ('QUEUED', 'PLAYING', 'PLAYED', 'VETOED')),
    added_at     TIMESTAMP   NOT NULL DEFAULT now(),
    played_at    TIMESTAMP,
    UNIQUE (party_id, track_id)
);

-- Голоса (M2M: пользователи <-> треки в очереди)
CREATE TABLE votes (
    user_id        BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    party_track_id BIGINT    NOT NULL REFERENCES party_tracks (id) ON DELETE CASCADE,
    value          SMALLINT  NOT NULL CHECK (value IN (-1, 1)),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, party_track_id)
);

CREATE INDEX idx_tracks_uploaded_by ON tracks (uploaded_by);
CREATE INDEX idx_parties_host ON parties (host_id);
CREATE INDEX idx_party_tracks_party_status ON party_tracks (party_id, status);

-- Начальные данные
INSERT INTO genres (name) VALUES
    ('Поп'), ('Рок'), ('Хип-хоп'), ('Электроника'), ('R&B'), ('Инди'), ('Джаз'), ('Другое');
