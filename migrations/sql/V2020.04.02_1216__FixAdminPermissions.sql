DELETE FROM orderlyweb_user_group_global_permission where user_group = 'Admin';

INSERT INTO "orderlyweb_user_group_global_permission"
SELECT ID FROM "orderlyweb_user_group_permission" WHERE user_group = 'Admin';
