CREATE TABLE "orderlyweb_user" (
"username"  TEXT ,
"display_name" TEXT ,
"email" TEXT ,
"disabled" INTEGER DEFAULT 0 ,
"user_source" TEXT ,
"last_logged_in" TEXT ,
PRIMARY KEY ("email")
);

CREATE TABLE "orderlyweb_user_group" (
"id"  TEXT ,
PRIMARY KEY ("id")
);

CREATE TABLE "orderlyweb_user_group_user" (
"email"  TEXT NOT NULL,
"user_group" TEXT NOT NULL,
FOREIGN KEY ("email") REFERENCES "orderlyweb_user" ("email") ,
FOREIGN KEY ("user_group") REFERENCES "orderlyweb_user_group" ("id")
);

CREATE TABLE "orderlyweb_permission" (
"id"  TEXT ,
PRIMARY KEY ("id")
);

CREATE TABLE "orderlyweb_user_group_permission" (
"id" INTEGER PRIMARY KEY NOT NULL,
"user_group" TEXT NOT NULL,
"permission" TEXT NOT NULL,
FOREIGN KEY ("user_group") REFERENCES "orderlyweb_user_group" ("id") ,
FOREIGN KEY ("permission") REFERENCES "orderlyweb_permission" ("id")
);

CREATE TABLE "orderlyweb_user_group_report_permission" (
"id" INTEGER NOT NULL ,
"report" TEXT NOT NULL ,
FOREIGN KEY ("id") REFERENCES "orderlyweb_user_group_permission" ("id") ,
FOREIGN KEY ("report") REFERENCES "report" ("name")
);

CREATE TABLE "orderlyweb_user_group_version_permission" (
"id" INTEGER NOT NULL ,
"version" TEXT NOT NULL ,
FOREIGN KEY ("id") REFERENCES "orderlyweb_user_group_permission" ("id") ,
FOREIGN KEY ("version") REFERENCES "report_version" ("id")
);

CREATE TABLE "orderlyweb_user_group_global_permission" (
"id" INTEGER NOT NULL ,
FOREIGN KEY ("id") REFERENCES "orderlyweb_user_group_permission" ("id")
);

CREATE VIEW "orderlyweb_user_group_permission_all"
AS
SELECT abstract.*, scoped.scope_prefix, scoped.scope_id
FROM orderlyweb_user_group_permission as abstract
INNER JOIN
  (SELECT id, '*' AS scope_prefix, NULL AS scope_id
    FROM orderlyweb_user_group_global_permission
  UNION
  SELECT id, 'report' AS scope_prefix, report AS scope_id
  FROM orderlyweb_user_group_report_permission
  UNION
  SELECT id, 'version' AS scope_prefix, version AS scope_id
  FROM orderlyweb_user_group_version_permission) AS scoped
on abstract.id = scoped.id;

