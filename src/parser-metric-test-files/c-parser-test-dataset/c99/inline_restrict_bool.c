/*
 * Feature : `inline`, `restrict`, `_Bool`, `<stdbool.h>`, `<stdint.h>`
 * Version : C99
 * Spec    : N1256 6.7.4 (inline), 6.7.3.1 (restrict), 6.2.5p2 (_Bool),
 *           7.16 (stdbool.h), 7.18 (stdint.h)
 *
 * C99 added three new keywords used here: `inline`, `restrict`, `_Bool`.
 * `bool`/`true`/`false` are MACROS from <stdbool.h> in C99-C17 (they become
 * real keywords in C23).
 *
 * Parser edge cases:
 *  - `restrict` is a type qualifier that binds to the POINTER:
 *    `int *restrict p` (correct) vs `restrict int *p` (constraint violation).
 *  - `restrict` is NOT a keyword in C++ (compilers use __restrict).
 *  - `inline` without `static`/`extern` has special "inline definition"
 *    semantics; `static inline` is the portable form.
 *  - `_Bool` starts with underscore+capital (reserved namespace) - a C89
 *    parser treats it as an identifier.
 *  - `int *restrict *restrict pp` - qualifiers at multiple levels.
 */
#include <stdio.h>
#include <stdbool.h>
#include <stdint.h>
#include <inttypes.h>
#include <string.h>

static inline int max_i(int a, int b) { return a > b ? a : b; }
static inline uint32_t rotl32(uint32_t x, unsigned r)
{
    return (x << (r & 31)) | (x >> ((32 - r) & 31));
}

/* restrict: promise that dst and src do not alias */
static void vec_add(size_t n, double *restrict dst,
                    const double *restrict a, const double *restrict b)
{
    for (size_t i = 0; i < n; i++) dst[i] = a[i] + b[i];
}

/* restrict at multiple pointer levels and inside array parameter brackets */
static void copy_rows(size_t n, char *restrict *restrict out,
                      const char *const in[restrict])
{
    for (size_t i = 0; i < n; i++) strcpy(out[i], in[i]);
}

static _Bool is_even(int n) { return (n & 1) == 0; }
static bool  is_power_of_two(uint64_t v) { return v && !(v & (v - 1)); }

int main(void)
{
    printf("max_i(3, 9) = %d\n", max_i(3, 9));
    printf("rotl32(0x80000001, 4) = 0x%08" PRIx32 "\n", rotl32(0x80000001u, 4));

    double a[4] = { 1, 2, 3, 4 }, b[4] = { 10, 20, 30, 40 }, d[4];
    vec_add(4, d, a, b);
    printf("vec_add: %.0f %.0f %.0f %.0f\n", d[0], d[1], d[2], d[3]);

    char r0[16], r1[16];
    char *outs[2] = { r0, r1 };
    const char *ins[2] = { "alpha", "beta" };
    copy_rows(2, outs, ins);
    printf("copy_rows: %s %s\n", r0, r1);

    /* _Bool conversion: any nonzero value becomes exactly 1 */
    _Bool flag = 42;
    bool other = 0.0001;
    printf("flag=%d other=%d sizeof(_Bool)=%zu\n", flag, other, sizeof(_Bool));
    printf("true=%d false=%d __bool_true_false_are_defined=%d\n",
           true, false, __bool_true_false_are_defined);

    for (int i = 0; i < 5; i++)
        printf("%d is %s\n", i, is_even(i) ? "even" : "odd");

    /* <stdint.h> exact-width and limit macros */
    uint8_t  u8  = UINT8_MAX;
    int16_t  i16 = INT16_MIN;
    uint64_t big = UINT64_C(1) << 40;
    intmax_t im  = INTMAX_MAX;
    printf("u8=%" PRIu8 " i16=%" PRId16 " big=%" PRIu64 " pow2=%d\n",
           u8, i16, big, is_power_of_two(big));
    printf("intmax_t max = %" PRIdMAX "\n", im);

    /* restrict-qualified local pointer */
    int storage[3] = { 7, 8, 9 };
    int *restrict rp = storage;
    rp[1] += 100;
    printf("storage[1] = %d\n", storage[1]);
    return 0;
}
