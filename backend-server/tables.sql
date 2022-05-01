create table computed_distances
(
    row_id                      int auto_increment
        primary key,
    possible_contact_user_id    varchar(50)          not null,
    distance_ciphertext1        longblob             not null,
    location_id                 int                  not null,
    timestamp                   int                  not null,
    infected_location_id        int                  not null,
    infected_user_id            varchar(50)          not null,
    timestamp_end               int                  not null,
    altitude_difference1        longblob             not null,
    partial_altitude_difference longblob             null,
    partial_distance            longblob             null,
    distance_ciphertext2        longblob             null,
    altitude_difference2        longblob             null,
    downloaded                  tinyint(1) default 0 null
);

create table contacts
(
    row_id           int auto_increment,
    user_id          varchar(50) not null,
    infected_user_id varchar(50) not null,
    timestamp        int         null,
    timestamp_end    int         not null,
    constraint contacts_row_id_uindex
        unique (row_id)
);

alter table contacts
    add primary key (row_id);

create table fcm_tokens
(
    user_id varchar(50)  not null,
    token   varchar(256) not null,
    constraint fcm_tokens_user_id_uindex
        unique (user_id)
);

alter table fcm_tokens
    add primary key (user_id);

create table `keys`
(
    row_id    int auto_increment
        primary key,
    user_id   varchar(50) not null,
    pub_key   longblob    not null,
    relin_key longblob    not null
);

create index user_id_index
    on `keys` (user_id);

create table location_history
(
    id        int auto_increment,
    location  point not null,
    timestamp int   not null,
    constraint location_history_id_uindex
        unique (id)
);

create index location_history_location_index
    on location_history (location(25));

alter table location_history
    add primary key (id);

create table locations
(
    id            int(11) unsigned auto_increment
        primary key,
    latitude_cos  longblob                             not null,
    latitude_sin  longblob                             not null,
    longitude_cos longblob                             not null,
    longitude_sin longblob                             not null,
    user_id       varchar(50) default ''               not null,
    timestamp     int         default unix_timestamp() not null,
    timestamp_end int                                  not null,
    altitude      longblob                             not null
);

create index locations_user_id_timestamp_timestamp_end_index
    on locations (user_id, timestamp, timestamp_end);

create table processed_distances
(
    row_id          int auto_increment,
    infected_loc_id int not null,
    contact_loc_id  int null,
    constraint processed_distances__pair
        unique (infected_loc_id, contact_loc_id),
    constraint processed_distances_row_id_uindex
        unique (row_id)
);

alter table processed_distances
    add primary key (row_id);

create table quarantined_users
(
    row_id  int(11) unsigned auto_increment
        primary key,
    user_id varchar(50) default ''               not null,
    end     int         default unix_timestamp() null,
    constraint quarantined_users_user_id_uindex
        unique (user_id)
);
