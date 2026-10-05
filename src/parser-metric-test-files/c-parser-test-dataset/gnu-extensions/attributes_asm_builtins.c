/*
 * Feature : GNU __attribute__((...)), inline asm, __builtin_* functions
 * Version : GNU C extension - NOT ISO C
 * Spec    : GCC manual "Attribute Syntax", "Extended Asm", "Other Builtins"
 *
 * EXPECTED: grammar-only ISO C parser -> FAIL.
 *           GNU-mode parser -> PASS (x86-64/AArch64; asm block is guarded).
 *
 * NOTE: `gcc -std=c17 -pedantic-errors` ACCEPTS this file. Every extension
 * here is spelled with a double-underscore name (__attribute__, __asm__,
 * __builtin_*), which ISO C reserves for the implementation, so using them
 * is "conforming" for GCC. A parser that implements only the ISO grammar has
 * no such escape hatch and must reject it. Record which behaviour you want.
 *
 * Parser edge cases:
 *  - `__attribute__((a, b(1, 2)))` - DOUBLE parentheses, may appear before
 *    or after declarators, on types, statements, labels and parameters.
 *  - `asm`/`__asm__` statements with operand lists using `:` separators,
 *    string templates and constraint strings.
 *  - `__asm__("symbol")` renaming on declarations.
 *  - `__builtin_expect`, `__builtin_types_compatible_p(type, type)` -
 *    the latter takes TYPE NAMES as arguments, like sizeof.
 *  - `__builtin_offsetof(type, member.sub[2])`.
 *  - Attribute after a label: `lbl: __attribute__((unused));`.
 */
#include <stdio.h>
#include <stdlib.h>

#define likely(x)   __builtin_expect(!!(x), 1)
#define unlikely(x) __builtin_expect(!!(x), 0)
#define SAME_TYPE(a, b) __builtin_types_compatible_p(__typeof__(a), __typeof__(b))

struct __attribute__((packed)) wire_header {
    unsigned char  kind;
    unsigned int   length;
    unsigned short crc;
};

struct aligned_block {
    char data[10];
} __attribute__((aligned(32)));

typedef int vec4i __attribute__((vector_size(16)));

static int counter __attribute__((unused)) = 0;

__attribute__((noinline, cold))
static void report_error(const char *msg)
{
    fprintf(stderr, "error: %s\n", msg);
}

static int __attribute__((const)) cube(int x) { return x * x * x; }

__attribute__((format(printf, 1, 2)))
static void logf_(const char *fmt, ...);

static void logf_(const char *fmt, ...)
{
    (void)fmt;
}

extern int renamed_symbol(int) __asm__("strtol_wrapper_unused");

__attribute__((constructor))
static void before_main(void) { puts("constructor ran before main"); }

static int add_asm(int a, int b)
{
#if defined(__x86_64__)
    int result;
    __asm__ volatile ("addl %2, %0" : "=r"(result) : "0"(a), "r"(b) : "cc");
    return result;
#elif defined(__aarch64__)
    int result;
    __asm__ volatile ("add %w0, %w1, %w2" : "=r"(result) : "r"(a), "r"(b));
    return result;
#else
    return a + b;
#endif
}

int main(void)
{
    printf("sizeof packed wire_header = %zu\n", sizeof(struct wire_header));
    printf("alignof aligned_block = %zu\n", __alignof__(struct aligned_block));
    printf("offsetof length = %zu\n", __builtin_offsetof(struct wire_header, length));

    vec4i v = { 1, 2, 3, 4 }, w = { 10, 20, 30, 40 };
    vec4i s = v + w;
    printf("vector add: %d %d %d %d\n", s[0], s[1], s[2], s[3]);

    printf("add_asm(40, 2) = %d\n", add_asm(40, 2));
    printf("cube(3) = %d\n", cube(3));

    int x = 5; long y = 5;
    printf("SAME_TYPE(int,int)=%d SAME_TYPE(int,long)=%d\n",
           SAME_TYPE(x, x), SAME_TYPE(x, y));

    printf("popcount(0xFF) = %d, clz(1) = %d\n",
           __builtin_popcount(0xFFu), __builtin_clz(1u));
    printf("constant_p(42) = %d\n", __builtin_constant_p(42));

    if (unlikely(x > 100)) report_error("x too large");
    if (likely(x == 5)) puts("likely branch taken");

    logf_("value %d", x);
    goto done;
done: __attribute__((unused));
    return 0;
}
