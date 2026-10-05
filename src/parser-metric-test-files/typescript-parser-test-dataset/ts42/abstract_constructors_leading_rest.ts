/*
 * Feature : Abstract construct signatures, leading/middle rest elements in
 *           tuple types, `--noPropertyAccessFromIndexSignature`, smarter
 *           type alias preservation
 * Version : TypeScript 4.2 (February 2021)
 * Spec    : TS 4.2 release notes "abstract Construct Signatures",
 *           "Leading/Middle Rest Elements in Tuple Types"
 *
 * Parser edge cases:
 *  - `abstract new () => T` - `abstract` modifier on a constructor TYPE.
 *  - `[...string[], number]` - rest element FIRST; `[boolean, ...string[],
 *    number]` - rest in the middle. Only one rest element allowed, and no
 *    optional element may follow it.
 */
export {};

abstract class Shape {
  abstract area(): number;
  describe(): string { return `${this.constructor.name} with area ${this.area().toFixed(2)}`; }
}
class Circle extends Shape { constructor(public r = 1) { super(); } area() { return Math.PI * this.r ** 2; } }
class Square extends Shape { constructor(public s = 1) { super(); } area() { return this.s ** 2; } }

type AbstractCtor<T> = abstract new (...args: never[]) => T;
// Mixins require exactly `...args: any[]` (TS2545)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type MixinBase = abstract new (...args: any[]) => object;

// Mixin that accepts ABSTRACT base classes
function Timestamped<TBase extends MixinBase>(Base: TBase) {
  abstract class WithTime extends Base {
    createdAt = new Date(0).toISOString();
  }
  return WithTime;
}

const TimedShapeBase = Timestamped(Shape);
class TimedTriangle extends TimedShapeBase {
  area(): number { return 0.5 * 3 * 4; }
}

function countSubclasses(ctors: AbstractCtor<Shape>[]): number { return ctors.length; }

// Leading and middle rest elements
type Trailing = [...names: string[], total: number];
type Middle = [first: boolean, ...middle: string[], last: number];

function sumLast(...args: Trailing): string {
  const total = args[args.length - 1] as number;
  const names = args.slice(0, -1) as string[];
  return `${names.join("+")} = ${total}`;
}

function framed(...args: Middle): string {
  const [first, ...rest] = args;
  const last = rest.pop() as number;
  return `${first} | ${(rest as string[]).join(" ")} | ${last}`;
}

for (const s of [new Circle(1), new Square(2), new TimedTriangle()]) console.log(s.describe());
console.log(`mixin field: ${new TimedTriangle().createdAt}`);
console.log(`abstract ctor array accepts abstract class: ${countSubclasses([Shape, Circle, Square])}`);
console.log(sumLast("a", "b", "c", 6));
console.log(sumLast(0));
console.log(framed(true, "x", "y", "z", 9));
