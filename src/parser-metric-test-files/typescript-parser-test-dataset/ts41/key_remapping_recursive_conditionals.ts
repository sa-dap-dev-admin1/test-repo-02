/*
 * Feature : Key remapping in mapped types (`as` clause), recursive
 *           conditional types, checked indexed access, `abstract`
 *           construct signatures preview (4.2 - see ts42)
 * Version : TypeScript 4.1
 * Spec    : TS 4.1 release notes "Key Remapping in Mapped Types",
 *           "Recursive Conditional Types", "--noUncheckedIndexedAccess"
 *
 * Parser edge cases:
 *  - `{ [K in keyof T as NewKey]: ... }` - `as` inside the brackets of a
 *    mapped type, after the `in` clause.
 *  - `as never` filters keys out; `as Exclude<K, "x">`.
 *  - Mapping modifiers: `-readonly [K in keyof T]-?: T[K]`, `+readonly`.
 *  - Deeply recursive conditional types (Awaited-like, DeepFlatten).
 */
export {};

type RemoveKind<T> = { [K in keyof T as Exclude<K, "kind">]: T[K] };
type OnlyFunctions<T> = { [K in keyof T as T[K] extends (...a: never[]) => unknown ? K : never]: T[K] };
type Setters<T> = { [K in keyof T as `set${Capitalize<string & K>}`]: (value: T[K]) => void };
type Mutable<T> = { -readonly [K in keyof T]-?: T[K] };
type Frozen<T> = { +readonly [K in keyof T]+?: T[K] };

type DeepFlatten<T> = T extends readonly (infer U)[] ? DeepFlatten<U> : T;
type DeepPartial<T> = T extends object ? { [K in keyof T]?: DeepPartial<T[K]> } : T;
type UnwrapPromise<T> = T extends PromiseLike<infer U> ? UnwrapPromise<U> : T;

interface Circle { readonly kind: "circle"; readonly radius: number; area(): number; label?: string }

function flatten<T extends readonly unknown[]>(arr: T): DeepFlatten<T>[] {
  return (arr as readonly unknown[]).flat(Infinity) as DeepFlatten<T>[];
}

function withSetters<T extends object>(target: T): T & Setters<T> {
  const out: Record<string, unknown> = { ...(target as Record<string, unknown>) };
  for (const key of Object.keys(target)) {
    out[`set${key[0].toUpperCase()}${key.slice(1)}`] = (v: unknown) => { out[key] = v; };
  }
  return out as T & Setters<T>;
}

const c: Circle = { kind: "circle", radius: 2, area() { return Math.PI * this.radius ** 2; } };
const noKind: RemoveKind<Circle> = { radius: 2, area: c.area };
const fns: OnlyFunctions<Circle> = { area: () => 1 };
const editable: Mutable<Circle> = { kind: "circle", radius: 1, area: () => 0, label: "now required" };
editable.radius = 5;                                  // readonly removed
const partial: Frozen<{ a: number }> = {};
const deep: DeepPartial<{ a: { b: { c: number } } }> = { a: { b: {} } };
const unwrapped: UnwrapPromise<Promise<Promise<string>>> = "deeply unwrapped";

const nums = flatten([1, [2, [3, [4, [5]]]]] as const);
console.log(`DeepFlatten: ${nums.join(",")}`);
const person = withSetters({ name: "ada", age: 36 });
person.setAge(37);
console.log(`key-remapped setters: ${person.name} ${person.age}`);
console.log(`remapped keys: ${Object.keys(noKind).join(",")}, functions only: ${Object.keys(fns)}`);
console.log(`mutable radius ${editable.radius}, frozen ${JSON.stringify(partial)}, deep ${JSON.stringify(deep)}, ${unwrapped}`);
