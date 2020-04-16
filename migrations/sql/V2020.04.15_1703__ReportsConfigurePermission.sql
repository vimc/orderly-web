INSERT INTO "orderlyweb_permission" VALUES ('reports.configure');

INSERT OR IGNORE INTO "orderlyweb_user_group_permission" (user_group, permission)
VALUES ('Admin', 'reports.configure');

INSERT OR IGNORE INTO "orderlyweb_user_group_global_permission"
SELECT ID FROM "orderlyweb_user_group_permission" WHERE user_group = 'Admin' and permission = 'reports.configure';