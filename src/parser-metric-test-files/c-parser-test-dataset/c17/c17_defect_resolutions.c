/*
 * Feature : C17 - defect-report resolutions (no new syntax)
 * Version : C17 / C18 (ISO/IEC 9899:2018), __STDC_VERSION__ == 201710L
 * Spec    : N2176 (C17 final draft); DR 400-500 series. Key clarifications:
 *           DR 476 (volatile semantics), DR 481 (_Generic and lvalue
 *           conversion), DR 494 (ATOMIC_VAR_INIT), DR 467 (FLT_MAX_10_EXP)
 *
 * C17 introduced NO new language features. It folds technical corrigenda
 * into C11. A parser that accepts C11 must accept every C17 program. This
 * file exists so the "c17" row in the results table is not empty, and to
 * pin down behaviours that were AMBIGUOUS in C11 and fixed in C17:
 *
 *  - DR 481: the controlling expression of _Generic undergoes lvalue
 *    conversion, so qualifiers are dropped and arrays decay.
 *  - DR 476: an access through a volatile-qualified lvalue is a volatile
 *    access even if the object itself isn't volatile.
 *  - ATOMIC_VAR_INIT became obsolescent (removed in C23).
 *  - __STDC_VERSION__ is 201710L (a parser/preprocessor may expose this).
 *
 * Parser edge cases:
 *  - The C17 value must be accepted in `#if __STDC_VERSION__ >= 201710L`.
 *  - _Generic on `const int` matches `int:` (not a separate association).
 *  - `register int a[3];` is valid but `a[0]` on it is a constraint violation.
 */
#include <stdio.h>
#include <stdatomic.h>

#if defined(__STDC_VERSION__) && __STDC_VERSION__ >= 201710L
#  define STD_LABEL "C17 or later"
#elif defined(__STDC_VERSION__) && __STDC_VERSION__ >= 201112L
#  define STD_LABEL "C11"
#else
#  define STD_LABEL "pre-C11"
#endif

#define QUAL_TEST(x) _Generic((x),          \
    int: "int (qualifiers dropped - DR 481)", \
    const int: "const int (pre-DR 481 behaviour)", \
    default: "other")

#define DECAY_TEST(x) _Generic((x), char *: "char * (array decayed)", \
                                    default: "not decayed")

/* DR 476 - volatile access through a cast */
static int poll_value(int *p)
{
    return *(volatile int *)p;
}

static atomic_int legacy_init = ATOMIC_VAR_INIT(5);   /* obsolescent in C17 */
static atomic_int modern_init = 7;                    /* preferred */

struct sensor { const int id; volatile int reading; };

int main(void)
{
    printf("__STDC_VERSION__ = %ldL -> %s\n", (long)__STDC_VERSION__, STD_LABEL);

    const int ci = 3;
    char arr[4] = "abc";
    printf("_Generic(const int) -> %s\n", QUAL_TEST(ci));
    printf("_Generic(char[4])   -> %s\n", DECAY_TEST(arr));

    int raw = 99;
    printf("volatile access via cast = %d\n", poll_value(&raw));

    printf("atomics: legacy=%d modern=%d\n",
           atomic_load(&legacy_init), atomic_load(&modern_init));

    struct sensor s = { .id = 1, .reading = 250 };
    s.reading += 5;
    printf("sensor %d reading %d\n", s.id, s.reading);

    /* register objects: declaring a register ARRAY is legal, but subscripting
     * it is not (subscript needs the array's address) - constraint 6.5.2.1. */
    register int r1 = 1, r2 = 2;
    register int regs[3] = { 1, 2, 3 };
    (void)sizeof regs;               /* sizeof does not need the address */
    printf("register sum = %d, sizeof regs = %zu\n", r1 + r2, sizeof regs);

    /* Everything C11 offers still works in C17 */
    _Static_assert(sizeof(int) >= 2, "C11 static assertion in C17");
    _Alignas(16) double v[2] = { 0.5, 1.5 };
    printf("aligned v = %.1f %.1f, alignof(double) = %zu\n",
           v[0], v[1], _Alignof(double));
    return 0;
}
