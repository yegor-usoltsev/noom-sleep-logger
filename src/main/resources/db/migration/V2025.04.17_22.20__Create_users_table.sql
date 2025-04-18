create table users
(
  id         uuid      default gen_random_uuid() not null,
  name       varchar(50)                         not null,
  created_at timestamp default now()             not null,
  updated_at timestamp default now()             not null,

  primary key (id),
  unique (name),
  check (name = lower(trim(name)) and name != '')
);
