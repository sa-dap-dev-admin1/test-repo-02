# TypeScript parser test dataset: expected results

Strict mode = `tsc --strict --target es2022` with `--module commonjs` for `.ts`, `--jsx react` for `.tsx`, `--module nodenext` for `.mts`/`.cts`. A strict ECMAScript parser is a separate mode (see notes).

Validation: compiled with tsc 6.0.3 and the emitted JS RUN on Node 22, unless notes say otherwise. tsc 6 accepts all older syntax, so the Version column marks where a feature was introduced; older tsc releases will reject later folders.

Fill in the "Parser result" column when you run your parser. "Pass" means the parser accepts the file in the mode for its version. "Fail" means a correct parser must REJECT it with a useful error, not crash or silently mis-parse.

| File | Version | Expected (strict) | Parser result | Validated with / notes |
|---|---|---|---|---|
| `declaration-files/geometry-lib.d.ts` | .d.ts | Pass (types only) | | tsc 6.0.3 `--noEmit`. Any implementation code in a .d.ts must be rejected. |
| `declaration-files/globals-and-augmentation.d.ts` | .d.ts | Pass (types only) | | tsc 6.0.3 `--noEmit` |
| `module-extensions/cjs_entry.cts` | .mts/.cts | Pass (always CJS) | | tsc 6.0.3 nodenext + @types/node, ran. `import = require` / `export =`; top-level await would be an error. |
| `module-extensions/esm_entry.mts` | .mts/.cts | Pass (always ESM) | | tsc 6.0.3 nodenext + @types/node, ran. Top-level await and `import.meta` legal ONLY because of the `.mts` extension. |
| `negative/erasable_syntax_only.ts` | n/a | Pass (default) / Fail (`--erasableSyntaxOnly`) | | tsc: 5x TS1294; Node `--experimental-strip-types`: ERR_UNSUPPORTED_TYPESCRIPT_SYNTAX |
| `negative/type_annotations_in_js.js` | n/a | Fail | | tsc `--checkJs`: TS8006/8008/8010/8016; `node --check`: SyntaxError. A TS parser that ignores the `.js` extension and PASSES has a bug. |
| `ts3x/bigint_const_assertions_readonly.ts` | TS 3.0-3.9 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts3x/optional_chaining_nullish_assertions.ts` | TS 3.0-3.9 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts3x/type_only_imports_private_fields.ts` | TS 3.0-3.9 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts3x/unknown_and_tuple_rest.ts` | TS 3.0-3.9 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts40/labeled_tuples_logical_assignment.ts` | TS 4.0 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts40/variadic_tuple_types.ts` | TS 4.0 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts41/key_remapping_recursive_conditionals.ts` | TS 4.1 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts41/template_literal_types.ts` | TS 4.1 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts42/abstract_constructors_leading_rest.ts` | TS 4.2 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts43/override_static_index_signatures.ts` | TS 4.3/4.4 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts44/exact_optional_static_blocks.ts` | TS 4.4 | Pass | | tsc 6.0.3 `--exactOptionalPropertyTypes`, ran. Compile with `--exactOptionalPropertyTypes`. |
| `ts45/type_modifiers_on_imports_awaited.ts` | TS 4.5 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts47/variance_instantiation_extends_infer.ts` | TS 4.6-4.8 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts49/satisfies_accessor_in_narrowing.ts` | TS 4.9 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts50/const_type_params_enums_extends.ts` | TS 5.0 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts50/standard_decorators.ts` | TS 5.0 | Pass (TS) / Fail (strict JS) | | tsc 6.0.3, ran. Stage-3 decorators: not yet ECMAScript (matches the JS dataset proposals/ track). |
| `ts52/using_declarations.ts` | TS 5.2 | Pass | | tsc 6.0.3, ran. `DisposableStack` part self-skips on runtimes without the global (Node 22). |
| `ts53/import_attributes_switch_true.ts` | TS 5.3 | Pass | | tsc 6.0.3, ran. JSON import-attribute forms appear in comments (need `--module esnext`). |
| `ts54/noinfer_closure_narrowing.ts` | TS 5.4 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts55/inferred_type_predicates_regex.ts` | TS 5.5 | Pass | | tsc 6.0.3, ran on Node 22 |
| `ts56plus/late_5x_features.ts` | TS 5.6-5.9 | Pass | | tsc 6.0.3, ran on Node 22 |
| `tsx/tsx_parsing_ambiguities.tsx` | TSX | Pass (as TSX) / Fail (as .ts) | | tsc 6.0.3 `--jsx react`, ran |
| `tsx/typed_components.tsx` | TSX | Pass (as TSX) / Fail (as .ts) | | tsc 6.0.3 `--jsx react`, ran. No React: local `h()` factory via `@jsx` pragma. |
