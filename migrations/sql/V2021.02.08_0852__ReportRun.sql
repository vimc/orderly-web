CREATE TABLE orderlyweb_report_run
(
    key         TEXT      NOT NULL,
    email       TEXT      NOT NULL,
    date        TIMESTAMP NOT NULL,
    report_name TEXT      NOT NULL,
    instances   TEXT      NOT NULL,
    params      TEXT      NOT NULL,
    git_branch  TEXT,
    git_commit  TEXT,
    status      TEXT,
    logs        TEXT,
    report_id   TEXT,
    PRIMARY KEY (key),
    FOREIGN KEY (email) REFERENCES orderlyweb_user (email),
    FOREIGN KEY (report_id) REFERENCES report_version (id)
);
