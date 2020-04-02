INSERT INTO "orderlyweb_user_group_global_permission"
SELECT ID FROM "orderlyweb_user_group_permission"
WHERE user_group = 'Admin' and permission = 'documents.manage' or  permission = 'tags.manage'
