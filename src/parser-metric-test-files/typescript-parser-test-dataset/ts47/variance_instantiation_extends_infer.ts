/*
 * Feature : Optional variance annotations (in/out), instantiation
 *           expressions, `extends` constraints on `infer`, control-flow
 *           narrowing for bracketed element access, ESM support in Node (module: node16) (4.7);
 *           also covers 4.6 control-flow analysis for destructured
 *           discriminated unions and 4.8 improved intersection reduction
 * Version : TypeScript 4.6 (Feb 2022), 4.7 (May 2022), 4.8 (Aug 2022)
 * Spec    : TS 4.7 release notes "Optional Variance Annotations",
 *           "Instantiation Expressions", "extends Constraints on infer";
 *           TS 4.6 "Control-Flow Analysis for Destructured Discriminated
 *           Unions"
 *
 * Parser edge cases:
 *  - `interface Producer<out T>` / `<in T>` / `<in out T>` - variance
 *    keywords before a type parameter name. `in` and `out` are still valid
 *    as type parameter NAMES: `<in>` is a type param called `in`!
 *  - `const makeBox = createBox<string>;` - type arguments applied to an
 *    expression WITHOUT a call. Parser must decide `a < b > c` vs
 *    instantiation; the rule: `<...>` followed by a token that cannot start
 *    an expression (`;`, `)`, `.`...) is an instantiation expression.
 *  - NOTE: `typeof this.#field` appeared in the 4.7 beta but was pulled
 *    before release; tsc 6 rejects it, so it is not used here.
 *  - `T extends [infer H extends string, ...unknown[]]` - `extends` inside
 *    an `infer` declaration within a conditional's extends clause.
 */
export {};

interface Producer<out T> { produce(): T }
interface Consumer<in T> { consume(value: T): void }
interface Store<in out T> { get(): T; set(v: T): void }

class Box<T> { constructor(public value: T) {} }
function createBox<T>(value: T): Box<T> { return new Box(value); }
const makeStringBox = createBox<string>;              // instantiation expression
const StringMap = Map<string, number>;                // on a constructor
const ErrorMap = Map<string, Error>;

type FirstString<T> = T extends [infer S extends string, ...unknown[]] ? S : never;
type NumericLiteral<T> = T extends `${infer N extends number}` ? N : never;

// 4.6: destructured discriminated union narrowing
type Action = { kind: "inc"; by: number } | { kind: "rename"; to: string };
function apply({ kind, ...rest }: Action, state: { n: number; name: string }) {
  if (kind === "inc") return { ...state, n: state.n + (rest as { by: number }).by };
  return { ...state, name: (rest as { to: string }).to };
}
function applyDestructured(action: Action, state: { n: number; name: string }) {
  const { kind } = action;
  if (kind === "inc") return { ...state, n: state.n + action.by };   // narrowed via alias
  return { ...state, name: action.to };
}

// 4.7: narrowing on bracketed access with a const key
const key = "payload";
function readPayload(obj: { [key]?: string }): string {
  if (typeof obj[key] === "string") return obj[key].toUpperCase();
  return "(none)";
}

const animals: Producer<string> = { produce: () => "cat" };
const sink: Consumer<string> = { consume: (v) => console.log(`consumed ${v}`) };
let held = 1;
const store: Store<number> = { get: () => held, set: (v) => { held = v; } };

sink.consume(animals.produce());
store.set(store.get() + 41);
console.log(`store = ${store.get()}`);

console.log(`instantiation expression: ${makeStringBox("typed").value}`);
const m = new StringMap([["a", 1]]);
const e = new ErrorMap();
console.log(`Map<string, number> via alias: ${m.get("a")}, ErrorMap size ${e.size}`);

const fs: FirstString<["first", 2]> = "first";
const n: NumericLiteral<"42"> = 42;
console.log(`infer extends: ${fs}, ${n + 1}`);
console.log(JSON.stringify(apply({ kind: "inc", by: 2 }, { n: 1, name: "x" })), JSON.stringify(applyDestructured({ kind: "rename", to: "y" }, { n: 1, name: "x" })));
console.log(`bracketed narrowing: ${readPayload({ payload: "data" })}, ${readPayload({})}`);

// `in` and `out` are still legal type parameter names
function legacy<in_ = string, out_ = number>(a: in_, b: out_): [in_, out_] { return [a, b]; }
console.log(`legacy: ${legacy("x", 1)}`);
