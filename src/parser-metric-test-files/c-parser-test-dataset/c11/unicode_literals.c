/*
 * Feature : Unicode string literals and <uchar.h>
 * Version : C11
 * Spec    : N1570 6.4.4.4 (char16_t/char32_t constants u'x' U'x'),
 *           6.4.5 (u8"", u"", U"" string literals), 7.28 (uchar.h)
 *
 * C11 adds encoding prefixes: u8"..." (UTF-8, type char[] in C11-C17,
 * char8_t-like unsigned char in C23), u"..." (char16_t), U"..." (char32_t).
 * u'x' and U'x' character constants are added; u8'x' arrives only in C23.
 *
 * Parser edge cases:
 *  - `u8"..."`, `u"..."`, `U"..."`, `L"..."` - the prefix is part of the
 *    token; `u8 "x"` (with a space) is identifier u8 followed by a string.
 *  - Adjacent literal concatenation with mixed prefixes: u"a" "b" is OK,
 *    u"a" U"b" is a constraint violation.
 *  - `u8'a'` is a C23 token; in C11 it lexes as identifier `u8` + 'a'.
 *  - Universal character names \u00E9 and \U0001F600 in literals/identifiers.
 */
#include <stdio.h>
#include <string.h>
#include <uchar.h>
#include <wchar.h>

static size_t len16(const char16_t *s) { size_t n = 0; while (s[n]) n++; return n; }
static size_t len32(const char32_t *s) { size_t n = 0; while (s[n]) n++; return n; }

int main(void)
{
    const char     *narrow = "caf\u00E9";
    const char     *utf8   = u8"caf\u00E9 \U0001F600";
    const char16_t *utf16  = u"caf\u00E9 \U0001F600";
    const char32_t *utf32  = U"caf\u00E9 \U0001F600";
    const wchar_t  *wide   = L"wide \u03A9";

    printf("utf8 bytes = %zu, text = %s\n", strlen(utf8), utf8);
    printf("narrow     = %s\n", narrow);
    printf("utf16 code units = %zu (emoji is a surrogate pair)\n", len16(utf16));
    printf("utf32 code points = %zu\n", len32(utf32));
    printf("wide length = %zu\n", wcslen(wide));

    char16_t c16 = u'\u00E9';
    char32_t c32 = U'\U0001F600';
    printf("c16 = U+%04X, c32 = U+%05X\n", (unsigned)c16, (unsigned)c32);
    printf("sizeof u'x' = %zu, sizeof U'x' = %zu\n", sizeof u'x', sizeof U'x');

    /* Concatenation: unprefixed piece adopts the prefix of the other */
    const char16_t *joined = u"left-" "right";
    printf("joined length = %zu\n", len16(joined));

    /* Universal character name in an identifier (C99+, still exercised) */
    int caf\u00E9_count = 3;
    printf("identifier with UCN = %d\n", caf\u00E9_count);

    /* Convert UTF-32 code point to multibyte via c32rtomb */
    mbstate_t st;
    memset(&st, 0, sizeof st);
    char out[8];
    size_t n = c32rtomb(out, U'A', &st);
    printf("c32rtomb('A') wrote %zu byte(s)\n", n);
    return 0;
}
