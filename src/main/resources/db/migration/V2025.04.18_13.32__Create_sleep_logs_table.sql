create type mood as enum ('BAD', 'OK', 'GOOD');

create table sleep_logs
(
  id         uuid      default gen_random_uuid()                        not null,
  user_id    uuid                                                       not null,
  bed_time   timestamp                                                  not null,
  wake_time  timestamp                                                  not null,
  mood       mood                                                       not null,
  date       date generated always as (date(wake_time)) stored          not null,
  duration   interval generated always as (wake_time - bed_time) stored not null,
  created_at timestamp default now()                                    not null,
  updated_at timestamp default now()                                    not null,

  primary key (id),
  foreign key (user_id) references users (id),
  unique (user_id, date),
  check (wake_time > bed_time)
);

create index on sleep_logs (user_id);
