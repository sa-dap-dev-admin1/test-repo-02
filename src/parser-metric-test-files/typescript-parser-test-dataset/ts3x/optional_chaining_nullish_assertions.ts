/*
 * Feature : Optional chaining (?.), nullish coalescing (??), assertion
 *           functions (asserts x / asserts x is T), recursive type aliases,
 *           `useDefineForClassFields` semantics, `declare` class fields
 * Version : TypeScript 3.7 (November 2019)
 * Spec    : TS 3.7 release notes; ES2020 optional chaining / nullish
 *           coalescing (TC39 stage 4)
 *
 * Parser edge cases:
 *  - `a?.b`, `a?.[expr]`, `fn?.(args)` - three forms. `a?.b` vs `a ? .5 : 1`
 *    (`?.` followed by a digit is the conditional operator!).
 *  - `a ?? b || c` without parentheses is a SyntaxError (mixing ?? with
 *    || or && requires parens).
 *  - `function f(x: unknown): asserts x is string` - `asserts` in a return
 *    type position; `asserts this` / `asserts condition`.
 *  - Recursive type alias referring to itself inside an array/object:
 *    `type Json = string | number | Json[] | { [k: string]: Json }`.
 *  - `declare name: string;` - class field declaration with `declare`.
 *  - Non-null assertion after optional chain: `a?.b!.c`.
 */

type Json = string | number | boolean | null | Json[] | { [key: string]: Json };

interface User {
  name: string;
  address?: { city?: string; geo?: { lat: number; lng: number } };
  tags?: string[];
  greet?: (greeting: string) => string;
}

function assert(condition: unknown, message: string): asserts condition {
  if (!condition) throw new Error(message);
}

function assertIsString(value: unknown): asserts value is string {
  if (typeof value !== "string") throw new TypeError(`expected string, got ${typeof value}`);
}

class Base { name = "base"; }
class Derived extends Base {
  declare name: "derived" | "base";                  // redeclare type only, no emit
}

const users: User[] = [
  { name: "ada", address: { city: "London", geo: { lat: 51.5, lng: -0.12 } }, tags: ["math"], greet: (g) => `${g}, I'm Ada` },
  { name: "bob" },
  { name: "cy", address: {} },
];

for (const u of users) {
  const city = u.address?.city ?? "(unknown city)";
  const lat = u.address?.geo?.lat ?? NaN;
  const firstTag = u.tags?.[0] ?? "-";
  const hello = u.greet?.("Hello") ?? `${u.name} says nothing`;
  console.log(`${u.name}: ${city}, lat ${lat}, tag ${firstTag}, ${hello}`);
}

// ?? differs from || for falsy-but-defined values
const settings = { retries: 0, label: "" };
console.log(`?? keeps 0 and "": retries=${settings.retries ?? 5}, label="${settings.label ?? "x"}"`);
console.log(`|| replaces them: retries=${settings.retries || 5}, label="${settings.label || "x"}"`);

// Mixing requires parentheses
const mixed = (settings.label || null) ?? "fallback";
console.log(`(a || b) ?? c = ${mixed}`);

// Conditional operator followed by a decimal literal - NOT optional chaining
const flag = true;
const ratio = flag?.5:1;
console.log(`flag?.5:1 = ${ratio}`);

// Assertion functions narrow subsequent code
const raw: unknown = JSON.parse('"narrowed"');
assertIsString(raw);
console.log(`after asserts: ${raw.toUpperCase()}`);
const maybeUser = users.find((u) => u.name === "ada");
assert(maybeUser, "ada must exist");
console.log(`assert narrowed to User: ${maybeUser.name}`);

// Recursive type alias in use
const doc: Json = { list: [1, "two", { deep: [true, null] }] };
function depth(j: Json): number {
  if (Array.isArray(j)) return 1 + Math.max(0, ...j.map(depth));
  if (j !== null && typeof j === "object") return 1 + Math.max(0, ...Object.values(j).map(depth));
  return 0;
}
console.log(`Json depth = ${depth(doc)}`);
console.log(`declare field: ${new Derived().name}`);

// Optional call on a possibly-undefined method, and delete with ?.
const registry: { handlers?: Record<string, () => string> } = { handlers: { ping: () => "pong" } };
console.log(`registry.handlers?.ping?.() = ${registry.handlers?.ping?.()}`);
console.log(`registry.handlers?.["nope"]?.() = ${registry.handlers?.["nope"]?.()}`);
