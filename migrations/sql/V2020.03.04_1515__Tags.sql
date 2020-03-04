CREATE TABLE "orderlyweb_report_tag" (
"report"  TEXT NOT NULL,
"tag" TEXT NOT NULL,
FOREIGN KEY ("report") REFERENCES "report" ("name"),
PRIMARY KEY("report", "tag")
);

CREATE TABLE "orderlyweb_report_version_tag" (
"report_version"  TEXT NOT NULL,
"tag" TEXT NOT NULL,
FOREIGN KEY ("report_version") REFERENCES "report_version" ("id"),
PRIMARY KEY("report_version", "tag")
);