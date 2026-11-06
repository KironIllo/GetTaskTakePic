import {
  bodyJson,
  fail,
  getId,
  json,
  validStoragePath,
  validUuid,
} from "../supabase/functions/api-v1/utils.ts";

const USER_ID = "0cb9ced3-6b88-4f89-ba43-5bf76f4115c5";
const TASK_ID = "507b5545-b092-43b1-a16c-9933ced667fd";

Deno.test("1. validUuid accepts a valid UUID", () => {
  if (!validUuid(TASK_ID)) throw new Error("Valid UUID rejected");
});

Deno.test("2. validUuid rejects an invalid UUID", () => {
  if (validUuid("not-a-uuid")) throw new Error("Invalid UUID accepted");
});

Deno.test("3. validUuid rejects non-string values", () => {
  if (validUuid(123)) throw new Error("Number accepted as UUID");
});

Deno.test("4. getId extracts a task ID", () => {
  const result = getId(`/tasks/${TASK_ID}`, "tasks");
  if (result !== TASK_ID) throw new Error(`Unexpected result: ${result}`);
});

Deno.test("5. getId rejects a wrong route prefix", () => {
  if (getId(`/submissions/${TASK_ID}`, "tasks") !== null) {
    throw new Error("Wrong route prefix accepted");
  }
});

Deno.test("6. getId rejects an incomplete ID", () => {
  if (getId("/tasks/123", "tasks") !== null) {
    throw new Error("Incomplete ID accepted");
  }
});

Deno.test("7. validStoragePath accepts a user's JPG path", () => {
  const path = `${USER_ID}/${TASK_ID}/original.jpg`;
  if (!validStoragePath(path, USER_ID)) throw new Error("Valid path rejected");
});

Deno.test("8. validStoragePath accepts PNG and WebP", () => {
  for (const extension of ["png", "webp"]) {
    const path = `${USER_ID}/${TASK_ID}/original.${extension}`;
    if (!validStoragePath(path, USER_ID)) {
      throw new Error(`Valid ${extension} path rejected`);
    }
  }
});

Deno.test("9. validStoragePath rejects another user's path", () => {
  const path = `${TASK_ID}/${TASK_ID}/original.jpg`;
  if (validStoragePath(path, USER_ID)) throw new Error("Foreign path accepted");
});

Deno.test("10. validStoragePath rejects unsupported extensions", () => {
  const path = `${USER_ID}/${TASK_ID}/original.exe`;
  if (validStoragePath(path, USER_ID)) throw new Error("Unsupported extension accepted");
});

Deno.test("11. json returns the requested HTTP status and JSON body", async () => {
  const response = json({ ok: true }, 201);
  const data = await response.json();
  if (response.status !== 201 || data.ok !== true) {
    throw new Error("Incorrect JSON response");
  }
});

Deno.test("12. fail returns a structured error", async () => {
  const response = fail(422, "VALIDATION_ERROR", "Invalid input");
  const data = await response.json();
  if (
    response.status !== 422 ||
    data.error.code !== "VALIDATION_ERROR" ||
    !data.error.requestId
  ) {
    throw new Error("Incorrect error response");
  }
});

Deno.test("13. fail adds Retry-After for rate limiting", () => {
  const response = fail(429, "RATE_LIMITED", "Too many requests");
  if (response.headers.get("Retry-After") !== "60") {
    throw new Error("Retry-After header missing");
  }
});

Deno.test("14. bodyJson parses valid JSON", async () => {
  const request = new Request("http://localhost", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ difficulty: "easy" }),
  });
  const data = await bodyJson(request);
  if (data.difficulty !== "easy") throw new Error("JSON was not parsed");
});

Deno.test("15. bodyJson rejects an incorrect content type", async () => {
  const request = new Request("http://localhost", {
    method: "POST",
    headers: { "Content-Type": "text/plain" },
    body: "{}",
  });
  const data = await bodyJson(request);
  if (!data.__invalidContentType) throw new Error("Incorrect content type accepted");
});

Deno.test("16. bodyJson rejects malformed JSON", async () => {
  const request = new Request("http://localhost", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: "{invalid",
  });
  const data = await bodyJson(request);
  if (data !== null) throw new Error("Malformed JSON accepted");
});

Deno.test("17. bodyJson rejects a body larger than 16 KB", async () => {
  const request = new Request("http://localhost", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ value: "x".repeat(17000) }),
  });
  const data = await bodyJson(request);
  if (!data.__tooLarge) throw new Error("Oversized body accepted");
});
