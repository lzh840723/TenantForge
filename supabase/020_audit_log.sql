-- Change log table to capture DML across multi-tenant tables
create table if not exists change_log (
    id bigserial primary key,
    changed_at timestamptz not null default now(),
    table_name text not null,
    op text not null,
    tenant_id uuid,
    row_id uuid,
    changed_by uuid,
    diff jsonb
);

create or replace function log_change() returns trigger as $$
declare
  t_id uuid := null;
  r_id uuid := null;
  payload jsonb := '{}'::jsonb;
begin
  begin t_id := current_setting('app.tenant_id', true)::uuid; exception when others then t_id := null; end;
  if (TG_OP = 'INSERT') then
    r_id := NEW.id;
    payload := to_jsonb(NEW);
  elsif (TG_OP = 'UPDATE') then
    r_id := NEW.id;
    payload := jsonb_build_object('old', to_jsonb(OLD), 'new', to_jsonb(NEW));
  elsif (TG_OP = 'DELETE') then
    r_id := OLD.id;
    payload := to_jsonb(OLD);
  end if;
  insert into change_log(table_name, op, tenant_id, row_id, changed_by, diff)
  values (TG_TABLE_NAME, TG_OP, coalesce(t_id, null), r_id, null, payload);
  if (TG_OP = 'DELETE') then return OLD; else return NEW; end if;
end; $$ language plpgsql;

do $$ begin
  if not exists (select 1 from pg_trigger where tgname = 'trg_log_tenants') then
    create trigger trg_log_tenants after insert or update or delete on tenants
      for each row execute function log_change();
  end if;
  if not exists (select 1 from pg_trigger where tgname = 'trg_log_users') then
    create trigger trg_log_users after insert or update or delete on users
      for each row execute function log_change();
  end if;
  if not exists (select 1 from pg_trigger where tgname = 'trg_log_projects') then
    create trigger trg_log_projects after insert or update or delete on projects
      for each row execute function log_change();
  end if;
  if not exists (select 1 from pg_trigger where tgname = 'trg_log_tasks') then
    create trigger trg_log_tasks after insert or update or delete on tasks
      for each row execute function log_change();
  end if;
  if not exists (select 1 from pg_trigger where tgname = 'trg_log_time_entries') then
    create trigger trg_log_time_entries after insert or update or delete on time_entries
      for each row execute function log_change();
  end if;
end $$;

