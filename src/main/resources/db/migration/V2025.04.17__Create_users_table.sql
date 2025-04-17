create table users
(
  id         uuid      default gen_random_uuid() not null primary key,
  name       varchar(50)                         not null unique
    check ( name = lower(trim(name)) and name != '' ),
  created_at timestamp default now()             not null,
  updated_at timestamp default now()             not null
);
