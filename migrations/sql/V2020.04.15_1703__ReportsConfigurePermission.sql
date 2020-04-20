INSERT INTO "orderlyweb_permission" VALUES ('pinned-reports.manage');

INSERT OR IGNORE INTO "orderlyweb_user_group_permission" (user_group, permission)
VALUES ('Admin', 'pinned-reports.manage');

INSERT OR IGNORE INTO "orderlyweb_user_group_global_permission"
SELECT ID FROM "orderlyweb_user_group_permission" WHERE user_group = 'Admin' and permission = 'pinned-reports.manage';