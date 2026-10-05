/*
 * Feature : #embed and __has_embed
 * Version : C23
 * Spec    : N3017 "#embed - a scannable, tooling-friendly binary resource
 *           inclusion mechanism"; C23 6.10.4
 *
 * `#embed "file"` expands to a comma-separated list of integer constants,
 * one per byte of the resource. Parameters: limit(N), prefix(...),
 * suffix(...), if_empty(...). `__has_embed(...)` returns
 * __STDC_EMBED_NOT_FOUND__ (0), __STDC_EMBED_FOUND__ (1) or
 * __STDC_EMBED_EMPTY__ (2).
 *
 * This file embeds ITSELF so it stays self-contained.
 *
 * Compiler support: GCC 15+, Clang 19+. GCC 14 and older reject #embed.
 *
 * Parser edge cases:
 *  - A brand-new preprocessing directive: older preprocessors report
 *    "invalid preprocessing directive #embed".
 *  - Parameters use `name(balanced-tokens)` syntax and also accept
 *    vendor-prefixed names: `clang::offset(4)`, `gnu::offset(4)`.
 *  - The directive appears in the MIDDLE of an initializer list.
 *  - `__has_embed` is only valid inside #if / #elif.
 */
#include <stdio.h>
#include <string.h>

#if !defined(__has_embed)
#  error "this compiler does not support C23 #embed"
#endif

#if __has_embed("embed_directive.c") == __STDC_EMBED_FOUND__
#  define SELF_FOUND 1
#else
#  define SELF_FOUND 0
#endif

static const unsigned char self[] = {
#embed "embed_directive.c"
};

/* limit(): only the first 2 bytes - this file starts with a comment opener */
static const unsigned char head[] = {
#embed "embed_directive.c" limit(2)
};

/* prefix/suffix: add tokens around the expansion; if_empty for 0 bytes */
static const unsigned char framed[] = {
    0xFF,
#embed "embed_directive.c" limit(4) prefix(0xAA, ) suffix(, 0xBB)
    , 0xFF
};

/* Embedding into a string-like char array with a NUL terminator */
static const char first_line[] = {
#embed "embed_directive.c" limit(16) suffix(, 0)
};

/* if_empty: used when the resource has zero bytes (limit(0) forces that) */
static const int empty_marker[] = {
#embed "embed_directive.c" limit(0) if_empty(-1)
};

static unsigned checksum(const unsigned char *p, size_t n)
{
    unsigned h = 2166136261u;          /* FNV-1a */
    for (size_t i = 0; i < n; i++) { h ^= p[i]; h *= 16777619u; }
    return h;
}

int main(void)
{
    printf("__has_embed found self: %d\n", SELF_FOUND);
    printf("embedded %zu bytes of source, FNV-1a = 0x%08x\n",
           sizeof self, checksum(self, sizeof self));
    printf("head = '%c%c'\n", head[0], head[1]);
    printf("framed (%zu bytes): ", sizeof framed);
    for (size_t i = 0; i < sizeof framed; i++) printf("%02X ", framed[i]);
    printf("\nfirst 16 chars: \"%.16s\"\n", first_line);
    printf("empty_marker[0] = %d\n", empty_marker[0]);
    printf("self starts with comment: %s\n",
           memcmp(self, "/*", 2) == 0 ? "yes" : "no");
    return 0;
}
