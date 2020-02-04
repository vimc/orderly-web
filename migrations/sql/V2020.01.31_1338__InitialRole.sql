INSERT OR IGNORE INTO "orderlyweb_user_group"
VALUES ("Admin");

INSERT OR IGNORE INTO "orderlyweb_user_group_permission" (user_group, permission)
VALUES ("Admin", "users.manage"),
       ("Admin", "reports.read"),
       ("Admin", "reports.review"),
       ("Admin", "reports.run");

INSERT OR IGNORE INTO "orderlyweb_user_group_global_permission"
SELECT ID FROM "orderlyweb_user_group_permission" WHERE user_group = 'Admin';
