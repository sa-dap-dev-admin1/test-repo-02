/*
 * Feature : NEGATIVE TEST - TypeScript-only syntax inside a plain .js file
 * Version : n/a (ECMAScript has no type annotation syntax as of ES2025;
 *           the TC39 "type annotations" proposal is stage 1)
 * Spec    : tsc diagnostic TS8010 "Type annotations can only be used in
 *           TypeScript files", TS8008 "Type aliases can only be used in
 *           TypeScript files", TS8006 "'interface' declarations can only be
 *           used in TypeScript files"
 *
 * EXPECTED:
 *  - JavaScript parser (any ES version)            -> FAIL (SyntaxError)
 *  - TypeScript parser honouring the .js extension -> FAIL (TS8010 etc.)
 *  - TypeScript parser that IGNORES the extension  -> PASS  <- the bug this
 *    file is designed to catch
 *
 * Parser edge cases: the extension, not the content, decides the grammar.
 */
interface Point { x: number; y: number }

type Pair = [number, number];

function add(a: number, b: number): number {
  return a + b;
}

const origin: Point = { x: 0, y: 0 };
const pair = [1, 2] as Pair;

console.log(add(origin.x, pair[1]));
