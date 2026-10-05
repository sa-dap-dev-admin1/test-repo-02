/*
 * Feature : NoInfer<T> intrinsic, preserved narrowing in closures after
 *           last assignment, Object.groupBy / Map.groupBy typings
 * Version : TypeScript 5.4 (March 2024)
 * Spec    : TS 5.4 release notes "The NoInfer Utility Type", "Preserved
 *           Narrowing in Closures Following Last Assignments"
 *
 * Parser edge cases:
 *  - `NoInfer<T>` looks like any generic type reference; it is an
 *    intrinsic (declared `type NoInfer<T> = intrinsic`).
 *  - No new syntax otherwise - this file guards against regressions in
 *    closure/arrow-function parsing inside narrowed blocks.
 */
export {};

function createStreetLight<C extends string>(colors: C[], defaultColor?: NoInfer<C>): string {
  return `${colors.join("/")} default=${defaultColor ?? colors[0]}`;
}

function pick<T>(items: T[], fallback: NoInfer<T>): T { return items.length ? items[0] : fallback; }

function urlFor(base: string | URL, path: string): () => string {
  if (typeof base === "string") base = new URL(base);   // last assignment
  return () => new URL(path, base).toString();         // narrowing to URL preserved (5.4)
}

const inventory = [
  { name: "asparagus", type: "vegetables", qty: 5 },
  { name: "bananas", type: "fruit", qty: 0 },
  { name: "goat", type: "meat", qty: 23 },
  { name: "cherries", type: "fruit", qty: 5 },
];

console.log(createStreetLight(["red", "yellow", "green"], "red"));
// createStreetLight(["red", "yellow", "green"], "blue");   // error: "blue" not inferred into C
console.log(`pick: ${pick([] as number[], 99)}`);
console.log(`closure narrowing: ${urlFor("https://example.org/api/", "v1/items")()}`);

const grouped = Object.groupBy(inventory, (item) => item.type);
console.log(`Object.groupBy fruit: ${grouped.fruit?.map((i) => i.name).join(", ")}`);
const byStock = Map.groupBy(inventory, (i) => (i.qty > 0 ? "in stock" : "sold out"));
console.log(`Map.groupBy sold out: ${byStock.get("sold out")?.map((i) => i.name)}`);
