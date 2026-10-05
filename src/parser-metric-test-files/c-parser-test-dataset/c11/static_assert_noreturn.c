/*
 * Feature : _Static_assert, _Noreturn, <assert.h> static_assert,
 *           <stdnoreturn.h> noreturn, quick_exit/at_quick_exit
 * Version : C11
 * Spec    : N1570 6.7.10 (static assertions), 6.7.4p8 (_Noreturn),
 *           7.2 (static_assert macro), 7.23 (stdnoreturn.h)
 *
 * _Static_assert(constant-expression, string-literal); is a DECLARATION, so
 * it may appear at file scope, block scope and inside struct definitions.
 * _Noreturn is a function specifier promising the function never returns.
 *
 * Parser edge cases:
 *  - _Static_assert inside a struct member list is legal.
 *  - In C11 the message is REQUIRED; C23 makes it optional and also turns
 *    `static_assert` into a keyword (here it is a macro from <assert.h>).
 *  - `_Noreturn` sits with `static`/`inline`: `static _Noreturn void f(void)`.
 *  - In C23, `_Noreturn` is obsolescent in favour of `[[noreturn]]`.
 */
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <limits.h>
#include <stdnoreturn.h>
#include <stdint.h>

/* File scope */
_Static_assert(CHAR_BIT == 8, "this code assumes 8-bit bytes");
static_assert(sizeof(int) >= 4, "int must be at least 32 bits");

struct header {
    uint32_t magic;
    uint16_t version;
    uint16_t flags;
    /* Inside a struct definition */
    _Static_assert(sizeof(uint32_t) == 4, "uint32_t must be 4 bytes");
};
_Static_assert(sizeof(struct header) == 8, "header must pack to 8 bytes");

enum level { LOW, MID, HIGH, LEVEL_COUNT };
static const char *level_names[] = { "low", "mid", "high" };
_Static_assert(sizeof level_names / sizeof level_names[0] == LEVEL_COUNT,
               "level_names out of sync with enum level");

static _Noreturn void fatal(const char *msg)
{
    fprintf(stderr, "fatal: %s\n", msg);
    exit(EXIT_FAILURE);
}

noreturn static void finish(int code)          /* macro spelling */
{
    printf("finishing with code %d\n", code);
    fflush(stdout);
    quick_exit(code);
}

static void on_quick_exit(void) { printf("at_quick_exit handler ran\n"); }

static int checked_div(int a, int b)
{
    if (b == 0) fatal("division by zero");
    return a / b;
}

int main(void)
{
    /* Block scope, with a constant expression using sizeof and casts */
    _Static_assert((int)sizeof(long long) >= 8, "long long is at least 64 bits");
    _Static_assert(LEVEL_COUNT == 3, "three levels");

    for (int l = LOW; l < LEVEL_COUNT; l++)
        printf("level %d = %s\n", l, level_names[l]);

    printf("sizeof(struct header) = %zu\n", sizeof(struct header));
    printf("10 / 3 = %d\n", checked_div(10, 3));

    at_quick_exit(on_quick_exit);
    finish(0);
    /* not reached: no return statement needed after a noreturn call */
}
