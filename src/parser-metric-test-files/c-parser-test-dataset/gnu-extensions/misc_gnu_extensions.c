/*
 * Feature : Assorted GNU C extensions: case ranges, range designators,
 *           zero-length arrays, nested functions, labels as values
 *           (computed goto), Elvis operator `?:`, binary literals pre-C23,
 *           `$` in identifiers, __int128
 * Version : GNU C extension - NOT ISO C
 * Spec    : GCC manual "Case Ranges", "Designated Inits", "Zero Length",
 *           "Nested Functions", "Labels as Values", "Conditionals",
 *           "Binary constants", "Dollar Signs", "__int128"
 *
 * EXPECTED: strict ISO C parser -> FAIL (many independent violations).
 *           GCC with -std=gnu17 -> PASS. Clang -> FAIL on nested functions
 *           (Clang never supported them) - a useful cross-compiler signal.
 *
 * Parser edge cases:
 *  - `case 'a' ... 'z':` - spaces around `...` are REQUIRED, because
 *    `1...5` lexes as a malformed floating pp-number.
 *  - `[0 ... 9] = -1` range designator.
 *  - `&&label` unary address-of-label and `goto *ptr;`.
 *  - `x ?: y` - middle operand omitted.
 *  - Function definition nested inside another function body.
 */
#include <stdio.h>

struct msg { int len; char body[0]; };          /* zero-length array */

static int classify(int c)
{
    switch (c) {
    case '0' ... '9': return 1;                  /* case range */
    case 'a' ... 'z':
    case 'A' ... 'Z': return 2;
    default:          return 0;
    }
}

static int run_bytecode(const unsigned char *code)
{
    static void *dispatch[] = { &&op_halt, &&op_inc, &&op_dbl };
    int acc = 0;
    goto *dispatch[*code++];
op_inc: acc += 1;  goto *dispatch[*code++];
op_dbl: acc *= 2;  goto *dispatch[*code++];
op_halt: return acc;
}

int main(void)
{
    int table[10] = { [0 ... 4] = 1, [5 ... 9] = 2 };   /* range designator */
    printf("table:");
    for (int i = 0; i < 10; i++) printf(" %d", table[i]);
    printf("\n");

    const char *s = "a1B?";
    for (const char *p = s; *p; p++)
        printf("classify('%c') = %d\n", *p, classify(*p));

    /* Nested function capturing a local variable */
    int base = 100;
    int add_base(int v) { return v + base; }
    printf("add_base(5) = %d\n", add_base(5));

    unsigned char prog[] = { 1, 1, 2, 2, 1, 0 };    /* ((0+1+1)*2*2)+1 */
    printf("bytecode result = %d\n", run_bytecode(prog));

    const char *name = NULL;
    printf("elvis: %s\n", name ?: "(default)");
    int zero = 0;
    printf("elvis int: %d\n", zero ?: 42);

    int bin = 0b101010;                             /* GNU before C23 */
    printf("0b101010 = %d\n", bin);

    int $dollar = 7;                                /* $ in identifier */
    printf("$dollar = %d\n", $dollar);

    __int128 wide = (__int128)1 << 100;
    printf("__int128 high bits = 0x%llx\n", (unsigned long long)(wide >> 64));

    printf("sizeof(struct msg) = %zu\n", sizeof(struct msg));
    return 0;
}
