create sequence users_storage_seq start with 1 increment by 50;
create sequence usr_seq start with 1 increment by 50;

create table user_access
(
    user_id       bigint not null,
    access_levels varchar(255) check (access_levels in ('LEVEL_1', 'LEVEL_2', 'LEVEL_3'))
);

create table user_role
(
    user_id bigint not null,
    roles   varchar(255) check (roles in ('MAIN_ADMIN', 'ADMIN', 'USER'))
);

create table user_spec_access
(
    user_id       bigint not null,
    spec_accesses varchar(255) check (spec_accesses in ('SPEC_ACCESS_1', 'SPEC_ACCESS_2', 'SPEC_ACCESS_3'))
);

create table users_storage
(
    is_children_storage boolean      not null,
    is_parent_storage   boolean      not null,
    administrator_id    bigint unique,
    changed_user_id     bigint unique,
    created_user_id     bigint unique,
    date_changed        timestamp(6),
    date_created        timestamp(6),
    id                  bigint       not null,
    log_file            text default '{}',
    storage_description text default '',
    users_storage_name  varchar(255) not null,
    primary key (id)
);

create table users_storage_children_users_storages
(
    children_users_storages_id bigint not null unique,
    users_storage_id           bigint not null,
    primary key (children_users_storages_id, users_storage_id)
);

create table users_storage_storage_users
(
    storage_users_id bigint not null unique,
    users_storage_id bigint not null,
    primary key (storage_users_id, users_storage_id)
);

create table usr
(
    active           boolean not null,
    administrator_id bigint,
    changed_user_id  bigint unique,
    created_user_id  bigint unique,
    date_changed     timestamp(6),
    date_created     timestamp(6),
    id               bigint  not null,
    users_storage_id bigint,
    log_file         text default '{}',
    password         varchar(255),
    special_notes    text default 'Примечание',
    username         varchar(255),
    primary key (id)
);

alter table if exists user_access add constraint FKglwp8src5iuf44of20vruhff foreign key (user_id) references usr;

alter table if exists user_role add constraint FKfpm8swft53ulq2hl11yplpr5 foreign key (user_id) references usr;

alter table if exists user_spec_access add constraint FKp6niddy5dfmtly4ph3htw0lqv foreign key (user_id) references usr;

alter table if exists users_storage add constraint FKnsum4dvnrkgxfhh386opbl05k foreign key (administrator_id) references usr;

alter table if exists users_storage add constraint FKdm01a17hm2vnx0k4gp0wv1o7l foreign key (changed_user_id) references usr;

alter table if exists users_storage add constraint FKsjj15phorc4gaupj8mb5rk88x foreign key (created_user_id) references usr;

alter table if exists users_storage_children_users_storages add constraint FK41e2vfqotgitbob1o7t8hco61 foreign key (children_users_storages_id) references users_storage;

alter table if exists users_storage_children_users_storages add constraint FKlgk47wcrfqn8f42srhqtgf7q3 foreign key (users_storage_id) references users_storage;

alter table if exists users_storage_storage_users add constraint FKdlqjmdnpldmkancxrhrvhkot5 foreign key (storage_users_id) references usr;

alter table if exists users_storage_storage_users add constraint FKgnvtrw330rpvvvyyltxnwr3el foreign key (users_storage_id) references users_storage;

alter table if exists usr add constraint FKqoqssaruj9lebcvsut57xu9d0 foreign key (administrator_id) references usr;

alter table if exists usr add constraint FK9884ncyaa6gfgt6pdxo8gykco foreign key (changed_user_id) references usr;

alter table if exists usr add constraint FK7ikp03aw8c9fob230m6bivh9v foreign key (created_user_id) references usr;

alter table if exists usr add constraint FK2joswub6gvvqcv89vduou53l2 foreign key (users_storage_id) references users_storage