-- Party Playlist: тестовые данные.
-- Запуск после schema.sql: psql -U postgres -d party_playlist -f data.sql
-- Пароль у всех пользователей: password123
-- (хеш = SHA-256 в hex, как его считает UserService)
--
-- Треки намеренно только из YouTube: для источника FILE нужны реальные mp3
-- в storage.dir, которых в репозитории нет.

DELETE FROM votes;
DELETE FROM tracks;
DELETE FROM party_members;
DELETE FROM parties;
DELETE FROM users;

-- ---------- пользователи ---------------------------------------------
INSERT INTO users (id, username, password_hash) VALUES
    (1, 'dj_anna', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
    (2, 'max',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
    (3, 'lena',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
    (4, 'igor',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

-- ---------- вечеринки ------------------------------------------------
-- 1: идёт прямо сейчас, фаза PEAK — основная для демонстрации
-- 2: ещё не началась (DRAFT), с жёстким лимитом предложений
-- 3: уже закончилась (FINISHED)
INSERT INTO parties (id, host_id, title, join_code, phase, status, max_suggestions_per_guest) VALUES
    (1, 1, 'Днюха Ани',         'KT23WQ', 'PEAK',   'LIVE',     5),
    (2, 2, 'Пятничный созвон',  'M3S4KF', 'WARMUP', 'DRAFT',    2),
    (3, 1, 'Новый год',         'PQ7XZ2', 'SLOW',   'FINISHED', 10);

INSERT INTO party_members (party_id, user_id, role) VALUES
    (1, 1, 'HOST'),
    (1, 2, 'GUEST'),
    (1, 3, 'GUEST'),
    (1, 4, 'GUEST'),
    (2, 2, 'HOST'),
    (2, 3, 'GUEST'),
    (3, 1, 'HOST'),
    (3, 4, 'GUEST');

-- ---------- треки ----------------------------------------------------
-- Вечеринка 1: один PLAYING, очередь из QUEUED, плюс PLAYED и REJECTED.
INSERT INTO tracks (id, party_id, suggested_by, title, artist, genre_id, energy,
                    source_type, youtube_url, status, played_at) VALUES
    (1, 1, 1, 'Levels',            'Avicii',        4, 5, 'YOUTUBE', 'https://www.youtube.com/watch?v=_ovdm2yX4MA', 'PLAYING',  NULL),
    (2, 1, 2, 'Blinding Lights',   'The Weeknd',    1, 4, 'YOUTUBE', 'https://www.youtube.com/watch?v=4NRXx6U8ABQ', 'QUEUED',   NULL),
    (3, 1, 3, 'Titanium',          'David Guetta',  4, 5, 'YOUTUBE', 'https://www.youtube.com/watch?v=JRfuAukYTKg', 'QUEUED',   NULL),
    (4, 1, 4, 'Smells Like Teen Spirit', 'Nirvana', 2, 5, 'YOUTUBE', 'https://www.youtube.com/watch?v=hTWKbfoikeg', 'QUEUED',   NULL),
    (5, 1, 3, 'Skinny Love',       'Bon Iver',      6, 1, 'YOUTUBE', 'https://www.youtube.com/watch?v=ssdgFoHLwnk', 'QUEUED',   NULL),
    (6, 1, 2, 'Take Five',         'Dave Brubeck',  7, 2, 'YOUTUBE', 'https://www.youtube.com/watch?v=vmDDOFXSgAs', 'QUEUED',   NULL),
    (7, 1, 1, 'One More Time',     'Daft Punk',     4, 4, 'YOUTUBE', 'https://www.youtube.com/watch?v=FGBhQbmPwH8', 'PLAYED',   now() - interval '40 minutes'),
    (8, 1, 4, 'Крошка моя',        'Руки Вверх',    1, 3, 'YOUTUBE', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'REJECTED', NULL);

-- Вечеринка 2: гость lena уже израсходовал лимит в 2 трека.
INSERT INTO tracks (id, party_id, suggested_by, title, artist, genre_id, energy,
                    source_type, youtube_url, status) VALUES
    (9,  2, 3, 'Sunflower',  'Post Malone',  3, 3, 'YOUTUBE', 'https://www.youtube.com/watch?v=ApXoWvfEYVU', 'QUEUED'),
    (10, 2, 3, 'HUMBLE.',    'Kendrick Lamar', 3, 4, 'YOUTUBE', 'https://www.youtube.com/watch?v=tvTRZJ-4EyI', 'QUEUED'),
    (11, 2, 2, 'Do I Wanna Know?', 'Arctic Monkeys', 6, 3, 'YOUTUBE', 'https://www.youtube.com/watch?v=bpOSxM0rNPM', 'QUEUED');

-- Вечеринка 3: всё отыграно.
INSERT INTO tracks (id, party_id, suggested_by, title, artist, genre_id, energy,
                    source_type, youtube_url, status, played_at) VALUES
    (12, 3, 1, 'Last Christmas', 'Wham!',      1, 2, 'YOUTUBE', 'https://www.youtube.com/watch?v=E8gmARGvPlI', 'PLAYED', now() - interval '20 days'),
    (13, 3, 4, 'All Night',      'Icona Pop',  1, 4, 'YOUTUBE', 'https://www.youtube.com/watch?v=jtD2fjcAcKU', 'PLAYED', now() - interval '20 days');

-- ---------- голоса ---------------------------------------------------
-- Очередь сортируется по рейтингу: Titanium (+3) выше Blinding Lights (+1),
-- Take Five уходит в минус (-1).
INSERT INTO votes (user_id, track_id, value) VALUES
    (1, 3,  1),
    (2, 3,  1),
    (4, 3,  1),
    (1, 2,  1),
    (3, 2, -1),
    (4, 2,  1),
    (1, 4,  1),
    (2, 4,  1),
    (3, 5,  1),
    (2, 6, -1),
    (3, 9,  1),
    (2, 10, 1);

-- Явные id выше не двигают последовательности — выравниваем.
SELECT setval('users_id_seq',   (SELECT max(id) FROM users));
SELECT setval('parties_id_seq', (SELECT max(id) FROM parties));
SELECT setval('tracks_id_seq',  (SELECT max(id) FROM tracks));
