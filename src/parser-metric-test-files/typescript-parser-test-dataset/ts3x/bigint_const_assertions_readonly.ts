/*
 * Feature : bigint (3.2), readonly array/tuple types and `as const`
 *           const assertions (3.4), higher-order generic inference (3.4),
 *           `globalThis` (3.4), assertion-free unions of call signatures (3.3)
 * Version : TypeScript 3.2 - 3.4
 * Spec    : TS 3.2 release notes "BigInt"; TS 3.4 release notes
 *           "const assertions", "readonly array and tuple types",
 *           "Type-checking for globalThis"
 *
 * Parser edge cases:
 *  - `123n` - BigInt literal suffix (also an ES2020 JS feature).
 *  - `readonly string[]` - `readonly` as a TYPE OPERATOR prefix; only valid
 *    before array and tuple types: `readonly [number, number]`.
 *  - `expr as const` - `const` used as a type in an assertion.
 *  - `<const>["a", "b"]` - angle-bracket const assertion (not in .tsx).
 *  - `as const` cannot apply to arbitrary expressions: only literals,
 *    arrays, objects, template literals - a type-check error, not syntax.
 */

const big: bigint = 9007199254740993n;               // > Number.MAX_SAFE_INTEGER
const product = big * 3n;
const fromNumber = BigInt(42);
console.log(`bigint: ${big} * 3n = ${product}, BigInt(42) = ${fromNumber}, typeof = ${typeof big}`);
console.log(`2n ** 64n = ${2n ** 64n}`);

function factorial(n: bigint): bigint {
  return n <= 1n ? 1n : n * factorial(n - 1n);
}
console.log(`25! = ${factorial(25n)}`);

// readonly arrays and tuples
function total(values: readonly number[]): number {
  // values.push(1);   // error: push does not exist on readonly number[]
  return values.reduce((a, b) => a + b, 0);
}
const pair: readonly [number, string] = [1, "one"];
const legacy: ReadonlyArray<number> = [4, 5];
console.log(`total = ${total([1, 2, 3])}, pair = ${pair.join(":")}, ReadonlyArray length = ${legacy.length}`);

// const assertions
const directions = ["north", "south", "east", "west"] as const;
type Direction = typeof directions[number];          // "north" | "south" | "east" | "west"
const config = { mode: "fast", retries: 3, tags: ["a", "b"] } as const;
const angle = <const>["x", "y"];                      // angle-bracket form
const literalNum = 42 as const;

function move(d: Direction): string { return `moving ${d}`; }
console.log(move(directions[2]));
console.log(`config.mode = ${config.mode}, tags = ${config.tags.join(",")}, angle = ${angle.join("")}, literal = ${literalNum}`);

// Discriminated union built from const assertion
function action<T extends string, P>(type: T, payload: P) {
  return { type, payload } as const;
}
const a1 = action("add", 5);
const a2 = action("rename", "new-name");
type Action = typeof a1 | typeof a2;
function reduce(state: { n: number; name: string }, act: Action) {
  switch (act.type) {
    case "add": return { ...state, n: state.n + act.payload };
    case "rename": return { ...state, name: act.payload };
  }
}
console.log(JSON.stringify(reduce(reduce({ n: 1, name: "x" }, a1), a2)));

// Higher-order function type inference (3.4)
function compose<A, B, C>(f: (a: A) => B, g: (b: B) => C): (a: A) => C {
  return (a) => g(f(a));
}
function box<T>(value: T): { value: T } { return { value }; }
function list<T>(value: T): T[] { return [value]; }
const boxList = compose(list, box);                  // <T>(a: T) => { value: T[] }
console.log(`compose generic: ${JSON.stringify(boxList("item"))}`);

// globalThis
(globalThis as unknown as { appName: string }).appName = "parser-tests";
console.log(`globalThis.appName = ${(globalThis as unknown as { appName: string }).appName}`);
