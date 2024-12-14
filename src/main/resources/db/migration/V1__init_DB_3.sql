create sequence usr_seq start with 1 increment by 50;

create table user_access
(
    user_id       bigint not null,
    access_levels varchar(255) check (access_levels in ('LEVEL_1', 'LEVEL_2', 'LEVEL_3'))
);

create table user_spec_access
(
    user_id       bigint not null,
    spec_accesses varchar(255) check (spec_accesses in ('SPEC_ACCESS_1', 'SPEC_ACCESS_2', 'SPEC_ACCESS_3'))
);

create table user_role
(
    user_id bigint not null,
    roles   varchar(255) check (roles in ('MAIN_ADMIN', 'ADMIN', 'USER'))
);

create table usr
(
    active           boolean      not null,
    administrator_id bigint,
    changed_user_id  bigint unique,
    created_user_id  bigint unique,
    date_changed     timestamp(6),
    date_created     timestamp(6),
    id               bigint       not null,
    password         varchar(255) not null,
    username         varchar(255) not null,
    log_file         text,
    primary key (id)
);

alter table if exists user_access add constraint user_access__usr foreign key (user_id) references usr;

alter table if exists user_spec_access add constraint user_spec_access__usr foreign key (user_id) references usr;

alter table if exists user_role add constraint user_role__usr foreign key (user_id) references usr;

alter table if exists usr add constraint user_admin__usr foreign key (administrator_id) references usr;

alter table if exists usr add constraint user_changed__usr foreign key (changed_user_id) references usr;

alter table if exists usr add constraint user_created__usr foreign key (created_user_id) references usr