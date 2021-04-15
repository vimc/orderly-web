CREATE TABLE orderlyweb_workflow_run
(
    id         INTEGER PRIMARY KEY,
    name       TEXT      NOT NULL,
    key        TEXT      NOT NULL UNIQUE,
    email      TEXT      NOT NULL,
    date       TIMESTAMP NOT NULL,
    reports    TEXT      NOT NULL,
    instances  TEXT      NOT NULL,
    git_branch TEXT,
    git_commit TEXT,
    status     TEXT,
    UNIQUE (name, date),
    FOREIGN KEY (email) REFERENCES orderlyweb_user (email)
);
