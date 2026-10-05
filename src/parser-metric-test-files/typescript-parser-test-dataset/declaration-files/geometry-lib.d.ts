/*
 * Feature : Declaration file (.d.ts) for a UMD library - ambient
 *           declarations, overloads, namespace merging, generics,
 *           `export as namespace`, abstract classes, accessors
 * Version : TypeScript 2.0+ (UMD globals 2.0, getters in .d.ts 3.6/3.7,
 *           accessor declarations with differing types 4.3)
 * Spec    : TypeScript handbook "Declaration Files" - "Library Structures",
 *           "Templates: module.d.ts / global-modifying-module.d.ts"
 *
 * Types only - no runtime code. Accept with: tsc --noEmit geometry-lib.d.ts
 *
 * Parser edge cases:
 *  - A .d.ts file may NOT contain implementations: function bodies,
 *    initializers on non-const declarations, or statements are errors.
 *  - `export as namespace Geometry;` - UMD global name.
 *  - `declare` is implicit for top-level declarations in a .d.ts module
 *    but required in a .d.ts SCRIPT (no import/export).
 *  - Function overloads with no bodies, followed by a namespace of the
 *    same name (declaration merging).
 *  - `get`/`set` accessor signatures in classes and interfaces.
 *  - `unique symbol`, `asserts`, `this is T` return types.
 */
export as namespace Geometry;

export interface Point { readonly x: number; readonly y: number }

export type Shape =
  | { kind: "circle"; center: Point; radius: number }
  | { kind: "rect"; topLeft: Point; width: number; height: number }
  | { kind: "polygon"; points: readonly Point[] };

export declare const VERSION: string;
export declare const ORIGIN: Point;
export declare const shapeTag: unique symbol;

export declare function area(shape: Shape): number;

export declare function distance(a: Point, b: Point): number;
export declare function distance(points: readonly Point[]): number;
export declare function distance(x1: number, y1: number, x2: number, y2: number): number;

export declare function assertPoint(value: unknown): asserts value is Point;

export declare namespace distance {                   // merges with the overloaded function
  const metric: "euclidean" | "manhattan";
  function squared(a: Point, b: Point): number;
}

export declare abstract class Figure<TMeta = Record<string, unknown>> {
  protected constructor(name: string);
  readonly name: string;
  meta: TMeta;
  abstract area(): number;
  get label(): string;
  set label(value: string | number);
  isClosed(): this is ClosedFigure;
  static compare(a: Figure, b: Figure): -1 | 0 | 1;
  [shapeTag]: true;
}

export interface ClosedFigure extends Figure { perimeter(): number }

export declare class Circle extends Figure<{ color?: string }> implements ClosedFigure {
  constructor(center: Point, radius: number);
  readonly center: Point;
  radius: number;
  area(): number;
  perimeter(): number;
}

export interface Transform {
  (p: Point): Point;                                  // call signature
  new (matrix: readonly number[]): Transform;         // construct signature
  readonly matrix: readonly number[];
  compose(other: Transform): Transform;
}

export declare enum Units { Pixels = "px", Millimeters = "mm", Inches = "in" }

export declare function on<E extends "resize" | "rotate">(event: E, cb: (e: E extends "resize" ? { w: number; h: number } : { degrees: number }) => void): () => void;

export default Figure;
