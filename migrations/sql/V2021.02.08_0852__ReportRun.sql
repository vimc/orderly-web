CREATE TABLE orderlyweb_report_run
(
    id             INTEGER PRIMARY KEY,
    key            TEXT      NOT NULL UNIQUE,
    email          TEXT      NOT NULL,
    date           TIMESTAMP NOT NULL,
    report         TEXT      NOT NULL,
    instances      TEXT      NOT NULL,
    params         TEXT      NOT NULL,
    git_branch     TEXT,
    git_commit     TEXT,
    status         TEXT,
    logs           TEXT,
    report_version TEXT,
    FOREIGN KEY (email) REFERENCES orderlyweb_user (email),
    FOREIGN KEY (report_version) REFERENCES report_version (id)
);
