declare const EdgeRuntime: {
  waitUntil(promise: Promise<unknown>): void;
};
import { createClient, SupabaseClient, User } from "npm:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, idempotency-key",
  "Access-Control-Allow-Methods": "GET, POST, PATCH, DELETE, OPTIONS",
};

import { bodyJson, fail, getId, json, validStoragePath, validUuid } from "./utils.ts";

// Convert the private Storage image into a data URL for the OpenRouter vision request.
function bytesToBase64(bytes: Uint8Array): string {
  let binary = "";
  const chunkSize = 0x8000;
  for (let i = 0; i < bytes.length; i += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunkSize));
  }
  return btoa(binary);
}

function imageMimeType(blob: Blob, storagePath: string): string {
  const fromBlob = blob.type.toLowerCase();
  if (["image/jpeg", "image/png", "image/webp"].includes(fromBlob)) return fromBlob;
  const ext = storagePath.split(".").pop()?.toLowerCase();
  if (ext === "png") return "image/png";
  if (ext === "webp") return "image/webp";
  return "image/jpeg";
}

function parseModelJson(content: unknown): { passed: boolean; feedback: string; confidence: number } {
  if (typeof content !== "string") throw new Error("OpenRouter returned an empty or non-text answer.");
  const cleaned = content.replace(/```(?:json)?/gi, "").trim();
  const start = cleaned.indexOf("{");
  const end = cleaned.lastIndexOf("}");
  if (start < 0 || end <= start) throw new Error("Model response did not contain JSON.");
  const parsed = JSON.parse(cleaned.slice(start, end + 1));
  if (typeof parsed.passed !== "boolean") throw new Error("Model response is missing boolean 'passed'.");
  if (typeof parsed.feedback !== "string" || !parsed.feedback.trim()) {
    throw new Error("Model response is missing 'feedback'.");
  }
  const confidence = Number(parsed.confidence);
  if (!Number.isFinite(confidence)) throw new Error("Model response is missing numeric 'confidence'.");
  return {
    passed: parsed.passed,
    feedback: parsed.feedback.trim().slice(0, 2000),
    confidence: Math.max(0, Math.min(1, confidence)),
  };
}

// Runs outside the HTTP request. Service-role access is restricted to this server-side worker;
// it is never returned to the client.
async function processSubmission(submissionId: string) {
  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  const openRouterKey = Deno.env.get("OPENROUTER_API_KEY");
  const model = "nvidia/nemotron-3.5-super-vl:free";

  if (!supabaseUrl || !serviceRoleKey || !openRouterKey) {
    console.error(JSON.stringify({
      event: "ai_worker_config_error",
      submissionId,
      hasSupabaseUrl: Boolean(supabaseUrl),
      hasServiceRoleKey: Boolean(serviceRoleKey),
      hasOpenRouterKey: Boolean(openRouterKey),
    }));
    return;
  }

  const admin = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  try {
    const { data: submission, error: submissionError } = await admin
      .from("submissions")
      .select("id, storage_path, task_id, status")
      .eq("id", submissionId)
      .maybeSingle();

    if (submissionError) throw submissionError;
    if (!submission || submission.status !== "queued") return;

    const { error: processingError } = await admin
      .from("submissions")
      .update({ status: "processing", provider: model })
      .eq("id", submissionId);
    if (processingError) throw processingError;

    const { data: task, error: taskError } = await admin
      .from("tasks")
      .select("prompt")
      .eq("id", submission.task_id)
      .maybeSingle();
    if (taskError) throw taskError;
    if (!task?.prompt) throw new Error("Task prompt was not found.");

    const { data: imageBlob, error: downloadError } = await admin.storage
      .from("submissions")
      .download(submission.storage_path);
    if (downloadError) throw downloadError;
    if (!imageBlob) throw new Error("Image could not be downloaded from private Storage.");
    if (imageBlob.size > 10 * 1024 * 1024) throw new Error("Image exceeds the 10 MB processing limit.");

    const mimeType = imageMimeType(imageBlob, submission.storage_path);
    const base64 = bytesToBase64(new Uint8Array(await imageBlob.arrayBuffer()));
    const dataUrl = `data:${mimeType};base64,${base64}`;

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 55_000);

    let aiResponse: Response;
    try {
      aiResponse = await fetch("https://openrouter.ai/api/v1/chat/completions", {
        method: "POST",
        signal: controller.signal,
        headers: {
          "Authorization": `Bearer ${openRouterKey}`,
          "Content-Type": "application/json",
          "X-Title": "GeTaTaPi",
        },
        body: JSON.stringify({
          model,
          temperature: 0.1,
          max_tokens: 500,
          messages: [{
            role: "user",
            content: [
              {
                type: "text",
                text: [
                  "Ты проверяешь фотографию для учебного фотозадания.",
                  `Задание: ${task.prompt}`,
                  "Оцени только то, что действительно видно на фотографии. Не выдумывай объекты и детали.",
                  "passed=true, если изображение явно соответствует заданию; иначе false.",
                  "confidence — число от 0 до 1, отражающее уверенность в оценке.",
                  "feedback — короткое понятное объяснение на русском языке, 1–3 предложения.",
                  'Верни только JSON без Markdown в формате: {"passed":true,"feedback":"...","confidence":0.9}',
                ].join("\n"),
              },
              { type: "image_url", image_url: { url: dataUrl } },
            ],
          }],
        }),
      });
    } finally {
      clearTimeout(timeout);
    }

    if (!aiResponse.ok) {
      const providerBody = (await aiResponse.text()).slice(0, 1500);
      throw new Error(`OpenRouter HTTP ${aiResponse.status}: ${providerBody}`);
    }

    const aiJson = await aiResponse.json();
    const content = aiJson?.choices?.[0]?.message?.content;
    const result = parseModelJson(content);

    const { error: updateError } = await admin
      .from("submissions")
      .update({
        status: "completed",
        passed: result.passed,
        ai_feedback: result.feedback,
        confidence: result.confidence,
        provider: model,
        checked_at: new Date().toISOString(),
      })
      .eq("id", submissionId);
    if (updateError) throw updateError;

    console.log(JSON.stringify({
      event: "ai_submission_completed",
      submissionId,
      provider: model,
      passed: result.passed,
      confidence: result.confidence,
    }));
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown AI processing error";
    console.error(JSON.stringify({ event: "ai_submission_failed", submissionId, message }));

    const { error: updateError } = await admin
      .from("submissions")
      .update({
        status: "failed",
        ai_feedback: "Не удалось проверить фотографию. Попробуй отправить её ещё раз позже.",
        confidence: null,
        checked_at: new Date().toISOString(),
      })
      .eq("id", submissionId);

    if (updateError) {
      console.error(JSON.stringify({
        event: "ai_submission_status_update_failed",
        submissionId,
        message: updateError.message,
      }));
    }
  }
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

  const { data: withinLimit, error: rateLimitError } = await supabase.rpc("consume_api_rate_limit", {
    p_user_id: user.id, p_limit: 120, p_window_seconds: 60,
  });
  if (rateLimitError) {
    console.error(JSON.stringify({ event: "rate_limit_check_failed", userId: user.id, message: rateLimitError.message }));
    return fail(503, "SERVICE_UNAVAILABLE", "Request protection is temporarily unavailable.");
  }
  if (!withinLimit) return fail(429, "RATE_LIMITED", "Too many requests. Try again in one minute.");

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
      if (body?.__tooLarge) return fail(413, "PAYLOAD_TOO_LARGE", "JSON request body exceeds 16 KB.");
      if (body?.__invalidContentType) return fail(415, "UNSUPPORTED_MEDIA_TYPE", "Content-Type must be application/json.");
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
      if (!key || key.trim().length === 0 || key.length > 128) return fail(400, "BAD_REQUEST", "Idempotency-Key header is required (1–128 characters).");


      const body = await bodyJson(req);
      if (body?.__tooLarge) return fail(413, "PAYLOAD_TOO_LARGE", "JSON request body exceeds 16 KB.");
      if (body?.__invalidContentType) return fail(415, "UNSUPPORTED_MEDIA_TYPE", "Content-Type must be application/json.");
      if (!body || !validUuid(body.taskId) || !validStoragePath(body.storagePath, user.id)) {
        return fail(422, "VALIDATION_ERROR", "taskId or storagePath has an invalid format.", [
          { field: "taskId", reason: "Expected UUID" },
          { field: "storagePath", reason: "Expected <userId>/<taskUuid>/original.jpg, .jpeg, .png or .webp" },
        ]);
      }

      const pathParts = body.storagePath.split("/");
      const fileName = pathParts.pop()!;
      const folderPath = pathParts.join("/");
      const { data: storedFiles, error: storageError } = await supabase.storage
        .from("submissions").list(folderPath, { search: fileName, limit: 10 });
      if (storageError) throw storageError;
      if (!(storedFiles ?? []).some((file) => file.name === fileName)) {
        return fail(404, "NOT_FOUND", "Uploaded image was not found in private storage.");
      }

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
          if (existing) return json({
            id: existing.id, taskId: existing.task_id, status: existing.status,
            createdAt: existing.created_at, statusUrl: `/submissions/${existing.id}`,
          }, 202);
        }
        throw idemError;
      }

      // Keep the HTTP response fast; Supabase keeps this promise alive in the background.
      EdgeRuntime.waitUntil(processSubmission(submission.id));

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
        try {
          const decoded = atob(cursor);
          const [createdAt, id, ...rest] = decoded.split("|");
          if (rest.length || !createdAt || Number.isNaN(Date.parse(createdAt)) || !validUuid(id)) {
            return fail(422, "VALIDATION_ERROR", "Invalid cursor.");
          }
          query = query.or(`created_at.lt.${createdAt},and(created_at.eq.${createdAt},id.lt.${id})`);
        } catch {
          return fail(422, "VALIDATION_ERROR", "Invalid cursor.");
        }
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
    console.error(JSON.stringify({
      event: "api_error",
      path: new URL(req.url).pathname,
      method: req.method,
      message: error instanceof Error ? error.message : "Unknown error",
    }));
    return fail(500, "INTERNAL_ERROR", "An unexpected server error occurred.");
  }
});
