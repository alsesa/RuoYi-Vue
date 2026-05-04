-- SQL控制台菜单
insert into sys_menu values('118', 'SQL控制台', '3', '4', 'sqlConsole', 'tool/sqlConsole/index', '', '', 1, 0, 'C', '0', '0', 'tool:sqlConsole:list', 'sql', 'admin', sysdate(), '', null, 'SQL控制台菜单');

-- 关联管理员角色
insert into sys_role_menu values('1', '118');
