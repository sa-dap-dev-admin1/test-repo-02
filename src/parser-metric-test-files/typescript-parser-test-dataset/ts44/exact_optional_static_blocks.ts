/*
 * Feature : exactOptionalPropertyTypes, class static blocks, `#private in`
 *           brand checks preview, `useUnknownInCatchVariables`
 * Version : TypeScript 4.4 (August 2021); #x in obj brand checks are 4.5
 * Spec    : TS 4.4 release notes "Exact Optional Property Types",
 *           "static Blocks in Classes", "--useUnknownInCatchVariables"
 *
 * Compile with: --strict --exactOptionalPropertyTypes
 *
 * Parser edge cases:
 *  - Multiple `static { }` blocks, interleaved with static fields; they run
 *    in textual order and may reference private static members.
 *  - `static` followed by `{` (block) vs `static {` method named... no:
 *    `static async *gen() {}` vs `static {}` vs a property named `static`.
 *  - Under exactOptionalPropertyTypes, `prop?: T` rejects an explicit
 *    `undefined` - a type-level, not syntax-level, change; included so the
 *    flag combination is covered.
 */
export {};

interface Options {
  color?: string;                                    // may be MISSING, but not `undefined`
  size?: number | undefined;                         // explicitly allows undefined
}

function render(opts: Options): string {
  return `color=${"color" in opts ? opts.color : "(missing)"} size=${opts.size ?? "(none)"}`;
}

class Config {
  static #registry = new Map<string, number>();
  static defaults: Record<string, number> = {};
  static readonly order: string[] = [];

  static {
    Config.order.push("block 1");
    Config.#registry.set("timeout", 30);
  }

  static readonly computed = Config.#registry.get("timeout")! * 2;

  static {
    Config.order.push("block 2");
    try {
      Config.defaults = JSON.parse('{"retries": 3}');
    } catch (err) {                                   // err: unknown under --strict
      if (err instanceof Error) Config.order.push(err.message);
    }
  }

  static lookup(key: string): number | undefined { return Config.#registry.get(key); }

  static: string = "a property literally named static";
  static async *ticks(n: number) { for (let i = 0; i < n; i++) yield i; }
}

async function main() {
  console.log(render({ color: "red" }));
  console.log(render({ size: undefined }));
  console.log(render({}));
  // render({ color: undefined });   // error under --exactOptionalPropertyTypes

  console.log(`static blocks ran in order: ${Config.order.join(" -> ")}`);
  console.log(`computed between blocks: ${Config.computed}, defaults: ${JSON.stringify(Config.defaults)}`);
  console.log(`private static via static method: ${Config.lookup("timeout")}`);
  console.log(new Config().static);
  const seen: number[] = [];
  for await (const t of Config.ticks(3)) seen.push(t);
  console.log(`static async generator: ${seen.join(",")}`);
}
main();
