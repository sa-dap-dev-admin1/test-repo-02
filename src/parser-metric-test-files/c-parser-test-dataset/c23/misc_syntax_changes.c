/*
 * Feature : Grab-bag of C23 syntax changes
 * Version : C23
 * Spec    : N2900 (empty initializer {}), N2480 (unnamed parameters in
 *           definitions), N2508 (labels before declarations / at end of
 *           block), N2645 (#elifdef/#elifndef), N3033 (__VA_OPT__),
 *           N2686 (#warning), N2841 (K&R definitions removed; f() == f(void)),
 *           N2799 (__has_include), N2553/N2828 (__has_c_attribute)
 *
 * Parser edge cases:
 *  - `int x = {};` and `struct s v = {};` - empty braces now valid.
 *  - `int f(int, int unused_name_omitted) {}` - parameter names may be
 *    omitted in a DEFINITION.
 *  - A label may precede a declaration: `L: int x = 1;` and a label may
 *    end a compound statement: `{ ... done: }`.
 *  - `#elifdef X` / `#elifndef X` - new directives.
 *  - `__VA_OPT__(,)` inside a variadic macro body.
 *  - `void f();` now means `void f(void);` - `f(1)` is an error.
 *  - Old-style `int add(a, b) int a, b; { ... }` is REMOVED (negative test
 *    idea for strict C23 parsers).
 */
#include <stdio.h>
#include <string.h>

#define FEATURE_B

#ifdef FEATURE_A
#  define WHICH "A"
#elifdef FEATURE_B
#  define WHICH "B"
#elifndef FEATURE_C
#  define WHICH "not C"
#else
#  define WHICH "none"
#endif

#if __has_include(<stdbit.h>)
#  define HAVE_STDBIT 1
#else
#  define HAVE_STDBIT 0
#endif

#if defined(__has_c_attribute) && __has_c_attribute(nodiscard)
#  define HAVE_NODISCARD 1
#else
#  define HAVE_NODISCARD 0
#endif

/* __VA_OPT__: the comma only appears when there are variadic args */
#define LOG(fmt, ...) printf("[log] " fmt "\n" __VA_OPT__(,) __VA_ARGS__)
#define CALL(f, ...) f(__VA_OPT__(__VA_ARGS__))

struct options { int depth; double scale; const char *name; int tags[3]; };

/* Unnamed parameter in a definition (callback signature compatibility) */
static int on_event(int code, void *)
{
    return code * 2;
}

/* Empty parameter list now means (void) */
static int answer() { return 42; }

static int zero_args(void) { return 0; }

static int find_first_negative(const int *v, int n)
{
    int idx = -1;
    for (int i = 0; i < n; i++) {
        if (v[i] < 0) { idx = i; goto found; }
    }
    goto done;
found:
    int doubled = idx * 2;           /* label directly before a declaration */
    LOG("found at %d (doubled %d)", idx, doubled);
done:                                /* label at the end of a block */
}

int main(void)
{
    /* Empty initializers */
    int zero = {};
    struct options opt = {};
    int arr[5] = {};
    printf("zero=%d opt.depth=%d opt.name=%s arr[4]=%d\n",
           zero, opt.depth, opt.name ? opt.name : "(null)", arr[4]);

    LOG("no variadic arguments");
    LOG("with arguments: %d %s", 7, "seven");
    printf("CALL(zero_args) = %d, CALL(answer) = %d\n",
           CALL(zero_args), CALL(answer));

    printf("on_event(21, nullptr) = %d\n", on_event(21, nullptr));
    printf("#elifdef chose: %s\n", WHICH);
    printf("__has_include(<stdbit.h>) = %d, nodiscard attr = %d\n",
           HAVE_STDBIT, HAVE_NODISCARD);

    int data[] = { 4, 8, -3, 9 };
    find_first_negative(data, 4);
    find_first_negative((int[]){ 1, 2 }, 2);
    return 0;
}
