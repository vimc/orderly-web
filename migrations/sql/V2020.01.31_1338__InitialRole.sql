INSERT OR IGNORE INTO "orderlyweb_role"
VALUES ("Admin");

INSERT OR IGNORE INTO "orderlyweb_user_group_permission" (user_group, permission)
VALUES ("Admin", "users.manage");

INSERT OR IGNORE INTO "orderlyweb_user_group_global_permission" (user_group, permission)
SELECT ID, "Admin" FROM "orderlyweb_user_group_permission";
