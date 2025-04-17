create table users
(
  id         uuid      default gen_random_uuid() not null primary key,
  name       varchar(50)                         not null unique,
  created_at timestamp default now()             not null,
  updated_at timestamp default now()             not null
);
