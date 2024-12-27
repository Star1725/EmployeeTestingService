create sequence user_storage_seq start with 1 increment by 50;
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

create table user_storage
(
    is_child_storage       boolean not null,
    is_parent_storage      boolean not null,
    administrator_id       bigint,
    changed_user_id        bigint,
    created_user_id        bigint,
    date_changed           timestamp(6),
    date_created           timestamp(6),
    id                     bigint  not null,
    parent_user_storage_id bigint,
    log_file               text default '{}',
    storage_description    text default '',
    user_storage_name      varchar(255) not null,
    primary key (id)
);

create table user_storage_child_user_storages
(
    child_user_storages_id bigint not null,
    user_storage_id        bigint not null,
    primary key (child_user_storages_id, user_storage_id)
);

create table user_storage_storage_users
(
    storage_users_id bigint not null,
    user_storage_id  bigint not null,
    primary key (storage_users_id, user_storage_id)
);

create table usr
(
    active           boolean not null,
    administrator_id bigint,
    changed_user_id  bigint,
    created_user_id  bigint,
    date_changed     timestamp(6),
    date_created     timestamp(6),
    id               bigint  not null,
    user_storage_id bigint,
    log_file         text default '{}',
    password         varchar(255),
    special_notes    text default 'Примечание',
    username         varchar(255),
    primary key (id)
);

alter table if exists user_access
    add constraint FKglwp8src5iuf44of20vruhff foreign key (user_id) references usr;

alter table if exists user_role
    add constraint FKfpm8swft53ulq2hl11yplpr5 foreign key (user_id) references usr;

alter table if exists user_spec_access
    add constraint FKp6niddy5dfmtly4ph3htw0lqv foreign key (user_id) references usr;

alter table if exists user_storage
    add constraint FKn2hp07axjn9b33u41w5w1rshb foreign key (administrator_id) references usr;

alter table if exists user_storage
    add constraint FK53dhs9ds2ci08xm23o70s119c foreign key (changed_user_id) references usr;

alter table if exists user_storage
    add constraint FKln0hel2xfhipm9li0x08261l2 foreign key (created_user_id) references usr;

alter table if exists user_storage
    add constraint FKq12fbisjkcceaw2oa1mg7antn foreign key (parent_user_storage_id) references user_storage;

alter table if exists user_storage_child_user_storages
    add constraint FKbvuq3m70y4ef2malbnrg4fj2p foreign key (child_user_storages_id) references user_storage;

alter table if exists user_storage_child_user_storages
    add constraint FK2qbxm7qn406eww5f10ch9i2u9 foreign key (user_storage_id) references user_storage;

alter table if exists user_storage_storage_users
    add constraint FK4qnbak8ekwjxcoj7pbv9exien foreign key (storage_users_id) references usr;

alter table if exists user_storage_storage_users
    add constraint FKg2s24ff1crcalw864prj70rn5 foreign key (user_storage_id) references user_storage;

alter table if exists usr
    add constraint FKqoqssaruj9lebcvsut57xu9d0 foreign key (administrator_id) references usr;

alter table if exists usr
    add constraint FK9884ncyaa6gfgt6pdxo8gykco foreign key (changed_user_id) references usr;

alter table if exists usr
    add constraint FK7ikp03aw8c9fob230m6bivh9v foreign key (created_user_id) references usr;

alter table if exists usr
    add constraint FKmswnuk0n9tmd4a2afgoealjnk foreign key (user_storage_id) references user_storage