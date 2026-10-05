/*
 * Feature : bool/true/false, static_assert, alignas, alignof, thread_local
 *           become KEYWORDS
 * Version : C23 (ISO/IEC 9899:2024), __STDC_VERSION__ == 202311L
 * Spec    : N2934 (keywords), N3054 6.4.1; N2265/N2508 (single-arg
 *           static_assert was C2x earlier: N2665)
 *
 * Before C23 these were macros from <stdbool.h>, <assert.h>, <stdalign.h>
 * and <threads.h>. In C23 they are keywords - no header needed. `true` and
 * `false` have type `bool` (not int) and work in #if.
 *
 * Parser edge cases:
 *  - `bool x = true;` with NO #include must parse. A C17 parser sees an
 *    undeclared identifier `bool`.
 *  - `static_assert(expr);` with no message is valid in C23 only.
 *  - `#if true` evaluates to 1 in the preprocessor (C23 6.10.2).
 *  - `_Bool`, `_Static_assert`, `_Alignas`, `_Alignof`, `_Thread_local`
 *    remain as alternative spellings (obsolescent).
 *  - These words can no longer be used as identifiers: `int bool;` is now
 *    a syntax error (negative-test idea: see EXPECTED_RESULTS.md).
 */
#include <stdio.h>

#if true && !false
#  define PP_BOOL_OK 1
#else
#  define PP_BOOL_OK 0
#endif

static_assert(sizeof(int) >= 2);                 /* no message: C23 */
static_assert(true, "message still allowed");
static_assert(_Generic(true, bool: 1, default: 0), "true has type bool");

/* Note: `struct alignas(32) block` is C++ placement - invalid in C. */
struct block { alignas(32) unsigned char data[32]; };
static thread_local int tl_counter = 0;

static bool is_vowel(char c)
{
    switch (c | 0x20) {
    case 'a': case 'e': case 'i': case 'o': case 'u': return true;
    default: return false;
    }
}

static int count_if(const char *s, bool (*pred)(char))
{
    int n = 0;
    for (; *s; s++) n += pred(*s);   /* bool promotes to int 0/1 */
    return n;
}

int main(void)
{
    bool flags[4] = { true, false, 2, 0.0 };   /* 2 converts to true */
    printf("flags: %d %d %d %d\n", flags[0], flags[1], flags[2], flags[3]);
    printf("sizeof(bool)=%zu sizeof(true)=%zu\n", sizeof(bool), sizeof(true));
    printf("PP_BOOL_OK = %d\n", PP_BOOL_OK);

    const char *word = "Parser Engineering";
    printf("vowels in \"%s\": %d\n", word, count_if(word, is_vowel));

    printf("alignof(struct block) = %zu\n", alignof(struct block));
    alignas(16) float simd[4] = { 1, 2, 3, 4 };
    printf("simd[3] = %.0f\n", (double)simd[3]);

    tl_counter += 5;
    printf("thread_local counter = %d\n", tl_counter);

    /* Old spellings still accepted */
    _Bool legacy = 1;
    _Static_assert(1, "old spelling");
    printf("legacy _Bool = %d, _Alignof(double) = %zu\n", legacy, _Alignof(double));

    bool b = true;
    b = !b;
    printf("toggled b = %s\n", b ? "true" : "false");
    return 0;
}
