/*
 * Feature : GNU statement expressions ({ ... }), __typeof__, __auto_type,
 *           local labels (__label__)
 * Version : GNU C extension (gnu99/gnu11/gnu17) - NOT ISO C
 * Spec    : GCC manual "Statement Exprs", "Typeof", "Local Labels"
 *
 * EXPECTED: strict ISO C parser -> FAIL (reject with a clear error).
 *           GNU-mode parser (gcc -std=gnu17, clang) -> PASS.
 *
 * Parser edge cases:
 *  - `({ stmt; stmt; expr; })` - a compound statement used as an
 *    expression; its value is the last expression statement.
 *  - `__typeof__(x)` - GNU spelling; C23 standardizes `typeof` but NOT
 *    statement expressions.
 *  - `__auto_type` - GNU type inference (C23's `auto` is the ISO version).
 *  - `__label__ name;` - declares a block-local label for use in macros.
 *  - `__extension__` suppresses pedantic warnings - but a strict parser
 *    still must handle the token.
 */
#include <stdio.h>

/* Classic safe max: evaluates each argument once */
#define MAX(a, b) ({                \
    __typeof__(a) _a = (a);         \
    __typeof__(b) _b = (b);         \
    _a > _b ? _a : _b;              \
})

#define SQUARE_ONCE(x) ({ __auto_type _v = (x); _v * _v; })

/* Local label inside a macro, so multiple expansions don't clash */
#define FIND_INDEX(arr, n, target) ({                   \
    __label__ found;                                    \
    int _idx = -1;                                      \
    for (int _i = 0; _i < (n); _i++)                    \
        if ((arr)[_i] == (target)) { _idx = _i; goto found; } \
    found:                                              \
    _idx;                                               \
})

static int calls = 0;
static int next_value(void) { return ++calls * 10; }

int main(void)
{
    int a = 3, b = 7;
    printf("MAX(3, 7) = %d\n", MAX(a, b));
    printf("MAX(2.5, 1.5) = %.1f\n", MAX(2.5, 1.5));

    calls = 0;
    int m = MAX(next_value(), next_value());
    printf("MAX(next(), next()) = %d, calls = %d (each evaluated once)\n", m, calls);

    printf("SQUARE_ONCE(next_value()) = %d, calls = %d\n",
           SQUARE_ONCE(next_value()), calls);

    int data[] = { 5, 9, 2, 9, 4 };
    printf("FIND_INDEX(9) = %d\n", FIND_INDEX(data, 5, 9));
    printf("FIND_INDEX(7) = %d\n", FIND_INDEX(data, 5, 7));

    /* Statement expression as an initializer with internal declarations */
    int total = ({
        int s = 0;
        for (int i = 1; i <= 10; i++) s += i;
        s;
    });
    printf("total = %d\n", total);

    /* __typeof__ applied to a type and to an expression */
    __typeof__(int *) ptr = &total;
    __typeof__(*ptr) copy = *ptr + 1;
    __auto_type inferred = 1.0f / 3;
    printf("copy = %d, inferred = %f\n", copy, (double)inferred);

    /* __extension__ marker */
    __extension__ long long ext = 1LL << 40;
    printf("ext = %lld\n", ext);
    return 0;
}
