CREATE TABLE "orderlyweb_document"
(
    "path"         TEXT PRIMARY KEY NOT NULL,
    "name"         TEXT             NOT NULL,
    "show"         INTEGER DEFAULT 1,
    "display_name" TEXT,
    "description"  TEXT,
    "parent"       TEXT             NULL,
    "is_file"      INTEGER DEFAULT 1,
    FOREIGN KEY ("parent") REFERENCES "orderlyweb_document" ("path")
);
