export type ApiError = {
  code: string;
  message: string;
  details?: Array<{ field: string; reason: string }>;
};

export function json(data: unknown, status = 200, headers: HeadersInit = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Headers": "authorization, apikey, content-type, idempotency-key",
      "Access-Control-Allow-Methods": "GET, POST, PATCH, DELETE, OPTIONS",
      "Content-Type": "application/json",
      ...headers,
    },
  });
}

export function fail(
  status: number,
  code: string,
  message: string,
  details: ApiError["details"] = [],
) {
  return json(
    { error: { code, message, details, requestId: crypto.randomUUID() } },
    status,
    status === 429 ? { "Retry-After": "60" } : {},
  );
}

export function getId(path: string, prefix: string): string | null {
  const match = path.match(new RegExp(`^/${prefix}/([0-9a-fA-F-]{36})$`));
  return match?.[1] ?? null;
}

export async function bodyJson(req: Request) {
  const contentType = req.headers.get("Content-Type") ?? "";
  if (!contentType.toLowerCase().includes("application/json")) {
    return { __invalidContentType: true };
  }

  const raw = await req.text();
  if (raw.length > 16_384) return { __tooLarge: true };

  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function validStoragePath(value: unknown, userId: string): value is string {
  return typeof value === "string" &&
    new RegExp(
      `^${userId}/[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}/original\\.(jpg|jpeg|png|webp)$`,
      "i",
    ).test(value);
}

export function validUuid(value: unknown): value is string {
  return typeof value === "string" &&
    /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);
}
