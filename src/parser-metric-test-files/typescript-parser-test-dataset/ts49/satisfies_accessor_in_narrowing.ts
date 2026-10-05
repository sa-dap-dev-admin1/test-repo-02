/*
 * Feature : `satisfies` operator, auto-accessors (`accessor` keyword),
 *           unlisted property narrowing with `in`, NaN equality checks
 * Version : TypeScript 4.9 (November 2022)
 * Spec    : TS 4.9 release notes "The satisfies Operator", "Auto-Accessors
 *           in Classes", "Unlisted Property Narrowing with the in Operator"
 *
 * Parser edge cases:
 *  - `expr satisfies Type` - a new BINARY operator in expression position,
 *    same precedence family as `as`; chains: `x satisfies A as B`.
 *  - `accessor name = value;` - `accessor` is a contextual class-member
 *    modifier; `accessor` is still a valid property NAME (`accessor = 1`).
 *  - `static accessor`, `#private accessor`.
 *  - After `"prop" in obj`, obj is narrowed to `obj & Record<"prop", unknown>`.
 */
export {};

type Color = "red" | "green" | "blue";
type RGB = [number, number, number];

const palette = {
  red: [255, 0, 0],
  green: "#00ff00",
  blue: [0, 0, 255],
} satisfies Record<Color, string | RGB>;

// satisfies keeps the NARROW inferred type: green is string, red is a tuple
const greenUpper = palette.green.toUpperCase();
const redChannel = palette.red[0];

const routes = {
  home: "/",
  user: "/users/:id",
} as const satisfies Record<string, `/${string}`>;

class Counter {
  accessor count = 0;                                // auto-accessor
  static accessor instances = 0;
  accessor #secret = "hidden";
  accessor: string = "property named accessor";

  constructor() { Counter.instances++; }
  bump(): number { return ++this.count; }
  get peek(): string { return this.#secret; }
}

function readId(obj: object): string {
  if ("id" in obj && typeof obj.id === "number") return `id ${obj.id}`;   // 4.9 unlisted narrowing
  return "no id";
}

console.log(`satisfies keeps narrow types: ${greenUpper}, red[0] = ${redChannel}, blue = ${palette.blue.join(",")}`);
console.log(`routes.user = ${routes.user}`);

const c = new Counter();
c.bump(); c.bump();
new Counter();
console.log(`accessor count = ${c.count}, static accessor = ${Counter.instances}, private accessor = ${c.peek}, ${c.accessor}`);
console.log(`accessor creates a getter: ${typeof Object.getOwnPropertyDescriptor(Counter.prototype, "count")?.get}`);
console.log(readId({ id: 7 }), "/", readId({ name: "x" }));
console.log(`NaN check: ${Number.isNaN(0 / 0)}`);   // `x === NaN` is now a compile error (TS2845)
