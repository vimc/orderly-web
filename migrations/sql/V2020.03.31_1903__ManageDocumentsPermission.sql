INSERT INTO "orderlyweb_permission" VALUES ("documents.manage");

INSERT OR IGNORE INTO "orderlyweb_user_group_permission" (user_group, permission)
VALUES ("Admin", "documents.manage");