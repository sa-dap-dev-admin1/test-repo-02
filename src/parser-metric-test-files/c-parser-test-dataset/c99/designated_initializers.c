/*
 * Feature : Designated initializers
 * Version : C99 (ISO/IEC 9899:1999, 6.7.8 "Initialization")
 * Spec    : N1256 6.7.8p6-7 (designation: [constant-expression] or .identifier)
 *
 * Designators let an initializer name the array index or struct member it
 * initializes, in any order. Unnamed members are zero-initialized.
 *
 * Parser edge cases:
 *  - `[N] = value` inside braces is a designator, NOT an array subscript.
 *  - Designators can chain: `.pos.x = 1`, `[2].name = "x"`, `.arr[1] = 5`.
 *  - A later designator for the same member overrides an earlier one.
 *  - `[0 ... 3] = v` (range designator) is a GNU extension, NOT C99.
 *  - In C++ before C++20 designators are illegal; C++20 forbids out-of-order.
 */
#include <stdio.h>

struct point { int x, y; };
struct rect  { struct point pos; struct point size; const char *label; };

enum color { RED, GREEN, BLUE, COLOR_COUNT };

static const char *color_names[COLOR_COUNT] = {
    [BLUE]  = "blue",          /* out of order on purpose */
    [RED]   = "red",
    [GREEN] = "green",
};

struct config {
    int verbose;
    int retries;
    double timeout;
    int flags[4];
    struct point origin;
};

/* Sparse array: unlisted entries become 0 */
static int primes_mask[20] = { [2] = 1, [3] = 1, [5] = 1, [7] = 1,
                               [11] = 1, [13] = 1, [17] = 1, [19] = 1 };

/* Designator followed by positional continuation: [4] = 40, then 50, 60 */
static int continued[8] = { 1, 2, [4] = 40, 50, 60 };

/* Override: index 1 set twice, last one wins (compilers may warn) */
static int overridden[3] = { [1] = 10, [1] = 99 };

static void print_rect(const struct rect *r)
{
    printf("%-8s pos=(%d,%d) size=(%d,%d)\n", r->label ? r->label : "(null)",
           r->pos.x, r->pos.y, r->size.x, r->size.y);
}

int main(void)
{
    /* Basic member designators, any order */
    struct point p = { .y = 7, .x = 3 };
    printf("p = (%d, %d)\n", p.x, p.y);

    /* Nested designator chains */
    struct rect r1 = { .pos.x = 1, .pos.y = 2, .size = { .x = 10, .y = 20 },
                       .label = "chained" };
    struct rect r2 = { .label = "partial", .size.y = 5 };   /* rest zero */
    print_rect(&r1);
    print_rect(&r2);

    /* Mixing member and array designators */
    struct config cfg = {
        .timeout   = 2.5,
        .flags[2]  = 1,
        .flags[0]  = 1,
        .origin    = { .y = -1 },
        .verbose   = 1,
    };
    printf("cfg: verbose=%d retries=%d timeout=%.1f flags=%d%d%d%d origin.y=%d\n",
           cfg.verbose, cfg.retries, cfg.timeout,
           cfg.flags[0], cfg.flags[1], cfg.flags[2], cfg.flags[3], cfg.origin.y);

    /* Array of structs with index + member designators */
    struct point poly[4] = { [3].x = 30, [1] = { 10, 11 }, [3].y = 33 };
    for (int i = 0; i < 4; i++)
        printf("poly[%d] = (%d,%d)\n", i, poly[i].x, poly[i].y);

    for (int c = 0; c < COLOR_COUNT; c++)
        printf("color %d -> %s\n", c, color_names[c]);

    printf("primes < 20:");
    for (int i = 0; i < 20; i++)
        if (primes_mask[i]) printf(" %d", i);
    printf("\n");

    printf("continued:");
    for (int i = 0; i < 8; i++) printf(" %d", continued[i]);
    printf("\noverridden[1] = %d\n", overridden[1]);

    /* Size deduced from the highest designator: [9] => 10 elements */
    int deduced[] = { [9] = 1 };
    printf("elements in deduced = %zu\n", sizeof deduced / sizeof deduced[0]);
    return 0;
}
