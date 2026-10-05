/*
 * Feature : Enumerations with a fixed underlying type; improved enums
 * Version : C23
 * Spec    : N3030 "Enhancements to Enumerations"; N3029 "Improved
 *           Normal Enumerations" (values beyond int range)
 *
 * `enum name : type { ... }` fixes the underlying integer type. Constants
 * then have the enum type and may exceed `int`. Forward declarations of an
 * enum with fixed type (`enum e : long;`) become legal.
 *
 * Parser edge cases:
 *  - `enum E : unsigned char { ... }` - the `:` after the tag introduces a
 *    type, NOT a bit-field (contrast `enum E e : 3;` inside a struct, which
 *    IS a bit-field).
 *  - Anonymous enum with fixed type: `enum : short { A, B };`.
 *  - Opaque forward declaration `enum color : int;` with no list.
 *  - The underlying type must be an integer type other than an enumerated
 *    or bit-precise (_BitInt) type; qualifiers are ignored.
 *  - Pre-C23 parsers fail at the `:`.
 */
#include <stdio.h>
#include <stdint.h>
#include <limits.h>

/* Opaque forward declaration (only legal with a fixed type) */
enum status : int;

enum status : int { ST_OK = 0, ST_WARN = 1, ST_ERR = -1 };

enum flag8 : uint8_t {
    F_NONE = 0,
    F_A = 1u << 0,
    F_B = 1u << 1,
    F_C = 1u << 2,
    F_ALL = F_A | F_B | F_C,
};

enum big : unsigned long long {
    BIG_MAX = ULLONG_MAX,
    BIG_HALF = ULLONG_MAX / 2,
};

/* Anonymous enum with fixed type */
enum : short { SMALL_ONE = 1, SMALL_TWO = 2 };

/* C23 "improved normal enums": no fixed type, but values beyond int OK */
enum wide { WIDE_LOW = 0, WIDE_HIGH = 0x1'0000'0000LL };

struct packet {
    enum flag8 flags;                 /* takes exactly 1 byte */
    enum status st : 2;               /* THIS colon is a bit-field width */
    uint8_t payload[5];
};

static const char *status_name(enum status s)
{
    switch (s) {
    case ST_OK:   return "ok";
    case ST_WARN: return "warn";
    case ST_ERR:  return "error";
    }
    return "?";
}

int main(void)
{
    printf("sizeof(enum flag8) = %zu\n", sizeof(enum flag8));
    printf("sizeof(enum big)   = %zu, BIG_MAX = %llu\n", sizeof(enum big),
           (unsigned long long)BIG_MAX);
    printf("sizeof(SMALL_ONE)  = %zu (constant has the enum type)\n",
           sizeof SMALL_ONE);
    printf("sizeof(enum wide)  = %zu, WIDE_HIGH = %lld\n", sizeof(enum wide),
           (long long)WIDE_HIGH);

    enum flag8 f = F_A | F_C;
    printf("flags = 0x%02X, has B? %s\n", f, (f & F_B) ? "yes" : "no");

    struct packet p = { .flags = F_ALL, .st = ST_ERR };
    printf("packet: flags=0x%02X st=%s sizeof=%zu\n", p.flags,
           status_name(p.st), sizeof p);

    for (int s = -1; s <= 1; s++)
        printf("status %2d -> %s\n", s, status_name((enum status)s));

    static_assert(sizeof(enum flag8) == 1);
    static_assert(_Generic(F_A, enum flag8: 1, default: 0),
                  "constants of fixed-type enums have the enum type");
    return 0;
}
