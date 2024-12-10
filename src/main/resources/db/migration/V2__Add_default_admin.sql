insert into usr (id, username, password, active, access_to_sd)
    values (0, 'MAIN_ADMIN', 'MAIN_ADMIN', true, true);

insert into user_role (user_id, roles)
    values (0, 'USER');

insert into user_role (user_id, roles)
    values (0, 'ADMIN');

insert into user_role (user_id, roles)
values (0, 'MAIN_ADMIN');

insert into user_access (user_id, access_levels)
values (0, 'LEVEL_1');

insert into user_access (user_id, access_levels)
values (0, 'LEVEL_2');

insert into user_access (user_id, access_levels)
values (0, 'LEVEL_3')