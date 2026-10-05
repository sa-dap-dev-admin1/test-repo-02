# C parser test dataset: expected results

Strict mode = an ISO C parser for the standard named in the Version column, equivalent to `gcc -std=cXX -pedantic-errors`. GNU mode = `-std=gnu17`.

Validation: every Pass row was compiled AND run with GCC 13.3 (C99-C17), GCC 14.2 (`-std=c23`) and cross-checked with Clang 21.1 (`-pedantic-errors`), unless the notes say otherwise. Header files were checked with `-fsyntax-only -x c`.

Fill in the "Parser result" column when you run your parser. "Pass" means the parser accepts the file in the mode for its version. "Fail" means a correct parser must REJECT it with a useful error, not crash or silently mis-parse.

| File | Version | Expected (strict) | Parser result | Validated with / notes |
|---|---|---|---|---|
| `c11/alignas_alignof.c` | C11 | Pass | | GCC+Clang, ran |
| `c11/anonymous_structs_unions.c` | C11 | Pass | | GCC+Clang, ran |
| `c11/atomics_threads.c` | C11 | Pass | | GCC+Clang, ran. Needs `<threads.h>` (glibc 2.28+); link with `-lpthread`. `__STDC_NO_THREADS__` implementations may lack it. |
| `c11/generic_selection.c` | C11 | Pass | | GCC+Clang, ran. Link with `-lm`. Shows that commas inside compound-literal macro arguments need extra parentheses. |
| `c11/static_assert_noreturn.c` | C11 | Pass | | GCC+Clang, ran |
| `c11/unicode_literals.c` | C11 | Pass | | GCC+Clang, ran |
| `c17/c17_defect_resolutions.c` | C17 | Pass | | GCC+Clang. Clang `-pedantic-errors` reports deprecated `ATOMIC_VAR_INIT` (intentional, obsolescent in C17). |
| `c23/bitint_checked_arith_stdbit.c` | C23 | Pass | | GCC+Clang, ran |
| `c23/bool_and_new_keywords.c` | C23 | Pass | | GCC+Clang, ran |
| `c23/constexpr_objects.c` | C23 | Pass | | GCC+Clang, ran |
| `c23/digit_separators_binary_literals.c` | C23 | Pass | | GCC+Clang, ran |
| `c23/embed_directive.c` | C23 | Pass | | Clang 21 only, ran. `#embed` needs GCC 15+ / Clang 19+. GCC 14 rejects it. C17 mode: Fail. |
| `c23/enum_underlying_type.c` | C23 | Pass | | GCC+Clang, ran |
| `c23/misc_syntax_changes.c` | C23 | Pass | | GCC+Clang, ran |
| `c23/nullptr_constant.c` | C23 | Pass | | GCC+Clang, ran |
| `c23/standard_attributes.c` | C23 | Pass | | GCC+Clang, ran |
| `c23/typeof_and_auto.c` | C23 | Pass | | GCC+Clang, ran |
| `c99/compound_literals.c` | C99 | Pass | | GCC+Clang, ran |
| `c99/designated_initializers.c` | C99 | Pass | | GCC+Clang, ran |
| `c99/flexible_array_members.c` | C99 | Pass | | GCC+Clang, ran |
| `c99/inline_restrict_bool.c` | C99 | Pass | | GCC+Clang, ran |
| `c99/lexical_and_declaration_changes.c` | C99 | Pass | | GCC+Clang. Clang `-pedantic-errors` turns its `-Wcomment` warning (intentional multi-line `//` comment) into an error; valid ISO C99. |
| `c99/variable_length_arrays.c` | C99 | Pass | | GCC+Clang, ran |
| `gnu-extensions/attributes_asm_builtins.c` | GNU C | Fail (grammar-only ISO) / Pass (GNU) | | GCC gnu17 ran. `gcc -std=c17 -pedantic-errors` ACCEPTS this (double-underscore names are implementation-reserved). Decide which behaviour you treat as correct. |
| `gnu-extensions/misc_gnu_extensions.c` | GNU C | Fail (strict) / Pass (GCC GNU) | | GCC gnu17 ran; c17 rejects. Clang rejects even in GNU mode (no nested functions). |
| `gnu-extensions/statement_exprs_and_typeof.c` | GNU C | Fail (strict) / Pass (GNU) | | GCC gnu17 ran; c17 rejects |
| `headers/geometry.h` | C99+ | Pass | | GCC C99-C23 + G++ C++17, syntax-only. Also valid C++ (`extern "C"` guard). Tests `.h` C-vs-C++ ambiguity. |
| `headers/platform_config.h` | C99+ | Pass | | GCC C99-C23, syntax-only |
| `headers/typed_ring_buffer.h` | C99+ | Pass (C11+) | | GCC C11-C23, syntax-only. Fails C99 (`_Alignof`). `#pragma once` is non-standard but universally accepted. |
| `preprocessor/conditional_compilation.c` | C99+ | Pass | | GCC+Clang, ran |
| `preprocessor/macro_edge_cases.c` | C99+ | Pass | | GCC+Clang, ran |
| `preprocessor/x_macros_and_generic_dispatch.c` | C99+ | Pass | | GCC+Clang, ran. Requires C11 (`_Generic`). |
