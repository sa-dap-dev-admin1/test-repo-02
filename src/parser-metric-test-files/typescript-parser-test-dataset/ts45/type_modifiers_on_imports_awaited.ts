/*
 * Feature : `type` modifiers on import/export names, `Awaited<T>`,
 *           private-name brand checks (`#x in obj`), template string types
 *           as discriminants, tail-recursion elimination on conditional
 *           types, import assertions (later renamed attributes)
 * Version : TypeScript 4.5 (November 2021)
 * Spec    : TS 4.5 release notes "type Modifiers on Import Names", "The
 *           Awaited Type", "Private Field Presence Checks", "Template
 *           String Types as Discriminants", "Tail-Recursion Elimination"
 *
 * Parser edge cases:
 *  - `import { type A, B } from "..."` - `type` on an INDIVIDUAL specifier
 *    (vs 3.8's whole-statement `import type`).
 *  - `export { type A, b }` likewise.
 *  - Ambiguity: `import { type } from "x"` imports a binding NAMED `type`;
 *    `import { type as } from "x"` imports `as` as a type; `import { type
 *    as as as }` is legal and imports type `as` renamed to `as`.
 *  - `#brand in obj` - a private name on the LEFT of `in`.
 */
import { type Shape as SelfShape, describe as selfDescribe } from "./type_modifiers_on_imports_awaited";

export interface Shape { kind: string }
export function describe(s: Shape): string { return `shape:${s.kind}`; }
export { type Shape as ShapeType };

class Token {
  #brand = true;
  static is(obj: unknown): obj is Token {
    return typeof obj === "object" && obj !== null && #brand in obj;   // brand check
  }
}

type A1 = Awaited<Promise<string>>;                       // string
type A2 = Awaited<Promise<Promise<number>>>;              // number
type A3 = Awaited<boolean | Promise<bigint>>;             // boolean | bigint

// Template string types as discriminants
type Result =
  | { status: `ok-${number}`; data: string }
  | { status: `err-${string}`; error: string };

function handle(r: Result): string {
  if (r.status.startsWith("ok-")) return `success ${(r as { data: string }).data}`;
  return `failure ${(r as { error: string }).error}`;
}

// Tail-recursive conditional type (deep recursion allowed since 4.5)
type TrimAll<S extends string> = S extends ` ${infer R}` ? TrimAll<R> : S extends `${infer R} ` ? TrimAll<R> : S;
type BuildTuple<N extends number, Acc extends unknown[] = []> = Acc["length"] extends N ? Acc : BuildTuple<N, [...Acc, unknown]>;

async function main() {
  const s: SelfShape = { kind: "circle" };
  console.log(selfDescribe(s));
  console.log(`Token.is(new Token()) = ${Token.is(new Token())}, Token.is({}) = ${Token.is({})}`);

  const v1: A1 = await Promise.resolve("awaited string");
  const v2: A2 = await Promise.resolve(Promise.resolve(7));
  const v3: A3 = await Promise.resolve(10n);
  console.log(`Awaited: ${v1}, ${v2}, ${v3}`);

  const all = await Promise.all([Promise.resolve(1), "two", Promise.resolve(true)] as const);
  console.log(`Promise.all typed via Awaited: ${all.join(", ")}`);

  console.log(handle({ status: "ok-200", data: "payload" }));
  console.log(handle({ status: "err-timeout", error: "too slow" }));

  const trimmed: TrimAll<"   lots of space   "> = "lots of space";
  const tuple: BuildTuple<50>["length"] = 50;
  console.log(`tail-recursive types: [${trimmed}] ${tuple}`);
}
main();
