INSERT OR IGNORE INTO "orderlyweb_user_group_global_permission"
SELECT ID FROM "orderlyweb_user_group_permission" WHERE user_group = 'Admin';
