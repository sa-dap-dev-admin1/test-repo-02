/*
 * Feature : C99 lexical & declaration changes vs C89
 * Version : C99
 * Spec    : N1256 6.4.9 (// comments), 6.8.5p3 (for-init declaration),
 *           6.8.2 (mixed declarations and code), 6.4.4.2 (hex floats),
 *           6.2.5 (long long), 6.4.2.2 (__func__), 6.10.3 (variadic macros),
 *           6.10.9 (_Pragma), 6.7.2 (implicit int removed)
 *
 * Small changes that break C89 parsers:
 *  - `//` line comments.
 *  - Declarations after statements in a block, and in `for (int i = ...)`.
 *  - Hexadecimal floating constants: 0x1.8p3 (the `p` exponent is required).
 *  - `long long` / `LL` / `ULL` suffixes.
 *  - __func__ predefined identifier.
 *  - Variadic macros with __VA_ARGS__.
 *  - _Pragma("...") operator usable inside macros.
 *  - Implicit `int` removed: `static x = 1;` is a constraint violation.
 *
 * Parser edge cases:
 *  - `a //* comment * / b` (no space): C89 reads `a / b`; C99 reads `a`
 *    followed by a line comment. See demo_comments() below.
 *  - `0x1p-2` contains a `-` that belongs to the number, not an operator.
 *  - `0xe+1` is a single (invalid) pp-number, not 0xe + 1.
 */
#include <stdio.h>
#include <limits.h>

#define LOG(fmt, ...) printf("[%s:%d] " fmt "\n", __func__, __LINE__, __VA_ARGS__)
#define LOG0(msg)     printf("[%s] %s\n", __func__, msg)
#define FIRST(a, ...) (a)
#define COUNT_ARGS(...) COUNT_IMPL(__VA_ARGS__, 5, 4, 3, 2, 1, 0)
#define COUNT_IMPL(_1, _2, _3, _4, _5, N, ...) N
#define NO_UNROLL _Pragma("GCC diagnostic push") \
                  _Pragma("GCC diagnostic pop")

static void demo_comments(void)
{
    int a = 10, b = 2;
    int c = a //* this is a line comment in C99 */ b
        ;
    LOG("a //* b */ parses as a in C99: c = %d", c);
    // A single-line comment with a trailing backslash continues \
       onto this line too (still part of the comment)
    LOG0("line-splice inside // comment handled");
}

static void demo_mixed_declarations(void)
{
    int total = 0;
    total += 5;
    int later = total * 2;           /* declaration after a statement */
    for (int i = 0; i < 3; i++) {    /* declaration in for-init */
        int sq = i * i;
        later += sq;
    }
    LOG("later = %d", later);
}

static void demo_numbers(void)
{
    double h1 = 0x1.8p3;        /* 1.5 * 2^3 = 12.0 */
    double h2 = 0x1p-2;         /* 0.25 */
    double h3 = 0xA.Bp0;        /* 10.6875 */
    float  hf = 0x1.0p+1f;      /* 2.0f */
    long long big = 9223372036854775807LL;
    unsigned long long ubig = 18446744073709551615ULL;
    long long neg = -0x7fffffffffffffffLL - 1;
    LOG("hex floats: %g %g %g %g", h1, h2, h3, (double)hf);
    LOG("long long: %lld %llu %lld (LLONG_MAX=%lld)", big, ubig, neg, LLONG_MAX);
}

static void demo_variadic_macros(void)
{
    LOG("FIRST(7, 8, 9) = %d", FIRST(7, 8, 9));
    LOG("COUNT_ARGS(a,b,c) = %d", COUNT_ARGS(a, b, c));
    LOG("COUNT_ARGS(x) = %d", COUNT_ARGS(x));
    NO_UNROLL
    LOG0("_Pragma inside macro expansion accepted");
}

static void demo_func_identifier(void)
{
    const char *name = __func__;     /* static const char __func__[] */
    LOG("sizeof __func__ = %zu, name = %s", sizeof __func__, name);
}

int main(void)
{
    demo_comments();
    demo_mixed_declarations();
    demo_numbers();
    demo_variadic_macros();
    demo_func_identifier();
    return 0;   /* C99: reaching } of main also returns 0 */
}
