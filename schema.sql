
CREATE TYPE party_phase  AS ENUM ('WARMUP', 'PEAK', 'SLOW');
CREATE TYPE party_status AS ENUM ('DRAFT', 'LIVE', 'FINISHED');
CREATE TYPE member_role  AS ENUM ('HOST', 'GUEST');
CREATE TYPE track_source AS ENUM ('FILE', 'YOUTUBE');
CREATE TYPE track_status AS ENUM ('QUEUED', 'PLAYING', 'PLAYED', 'REJECTED');

-- ---------- users ----------------------------------------------------
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- ---------- genres ---------------------------------------------------
CREATE TABLE genres (
    id   SERIAL       PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE);

-- ---------- parties --------------------------------------------------
CREATE TABLE parties (
    id                        BIGSERIAL    PRIMARY KEY,
    host_id                   BIGINT       NOT NULL
                                  REFERENCES users(id) ON DELETE CASCADE,
    title                     VARCHAR(255) NOT NULL,
    join_code                 VARCHAR(32)  NOT NULL UNIQUE,
    phase                     party_phase  NOT NULL DEFAULT 'WARMUP',
    status                    party_status NOT NULL DEFAULT 'DRAFT',
    max_suggestions_per_guest INTEGER      NOT NULL DEFAULT 5
                                  CHECK (max_suggestions_per_guest >= 0),
    created_at                TIMESTAMP    NOT NULL DEFAULT now()
);

-- ---------- party_members -------------------------------------------
CREATE TABLE party_members (
    party_id  BIGINT      NOT NULL REFERENCES parties(id) ON DELETE CASCADE,
    user_id   BIGINT      NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    role      member_role NOT NULL DEFAULT 'GUEST',
    joined_at TIMESTAMP   NOT NULL DEFAULT now(),
    PRIMARY KEY (party_id, user_id)
);

-- ---------- tracks ---------------------------------------------------
CREATE TABLE tracks (
    id           BIGSERIAL    PRIMARY KEY,
    party_id     BIGINT       NOT NULL REFERENCES parties(id) ON DELETE CASCADE,
    suggested_by BIGINT           NULL REFERENCES users(id)   ON DELETE SET NULL,
    title        VARCHAR(255) NOT NULL,
    artist       VARCHAR(255),
    genre_id     INTEGER          NULL REFERENCES genres(id)  ON DELETE SET NULL,
    energy       INTEGER      NOT NULL DEFAULT 1
                                  CHECK (energy BETWEEN 1 AND 5),
    source_type  track_source NOT NULL,
    audio_path   VARCHAR(512),
    youtube_url  VARCHAR(512),
    cover_path   VARCHAR(512),
    status       track_status NOT NULL DEFAULT 'QUEUED',
    added_at     TIMESTAMP    NOT NULL DEFAULT now(),
    played_at    TIMESTAMP,
    CONSTRAINT chk_tracks_source CHECK (
        (source_type = 'FILE'    AND audio_path  IS NOT NULL) OR
        (source_type = 'YOUTUBE' AND youtube_url IS NOT NULL)
    )
);

-- ---------- votes ----------------------------------------------------
CREATE TABLE votes (
    user_id    BIGINT    NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    track_id   BIGINT    NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
    value      SMALLINT  NOT NULL CHECK (value IN (-1, 1)),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, track_id)
);