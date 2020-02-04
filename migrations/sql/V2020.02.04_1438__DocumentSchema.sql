CREATE TABLE "orderlyweb_document"(
    "id" INTEGER PRIMARY KEY NOT NULL,
    "filename" TEXT NOT NULL,
    "show" INTEGER DEFAULT 1,
    "display_name" TEXT,
    "description" TEXT,
    FOREIGN KEY ("parent") REFERENCES "orderlyweb_document_dir" ("path") NOT NULL
)

CREATE TABLE "orderlyweb_document_dir"(
    "path" TEXT,
    PRIMARY KEY ("path"),
    FOREIGN KEY ("parent") REFERENCES "orderlyweb_document_dir" ("path")
)