/*
 * Feature : Binary literals, digit separators, %b printf, u8 char constants
 * Version : C23
 * Spec    : N2549 (binary constants 0b/0B), N2626 (digit separators '),
 *           N2630 (%b / %B formatted I/O), N2418 (u8 character constants)
 *
 * Parser edge cases:
 *  - `0b1010` is a C23 integer constant (GNU extension before C23).
 *  - The single quote is a DIGIT SEPARATOR inside a number:
 *    1'000'000, 0xFF'FF, 0b1111'0000, 3.141'592. The lexer must NOT start
 *    a character constant here. Separators cannot lead, trail, or appear
 *    next to the prefix/exponent: `'1000`, `1000'`, `0x'FF` are invalid.
 *  - The pp-number grammar changed so `1'2` is one pp-number token.
 *    A pre-C23 lexer sees `1` then the start of a char constant `'2...`.
 *  - `u8'a'` is a C23 character constant of type unsigned char.
 *  - Separators also work in suffixed literals: 1'000ULL.
 */
#include <stdio.h>
#include <stdint.h>
#include <limits.h>

#define KB 1'024
#define MB (KB * 1'024)

int main(void)
{
    int million = 1'000'000;
    long long big = 9'223'372'036'854'775'807LL;
    unsigned mask = 0b1111'0000;
    unsigned nibble = 0B0101;
    uint32_t color = 0xFF'80'40'20;
    int octal = 0'755;
    double pi = 3.141'592'653;
    double avogadro = 6.022'140'76e23;
    unsigned long long ull = 18'446'744'073'709'551'615ULL;

    printf("million  = %d\n", million);
    printf("big      = %lld\n", big);
    printf("mask     = %u (binary %b)\n", mask, mask);
    printf("nibble   = %u (binary %#b)\n", nibble, nibble);
    printf("color    = 0x%08X\n", color);
    printf("octal    = %d\n", octal);
    printf("pi       = %.9f\n", pi);
    printf("avogadro = %.6e\n", avogadro);
    printf("ull      = %llu (== ULLONG_MAX? %d)\n", ull, ull == ULLONG_MAX);
    printf("1 MB     = %d bytes\n", MB);

    /* Bit manipulation reads naturally in binary */
    uint8_t reg = 0b0000'0000;
    reg |= 0b0000'0101;            /* set bits 0 and 2 */
    reg &= (uint8_t)~0b0000'0001;  /* clear bit 0 */
    reg ^= 0b1000'0000;            /* toggle bit 7 */
    printf("reg      = %08b\n", reg);

    /* Character constants next to numbers: the lexer must disambiguate */
    int counts[3] = { 1'0, '0', 0'0 };       /* 10, 48 (char '0'), 0 */
    printf("counts   = %d %d %d\n", counts[0], counts[1], counts[2]);

    /* u8 character constant: type unsigned char in C23 */
    unsigned char ua = u8'a';
    printf("u8'a'    = %d, sizeof = %zu\n", ua, sizeof u8'a');

    /* Binary in a loop and switch */
    for (unsigned v = 0b00; v <= 0b11; v++) {
        switch (v) {
        case 0b00: puts("00 -> none"); break;
        case 0b01: puts("01 -> low");  break;
        case 0b10: puts("10 -> high"); break;
        case 0b11: puts("11 -> both"); break;
        }
    }
    return 0;
}
