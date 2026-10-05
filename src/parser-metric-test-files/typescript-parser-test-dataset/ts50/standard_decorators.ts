/*
 * Feature : ECMAScript (TC39 stage 3) decorators - class, method, field,
 *           accessor, getter decorators, decorator context objects,
 *           addInitializer, decorator factories
 * Version : TypeScript 5.0 (March 2023) - without --experimentalDecorators
 * Spec    : TS 5.0 release notes "Decorators"; TC39 proposal-decorators
 *           (stage 3, 2022-03 semantics)
 *
 * Parser edge cases:
 *  - `@expr` before `class`, before members, and before `export`/`export
 *    default` (TS 5.0 allows decorators either before or after `export`).
 *  - Decorator expression grammar is restricted: `@a.b.c`, `@a()`, `@(expr)`
 *    are valid; `@a[0]` or `@a.b()()` are NOT (need parentheses).
 *  - `@dec accessor x` - decorator on an auto-accessor.
 *  - Decorators on PARAMETERS are NOT part of the standard (only legacy
 *    --experimentalDecorators) - see negative/ for the legacy form.
 *  - These decorators are a STRICT JS parser failure until ES adopts them
 *    (matches the JavaScript dataset's proposals/decorators_stage3.js).
 */
export {};

const log: string[] = [];

function logged<This, Args extends unknown[], Return>(
  target: (this: This, ...args: Args) => Return,
  context: ClassMethodDecoratorContext<This, (this: This, ...args: Args) => Return>,
) {
  const name = String(context.name);
  return function (this: This, ...args: Args): Return {
    log.push(`-> ${name}(${args.join(", ")})`);
    const result = target.call(this, ...args);
    log.push(`<- ${name} = ${String(result)}`);
    return result;
  };
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function bound<This, T extends (this: This, ...a: any[]) => any>(target: T, context: ClassMethodDecoratorContext<This, T>) {
  context.addInitializer(function (this: This) {
    const self = this as Record<string | symbol, unknown>;
    self[context.name] = (target as unknown as Function).bind(this);
  });
}

function defaultTo<T>(fallback: T) {
  return function <This>(_value: undefined, _context: ClassFieldDecoratorContext<This, T | undefined>) {
    return (initial: T | undefined) => initial ?? fallback;
  };
}

function clamp(min: number, max: number) {
  return function <This>(target: ClassAccessorDecoratorTarget<This, number>, _ctx: ClassAccessorDecoratorContext<This, number>): ClassAccessorDecoratorResult<This, number> {
    return {
      set(value: number) { target.set.call(this, Math.min(max, Math.max(min, value))); },
      get() { return target.get.call(this); },
    };
  };
}

function sealed<T extends abstract new (...args: never[]) => object>(cls: T, context: ClassDecoratorContext<T>) {
  log.push(`sealed class ${String(context.name)}`);
  Object.seal(cls);
  Object.seal(cls.prototype);
}

const registry = { tag: (name: string) => (cls: Function, _c: ClassDecoratorContext) => { log.push(`registered ${name} as ${cls.name}`); } };

@sealed
@registry.tag("thermostat")
class Thermostat {
  @defaultTo(20) target: number | undefined = undefined;

  @clamp(10, 30) accessor setpoint = 20;

  constructor(public name: string) {}

  @logged
  adjust(delta: number): number {
    this.setpoint = this.setpoint + delta;
    return this.setpoint;
  }

  @bound
  describe(): string { return `${this.name} at ${this.setpoint}C (target ${this.target})`; }
}

const t = new Thermostat("hall");
t.adjust(5);
t.adjust(50);                                        // clamped to 30
const detached = t.describe;                          // works because @bound
console.log(detached());
for (const line of log) console.log(line);
