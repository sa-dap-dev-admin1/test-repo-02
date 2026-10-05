/*
 * Feature : `unknown` type, rest elements and optional elements in tuple
 *           types, generic rest parameters, `defaultProps`-free JSX aside
 * Version : TypeScript 3.0 (July 2018)
 * Spec    : TS 3.0 release notes - "New unknown top type", "Tuples in rest
 *           parameters and spread expressions", "Optional elements in tuple
 *           types", "Rest elements in tuple types"
 *
 * Parser edge cases:
 *  - `unknown` is a contextual TYPE keyword; still a valid identifier in
 *    value positions (`const unknown = 1` is legal).
 *  - Tuple type with optional elements `[string, number?]` - `?` AFTER a
 *    type inside a tuple, not a conditional or optional property.
 *  - Rest element in a tuple type `[string, ...number[]]`.
 *  - Generic rest parameter `...args: T` where `T extends any[]`.
 *  - `[...T, U]` (rest not at the end) is TS 4.0 / 4.2, NOT valid in 3.0.
 */

function describe(value: unknown): string {
  if (typeof value === "string") return `string(${value.length})`;
  if (typeof value === "number") return `number(${value.toFixed(1)})`;
  if (Array.isArray(value)) return `array(${value.length})`;
  if (value !== null && typeof value === "object" && "id" in value) return "object with id";
  if (value instanceof Date) return "date";
  return typeof value;
}

// unknown is only assignable to unknown/any without narrowing
function safeParse(json: string): unknown {
  try { return JSON.parse(json); } catch { return undefined; }
}

function isPoint(v: unknown): v is { x: number; y: number } {
  return typeof v === "object" && v !== null &&
    typeof (v as { x?: unknown }).x === "number" &&
    typeof (v as { y?: unknown }).y === "number";
}

// Optional tuple elements
type SpanRange = [number, number?];                       // optional tuple element
function span([start, end]: SpanRange): number { return (end ?? start) - start; }

// Rest elements in tuple types
type Command = [string, ...number[]];
function run(cmd: Command): string {
  const [name, ...args] = cmd;
  return `${name}(${args.join(", ")}) = ${args.reduce((a, b) => a + b, 0)}`;
}

// Generic rest parameters: capture the parameter list as a tuple type
function bind<T extends unknown[], R>(fn: (...args: T) => R, ...args: T): () => R {
  return () => fn(...args);
}

function curryFirst<A, Rest extends unknown[], R>(fn: (a: A, ...rest: Rest) => R, a: A) {
  return (...rest: Rest): R => fn(a, ...rest);
}

type Params<F> = F extends (...args: infer P) => unknown ? P : never;

const add3 = (a: number, b: number, c: number): number => a + b + c;
type Add3Params = Params<typeof add3>;               // [number, number, number]
const tupleArgs: Add3Params = [1, 2, 3];

const unknown = "contextual keyword used as an identifier";

const samples: unknown[] = ["hi", 3.14159, [1, 2], { id: 1 }, new Date(0), null, true];
for (const s of samples) console.log(describe(s));

const parsed = safeParse('{"x": 3, "y": 4}');
if (isPoint(parsed)) console.log(`point distance = ${Math.hypot(parsed.x, parsed.y)}`);

console.log(`span([5]) = ${span([5])}, span([5, 12]) = ${span([5, 12])}`);
console.log(run(["sum", 1, 2, 3, 4]));
console.log(run(["noop"]));
console.log(`bind: ${bind(add3, 1, 2, 3)()}`);
console.log(`curryFirst: ${curryFirst(add3, 10)(20, 30)}`);
console.log(`spread tuple: ${add3(...tupleArgs)}`);
console.log(unknown);
