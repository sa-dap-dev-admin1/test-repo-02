/** @jsx h */
/*
 * Feature : TSX-specific parsing ambiguities between JSX, generics, type
 *           assertions, comparisons and arrow functions
 * Version : TypeScript 2.x+ (TSX), satisfies (4.9), generic JSX (2.9)
 * Spec    : TypeScript handbook "JSX" - "The as operator"; TS 2.9 release
 *           notes "Generic type arguments in JSX elements"
 *
 * Every construct below is valid .tsx, but each would be parsed
 * differently (or rejected) in a .ts file or by a naive JSX parser.
 *
 * Parser edge cases:
 *  - `a < b` comparisons where `b` is an identifier - in expression
 *    position after an operand, `<` is a less-than, not a tag.
 *  - `<div>` at the start of an expression IS a tag.
 *  - `x as unknown as T` chained assertions (angle form forbidden).
 *  - Arrow with a return type annotation containing `<`: `(): Array<number>
 *    => ...`.
 *  - Conditional rendering with ternaries nesting JSX on both branches.
 *  - Comments inside JSX: an empty expression container holding a block
 *    comment, as used in the `view` element below.
 *  - Entities and string literal attributes with quotes/braces.
 */
export {};

declare global {
  namespace JSX {
    type Element = string;
    interface IntrinsicElements { [tag: string]: Record<string, unknown> }
  }
}

function h(tag: string | ((p: Record<string, unknown>) => string), props: Record<string, unknown> | null, ...kids: unknown[]): string {
  if (typeof tag === "function") return tag({ ...(props ?? {}), children: kids });
  const attrs = Object.entries(props ?? {}).map(([k, v]) => ` ${k}="${String(v)}"`).join("");
  return `<${tag}${attrs}>${kids.flat(Infinity).filter((k) => k !== false && k != null).join("")}</${tag}>`;
}

const a = 1, b = 2, c = 3;
const lessThan = a < b;                               // comparison, not a tag
const chain = a < b && b < c;
const tagNext = <i>tag</i>;                           // a tag
const generic = <T,>(x: T): Array<T> => [x, x];       // generic arrow with return type
const twoParams = <K extends string, V,>(k: K, v: V): Record<K, V> => ({ [k]: v } as Record<K, V>);

const raw: unknown = "assert me";
const asserted = raw as unknown as string;            // angle-bracket form is illegal in .tsx
const conf = { mode: "dark" } satisfies { mode: string };

const Item = (p: Record<string, unknown>) => <li>{String(p.label)}</li>;
const loggedIn = true;

const view = (
  <section title='single "quoted" attr' data-json="{&quot;k&quot;:1}">
    {/* a JSX comment is an empty expression container */}
    {lessThan ? <b>a is less</b> : <em>a is not less</em>}
    {loggedIn ? (chain ? <Item label="chained" /> : null) : <Item label="guest" />}
    <ul>{generic("x").map((s, i) => <Item label={`${s}${i}`} />)}</ul>
    &copy; 2026 &mdash; {"{"}literal braces{"}"}
  </section>
);

console.log(view);
console.log(`${tagNext} ${asserted.length} ${conf.mode} ${JSON.stringify(twoParams("key", 42))}`);
