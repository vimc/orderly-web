CREATE TABLE orderlyweb_workflow_run_reports
(
    id              INTEGER PRIMARY KEY,
    workflow_key    TEXT      NOT NULL,
    report_key      TEXT      NOT NULL UNIQUE,
    name            TEXT      NOT NULL,
    params          TEXT      NOT NULL,
    FOREIGN KEY(workflow_key) REFERENCES orderlyweb_workflow_run(key)
);
