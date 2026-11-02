-- GeTaTaPi initial schema. Apply with: supabase db reset

create extension if not exists pgcrypto;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  avatar_path text,
  created_at timestamptz not null default now()
);

create table if not exists public.topics (
  id uuid primary key default gen_random_uuid(),
  code text not null unique,
  title text not null,
  description text,
  is_active boolean not null default true,
  sort_order integer not null default 0,
  created_at timestamptz not null default now()
);

create table if not exists public.tasks (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  topic_id uuid not null references public.topics(id) on delete restrict,
  difficulty text not null check (difficulty in ('easy', 'medium', 'hard')),
  prompt text not null,
  status text not null default 'active' check (status in ('active', 'completed', 'expired', 'cancelled')),
  created_at timestamptz not null default now(),
  expires_at timestamptz
);

create table if not exists public.submissions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  task_id uuid not null references public.tasks(id) on delete cascade,
  storage_path text not null,
  status text not null default 'queued' check (status in ('queued', 'processing', 'completed', 'failed')),
  passed boolean,
  ai_feedback text,
  confidence real check (confidence is null or (confidence >= 0 and confidence <= 1)),
  provider text,
  created_at timestamptz not null default now(),
  checked_at timestamptz,
  constraint submissions_storage_path_owner check (split_part(storage_path, '/', 1) = user_id::text)
);

create table if not exists public.user_statistics (
  user_id uuid primary key references auth.users(id) on delete cascade,
  completed_count integer not null default 0 check (completed_count >= 0),
  passed_count integer not null default 0 check (passed_count >= 0),
  current_streak integer not null default 0 check (current_streak >= 0),
  best_streak integer not null default 0 check (best_streak >= 0),
  last_activity_date date,
  updated_at timestamptz not null default now()
);

create table if not exists public.submission_idempotency (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  idempotency_key text not null,
  submission_id uuid references public.submissions(id) on delete cascade,
  created_at timestamptz not null default now(),
  unique (user_id, idempotency_key)
);

create index if not exists tasks_user_created_idx on public.tasks(user_id, created_at desc);
create index if not exists submissions_user_created_idx on public.submissions(user_id, created_at desc, id desc);
create index if not exists submissions_status_created_idx on public.submissions(status, created_at);
create index if not exists topics_active_sort_idx on public.topics(is_active, sort_order);

-- Create profile and statistics rows for each newly registered account.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, display_name)
  values (new.id, coalesce(new.raw_user_meta_data ->> 'display_name', new.raw_user_meta_data ->> 'name'));

  insert into public.user_statistics (user_id)
  values (new.id);

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute procedure public.handle_new_user();

-- Keep aggregates in sync when a submission is completed.
create or replace function public.refresh_user_statistics()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_user_id uuid;
begin
  v_user_id := coalesce(new.user_id, old.user_id);

  insert into public.user_statistics (user_id, completed_count, passed_count, last_activity_date, updated_at)
  select v_user_id,
         count(*) filter (where status = 'completed'),
         count(*) filter (where status = 'completed' and passed is true),
         max((created_at at time zone 'UTC')::date) filter (where status = 'completed'),
         now()
  from public.submissions
  where user_id = v_user_id
  on conflict (user_id) do update set
    completed_count = excluded.completed_count,
    passed_count = excluded.passed_count,
    last_activity_date = excluded.last_activity_date,
    updated_at = now();

  return coalesce(new, old);
end;
$$;

drop trigger if exists submissions_refresh_stats on public.submissions;
create trigger submissions_refresh_stats
after insert or update of status, passed or delete on public.submissions
for each row execute procedure public.refresh_user_statistics();

-- RLS
alter table public.profiles enable row level security;
alter table public.topics enable row level security;
alter table public.tasks enable row level security;
alter table public.submissions enable row level security;
alter table public.user_statistics enable row level security;
alter table public.submission_idempotency enable row level security;

create policy "profiles_select_own" on public.profiles
for select to authenticated using (id = (select auth.uid()));
create policy "profiles_insert_own" on public.profiles
for insert to authenticated with check (id = (select auth.uid()));
create policy "profiles_update_own" on public.profiles
for update to authenticated using (id = (select auth.uid())) with check (id = (select auth.uid()));

create policy "topics_read_authenticated" on public.topics
for select to authenticated using (is_active = true);

create policy "tasks_select_own" on public.tasks
for select to authenticated using (user_id = (select auth.uid()));
create policy "tasks_insert_own" on public.tasks
for insert to authenticated with check (user_id = (select auth.uid()));
create policy "tasks_update_own" on public.tasks
for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "tasks_delete_own" on public.tasks
for delete to authenticated using (user_id = (select auth.uid()));

create policy "submissions_select_own" on public.submissions
for select to authenticated using (user_id = (select auth.uid()));
create policy "submissions_insert_own" on public.submissions
for insert to authenticated with check (
  user_id = (select auth.uid())
  and exists (
    select 1 from public.tasks t
    where t.id = task_id and t.user_id = (select auth.uid())
  )
);
-- Client cannot alter AI verdict fields. Updates are reserved for trusted backend/service role.
create policy "submissions_delete_own" on public.submissions
for delete to authenticated using (user_id = (select auth.uid()));

create policy "statistics_select_own" on public.user_statistics
for select to authenticated using (user_id = (select auth.uid()));

create policy "idempotency_select_own" on public.submission_idempotency
for select to authenticated using (user_id = (select auth.uid()));
create policy "idempotency_insert_own" on public.submission_idempotency
for insert to authenticated with check (user_id = (select auth.uid()));

-- Private image bucket. Path format: <auth.uid>/<submission-id>/original.jpg
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('submissions', 'submissions', false, 10485760, array['image/jpeg','image/png','image/webp'])
on conflict (id) do update set
  public = false,
  file_size_limit = 10485760,
  allowed_mime_types = array['image/jpeg','image/png','image/webp'];

create policy "submission_files_read_own" on storage.objects
for select to authenticated
using (bucket_id = 'submissions' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy "submission_files_upload_own" on storage.objects
for insert to authenticated
with check (bucket_id = 'submissions' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy "submission_files_update_own" on storage.objects
for update to authenticated
using (bucket_id = 'submissions' and (storage.foldername(name))[1] = (select auth.uid())::text)
with check (bucket_id = 'submissions' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy "submission_files_delete_own" on storage.objects
for delete to authenticated
using (bucket_id = 'submissions' and (storage.foldername(name))[1] = (select auth.uid())::text);

-- Seed topics for local development.
insert into public.topics (code, title, description, sort_order) values
  ('nature', 'Природа', 'Объекты и явления природы', 1),
  ('architecture', 'Архитектура', 'Здания и архитектурные детали', 2),
  ('everyday', 'Повседневность', 'Предметы вокруг нас', 3)
on conflict (code) do nothing;
