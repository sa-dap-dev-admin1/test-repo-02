/*
 * Feature : constexpr objects
 * Version : C23
 * Spec    : N3018 "The constexpr specifier for object definitions"
 *
 * `constexpr` is a storage-class specifier for OBJECTS (not functions, unlike
 * C++). The object is implicitly const, must be initialized with a constant
 * expression, and can then be used anywhere an integer constant expression
 * is required (array sizes, case labels, static_assert, bit-field widths).
 *
 * Parser edge cases:
 *  - `constexpr` is a keyword only in C23.
 *  - `constexpr int f(void) { ... }` is INVALID in C (C++ only).
 *  - constexpr cannot be combined with `extern`, `_Thread_local`, VLA types,
 *    pointers other than null, atomic, volatile or restrict types.
 *  - Initializer value must be exactly representable: `constexpr unsigned
 *    char c = 300;` is a constraint violation (not silently truncated).
 *  - Unlike `const int N = 4;`, `int a[N]` with constexpr N is NOT a VLA.
 */
#include <stdio.h>

constexpr int BUFFER_SIZE = 16;
constexpr double PI = 3.14159265358979;
constexpr unsigned FLAG_READ = 1u << 0;
constexpr unsigned FLAG_WRITE = 1u << 1;
constexpr unsigned FLAG_EXEC = 1u << 2;
constexpr unsigned FLAG_ALL = FLAG_READ | FLAG_WRITE | FLAG_EXEC;
constexpr char GREETING[] = "constexpr array";
constexpr int *NO_PTR = nullptr;          /* only null pointers allowed */

struct limits { int lo, hi; };
constexpr struct limits RANGE = { .lo = -5, .hi = 5 };

static_assert(BUFFER_SIZE % 8 == 0, "buffer size must be a multiple of 8");
static_assert(FLAG_ALL == 7);

/* File-scope array sized by constexpr: a true constant, not a VLA */
static int ring[BUFFER_SIZE];

struct packed_flags {
    unsigned perms : 3;              /* could also be sized by constexpr */
};
constexpr int PERM_BITS = 3;
struct sized_flags { unsigned perms : PERM_BITS; };

static const char *describe(unsigned f)
{
    switch (f) {
    case FLAG_READ:                return "read";
    case FLAG_WRITE:               return "write";
    case FLAG_READ | FLAG_WRITE:   return "read+write";
    case FLAG_ALL:                 return "all";
    default:                       return "other";
    }
}

static int clamp(int v)
{
    return v < RANGE.lo ? RANGE.lo : v > RANGE.hi ? RANGE.hi : v;
}

int main(void)
{
    for (int i = 0; i < BUFFER_SIZE; i++) ring[i] = i * i;
    printf("ring[%d] = %d (sizeof ring = %zu)\n",
           BUFFER_SIZE - 1, ring[BUFFER_SIZE - 1], sizeof ring);

    constexpr int LOCAL_N = BUFFER_SIZE / 4;
    int local[LOCAL_N] = { };            /* constant size: can use {} init */
    printf("local elements = %zu\n", sizeof local / sizeof local[0]);

    for (unsigned f = 1; f <= FLAG_ALL; f++)
        printf("flags %u -> %s\n", f, describe(f));

    printf("circle r=2 area = %.4f\n", PI * 2 * 2);
    printf("%s (%zu bytes)\n", GREETING, sizeof GREETING);
    printf("clamp(-9)=%d clamp(3)=%d clamp(12)=%d\n", clamp(-9), clamp(3), clamp(12));
    printf("NO_PTR is %s\n", NO_PTR == nullptr ? "null" : "set");

    struct sized_flags sf = { .perms = FLAG_ALL };
    printf("bit-field perms = %u (width %d)\n", sf.perms, PERM_BITS);
    return 0;
}
