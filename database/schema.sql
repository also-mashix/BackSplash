BEGIN TRANSACTION;

DROP TABLE IF EXISTS unsplash_watcher;

CREATE TABLE unsplash_watcher (
    id serial,
    unsplash_photo_hash VARCHAR(255) NOT NULL,
    photographer_name VARCHAR(255) NULL,
    photographer_page_link VARCHAR(255) NULL,
    saved_boolean boolean NOT NULL,
    file_path VARCHAR(255) NULL,


CONSTRAINT pk_unsplash_watcher PRIMARY KEY(id),
UNIQUE (unsplash_photo_hash)

);

COMMIT;