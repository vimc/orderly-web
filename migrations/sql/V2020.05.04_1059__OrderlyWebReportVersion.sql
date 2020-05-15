CREATE TABLE "orderlyweb_report_version" (
"id"  TEXT NOT NULL,
"published" BOOLEAN NOT NULL,
FOREIGN KEY ("id") REFERENCES "report_version" ("id"),
PRIMARY KEY("id")
);

INSERT INTO "orderlyweb_report_version"
SELECT id, published
FROM report_version;

CREATE TRIGGER insert_orderlyweb_report_version
   AFTER INSERT ON report_version
BEGIN
    INSERT OR IGNORE INTO orderlyweb_report_version(id, published)
    VALUES(NEW.id, 0);
END;

CREATE VIEW orderlyweb_report_version_full AS
SELECT rv.id, report, date, displayname, description, connection, elapsed, git_sha, git_branch, git_clean, requester, author, ow_rv.published
FROM report_version rv
INNER JOIN orderlyweb_report_version ow_rv
ON rv.id = ow_rv.id;