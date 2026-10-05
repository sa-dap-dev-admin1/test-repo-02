# go dataset statistics

Tokens are estimated as characters / 4. Cost assumes $3 per 1M output tokens.

## Per file

| File | Lines | Chars | ~Tokens |
|---|---:|---:|---:|
| `build-tags/build_constraints.go` | 53 | 1924 | 481 |
| `build-tags/excluded_by_ignore_tag.go` | 35 | 1233 | 308 |
| `build-tags/sysinfo_linux.go` | 44 | 1440 | 360 |
| `directives/compiler_directives.go` | 58 | 1889 | 472 |
| `go113/number_literals.go` | 70 | 2034 | 508 |
| `go114/overlapping_interfaces.go` | 87 | 2252 | 563 |
| `go116/embed_directive.go` | 54 | 1784 | 446 |
| `go117/slice_to_array_pointer.go` | 62 | 1899 | 474 |
| `go118/constraints_type_sets.go` | 112 | 3046 | 761 |
| `go118/generic_functions.go` | 118 | 3249 | 812 |
| `go118/generic_types_methods.go` | 116 | 2920 | 730 |
| `go118/inference_and_parse_ambiguities.go` | 74 | 2589 | 647 |
| `go119/doc_comments_atomic_types.go` | 78 | 2265 | 566 |
| `go120/slice_to_array_comparable.go` | 65 | 2014 | 503 |
| `go121/min_max_clear_inference.go` | 77 | 2533 | 633 |
| `go122/per_iteration_loop_variables.go` | 76 | 1941 | 485 |
| `go122/range_over_int.go` | 66 | 1686 | 421 |
| `go123/iter_package.go` | 83 | 2178 | 544 |
| `go123/range_over_func.go` | 132 | 2837 | 709 |
| `go124/generic_type_aliases.go` | 85 | 2289 | 572 |
| `go125/core_types_removed_spec.go` | 66 | 2183 | 545 |
| `go126/new_with_expression.go` | 72 | 2109 | 527 |
| `gomod-directive/generics_rejected_go117/go.mod` | 5 | 161 | 40 |
| `gomod-directive/generics_rejected_go117/main.go` | 38 | 1181 | 295 |
| `gomod-directive/legacy_loopvar_go121/go.mod` | 9 | 470 | 117 |
| `gomod-directive/legacy_loopvar_go121/main.go` | 35 | 1081 | 270 |
| `negative/range_over_int_with_go121_constraint.go` | 40 | 971 | 242 |

## Per version / track

| Folder | Files | Lines | Chars | ~Tokens |
|---|---:|---:|---:|---:|
| build-tags | 3 | 132 | 4597 | 1149 |
| directives | 1 | 58 | 1889 | 472 |
| go113 | 1 | 70 | 2034 | 508 |
| go114 | 1 | 87 | 2252 | 563 |
| go116 | 1 | 54 | 1784 | 446 |
| go117 | 1 | 62 | 1899 | 474 |
| go118 | 4 | 420 | 11804 | 2950 |
| go119 | 1 | 78 | 2265 | 566 |
| go120 | 1 | 65 | 2014 | 503 |
| go121 | 1 | 77 | 2533 | 633 |
| go122 | 2 | 142 | 3627 | 906 |
| go123 | 2 | 215 | 5015 | 1253 |
| go124 | 1 | 85 | 2289 | 572 |
| go125 | 1 | 66 | 2183 | 545 |
| go126 | 1 | 72 | 2109 | 527 |
| gomod-directive | 4 | 87 | 2893 | 722 |
| negative | 1 | 40 | 971 | 242 |

**Total: 27 files, 1810 lines, 52158 chars, ~13039 tokens, est. cost $0.0391**
