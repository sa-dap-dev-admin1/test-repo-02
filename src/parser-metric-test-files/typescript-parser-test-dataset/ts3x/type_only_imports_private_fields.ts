/*
 * Feature : ECMAScript private fields (#x), `import type` / `export type`,
 *           `@ts-expect-error` comments
 * Version : TypeScript 3.8 (February 2020) and 3.9 (May 2020)
 * Spec    : TS 3.8 release notes "Type-Only Imports and Export",
 *           "ECMAScript Private Fields", "export * as ns Syntax";
 *           TS 3.9 release notes "// @ts-expect-error Comments"
 *
 * Self-contained: the only import is a TYPE-ONLY import of this same
 * file, which is erased entirely at emit.
 *
 * Parser edge cases:
 *  - `#count` - private name token; `#count in obj` brand check is TS 4.5.
 *  - `this.#count` vs TS `private count` - different emit and semantics.
 *  - `import type { X } from "..."` - `type` after `import`; `import type X,
 *    { Y }` is an ERROR (cannot combine default and named in type import).
 *  - `export type { Foo };` - type-only re-export.
 *  - `// @ts-expect-error` must be followed by a line that HAS an error,
 *    otherwise it is itself an error.
 *  - Private names are only valid inside the class body that declares them.
 */

export interface Shape { area(): number }
export type Color = "red" | "green" | "blue";

class Counter {
  #count = 0;                                        // ES private field
  static #instances = 0;                             // static private field
  private legacy = "ts-private";                     // TS private (compile-time only)

  constructor() { Counter.#instances++; }

  #bump(by: number): void { this.#count += by; }     // private method (TS 4.3 for methods)

  increment(): this { this.#bump(1); return this; }
  get value(): number { return this.#count; }
  static get instances(): number { return Counter.#instances; }

  equals(other: Counter): boolean { return this.#count === other.#count; }  // same-class access
  describe(): string { return `${this.legacy}:${this.#count}`; }
}

class Square implements Shape {
  constructor(private readonly side: number) {}
  area(): number { return this.side ** 2; }
}

// type-only export of an interface that already exists
export type { Shape as ShapeContract };

// A type-only SELF-import: legal, and erased entirely at emit
import type { Color as ImportedColor } from "./type_only_imports_private_fields";
type MaybeColor = ImportedColor | undefined;
const noEmitter: MaybeColor = undefined;

const c1 = new Counter().increment().increment();
const c2 = new Counter().increment().increment();
console.log(`c1.value = ${c1.value}, equal = ${c1.equals(c2)}, instances = ${Counter.instances}`);
console.log(`describe = ${c1.describe()}`);
console.log(`private field is not a property: ${Object.keys(c1).includes("#count")}`);

const shapes: Shape[] = [new Square(3), { area: () => 1.5 }];
console.log(`areas = ${shapes.map((s) => s.area()).join(", ")}`);

const color: Color = "green";
console.log(`color = ${color}, emitter = ${noEmitter ?? "none"}`);

// @ts-expect-error - assigning a number to a string is a type error, suppressed here
const wrong: string = 123;
console.log(`@ts-expect-error suppressed a real error; runtime value = ${wrong}`);
