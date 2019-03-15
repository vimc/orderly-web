CREATE TABLE "orderlyweb_user" (
"username"  TEXT ,
"email" TEXT ,
"disabled" INTEGER DEFAULT 0 ,
"user_source" TEXT ,
PRIMARY KEY ("email")
);

CREATE TABLE "orderlyweb_user_group" (
"id"  TEXT ,
PRIMARY KEY ("id")
);

CREATE TABLE "orderlyweb_user_group_user" (
"user"  TEXT NOT NULL,
"user_group" TEXT NOT NULL,
FOREIGN KEY ("user") REFERENCES "orderlyweb_user" ("username") ,
FOREIGN KEY ("user_group") REFERENCES "orderlyweb_user_group" ("id")
);

CREATE TABLE "orderlyweb_permission" (
"id"  TEXT ,
PRIMARY KEY ("id")
);

CREATE TABLE "orderlyweb_user_group_permission" (
"id" SERIAL ,
"user_group" TEXT NOT NULL,
"permission" TEXT NOT NULL,
PRIMARY KEY ("id") ,
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

