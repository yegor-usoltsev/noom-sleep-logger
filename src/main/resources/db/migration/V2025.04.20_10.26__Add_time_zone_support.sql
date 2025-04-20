create function is_timezone(tz text) returns boolean as
$$
begin
  perform now() at time zone tz; return true;
exception
  when invalid_parameter_value then return false;
end;
$$ language plpgsql;

alter table users
  add column time_zone text default 'UTC' not null check (is_timezone(time_zone));

alter table sleep_logs
  drop constraint sleep_logs_user_id_date_key,
  drop column date;

create unique index on sleep_logs (user_id, date(wake_time));

create view sleep_logs_view as
select sleep_logs.*,
       users.time_zone                              as time_zone,
       date(wake_time at time zone users.time_zone) as date
from sleep_logs
       join users on sleep_logs.user_id = users.id;
