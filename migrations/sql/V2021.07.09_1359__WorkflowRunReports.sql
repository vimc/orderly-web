CREATE TABLE orderlyweb_workflow_run_reports
(
    id              INTEGER PRIMARY KEY,
    workflow_key    TEXT      NOT NULL,
    key             TEXT      NOT NULL UNIQUE,
    report          TEXT      NOT NULL,
    params          TEXT,
    FOREIGN KEY(workflow_key) REFERENCES orderlyweb_workflow_run(key)
);