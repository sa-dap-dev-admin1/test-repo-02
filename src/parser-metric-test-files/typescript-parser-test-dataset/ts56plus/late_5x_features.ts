/*
 * Feature : Late TypeScript 5.x additions - arbitrary module namespace
 *           identifiers (5.6), disallowed nullish/truthy checks (5.6),
 *           iterator helper types (5.6), checks for never-initialized
 *           variables (5.7), `--rewriteRelativeImportExtensions` (5.7),
 *           granular return-expression checks (5.8),
 *           `--erasableSyntaxOnly` (5.8), `import defer` (5.9)
 * Version : TypeScript 5.6 (Sep 2024) - 5.9 (Aug 2025); verified with tsc 6.0
 * Spec    : TS 5.6-5.9 release notes
 *
 * Parser edge cases:
 *  - `export { value as "string name" }` and
 *    `import { "string name" as alias } from "..."` - STRING LITERALS as
 *    import/export names (ES2022 arbitrary module namespace identifiers).
 *  - Iterator helpers: `Iterator.from(...)`, `.map().filter().take()` on
 *    iterators (ES2025 lib).
 *  - `import defer * as ns from "mod"` (5.9; module: esnext/preserve only)
 *    is described here but not used, because this file compiles as
 *    CommonJS.
 *  - 5.6 makes `if (/regex/)` and `if (x => 0)` errors ("this condition will
 *    always return true") - a check, not syntax; shown in comments only.
 */
const greeting = "hello from an arbitrary namespace identifier";
export { greeting as "greeting-with-dashes" };
import { "greeting-with-dashes" as aliasedGreeting } from "./late_5x_features";

function* naturals(): Generator<number> { let n = 1; while (true) yield n++; }

const firstSquaresOfOdds = naturals()
  .filter((n) => n % 2 === 1)
  .map((n) => n * n)
  .take(5)
  .toArray();

const fromArray = Iterator.from(["x", "y", "z"]).map((s) => s.toUpperCase()).toArray();

function config(debug: boolean): string {
  let level: string;                                  // 5.7 checks use-before-init
  if (debug) level = "verbose";
  else level = "quiet";
  return level;
}

// if (/abc/) {}         // 5.6: error TS2872 - this kind of expression is always truthy
// if (x => 0) {}        // 5.6: error - always truthy

console.log(aliasedGreeting);
console.log(`iterator helpers: ${firstSquaresOfOdds.join(",")} | Iterator.from: ${fromArray.join("")}`);
console.log(`config: ${config(true)} / ${config(false)}`);
