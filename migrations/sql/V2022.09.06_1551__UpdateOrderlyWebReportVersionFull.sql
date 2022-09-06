DROP VIEW orderlyweb_report_version_full;

CREATE VIEW orderlyweb_report_version_full AS
SELECT rv.id, report, date, displayname, description, connection, elapsed, git_sha, git_branch, git_clean, ow_rv.published
FROM report_version rv
INNER JOIN orderlyweb_report_version ow_rv
ON rv.id = ow_rv.id;
