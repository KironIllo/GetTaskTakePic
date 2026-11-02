//import { createClient, SupabaseClient, User } from "https://esm.sh/@supabase/supabase-js@2";
import { createClient, SupabaseClient, User } from "npm:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, idempotency-key",
  "Access-Control-Allow-Methods": "GET, POST, PATCH, DELETE, OPTIONS",
};

type ApiError = { code: string; message: string; details?: Array<{field: string; reason: string}> };

function json(data: unknown, status = 200, headers: HeadersInit = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json", ...headers },
  });
}

function fail(status: number, code: string, message: string, details: ApiError["details"] = []) {
  return json({ error: { code, message, details, requestId: crypto.randomUUID() } }, status);
}

function getId(path: string, prefix: string): string | null {
  const match = path.match(new RegExp(`^/${prefix}/([0-9a-fA-F-]{36})$`));
  return match?.[1] ?? null;
}

async function bodyJson(req: Request) {
  try { return await req.json(); } catch { return null; }
}

function validUuid(value: unknown): value is string {
  return typeof value === "string" &&
    /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  if (!supabaseUrl || !anonKey) return fail(503, "SERVICE_UNAVAILABLE", "Supabase environment is not configured.");

  const authHeader = req.headers.get("Authorization") ?? "";
  const token = authHeader.match(/^Bearer\s+(.+)$/i)?.[1];
  if (!token) return fail(401, "UNAUTHORIZED", "Bearer access token is required.");

  // Validate the JWT against Supabase Auth, then pass it through so Postgres RLS sees auth.uid().
  const supabase: SupabaseClient = createClient(supabaseUrl, anonKey, {
    global: { headers: { Authorization: `Bearer ${token}` } },
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const { data: authData, error: authError } = await supabase.auth.getUser(token);
  const user: User | null = authData?.user ?? null;
  if (authError || !user) return fail(401, "UNAUTHORIZED", "Invalid or expired access token.");

  const url = new URL(req.url);
  const path = url.pathname.replace(/\/+$/, "").replace(/^.*\/api-v1/, "") || "/";
  const method = req.method;

  try {
    if (method === "GET" && path === "/topics") {
      const { data, error } = await supabase.from("topics")
        .select("id,code,title,description,is_active,sort_order")
        .eq("is_active", true).order("sort_order", { ascending: true });
      if (error) throw error;
      return json({ data: (data ?? []).map((t) => ({
        id: t.id, code: t.code, title: t.title, description: t.description,
        isActive: t.is_active, sortOrder: t.sort_order,
      })) });
    }

    if (method === "POST" && path === "/tasks") {
      const body = await bodyJson(req);
      if (!body || !validUuid(body.topicId) || !["easy", "medium", "hard"].includes(body.difficulty)) {
        return fail(422, "VALIDATION_ERROR", "topicId and valid difficulty are required.", [
          { field: "topicId", reason: "Expected UUID" },
          { field: "difficulty", reason: "Expected easy, medium or hard" },
        ]);
      }
      const { data: topic, error: topicError } = await supabase.from("topics")
        .select("id,title").eq("id", body.topicId).eq("is_active", true).maybeSingle();
      if (topicError) throw topicError;
      if (!topic) return fail(404, "NOT_FOUND", "Active topic not found.");

      const prompts: Record<string, string> = {
        easy: `Сфотографируй любой интересный объект по теме «${topic.title}».`,
        medium: `Найди необычный объект по теме «${topic.title}» и сделай выразительную фотографию.`,
        hard: `Сделай оригинальную фотографию по теме «${topic.title}», уделив внимание композиции и деталям.`,
      };
      const { data, error } = await supabase.from("tasks").insert({
        user_id: user.id, topic_id: topic.id, difficulty: body.difficulty,
        prompt: prompts[body.difficulty], status: "active",
        expires_at: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      }).select("id,topic_id,difficulty,prompt,status,created_at,expires_at").single();
      if (error) throw error;
      return json({
        id: data.id, topicId: data.topic_id, difficulty: data.difficulty,
        prompt: data.prompt, status: data.status, createdAt: data.created_at, expiresAt: data.expires_at,
      }, 201);
    }

    const taskId = getId(path, "tasks");
    if (method === "GET" && taskId) {
      const { data, error } = await supabase.from("tasks")
        .select("id,topic_id,difficulty,prompt,status,created_at,expires_at")
        .eq("id", taskId).maybeSingle();
      if (error) throw error;
      if (!data) return fail(404, "NOT_FOUND", "Task not found.");
      return json({
        id: data.id, topicId: data.topic_id, difficulty: data.difficulty,
        prompt: data.prompt, status: data.status, createdAt: data.created_at, expiresAt: data.expires_at,
      });
    }

    if (method === "POST" && path === "/submissions") {
      const key = req.headers.get("Idempotency-Key");
      if (!key || key.length > 128) return fail(400, "BAD_REQUEST", "Idempotency-Key header is required (max 128 chars).");
      const body = await bodyJson(req);
      if (!body || !validUuid(body.taskId) || typeof body.storagePath !== "string") {
        return fail(422, "VALIDATION_ERROR", "taskId and storagePath are required.");
      }
      if (!body.storagePath.startsWith(`${user.id}/`)) return fail(403, "FORBIDDEN", "Storage path must belong to the authenticated user.");

      const { data: existingKey, error: keyError } = await supabase.from("submission_idempotency")
        .select("submission_id").eq("user_id", user.id).eq("idempotency_key", key).maybeSingle();
      if (keyError) throw keyError;
      if (existingKey?.submission_id) {
        const { data: existing } = await supabase.from("submissions")
          .select("id,task_id,status,created_at").eq("id", existingKey.submission_id).maybeSingle();
        if (existing) return json({
          id: existing.id, taskId: existing.task_id, status: existing.status,
          createdAt: existing.created_at, statusUrl: `/submissions/${existing.id}`,
        }, 202);
      }

      const { data: task, error: taskError } = await supabase.from("tasks")
        .select("id").eq("id", body.taskId).maybeSingle();
      if (taskError) throw taskError;
      if (!task) return fail(404, "NOT_FOUND", "Task not found.");

      const { data: submission, error: insertError } = await supabase.from("submissions").insert({
        user_id: user.id, task_id: body.taskId, storage_path: body.storagePath, status: "queued",
      }).select("id,task_id,status,created_at").single();
      if (insertError) throw insertError;

      const { error: idemError } = await supabase.from("submission_idempotency").insert({
        user_id: user.id, idempotency_key: key, submission_id: submission.id,
      });
      if (idemError) {
        // If a concurrent request won the unique-key race, return the winner's record.
        const { data: winner } = await supabase.from("submission_idempotency")
          .select("submission_id").eq("user_id", user.id).eq("idempotency_key", key).maybeSingle();
        if (winner?.submission_id) {
          const { data: existing } = await supabase.from("submissions")
            .select("id,task_id,status,created_at").eq("id", winner.submission_id).maybeSingle();
          if (existing) return json({ id: existing.id, taskId: existing.task_id, status: existing.status,
            createdAt: existing.created_at, statusUrl: `/submissions/${existing.id}` }, 202);
        }
        throw idemError;
      }
      return json({
        id: submission.id, taskId: submission.task_id, status: submission.status,
        createdAt: submission.created_at, statusUrl: `/submissions/${submission.id}`,
      }, 202);
    }

    if (method === "GET" && path === "/submissions") {
      const limitRaw = Number(url.searchParams.get("limit") ?? "20");
      const limit = Number.isInteger(limitRaw) && limitRaw >= 1 && limitRaw <= 100 ? limitRaw : 20;
      const cursor = url.searchParams.get("cursor");
      let query = supabase.from("submissions")
        .select("id,task_id,status,passed,ai_feedback,confidence,created_at,checked_at")
        .order("created_at", { ascending: false }).order("id", { ascending: false }).limit(limit + 1);
      if (cursor) {
        const decoded = atob(cursor);
        const [createdAt, id] = decoded.split("|");
        if (!createdAt || !validUuid(id)) return fail(422, "VALIDATION_ERROR", "Invalid cursor.");
        query = query.or(`created_at.lt.${createdAt},and(created_at.eq.${createdAt},id.lt.${id})`);
      }
      const { data, error } = await query;
      if (error) throw error;
      const rows = data ?? [];
      const hasMore = rows.length > limit;
      const pageRows = hasMore ? rows.slice(0, limit) : rows;
      const last = pageRows[pageRows.length - 1];
      const nextCursor = hasMore && last ? btoa(`${last.created_at}|${last.id}`) : null;
      return json({
        data: pageRows.map((s) => ({
          id: s.id, taskId: s.task_id, status: s.status, passed: s.passed,
          feedback: s.ai_feedback, confidence: s.confidence, createdAt: s.created_at, checkedAt: s.checked_at,
        })),
        page: { limit, nextCursor },
      });
    }

    const submissionId = getId(path, "submissions");
    if (method === "GET" && submissionId) {
      const { data, error } = await supabase.from("submissions")
        .select("id,task_id,status,passed,ai_feedback,confidence,created_at,checked_at")
        .eq("id", submissionId).maybeSingle();
      if (error) throw error;
      if (!data) return fail(404, "NOT_FOUND", "Submission not found.");
      return json({
        id: data.id, taskId: data.task_id, status: data.status, passed: data.passed,
        feedback: data.ai_feedback, confidence: data.confidence,
        createdAt: data.created_at, checkedAt: data.checked_at,
      });
    }

    if (method === "GET" && path === "/statistics") {
      const { data: stats, error: statsError } = await supabase.from("user_statistics")
        .select("completed_count,passed_count,current_streak,best_streak,last_activity_date")
        .eq("user_id", user.id).maybeSingle();
      if (statsError) throw statsError;
      const { data: tasks, error: tasksError } = await supabase.from("tasks")
        .select("difficulty,topic_id,topics(title),submissions(status)")
        .eq("user_id", user.id);
      if (tasksError) throw tasksError;
      const byDifficulty: Record<string, number> = { easy: 0, medium: 0, hard: 0 };
      const topicMap = new Map<string, { topicId: string; title: string; completedCount: number }>();
      for (const task of tasks ?? []) {
        const completed = (task.submissions ?? []).some((s: {status: string}) => s.status === "completed");
        if (completed) {
          byDifficulty[task.difficulty] = (byDifficulty[task.difficulty] ?? 0) + 1;
          const topic = Array.isArray(task.topics) ? task.topics[0] : task.topics;
          if (topic) {
            const prev = topicMap.get(task.topic_id) ?? { topicId: task.topic_id, title: topic.title, completedCount: 0 };
            prev.completedCount += 1;
            topicMap.set(task.topic_id, prev);
          }
        }
      }
      return json({
        completedCount: stats?.completed_count ?? 0,
        passedCount: stats?.passed_count ?? 0,
        currentStreak: stats?.current_streak ?? 0,
        bestStreak: stats?.best_streak ?? 0,
        lastActivityDate: stats?.last_activity_date ?? null,
        byDifficulty,
        byTopic: [...topicMap.values()],
      });
    }

    return fail(404, "NOT_FOUND", "Endpoint not found.");
  } catch (error) {
    console.error("api-v1 error", error);
    return fail(500, "INTERNAL_ERROR", "An unexpected server error occurred.");
  }
});
