/*
 * Feature : `override` modifier (4.3), separate write types on properties
 *           (4.3), template string type improvements, ECMAScript #private
 *           methods/accessors (4.3), static index signatures (4.3),
 *           symbol and template-pattern index signatures (4.4), class
 *           static blocks (4.4), exactOptionalPropertyTypes (4.4),
 *           control-flow analysis of aliased conditions (4.4)
 * Version : TypeScript 4.3 (May 2021) / 4.4 (August 2021)
 * Spec    : TS 4.3 and 4.4 release notes
 *
 * Parser edge cases:
 *  - `override` as a class member modifier: `override method() {}`,
 *    `public override readonly x`, and in parameter properties.
 *  - Getter/setter pair with DIFFERENT types: `get size(): number` /
 *    `set size(v: number | string)`.
 *  - `static [key: string]: number` - `static` before an index signature.
 *  - `[key: `data-${string}`]: string` and `[sym: symbol]: unknown` index
 *    signatures.
 *  - `static { ... }` - class static initialization block.
 *  - `#privateMethod()` and `get #secret()`.
 */
export {};

class Animal {
  constructor(public name: string) {}
  speak(): string { return `${this.name} makes a sound`; }
  move(meters = 1): string { return `${this.name} moved ${meters}m`; }
}

class Dog extends Animal {
  override speak(): string { return `${this.name} barks`; }
  public override move(meters = 5): string { return `${super.move(meters)} (running)`; }
}

class Box {
  #size = 0;
  get size(): number { return this.#size; }
  set size(value: number | string) { this.#size = typeof value === "string" ? parseInt(value, 10) : value; }

  #log(msg: string): string { return `[box] ${msg}`; }
  get #secret(): string { return "hidden accessor"; }
  reveal(): string { return this.#log(this.#secret); }
}

class Registry {
  static [key: string]: number | ((...a: never[]) => unknown);   // static index signature
  static count = 0;
  static {                                                         // static block (4.4)
    Registry.count = 10;
    Registry["computed"] = Registry.count * 2;
  }
}

interface DataAttributes {
  [attr: `data-${string}`]: string;                  // template-pattern index signature (4.4)
  [sym: symbol]: number;                             // symbol index signature (4.4)
  id: string;
}

const marker = Symbol("marker");
const el: DataAttributes = { id: "main", "data-role": "button", "data-state": "open", [marker]: 1 };

function describeValue(v: string | number) {
  const isString = typeof v === "string";            // aliased condition (4.4)
  if (isString) return `string of length ${v.length}`;
  return `number ${v.toFixed(2)}`;
}

const d = new Dog("rex");
console.log(d.speak());
console.log(d.move());

const box = new Box();
box.size = "42";
console.log(`setter accepted string, getter returns number: ${box.size + 1}`);
console.log(box.reveal());
console.log(`static block ran: count=${Registry.count}, computed=${Registry["computed"]}`);
console.log(`data attributes: ${Object.keys(el).join(", ")}, symbol value ${el[marker]}`);
console.log(describeValue("hello"), "/", describeValue(3.14159));
