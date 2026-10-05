/*
 * Feature : Variadic tuple types - spreads of generic tuples in tuple types
 * Version : TypeScript 4.0 (August 2020)
 * Spec    : TS 4.0 release notes "Variadic Tuple Types" (PR #39094)
 *
 * `[...T, ...U]` where T and U are generic tuple types. Enables strongly
 * typed concat, partial application, and argument-list manipulation.
 *
 * Parser edge cases:
 *  - `...T` inside a tuple TYPE where T is a type parameter (3.x allowed
 *    only a trailing `...X[]`).
 *  - Spreads in the MIDDLE: `[string, ...T, number]`.
 *  - Multiple spreads `[...A, ...B]`.
 *  - Nested conditional types inferring a variadic slice:
 *    `T extends [infer H, ...infer R] ? ... : ...`.
 */
export {};

function concat<T extends unknown[], U extends unknown[]>(a: [...T], b: [...U]): [...T, ...U] {
  return [...a, ...b];
}

function tail<T extends unknown[]>(arr: readonly [unknown, ...T]): T {
  const [, ...rest] = arr;
  return rest as unknown as T;
}

type Head<T extends unknown[]> = T extends [infer H, ...unknown[]] ? H : never;
type Tail<T extends unknown[]> = T extends [unknown, ...infer R] ? R : [];
type Last<T extends unknown[]> = T extends [...unknown[], infer L] ? L : never;
type Reverse<T extends unknown[]> = T extends [infer H, ...infer R] ? [...Reverse<R>, H] : [];
type Length<T extends unknown[]> = T["length"];

// Partial application with precise types
function partial<A extends unknown[], B extends unknown[], R>(
  fn: (...args: [...A, ...B]) => R,
  ...head: A
): (...rest: B) => R {
  return (...rest) => fn(...head, ...rest);
}

// Middle spread: wrap arguments with fixed first and last elements
type Framed<T extends unknown[]> = ["<start>", ...T, "<end>"];
function frame<T extends unknown[]>(...items: T): Framed<T> {
  return ["<start>", ...items, "<end>"];
}

function format(name: string, age: number, admin: boolean): string {
  return `${name} (${age})${admin ? " [admin]" : ""}`;
}

const joined = concat([1, "two"], [true, 4n]);         // [number, string, boolean, bigint]
const t = tail([0, "a", "b"] as const);                // readonly-derived ["a","b"]
const h: Head<[string, number]> = "head";
const l: Last<[string, number, boolean]> = true;
const r: Reverse<[1, 2, 3]> = [3, 2, 1];
const len: Length<[1, 2, 3, 4]> = 4;
const tl: Tail<[1, 2, 3]> = [2, 3];

console.log(`concat: ${JSON.stringify(joined, (_k, v) => (typeof v === "bigint" ? `${v}n` : v))}`);
console.log(`tail: ${JSON.stringify(t)}, head: ${h}, last: ${l}, reverse: ${r}, length: ${len}, Tail: ${tl}`);

const greetAda = partial(format, "Ada");
const greetAda36 = partial(format, "Ada", 36);
console.log(greetAda(36, true));
console.log(greetAda36(false));

console.log(`frame: ${frame(1, 2, 3).join(" ")}`);
