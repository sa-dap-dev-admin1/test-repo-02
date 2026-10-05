/*
 * Feature : .mts - TypeScript source that is ALWAYS an ES module,
 *           top-level await, import.meta, `node:` specifiers with explicit
 *           .js-style extensions
 * Version : TypeScript 4.7 (module: node16 / nodenext, .mts/.cts)
 * Spec    : TS 4.7 release notes "ECMAScript Module Support in Node.js",
 *           "New File Extensions"
 *
 * Compile: tsc --module nodenext esm_entry.mts  -> emits esm_entry.mjs
 *
 * Parser edge cases:
 *  - The extension alone decides module format: `.mts` is ESM regardless of
 *    package.json "type". Top-level await is legal ONLY because this is ESM.
 *  - `import.meta.url` - meta-property, only valid in modules.
 *  - `require` is NOT defined; `import x = require()` is an error in .mts.
 *  - `export {}`-free: any import/export makes it a module anyway.
 *  - Type-only import of a .cts module from .mts uses `import type`.
 */
import { createHash } from "node:crypto";
import { fileURLToPath } from "node:url";
import { basename } from "node:path";

const fileName = basename(fileURLToPath(import.meta.url));

// Top-level await
const delayed = await new Promise<string>((resolve) => setTimeout(() => resolve("top-level await resolved"), 5));

const digest = createHash("sha256").update("parser").digest("hex").slice(0, 12);

const dynamic = await import("node:os");

export const summary = { fileName, delayed, digest, platformKnown: typeof dynamic.platform() === "string" };

console.log(`running as ESM: ${fileName}`);
console.log(delayed);
console.log(`sha256("parser") prefix = ${digest}`);
console.log(`dynamic import of node:os ok: ${summary.platformKnown}`);
console.log(`typeof require in ESM: ${typeof (globalThis as Record<string, unknown>).require}`);
