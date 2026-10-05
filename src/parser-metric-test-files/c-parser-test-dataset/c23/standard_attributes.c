/*
 * Feature : Standard attributes [[...]]
 * Version : C23
 * Spec    : N2335 (attribute syntax), N2267 [[nodiscard]], N2270
 *           [[maybe_unused]], N2334 [[deprecated]], N2408 [[fallthrough]],
 *           N2764 [[noreturn]], N2448 [[nodiscard("reason")]]
 *
 * C23 adopts the C++11-style attribute-specifier-sequence. Standard
 * attributes: deprecated, fallthrough, maybe_unused, nodiscard, noreturn,
 * unsequenced, reproducible. Vendor attributes use a prefix: [[gnu::cold]].
 *
 * Parser edge cases:
 *  - `[[` is a new token sequence; `a[[0]]` doesn't occur in C syntax but
 *    the lexer must not treat `[[` as an attribute in `arr[ [x] ...` cases
 *    that cannot be valid anyway.
 *  - Placement matters: before a declaration (appertains to all declarators),
 *    after a declarator-id (appertains to that entity), after `struct`
 *    keyword, before a statement (`[[fallthrough]];` is an attribute on a
 *    null statement).
 *  - Unknown attributes are ignored, but the token sequence must balance.
 *  - Attribute arguments are balanced token sequences: [[deprecated("x")]].
 *  - `__attribute__((...))` is the GNU form - a different grammar entirely.
 *  - `[[__nodiscard__]]` - double-underscore spelling is also standard.
 */
#include <stdio.h>
#include <stdlib.h>

[[nodiscard]] static int parse_digit(char c)
{
    return (c >= '0' && c <= '9') ? c - '0' : -1;
}

[[nodiscard("allocated memory must be freed")]]
static int *make_buffer(size_t n)
{
    return calloc(n, sizeof(int));
}

[[deprecated]] static int old_api(int x) { return x + 1; }
[[deprecated("use new_api_v2 instead")]] static int new_api(int x) { return x * 2; }
static int new_api_v2(int x) { return x * 2; }

[[noreturn]] static void die(const char *msg)
{
    fprintf(stderr, "%s\n", msg);
    exit(1);
}

/* Attribute on a struct type and on a member */
struct [[deprecated]] legacy_config { int value; };
struct modern_config {
    int value;
    [[maybe_unused]] int reserved;
};

/* Attribute after the declarator-id */
static int helper_counter [[maybe_unused]] = 0;

/* Vendor-prefixed attributes: ignored if unknown, must still parse */
[[gnu::cold]] static void rarely_called(void) { puts("cold path"); }
[[gnu::always_inline]] static inline int fast_add(int a, int b) { return a + b; }
[[__nodiscard__]] static int also_nodiscard(void) { return 9; }

static const char *grade(int score)
{
    const char *result = "F";
    switch (score / 10) {
    case 10:
        [[fallthrough]];
    case 9:
        result = "A";
        break;
    case 8:
        result = "B";
        [[fallthrough]];           /* intentional: B also logs */
    case 7:
        if (!result[0]) result = "C";
        break;
    default:
        break;
    }
    return result;
}

int main([[maybe_unused]] int argc, [[maybe_unused]] char *argv[])
{
    int d = parse_digit('7');
    printf("parse_digit('7') = %d\n", d);

    int *buf = make_buffer(4);
    if (!buf) die("out of memory");
    buf[0] = new_api_v2(21);
    printf("buf[0] = %d\n", buf[0]);
    free(buf);

    [[maybe_unused]] int unused_local = 3;
    struct modern_config mc = { .value = 5 };
    printf("modern value = %d\n", mc.value);

    for (int s = 75; s <= 100; s += 12)
        printf("grade(%d) = %s\n", s, grade(s));

    printf("fast_add = %d, also_nodiscard = %d\n", fast_add(2, 3), also_nodiscard());
    if (d < 0) rarely_called();

    /* Calling deprecated functions is legal (compiler warns) */
    printf("old_api(1) = %d, new_api(4) = %d\n", old_api(1), new_api(4));
    return 0;
}
