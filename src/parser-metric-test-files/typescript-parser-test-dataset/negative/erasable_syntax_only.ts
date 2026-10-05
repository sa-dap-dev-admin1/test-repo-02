/*
 * Feature : NEGATIVE TEST - non-erasable TypeScript syntax under
 *           --erasableSyntaxOnly
 * Version : TypeScript 5.8 (February 2025) flag; matches Node.js 22.6+/23
 *           built-in type stripping (--experimental-strip-types), which
 *           cannot handle syntax that needs code generation.
 * Spec    : TS 5.8 release notes "The --erasableSyntaxOnly Option";
 *           Node.js docs "Modules: TypeScript - Type stripping"
 *
 * EXPECTED:
 *  - tsc (default)                         -> PASS
 *  - tsc --erasableSyntaxOnly              -> FAIL (TS1294 on each marked line)
 *  - node --experimental-strip-types       -> FAIL (ERR_UNSUPPORTED_TYPESCRIPT_SYNTAX)
 *  - a "type-stripping" parser mode         -> should reject marked lines
 *
 * Parser edge cases: these constructs have RUNTIME semantics, so removing
 * the type syntax with whitespace does not yield valid JavaScript:
 *  - `enum` (non-const)                      [marked]
 *  - `namespace` with values                 [marked]
 *  - constructor parameter properties        [marked]
 *  - `import x = require(...)`               (not used: needs CommonJS)
 *  - `<T>expr` angle-bracket assertions ARE erasable and stay allowed.
 */
export {};

enum Direction { Up, Down }                            // [marked] enum

namespace Utils {                                      // [marked] instantiated namespace
  export const pi = 3.14159;
}

class Account {
  constructor(public owner: string, private balance = 0) {}   // [marked] parameter properties
  deposit(n: number): number { return (this.balance += n); }
}

const value = <number>(Utils.pi * 2);                  // angle-bracket assertion: erasable
const acct = new Account("ada");
console.log(`${Direction[Direction.Down]} ${value.toFixed(2)} ${acct.owner} ${acct.deposit(10)}`);
