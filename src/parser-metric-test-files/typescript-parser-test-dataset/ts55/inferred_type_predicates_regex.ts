/*
 * Feature : Inferred type predicates, control-flow narrowing for constant
 *           indexed accesses, regular-expression syntax checking, the
 *           `${configDir}` template (config), isolatedDeclarations, new
 *           Set methods typings
 * Version : TypeScript 5.5 (June 2024)
 * Spec    : TS 5.5 release notes "Inferred Type Predicates", "Control Flow
 *           Narrowing for Constant Indexed Accesses", "Regular Expression
 *           Syntax Checking"
 *
 * Parser edge cases:
 *  - tsc now PARSES regular-expression literals and reports syntax errors
 *    inside them (e.g. unbalanced groups, invalid flags, ES2024 `v` flag
 *    features on an older target). Valid modern regex features used here:
 *    named groups, lookbehind, `d` indices flag, unicode property escapes.
 *  - `/(?<year>\d{4})-(?<month>\d{2})/v` - the `v` flag (ES2024) requires
 *    target es2024+; tsc 5.5+ flags it otherwise.
 */
export {};

// Inferred predicate: filter now narrows (string | undefined)[] to string[]
const maybe = ["a", undefined, "b", undefined, "c"];
const defined = maybe.filter((x) => x !== undefined);
console.log(`inferred predicate: ${defined.map((s) => s.toUpperCase()).join("")}`);

function isNumber(x: unknown) { return typeof x === "number"; }   // inferred: x is number
const mixed: unknown[] = [1, "two", 3];
const nums = mixed.filter(isNumber);
console.log(`isNumber inferred predicate sum: ${nums.reduce((a, b) => a + b, 0)}`);

// Narrowing on obj[key] when both are constant
function upperValue(obj: Record<string, unknown>, key: string): string {
  if (typeof obj[key] === "string") return obj[key].toUpperCase();
  return "(not a string)";
}
console.log(`constant indexed access: ${upperValue({ name: "ada" }, "name")}`);

// Regular expression features that tsc now validates
const date = /(?<year>\d{4})-(?<month>\d{2})-(?<day>\d{2})/d;
const m = date.exec("released 2024-06-20 at noon");
console.log(`named groups: ${m?.groups?.year}/${m?.groups?.month}, indices[1] = ${m?.indices?.[1]}`);
const price = /(?<=\$)\d+(\.\d\d)?/;
console.log(`lookbehind: ${"total: $42.50".match(price)?.[0]}`);
const greek = /\p{Script=Greek}+/u;
console.log(`unicode property: ${"abc αβγ def".match(greek)?.[0]}`);
// const broken = /(unclosed/;   // tsc 5.5+: error TS1005 ')' expected - inside a regex!

const a = new Set([1, 2, 3, 4]);
const b = new Set([3, 4, 5]);
console.log(`Set methods: union ${[...a.union(b)]}, intersection ${[...a.intersection(b)]}, difference ${[...a.difference(b)]}`);
