CREATE TABLE "orderlyweb_report_version" (
"id"  TEXT NOT NULL,
"published" BOOLEAN NOT NULL,
FOREIGN KEY ("id") REFERENCES "report_version" ("id"),
PRIMARY KEY("id")
);

INSERT INTO "orderlyweb_report_version"
SELECT id, published
FROM report_version;