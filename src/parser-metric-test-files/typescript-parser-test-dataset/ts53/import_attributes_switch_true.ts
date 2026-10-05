/*
 * Feature : Import attributes (`with { type: "json" }`), `switch (true)`
 *           narrowing, narrowing on comparisons to booleans, `instanceof`
 *           narrowing through Symbol.hasInstance, resolution-mode in import
 *           types
 * Version : TypeScript 5.3 (November 2023)
 * Spec    : TS 5.3 release notes "Import Attributes", "switch (true)
 *           Narrowing", "Narrowing On Comparisons to Booleans"; TC39
 *           proposal-import-attributes (stage 3/4)
 *
 * Parser edge cases:
 *  - `import data from "./x.json" with { type: "json" };` - `with` clause
 *    after the module specifier (replaced 4.5's `assert { ... }`).
 *  - Dynamic form: `import("./x.json", { with: { type: "json" } })`.
 *  - `import type T from "pkg" with { "resolution-mode": "import" }`.
 *  - `switch (true) { case typeof x === "string": ... }` - case
 *    expressions that are full boolean conditions.
 *
 * Self-contained: the import attribute is used on a TYPE-only import of
 * this file (erased at emit); JSON forms appear in comments.
 */
import type { Shape as SelfShape } from "./import_attributes_switch_true" with { "resolution-mode": "require" };

export interface Shape { kind: "circle" | "square"; size: number }

function describe(value: unknown): string {
  switch (true) {
    case typeof value === "string":
      return `string of ${value.length} chars`;
    case typeof value === "number" && value > 100:
      return `big number ${value.toFixed(0)}`;
    case typeof value === "number":
      return `number ${value}`;
    case Array.isArray(value):
      return `array with ${value.length} items`;
    case value instanceof Date:
      return `date ${value.getUTCFullYear()}`;
    default:
      return "something else";
  }
}

function isCircle(s: Shape): boolean { return s.kind === "circle"; }

function area(s: Shape | undefined): number {
  if (s?.kind === "circle" === true) return Math.PI * s!.size ** 2;   // comparison to boolean
  return s ? s.size ** 2 : 0;
}

class Even {
  static [Symbol.hasInstance](v: unknown): v is number { return typeof v === "number" && v % 2 === 0; }
}

// Static and dynamic import-attribute forms need --module esnext/nodenext
// and a real JSON file, so they are shown here as comments:
//   import data from "./data.json" with { type: "json" };
//   const mod = await import("./data.json", { with: { type: "json" } });
function loadJsonLater(): SelfShape { return { kind: "square", size: 0 }; }

const shapes: Shape[] = [{ kind: "circle", size: 1 }, { kind: "square", size: 3 }];
for (const v of ["hello", 512, 7, [1, 2], new Date(0), null]) console.log(describe(v));
console.log(`areas: ${shapes.map(area).map((a) => a.toFixed(2)).join(", ")}, circles: ${shapes.filter(isCircle).length}`);
console.log(`Symbol.hasInstance: 4 instanceof Even = ${(4 as unknown) instanceof Even}, 5 = ${(5 as unknown) instanceof Even}`);
console.log(`dynamic import fn defined: ${typeof loadJsonLater}`);
