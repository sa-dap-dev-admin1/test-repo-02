/*
 * Feature : Labeled tuple elements, logical assignment operators (&&=, ||=,
 *           ??=), `unknown` in catch clause bindings, class property type
 *           inference from constructors
 * Version : TypeScript 4.0
 * Spec    : TS 4.0 release notes "Labeled Tuple Elements", "Short-Circuiting
 *           Assignment Operators", "unknown on catch Clause Bindings",
 *           "Class Property Inference from Constructors"; ES2021 logical
 *           assignment (TC39 stage 4)
 *
 * Parser edge cases:
 *  - `[start: number, end?: number, ...rest: string[]]` - `name:` labels
 *    inside a tuple type; `?` goes AFTER the label, rest `...` BEFORE it.
 *    Mixing labeled and unlabeled elements is an error.
 *  - `a &&= b`, `a ||= b`, `a ??= b` - three new compound-assignment tokens.
 *  - `catch (e: unknown)` - type annotation on a catch binding (only `any`
 *    or `unknown` allowed).
 */
export {};

type Range = [start: number, end: number];
type Request = [method: string, url: string, body?: string];
type Spread = [name: string, ...scores: number[]];

function slice(text: string, ...[start, end]: Range): string {
  return text.slice(start, end);
}

function send(...[method, url, body]: Request): string {
  return `${method} ${url}${body ? ` with ${body.length}-char body` : ""}`;
}

function average(...[name, ...scores]: Spread): string {
  const avg = scores.reduce((a, b) => a + b, 0) / (scores.length || 1);
  return `${name}: ${avg.toFixed(1)}`;
}

class Connection {
  host;                                              // type inferred from the constructor
  port;
  constructor(host: string, port?: number) {
    this.host = host;
    this.port = port ?? 443;
  }
}

console.log(slice("labeled tuples", 0, 7));
console.log(send("GET", "/items"));
console.log(send("POST", "/items", '{"name":"x"}'));
console.log(average("ada", 90, 85, 100));

const opts: { retries?: number; verbose?: boolean; name?: string | null } = { verbose: true, name: null };
opts.retries ??= 3;                                  // assign if null/undefined
opts.verbose &&= false;                              // assign if truthy
opts.name ||= "anonymous";                           // assign if falsy
console.log(`logical assignment: ${JSON.stringify(opts)}`);

let counter = 0;
const lazy = { value: 1 as number | undefined };
lazy.value ??= ++counter;                            // RHS not evaluated: value already set
console.log(`short-circuit: counter = ${counter}`);

try {
  JSON.parse("{ bad json");
} catch (e: unknown) {
  if (e instanceof SyntaxError) console.log(`caught SyntaxError: ${e.name}`);
}

const conn = new Connection("example.org");
console.log(`inferred class props: ${conn.host}:${conn.port}`);
