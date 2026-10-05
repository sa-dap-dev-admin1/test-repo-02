/*
 * Feature : `const` type parameters, all enums are union enums, `extends`
 *           supports multiple configuration files (tsconfig, not syntax),
 *           `export type *`, `--verbatimModuleSyntax`, JSDoc @satisfies
 * Version : TypeScript 5.0 (March 2023)
 * Spec    : TS 5.0 release notes "const Type Parameters", "All enums Are
 *           Union enums", "Support for export type *"
 *
 * Parser edge cases:
 *  - `function f<const T>(x: T)` - `const` modifier on a TYPE PARAMETER.
 *    Also on classes and methods: `class C<const T>`.
 *  - `const` and `in`/`out` combine: `<const in T>`? (no - const is only
 *    allowed on function/method/class type params, not with variance).
 *  - Enum members initialized by computed expressions referencing other
 *    members; `enum` vs `const enum` vs `declare enum`.
 *  - `export type * from "./x"` / `export type * as ns from "./x"`.
 */
export {};

function routesOf<const T extends readonly string[]>(paths: T): T { return paths; }
function asConfig<const T extends Record<string, unknown>>(cfg: T): T { return cfg; }

class Registry<const K extends string> {
  constructor(public readonly keys: readonly K[]) {}
  has(k: string): k is K { return (this.keys as readonly string[]).includes(k); }
}

const routes = routesOf(["/", "/about", "/users"]);      // readonly ["/", "/about", "/users"]
type Route = typeof routes[number];
const cfg = asConfig({ mode: "strict", level: 3, flags: ["a", "b"] });
type Mode = typeof cfg.mode;                              // "strict", not string

enum Permission {
  None = 0,
  Read = 1 << 0,
  Write = 1 << 1,
  Execute = 1 << 2,
  ReadWrite = Read | Write,
  All = ReadWrite | Execute,
}

enum Level { Low = "LOW", High = "HIGH" }
const enum Inline { A = 10, B = A * 2 }                   // const enum: inlined at use sites

function can(p: Permission, needed: Permission): boolean { return (p & needed) === needed; }

function label(l: Level): string {
  switch (l) {
    case Level.Low: return "low";
    case Level.High: return "high";
  }
}

const r: Route = "/about";
const m: Mode = "strict";
const reg = new Registry(["alpha", "beta"]);
console.log(`const type params: ${routes.join(" ")} | route ${r} | mode ${m} | flags ${cfg.flags.join("")}`);
console.log(`Registry.has("beta") = ${reg.has("beta")}, has("gamma") = ${reg.has("gamma")}`);
console.log(`enum: ReadWrite=${Permission.ReadWrite}, All=${Permission.All}, reverse map ${Permission[4]}`);
console.log(`can(ReadWrite, Write) = ${can(Permission.ReadWrite, Permission.Write)}, can(Read, Execute) = ${can(Permission.Read, Permission.Execute)}`);
console.log(`string enum: ${label(Level.High)}, const enum B = ${Inline.B}`);
