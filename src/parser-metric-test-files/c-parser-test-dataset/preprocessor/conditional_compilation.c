/*
 * Feature : Conditional compilation and #if expression evaluation
 * Version : C99 baseline; valid in C11/C17/C23
 * Spec    : C17 6.10.1 (conditional inclusion), 6.10.4 (#line),
 *           6.10.5 (#error), 6.10.6 (#pragma), 6.10.7 (null directive)
 *
 * A source-level parser that does NOT run the preprocessor sees ALL branches
 * at once - including branches that are not valid C. This file deliberately
 * includes disabled code that would not compile, to test whether the parser
 * evaluates or skips conditional blocks correctly.
 *
 * Parser edge cases:
 *  - `#if` arithmetic is done in intmax_t/uintmax_t; undefined identifiers
 *    evaluate to 0; `defined X` and `defined(X)` both valid.
 *  - Skipped groups only need valid preprocessing TOKENS, not valid C.
 *    (An unmatched ' or " even in a skipped group is undefined behaviour,
 *    so it is avoided here.)
 *  - Null directive: a line containing only `#`.
 *  - Unbalanced braces across #if/#else branches: the parser must pick one.
 *  - Character constants in #if: `#if 'A' == 65`.
 */
#include <stdio.h>

#define LEVEL 3
#define FEATURE_X
#undef  FEATURE_Y

#
/* ^ null directive - valid and does nothing */

#if LEVEL >= 3 && defined FEATURE_X && !defined(FEATURE_Y)
#  define MODE "full"
#elif LEVEL == 2
#  define MODE "partial"
#else
#  define MODE "minimal"
#endif

#if UNDEFINED_IDENTIFIER == 0
#  define UNDEF_IS_ZERO 1
#endif

#if (-1 > 0u)
#  define UNSIGNED_PROMOTION "yes: -1 converted to uintmax_t"
#else
#  define UNSIGNED_PROMOTION "no"
#endif

#if 'A' == 65
#  define ASCII_HOST 1
#else
#  define ASCII_HOST 0
#endif

#if (1 ? 2 : (1 / 0))
#  define SHORT_CIRCUIT "division by zero not evaluated"
#endif

/* Unbalanced braces split across branches: only one branch is real */
static int choose(int v)
#if LEVEL > 1
{
    return v * LEVEL;
#else
{
    return -v;
#endif
}

#if 0
    This is not C code at all. It is skipped by the preprocessor.
    int broken = ;
    struct { missing semicolon }
    #notadirective is fine inside a skipped group
#endif

#ifdef NEVER_DEFINED
#  error "this #error is inside a skipped group and must not fire"
#endif

#if defined(__STDC_VERSION__)
#  if __STDC_VERSION__ >= 202311L
#    define STD "C23"
#  elif __STDC_VERSION__ >= 201710L
#    define STD "C17"
#  elif __STDC_VERSION__ >= 201112L
#    define STD "C11"
#  else
#    define STD "C99"
#  endif
#else
#  define STD "C89/C90"
#endif

#pragma STDC FP_CONTRACT OFF
#define DO_PRAGMA(x) _Pragma(#x)
DO_PRAGMA(GCC diagnostic push)
DO_PRAGMA(GCC diagnostic pop)

int main(void)
{
    printf("MODE = %s\n", MODE);
    printf("UNDEF_IS_ZERO = %d\n", UNDEF_IS_ZERO);
    printf("-1 > 0u in #if: %s\n", UNSIGNED_PROMOTION);
    printf("ASCII_HOST = %d\n", ASCII_HOST);
    printf("SHORT_CIRCUIT: %s\n", SHORT_CIRCUIT);
    printf("choose(5) = %d\n", choose(5));
    printf("compiled as %s\n", STD);
#if LEVEL > 5
    printf("high level\n");
#elif LEVEL > 2
    printf("medium level (%d)\n", LEVEL);
#endif
    return 0;
}
