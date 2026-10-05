/*
 * Feature : Generic selection (_Generic)
 * Version : C11 (ISO/IEC 9899:2011, 6.5.1.1)
 * Spec    : N1570 6.5.1.1 "Generic selection"
 *
 * `_Generic(controlling-expr, type: expr, ..., default: expr)` picks one
 * association at compile time based on the (lvalue-converted) type of the
 * controlling expression. The controlling expression is NOT evaluated.
 *
 * Parser edge cases:
 *  - The association list mixes TYPE-NAMES and the `default` keyword before
 *    the colon: `int: f, char *: g, default: h`.
 *  - Type names can be complex: `int (*)(void): ...`, `const char *: ...`.
 *  - Commas inside a compound literal argument to a macro wrapping _Generic
 *    split the macro arguments: `length((struct v){3,4})` is 2 args. Wrap
 *    in extra parentheses. Likewise commas inside the selected expression
 *    whole _Generic sits inside a macro argument.
 *  - Array/function controlling expressions decay; qualifiers are dropped
 *    (C11 DR 481), so `const int` matches `int`.
 *  - C23 (N3260, adopted for C2y) allows a TYPE NAME as the controlling
 *    operand - not valid C11.
 */
#include <stdio.h>
#include <math.h>

#define type_name(x) _Generic((x),                  \
    _Bool: "_Bool",                                 \
    char: "char", signed char: "signed char",       \
    unsigned char: "unsigned char",                 \
    short: "short", unsigned short: "unsigned short", \
    int: "int", unsigned int: "unsigned int",       \
    long: "long", unsigned long: "unsigned long",   \
    long long: "long long",                         \
    float: "float", double: "double",               \
    long double: "long double",                     \
    char *: "char *", const char *: "const char *", \
    int *: "int *",                                 \
    int (*)(int): "int (*)(int)",                   \
    default: "other")

#define abs_any(x) _Generic((x), \
    int: abs_i, long: labs_l, float: fabsf, double: fabs, default: fabs)(x)

#define print_val(x) _Generic((x),          \
    int: print_int, double: print_double,   \
    const char *: print_str, char *: print_str)(x)

static int abs_i(int v) { return v < 0 ? -v : v; }
static long labs_l(long v) { return v < 0 ? -v : v; }
static void print_int(int v) { printf("int:%d\n", v); }
static void print_double(double v) { printf("double:%.3f\n", v); }
static void print_str(const char *s) { printf("str:%s\n", s); }
static int twice(int v) { return 2 * v; }

struct vec2 { float x, y; };
struct vec3 { float x, y, z; };
static float len2(struct vec2 v) { return sqrtf(v.x * v.x + v.y * v.y); }
static float len3(struct vec3 v) { return sqrtf(v.x * v.x + v.y * v.y + v.z * v.z); }
#define length(v) _Generic((v), struct vec2: len2, struct vec3: len3)(v)

int main(void)
{
    int i = 0; const int ci = 1; char c = 'a'; float f = 1.0f;
    char buf[8] = "hi"; const char *cs = "lit"; int arr[3] = { 0 };

    printf("i      -> %s\n", type_name(i));
    printf("ci     -> %s (qualifier dropped)\n", type_name(ci));
    printf("c      -> %s\n", type_name(c));
    printf("'a'    -> %s (char constant is int in C)\n", type_name('a'));
    printf("f      -> %s\n", type_name(f));
    printf("1.0    -> %s\n", type_name(1.0));
    printf("1.0L   -> %s\n", type_name(1.0L));
    printf("buf    -> %s (array decays)\n", type_name(buf));
    printf("cs     -> %s\n", type_name(cs));
    printf("arr    -> %s\n", type_name(arr));
    printf("twice  -> %s (function decays)\n", type_name(twice));
    printf("c + c  -> %s (integer promotion)\n", type_name(c + c));
    printf("struct -> %s\n", type_name((struct vec2){ 0 }));

    /* Controlling expression is not evaluated */
    int side = 0;
    (void)type_name(side++);
    printf("side after _Generic(side++) = %d\n", side);

    printf("abs_any: %d %ld %.1f %.1f\n", abs_any(-3), abs_any(-4L),
           (double)abs_any(-2.5f), abs_any(-7.25));

    print_val(42);
    print_val(3.14159);
    print_val("generic");

    printf("length vec2 = %.1f, vec3 = %.1f\n",
           /* Extra parens: the braces of a compound literal do NOT protect
            * commas from the preprocessor - only parentheses do. */
           (double)length(((struct vec2){ 3, 4 })),
           (double)length(((struct vec3){ 2, 3, 6 })));

    /* Nested _Generic */
    const char *kind = _Generic(1u + 1L,
        long: _Generic(sizeof(long), default: "long (nested default)"),
        unsigned long: "unsigned long",
        default: "something else");
    printf("1u + 1L -> %s\n", kind);
    return 0;
}
