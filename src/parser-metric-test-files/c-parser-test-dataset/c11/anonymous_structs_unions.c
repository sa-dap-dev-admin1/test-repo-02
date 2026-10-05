/*
 * Feature : Anonymous structures and unions
 * Version : C11
 * Spec    : N1570 6.7.2.1p13 "Structure and union specifiers"
 *
 * A struct/union member that is itself an unnamed struct or union (with no
 * tag and no declarator) is "anonymous": its members are accessed as if
 * they were members of the enclosing object.
 *
 * Parser edge cases:
 *  - `struct { int x, y; };` as a MEMBER with no declarator - legal in C11,
 *    a "declaration does not declare anything" error in C99.
 *  - Nested anonymous members flatten recursively.
 *  - An anonymous member WITH a tag (`struct tag { ... };` inside another
 *    struct) is NOT anonymous in ISO C - that's the -fms-extensions form.
 *  - Designated initializers can address the flattened names directly.
 *  - Name collisions between flattened members are a constraint violation.
 */
#include <stdio.h>
#include <stdint.h>

/* A tagged-union "variant" with an anonymous union */
enum kind { K_INT, K_REAL, K_TEXT, K_PAIR };

struct value {
    enum kind kind;
    union {                       /* anonymous union */
        long   i;
        double r;
        const char *s;
        struct {                  /* anonymous struct inside anonymous union */
            int first, second;
        };
    };
};

/* Register overlay: view the same bits as a word or as fields */
union reg32 {
    uint32_t word;
    struct {
        uint8_t b0, b1, b2, b3;
    };
    struct {
        uint16_t lo, hi;
    };
};

/* Vector with multiple anonymous views */
union vec3 {
    struct { float x, y, z; };
    struct { float r, g, b; };
    float v[3];
};

static void print_value(const struct value *val)
{
    switch (val->kind) {
    case K_INT:  printf("int  %ld\n", val->i); break;
    case K_REAL: printf("real %.2f\n", val->r); break;
    case K_TEXT: printf("text \"%s\"\n", val->s); break;
    case K_PAIR: printf("pair (%d, %d)\n", val->first, val->second); break;
    }
}

int main(void)
{
    struct value vals[] = {
        { .kind = K_INT,  .i = 42 },
        { .kind = K_REAL, .r = 2.75 },
        { .kind = K_TEXT, .s = "anonymous" },
        { .kind = K_PAIR, .first = 3, .second = 4 },  /* flattened names */
    };
    for (size_t n = 0; n < sizeof vals / sizeof vals[0]; n++)
        print_value(&vals[n]);

    union reg32 reg = { .word = 0x11223344u };
    printf("word=0x%08x lo=0x%04x hi=0x%04x\n", reg.word, reg.lo, reg.hi);
    printf("bytes (memory order): %02x %02x %02x %02x\n",
           reg.b0, reg.b1, reg.b2, reg.b3);
    reg.b3 = 0xAA;
    printf("after b3=0xAA: word=0x%08x\n", reg.word);

    union vec3 c = { .x = 0.1f, .y = 0.5f, .z = 0.9f };
    printf("x/y/z = %.1f %.1f %.1f\n", (double)c.x, (double)c.y, (double)c.z);
    printf("r/g/b = %.1f %.1f %.1f\n", (double)c.r, (double)c.g, (double)c.b);
    printf("v[]   = %.1f %.1f %.1f\n", (double)c.v[0], (double)c.v[1], (double)c.v[2]);
    printf("sizeof(union vec3) = %zu\n", sizeof(union vec3));
    return 0;
}
