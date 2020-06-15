DROP TRIGGER insert_orderlyweb_report_version;
DROP VIEW orderlyweb_report_version_full;

CREATE VIEW orderlyweb_report_version_full AS
SELECT rv.id, report, date, displayname, description, connection, elapsed, git_sha, git_branch, git_clean, requester, author,
    COALESCE(ow_rv.published, 0) as published
FROM report_version rv
LEFT OUTER JOIN orderlyweb_report_version ow_rv
ON rv.id = ow_rv.id;