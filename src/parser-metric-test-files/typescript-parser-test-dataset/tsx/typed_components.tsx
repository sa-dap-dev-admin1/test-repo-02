/** @jsx h */
/** @jsxFrag Fragment */
/*
 * Feature : TSX - JSX in TypeScript files with typed props, generic
 *           components, custom JSX factory via pragma
 * Version : TypeScript 1.6+ (TSX); pragmas @jsx (2.8) / @jsxFrag (4.0)
 * Spec    : TypeScript handbook "JSX"; TS 4.0 release notes "Custom JSX
 *           Factories"
 *
 * Self-contained: NO React. A tiny `h()` factory renders elements to HTML
 * strings, and a global JSX namespace types the intrinsic elements.
 * Compile with: tsc --jsx react typed_components.tsx
 *
 * Parser edge cases:
 *  - `.tsx` changes the grammar: `<T>expr` angle-bracket assertions are
 *    ILLEGAL (they would start a JSX element); use `expr as T`.
 *  - Generic arrow functions need a disambiguator: `<T,>(x: T) => x` or
 *    `<T extends unknown>(x: T) => x`; plain `<T>(x: T) => x` is parsed as
 *    a JSX opening tag in .tsx.
 *  - Explicit type arguments on a JSX element: `<List<number> items=... />`.
 *  - Fragments `<>...</>`, spread attributes `{...props}`, namespaced
 *    attribute names `xlink:href`, hyphenated `data-id` / `aria-label`.
 *  - JSX text containing `>` or `}` must be escaped as `{">"}` / `{"}"}`.
 */
export {};

type Child = string | number | boolean | null | undefined | Child[];
type Props = Record<string, unknown> & { children?: Child[] };
type Component<P> = (props: P & { children?: Child[] }) => string;

declare global {
  namespace JSX {
    type Element = string;
    interface IntrinsicElements {
      div: { class?: string; id?: string; "data-id"?: string | number };
      span: { class?: string };
      ul: {}; li: { key?: string | number };
      button: { onclick?: string; disabled?: boolean; "aria-label"?: string };
      svg: { width: number; height: number };
      use: { "xlink:href": string };
      p: {}; strong: {}; h1: {};
    }
  }
}

function render(c: Child): string {
  if (Array.isArray(c)) return c.map(render).join("");
  if (c === null || c === undefined || c === false || c === true) return "";
  return String(c);
}

function h(tag: string | Component<Props>, props: Props | null, ...children: Child[]): string {
  if (typeof tag === "function") return tag({ ...(props ?? {}), children });
  const attrs = Object.entries(props ?? {})
    .filter(([, v]) => v !== false && v !== undefined)
    .map(([k, v]) => (v === true ? ` ${k}` : ` ${k}="${String(v)}"`))
    .join("");
  return `<${tag}${attrs}>${render(children)}</${tag}>`;
}
const Fragment = (props: { children?: Child[] }): string => render(props.children ?? []);

interface BadgeProps { label: string; tone?: "info" | "warn" }
const Badge = ({ label, tone = "info" }: BadgeProps) => <span class={`badge ${tone}`}>{label}</span>;

interface ListProps<T> { items: T[]; renderItem: (item: T) => string }
function List<T>({ items, renderItem }: ListProps<T>) {
  return <ul>{items.map((it, i) => <li key={i}>{renderItem(it)}</li>)}</ul>;
}

// Generic arrow in .tsx needs the trailing comma
const identity = <T,>(value: T): T => value;
const firstOf = <T extends unknown>(xs: T[]): T | undefined => xs[0];

function Card(props: { title: string; children?: Child[] }) {
  return (
    <div class="card" data-id={props.title.length}>
      <h1>{props.title}</h1>
      {props.children}
    </div>
  );
}

const buttonProps = { disabled: true, "aria-label": "close" };
const users = [{ name: "ada", admin: true }, { name: "bob", admin: false }];

const page = (
  <>
    <Card title="Users">
      <List<{ name: string; admin: boolean }> items={users} renderItem={(u) => (u.admin ? `${u.name} (admin)` : u.name)} />
      <Badge label="beta" />
      <Badge label="careful" tone="warn" />
    </Card>
    <button {...buttonProps}>x</button>
    <p>Arrow text needs escaping: a {">"} b and {"}"}</p>
    <svg width={10} height={10}><use xlink:href="#icon" /></svg>
    {users.length > 1 && <strong>{users.length} users</strong>}
  </>
);

const count = (users.length as number) + 0;          // `as` instead of <number>users.length
console.log(page);
console.log(`identity: ${identity("tsx")}, firstOf: ${firstOf([3, 2, 1])}, count: ${count}`);
