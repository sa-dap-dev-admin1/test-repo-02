/*
 * Feature : .cts - TypeScript source that is ALWAYS CommonJS,
 *           `import x = require("...")`, `export =`, `__filename`
 * Version : TypeScript 4.7 (module: node16 / nodenext); `import = require`
 *           and `export =` date from TS 1.x
 * Spec    : TS 4.7 release notes "New File Extensions"; handbook
 *           "Modules - export = and import = require()"
 *
 * Compile: tsc --module nodenext cjs_entry.cts  -> emits cjs_entry.cjs
 *
 * Parser edge cases:
 *  - `import fs = require("node:fs");` - TS-only import-equals syntax.
 *  - `export = value;` - replaces module.exports; cannot be combined with
 *    other `export` declarations in the same file.
 *  - Top-level `await` is an ERROR here (CommonJS), unlike the .mts file.
 *  - `import.meta` is an ERROR here.
 *  - `__filename` / `__dirname` / `require` are available.
 */
import path = require("node:path");
import os = require("node:os");

namespace Formatting {
  export function title(s: string): string { return s.replace(/\b\w/g, (c) => c.toUpperCase()); }
}

declare const __filename: string;

const info = {
  file: path.basename(__filename),
  sep: path.sep,
  cpus: os.cpus().length > 0,
  title: Formatting.title("common js entry point"),
};

console.log(`running as CommonJS: ${info.file}`);
console.log(`path.sep = ${info.sep}, has cpus: ${info.cpus}`);
console.log(info.title);

export = info;
