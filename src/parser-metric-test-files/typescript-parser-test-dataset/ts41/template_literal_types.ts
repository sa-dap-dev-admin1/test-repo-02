/*
 * Feature : Template literal types, intrinsic string manipulation types
 *           (Uppercase, Lowercase, Capitalize, Uncapitalize)
 * Version : TypeScript 4.1 (November 2020)
 * Spec    : TS 4.1 release notes "Template Literal Types" (PR #40336)
 *
 * Backtick strings in TYPE position: `${A}-${B}` where A and B are types.
 * Unions distribute: `${"a"|"b"}-x` is "a-x" | "b-x". `infer` works inside.
 *
 * Parser edge cases:
 *  - A template literal in a TYPE context, including `${infer X}` holes.
 *  - Nested template literal types and holes containing generic types.
 *  - Escaped `\${` and backticks inside template literal types.
 *  - `Uppercase<...>` etc. are intrinsic - declared `type Uppercase<S> =
 *    intrinsic;` in lib.d.ts (`intrinsic` is a contextual keyword there).
 */
export {};

type Side = "top" | "right" | "bottom" | "left";
type MarginProp = `margin-${Side}`;
type CssVar<N extends string> = `--${N}`;

type EventName<T extends string> = `on${Capitalize<T>}`;
type Getters<T> = { [K in keyof T as `get${Capitalize<string & K>}`]: () => T[K] };

// Parsing strings at the type level with infer
type Split<S extends string, D extends string> =
  S extends `${infer Head}${D}${infer Rest}` ? [Head, ...Split<Rest, D>] : [S];
type TrimLeft<S extends string> = S extends ` ${infer R}` ? TrimLeft<R> : S;
type RouteParams<P extends string> =
  P extends `${string}:${infer Param}/${infer Rest}` ? Param | RouteParams<`/${Rest}`>
  : P extends `${string}:${infer Param}` ? Param : never;

type Shout<S extends string> = `${Uppercase<S>}!`;
type Dotted = `${Lowercase<"API">}.${Uncapitalize<"Version">}`;

function setMargin(prop: MarginProp, px: number): string { return `${prop}: ${px}px`; }

function on<T extends string>(name: T, handler: () => void): EventName<T> {
  handler();
  return `on${name.charAt(0).toUpperCase()}${name.slice(1)}` as EventName<T>;
}

function route<P extends string>(pattern: P, params: Record<RouteParams<P>, string>): string {
  return pattern.replace(/:(\w+)/g, (_, k: string) => (params as Record<string, string>)[k]);
}

function makeGetters<T extends object>(obj: T): Getters<T> {
  const out: Record<string, () => unknown> = {};
  for (const key of Object.keys(obj) as (keyof T & string)[]) {
    out[`get${key[0].toUpperCase()}${key.slice(1)}`] = () => obj[key];
  }
  return out as Getters<T>;
}

const parts: Split<"a,b,c", ","> = ["a", "b", "c"];
const trimmed: TrimLeft<"   padded"> = "padded";
const shout: Shout<"hey"> = "HEY!";
const dotted: Dotted = "api.version";
const cssVar: CssVar<"main-color"> = "--main-color";
const escaped: `price: \${amount} \`quoted\`` = "price: ${amount} `quoted`";

console.log(setMargin("margin-left", 8));
console.log(on("click", () => console.log("  handler ran")));
console.log(route("/users/:userId/posts/:postId", { userId: "7", postId: "42" }));
const getters = makeGetters({ name: "ada", age: 36 });
console.log(`getters: ${getters.getName()} ${getters.getAge()}`);
console.log(`types as values: ${parts.join("|")} [${trimmed}] ${shout} ${dotted} ${cssVar}`);
console.log(escaped);
