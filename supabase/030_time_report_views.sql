-- Time aggregation views for weekly and monthly hours per user and project
create or replace view vw_time_hours_week as
select date_trunc('week', te.started_at) as bucket,
       te.user_id,
       t.project_id,
       sum(extract(epoch from (te.ended_at - te.started_at)) / 3600.0) as hours
from time_entries te
join tasks t on t.id = te.task_id
where te.deleted_at is null and t.deleted_at is null
group by bucket, te.user_id, t.project_id;

create or replace view vw_time_hours_month as
select date_trunc('month', te.started_at) as bucket,
       te.user_id,
       t.project_id,
       sum(extract(epoch from (te.ended_at - te.started_at)) / 3600.0) as hours
from time_entries te
join tasks t on t.id = te.task_id
where te.deleted_at is null and t.deleted_at is null
group by bucket, te.user_id, t.project_id;

-- indexes to support filtering by user/project and time range
create index if not exists idx_time_entries_started_at on time_entries(started_at);
create index if not exists idx_time_entries_user on time_entries(user_id);
create index if not exists idx_tasks_project_id on tasks(project_id);

