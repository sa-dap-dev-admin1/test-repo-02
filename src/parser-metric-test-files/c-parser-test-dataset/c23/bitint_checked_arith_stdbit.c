/*
 * Feature : _BitInt(N), <stdckdint.h> checked arithmetic, <stdbit.h>
 * Version : C23
 * Spec    : N2763 (_BitInt), N2683 (stdckdint.h), N3022 (stdbit.h)
 *
 * `_BitInt(N)` / `unsigned _BitInt(N)` are bit-precise integers of exactly
 * N bits, exempt from integer promotion. Literal suffix `wb` / `uwb`.
 * ckd_add/ckd_sub/ckd_mul return true on overflow and store the wrapped
 * result. <stdbit.h> gives stdc_popcount, stdc_leading_zeros, etc.
 *
 * Parser edge cases:
 *  - `_BitInt` is a type specifier taking a PARENTHESIZED constant
 *    expression: `unsigned _BitInt(7)`, `_BitInt(BITS * 2)`.
 *  - New integer suffixes `wb`, `WB`, `uwb`, `UWB` (any case mix of u).
 *  - `signed _BitInt(1)` is invalid (needs at least 2 bits); unsigned
 *    _BitInt(1) is fine.
 *  - BITINT_MAXWIDTH from <limits.h> is implementation-defined (GCC: 65535
 *    on x86-64).
 *  - ckd_* are type-generic macros, not functions.
 */
#include <stdio.h>
#include <limits.h>
#include <stdckdint.h>
#include <stdbit.h>
#include <stdint.h>

#define BITS 12

typedef unsigned _BitInt(BITS) u12;
typedef _BitInt(BITS * 2) s24;

static u12 wrap_add(u12 a, u12 b) { return a + b; }   /* wraps mod 4096 */

int main(void)
{
    u12 a = 4000uwb, b = 200uwb;
    u12 c = wrap_add(a, b);
    printf("u12: 4000 + 200 = %u (mod 4096)\n", (unsigned)c);

    s24 big = -8'000'000wb;
    printf("s24: %ld, sizeof = %zu\n", (long)big, sizeof big);

    unsigned _BitInt(1) bit = 1;
    bit = bit + 1;                     /* wraps to 0, no promotion */
    printf("unsigned _BitInt(1): 1 + 1 = %u\n", (unsigned)bit);

    _BitInt(128) huge = 1wb;
    huge <<= 100;
    printf("2^100 high 64 bits = 0x%llx\n", (unsigned long long)(huge >> 64));
    printf("BITINT_MAXWIDTH = %d\n", (int)BITINT_MAXWIDTH);

    /* Checked arithmetic */
    int r;
    bool ov1 = ckd_add(&r, INT_MAX, 1);
    printf("ckd_add(INT_MAX, 1): overflow=%d result=%d\n", ov1, r);
    bool ov2 = ckd_mul(&r, 46341, 46341);
    printf("ckd_mul(46341, 46341): overflow=%d\n", ov2);
    uint8_t small;
    bool ov3 = ckd_sub(&small, 5, 10);
    printf("ckd_sub into uint8_t (5-10): overflow=%d result=%u\n", ov3, small);
    long long ll;
    bool ov4 = ckd_add(&ll, 2'000'000'000, 2'000'000'000);
    printf("ckd_add into long long: overflow=%d result=%lld\n", ov4, ll);

    /* <stdbit.h> */
    unsigned v = 0b0010'1100;
    printf("popcount(0x%X) = %u\n", v, stdc_count_ones(v));
    printf("leading zeros  = %u\n", stdc_leading_zeros(v));
    printf("trailing zeros = %u\n", stdc_trailing_zeros(v));
    printf("bit width      = %u\n", stdc_bit_width(v));
    printf("has single bit (64)? %d\n", stdc_has_single_bit(64u));
    printf("bit_ceil(100)  = %u\n", stdc_bit_ceil(100u));
    return 0;
}
