# Go parser test dataset: expected results

Strict mode = `go vet` + `go build` with Go language version = the Version column (standalone files default to the toolchain version).

Validation: `go vet` + `go run` with Go 1.24.13, files `gofmt`-clean, unless notes say otherwise.

Fill in the "Parser result" column when you run your parser. "Pass" means the parser accepts the file in the mode for its version. "Fail" means a correct parser must REJECT it with a useful error, not crash or silently mis-parse.

| File | Version | Expected (strict) | Parser result | Validated with / notes |
|---|---|---|---|---|
| `build-tags/build_constraints.go` | Go 1.17+ | Pass | | Go 1.24 vet+run. `//go:build` + legacy `// +build` lines. |
| `build-tags/excluded_by_ignore_tag.go` | Go 1.17+ | Pass (syntax) / Excluded from package builds | | Go 1.24 vet + explicit run |
| `build-tags/sysinfo_linux.go` | Go 1.17+ | Pass (linux) / Excluded (other GOOS) | | Go 1.24 vet+run on linux. Constraint comes from the file name only. |
| `directives/compiler_directives.go` | Go 1.x | Pass | | Go 1.24 vet+run. `//line` changes reported positions for following lines. |
| `go113/number_literals.go` | Go 1.13 | Pass | | Go 1.24 vet+run |
| `go114/overlapping_interfaces.go` | Go 1.14 | Pass | | Go 1.24 vet+run |
| `go116/embed_directive.go` | Go 1.16 | Pass | | Go 1.24 vet+run. Embeds itself; run from its own directory. |
| `go117/slice_to_array_pointer.go` | Go 1.17 | Pass | | Go 1.24 vet+run |
| `go118/constraints_type_sets.go` | Go 1.18 | Pass | | Go 1.24 vet+run |
| `go118/generic_functions.go` | Go 1.18 | Pass | | Go 1.24 vet+run |
| `go118/generic_types_methods.go` | Go 1.18 | Pass | | Go 1.24 vet+run |
| `go118/inference_and_parse_ambiguities.go` | Go 1.18 | Pass | | Go 1.24 vet+run |
| `go119/doc_comments_atomic_types.go` | Go 1.19 | Pass | | Go 1.24 vet+run |
| `go120/slice_to_array_comparable.go` | Go 1.20 | Pass | | Go 1.24 vet+run |
| `go121/min_max_clear_inference.go` | Go 1.21 | Pass | | Go 1.24 vet+run |
| `go122/per_iteration_loop_variables.go` | Go 1.22 | Pass | | Go 1.24 vet+run |
| `go122/range_over_int.go` | Go 1.22 | Pass | | Go 1.24 vet+run |
| `go123/iter_package.go` | Go 1.23 | Pass | | Go 1.24 vet+run |
| `go123/range_over_func.go` | Go 1.23 | Pass | | Go 1.24 vet+run |
| `go124/generic_type_aliases.go` | Go 1.24 | Pass | | Go 1.24 vet+run |
| `go125/core_types_removed_spec.go` | Go 1.25 | Pass | | Go 1.24 vet+run. No syntax change in 1.25; `WaitGroup.Go` usage is commented out. |
| `go126/new_with_expression.go` | Go 1.26 | Pass (1.26) / Fail (<=1.25) | | UNVERIFIED - Go 1.24 rejects as expected. Go 1.24: "invalid recursive type" and `new(expr)` errors. Verify on Go 1.26. |
| `gomod-directive/generics_rejected_go117/go.mod` | go.mod | Pass | | Go 1.24. Sets language go1.17. |
| `gomod-directive/generics_rejected_go117/main.go` | go.mod | Fail (version-aware) / Pass (grammar-only) | | Go 1.24 vet: "type parameter requires go1.18". A per-file `//go:build go1.17` does NOT work: per-file versions are raised to at least go1.21. |
| `gomod-directive/legacy_loopvar_go121/go.mod` | go.mod | Pass | | Go 1.24. Sets language go1.21 for the module. |
| `gomod-directive/legacy_loopvar_go121/main.go` | go.mod | Pass (output `3 3 3`) | | Go 1.24 `go run .`. Same code prints `0 1 2` under go1.22+. |
| `negative/range_over_int_with_go121_constraint.go` | go1.21 file | Fail (version-aware) / Pass (grammar-only) | | Go 1.24 vet: "requires go1.22 or later". Per-file language version from `//go:build go1.21`. |
