-- Persistent per-user API rate limiting (120 requests per 60 seconds).
create table if not exists public.api_rate_limits (
  user_id uuid primary key references auth.users(id) on delete cascade,
  window_started_at timestamptz not null,
  request_count integer not null check (request_count >= 0)
);

alter table public.api_rate_limits enable row level security;
revoke all on public.api_rate_limits from anon, authenticated;

create or replace function public.consume_api_rate_limit(
  p_user_id uuid,
  p_limit integer default 120,
  p_window_seconds integer default 60
)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_started timestamptz;
  v_count integer;
begin
  if auth.uid() is null or auth.uid() <> p_user_id then
    raise exception 'not authorized';
  end if;
  if p_limit < 1 or p_window_seconds < 1 then
    raise exception 'invalid rate limit parameters';
  end if;

  insert into public.api_rate_limits(user_id, window_started_at, request_count)
  values (p_user_id, now(), 1)
  on conflict (user_id) do update
    set window_started_at = case
          when public.api_rate_limits.window_started_at <= now() - (p_window_seconds * interval '1 second')
          then now() else public.api_rate_limits.window_started_at end,
        request_count = case
          when public.api_rate_limits.window_started_at <= now() - (p_window_seconds * interval '1 second')
          then 1 else public.api_rate_limits.request_count + 1 end
  returning window_started_at, request_count into v_started, v_count;

  return v_count <= p_limit;
end;
$$;

revoke all on function public.consume_api_rate_limit(uuid, integer, integer) from public, anon;
grant execute on function public.consume_api_rate_limit(uuid, integer, integer) to authenticated;
