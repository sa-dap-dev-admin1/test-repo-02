/*
 * Feature : Global declarations, module declarations (ambient modules),
 *           wildcard modules, module augmentation, `declare global`,
 *           triple-slash directives, interface merging with lib types
 * Version : TypeScript 2.0+ (wildcard modules 2.0, declare global 1.8)
 * Spec    : TypeScript handbook "Declaration Merging", "Modules -
 *           Ambient Modules", "Global augmentation"
 *
 * Types only. Accept with: tsc --noEmit globals-and-augmentation.d.ts
 *
 * Parser edge cases:
 *  - `/// <reference lib="es2022" />` - triple-slash directive must appear
 *    before any statement; it's a COMMENT with semantic meaning.
 *  - `declare module "name" { ... }` - a STRING-named module block.
 *  - `declare module "*.svg" { ... }` - wildcard pattern in the name.
 *  - `declare module "./local"` shorthand without a body:
 *    `declare module "untyped-lib";` - everything is `any`.
 *  - `declare global { interface Array<T> { ... } }` - augmenting a lib
 *    interface (only legal inside a module, hence the `export {}`).
 *  - `declare var`, `declare let`, `declare const` - ambient variables.
 *  - `interface Window` merges with the DOM lib declaration.
 */
/// <reference lib="es2022" />

export {};

declare global {
  const __BUILD_ID__: string;
  var __DEV__: boolean;
  let featureFlags: Record<string, boolean>;

  interface Array<T> {
    groupInto<K extends PropertyKey>(key: (item: T) => K): Record<K, T[]>;
  }

  interface Window {
    analytics?: { track(event: string, props?: Record<string, unknown>): void };
  }

  namespace NodeJS {
    interface ProcessEnv { readonly APP_MODE?: "dev" | "prod" }
  }

  function structuredLog(level: "info" | "warn", ...parts: unknown[]): void;
}

declare module "*.svg" {
  const content: string;
  export default content;
}

declare module "*.module.css" {
  const classes: { readonly [className: string]: string };
  export default classes;
}

declare module "untyped-legacy-lib";                  // shorthand ambient module: all `any`

declare module "config-store" {
  export interface StoreOptions { path?: string; encrypt?: boolean }
  export default class Store<T extends object = Record<string, unknown>> {
    constructor(options?: StoreOptions);
    get<K extends keyof T>(key: K): T[K] | undefined;
    set<K extends keyof T>(key: K, value: T[K]): void;
    readonly size: number;
  }
  export function migrate(from: number, to: number): Promise<void>;
}

// Augment the ambient module declared above
declare module "config-store" {
  export function reset(): void;
}
