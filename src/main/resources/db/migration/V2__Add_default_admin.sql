insert into usr (id, username, password, active)
    values (0, 'MAIN_ADMIN', 'MAIN_ADMIN', true);

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
values (0, 'LEVEL_3');

insert into user_spec_access (user_id, spec_accesses)
values (0, 'SPEC_ACCESS_1');

insert into user_spec_access (user_id, spec_accesses)
values (0, 'SPEC_ACCESS_2');

insert into user_spec_access (user_id, spec_accesses)
values (0, 'SPEC_ACCESS_3')