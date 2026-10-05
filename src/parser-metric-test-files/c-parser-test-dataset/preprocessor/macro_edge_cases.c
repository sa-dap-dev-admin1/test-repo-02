/*
 * Feature : Preprocessor macro edge cases (portable ISO C, C99+)
 * Version : C99 baseline (variadic macros); valid C11/C17/C23
 * Spec    : C17 6.10.3 (macro replacement), 6.10.3.2 (#), 6.10.3.3 (##),
 *           6.10.3.4 (rescanning), 6.10.8 (predefined macros)
 *
 * Exercises the parts of macro expansion that trip up tools that try to
 * parse C without fully preprocessing it first.
 *
 * Parser edge cases:
 *  - Stringizing (#) and token pasting (##), including pasting that forms
 *    a new identifier which is then expanded again.
 *  - The two-level STR/XSTR trick: # suppresses argument expansion.
 *  - Function-like macro NAME without parentheses is not invoked.
 *  - Self-referential macros do not recurse ("blue paint").
 *  - Arguments spanning multiple lines and containing parenthesized commas.
 *  - Empty macro arguments: F(,) is two empty args (valid since C99).
 *  - A macro that expands to a directive-looking token sequence is NOT a
 *    directive.
 *  - Line splicing with backslash-newline in the middle of a token.
 */
#include <stdio.h>
#include <string.h>

#define STR(x)  #x
#define XSTR(x) STR(x)
#define CAT(a, b)  a ## b
#define XCAT(a, b) CAT(a, b)

#define VERSION_MAJOR 2
#define VERSION_MINOR 7
#define VERSION_STRING XSTR(VERSION_MAJOR) "." XSTR(VERSION_MINOR)

/* Token pasting builds identifiers */
#define DECLARE_GETTER(type, field) \
    static type get_##field(const struct record *r) { return r->field; }

/* Function-like macro used without parens is just an identifier */
#define square(x) ((x) * (x))

/* Multi-statement macro safely wrapped */
#define SWAP_INT(a, b) do { int t_ = (a); (a) = (b); (b) = t_; } while (0)

/* Empty arguments */
#define PAIR(a, b) "[" #a "|" #b "]"

/* Macro producing a # token that is NOT a directive */
#define HASH #
#define EMIT_HASH(x) STR(x)

/* Line splice in the middle of an identifier */
#define SPLICED_NA\
ME 99

/* Commas protected by parentheses */
#define FIRST_OF(a, b) a

struct record { int id; double weight; const char *name; };
DECLARE_GETTER(int, id)
DECLARE_GETTER(double, weight)
DECLARE_GETTER(const char *, name)

static int square_fn(int (*f)(int), int v) { return f(v); }
static int sq_impl(int v) { return v * v; }

int main(void)
{
    printf("VERSION_STRING = %s\n", VERSION_STRING);
    printf("STR(VERSION_MAJOR)  = %s (not expanded)\n", STR(VERSION_MAJOR));
    printf("XSTR(VERSION_MAJOR) = %s (expanded)\n", XSTR(VERSION_MAJOR));
    printf("STR with spaces: %s\n", STR(  a   +   b  ));
    printf("STR with quotes: %s\n", STR("quoted" 'c'));

    int XCAT(var_, 42) = 42;           /* declares var_42 */
    printf("var_42 = %d\n", var_42);

    struct record r = { 7, 81.5, "widget" };
    printf("getters: %d %.1f %s\n", get_id(&r), get_weight(&r), get_name(&r));

    int limit = 10;
    /* Self-reference: `limit` inside its own expansion is not re-expanded.
     * Defined AFTER the declaration, else `int limit` would become
     * `int (limit + 1)`. */
#define limit (limit + 1)
    printf("limit macro expands to (limit + 1) = %d\n", limit);
#undef limit

    int (*fp)(int) = sq_impl;
    printf("square(1+2) = %d\n", square(1 + 2));
    printf("square_fn via pointer = %d\n", square_fn(fp, 5));

    int a = 1, b = 2;
    if (a < b) SWAP_INT(a, b); else puts("unreachable");
    printf("after SWAP_INT: a=%d b=%d\n", a, b);

    printf("PAIR(,)   = %s\n", PAIR(,));
    printf("PAIR(x,)  = %s\n", PAIR(x,));
    printf("EMIT_HASH = %s\n", EMIT_HASH(HASH include));

    printf("SPLICED_NAME = %d\n", SPLICED_NAME);
    printf("FIRST_OF((1, 2), 3) = %d\n", FIRST_OF((1, 2), 3) );

    int multi = FIRST_OF(
        100,
        200
    );
    printf("multi-line args = %d\n", multi);

    printf("__FILE__ ends with .c: %s\n",
           strstr(__FILE__, ".c") ? "yes" : "no");
    printf("__LINE__ = %d, __STDC__ = %d\n", __LINE__, __STDC__);
#line 5000 "renamed.c"
    printf("after #line: %s:%d\n", __FILE__, __LINE__);
    return 0;
}
