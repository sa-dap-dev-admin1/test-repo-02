/*
 * Feature : Explicit resource management - `using` and `await using`
 *           declarations, Symbol.dispose / Symbol.asyncDispose,
 *           DisposableStack; decorator metadata (5.2); named and anonymous
 *           tuple elements mixing (5.2); copying array methods (5.2 lib)
 * Version : TypeScript 5.2 (August 2023)
 * Spec    : TS 5.2 release notes "using Declarations and Explicit Resource
 *           Management"; TC39 proposal-explicit-resource-management (stage 3)
 *
 * Compile with lib "esnext.disposable" (or esnext); runs on Node 18+ via
 * the TypeScript downlevel helpers when target < esnext.
 *
 * Parser edge cases:
 *  - `using res = expr;` - `using` is a contextual keyword; `using` alone
 *    or `using[0]` / `using.x` / `using = 1` treat it as an identifier.
 *  - `await using x = ...;` - only inside async functions / modules.
 *  - `for (using x of xs)` and `for (await using x of xs)`.
 *  - `using` is NOT allowed with destructuring: `using { a } = obj` is a
 *    syntax error; and not at the top level of a CommonJS script... (it is
 *    allowed at module/function/block scope).
 *  - Mixed labeled/unlabeled tuple elements became legal in 5.2:
 *    `[first: string, number]`.
 */
export {};

class TempFile implements Disposable {
  constructor(public path: string) { console.log(`  open ${path}`); }
  write(data: string): void { console.log(`  write "${data}" to ${this.path}`); }
  [Symbol.dispose](): void { console.log(`  dispose ${this.path}`); }
}

class Connection implements AsyncDisposable {
  constructor(public host: string) { console.log(`  connect ${host}`); }
  async query(sql: string): Promise<number> { return sql.length; }
  async [Symbol.asyncDispose](): Promise<void> {
    await new Promise((r) => setTimeout(r, 1));
    console.log(`  disconnect ${this.host}`);
  }
}

type Mixed = [label: string, number];                 // 5.2: mixing labeled and unlabeled

function syncDemo(): void {
  using a = new TempFile("/tmp/a");
  using b = new TempFile("/tmp/b");
  a.write("hello");
  b.write("world");
  console.log("  leaving scope - disposal in reverse order:");
}

function stackDemo(): void {
  if (typeof DisposableStack === "undefined") {      // runtime global arrived after Node 22
    console.log("  (DisposableStack not available in this runtime - skipped)");
    return;
  }
  using stack = new DisposableStack();
  const f = stack.use(new TempFile("/tmp/stacked"));
  stack.defer(() => console.log("  deferred callback"));
  f.write("via DisposableStack");
}

async function asyncDemo(): Promise<void> {
  await using conn = new Connection("db.local");
  console.log(`  query returned ${await conn.query("SELECT 1")}`);
}

function loopDemo(): void {
  for (using f of [new TempFile("/tmp/loop1"), new TempFile("/tmp/loop2")]) {
    f.write("loop");
  }
}

async function main() {
  console.log("sync using:"); syncDemo();
  console.log("DisposableStack:"); stackDemo();
  console.log("await using:"); await asyncDemo();
  console.log("for (using ...):"); loopDemo();

  const using = [1, 2, 3];                          // `using` as an ordinary identifier
  console.log(`identifier named using: ${using[0]}`);

  const m: Mixed = ["count", 3];
  const sorted = [3, 1, 2].toSorted();               // copying array methods (lib es2023)
  console.log(`mixed tuple ${m.join("=")}, toSorted ${sorted}, with ${[1, 2, 3].with(1, 9)}`);
}
main();
