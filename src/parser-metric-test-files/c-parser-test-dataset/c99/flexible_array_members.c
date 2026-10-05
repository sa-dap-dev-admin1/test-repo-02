/*
 * Feature : Flexible array members
 * Version : C99 (6.7.2.1p16 "Structure and union specifiers")
 * Spec    : N1256 6.7.2.1p16-18
 *
 * The last member of a struct with more than one named member may be an
 * array of incomplete type `T name[];`. Storage is supplied by malloc.
 *
 * Parser edge cases:
 *  - `char data[];` is legal ONLY as the last member of a struct.
 *  - `char data[0];` is the pre-C99 GNU zero-length array (extension, not
 *    ISO C - constraint violation in strict mode).
 *  - A struct with a FAM cannot be an array element or a member of another
 *    struct (constraint), but sizeof(struct) is still valid.
 *  - C++ has no flexible array members (compilers accept as an extension).
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stddef.h>

struct string_buf {
    size_t len;
    size_t cap;
    char data[];                /* flexible array member */
};

struct packet {
    unsigned short type;
    unsigned short count;
    int values[];
};

static struct string_buf *sb_new(const char *s)
{
    size_t n = strlen(s);
    struct string_buf *b = malloc(sizeof *b + n + 1);
    if (!b) return NULL;
    b->len = n;
    b->cap = n + 1;
    memcpy(b->data, s, n + 1);
    return b;
}

static struct packet *packet_new(unsigned short type, int n)
{
    struct packet *p = malloc(offsetof(struct packet, values) + n * sizeof(int));
    if (!p) return NULL;
    p->type = type;
    p->count = (unsigned short)n;
    for (int i = 0; i < n; i++) p->values[i] = (i + 1) * type;
    return p;
}

int main(void)
{
    struct string_buf *b = sb_new("flexible array member");
    printf("len=%zu cap=%zu data=\"%s\"\n", b->len, b->cap, b->data);
    printf("sizeof(struct string_buf) = %zu (FAM contributes 0)\n",
           sizeof(struct string_buf));
    printf("offsetof data = %zu\n", offsetof(struct string_buf, data));

    struct packet *p = packet_new(7, 5);
    printf("packet type=%u count=%u values:", p->type, p->count);
    for (int i = 0; i < p->count; i++) printf(" %d", p->values[i]);
    printf("\n");

    /* Growing a FAM struct with realloc */
    size_t extra = 8;
    struct string_buf *nb = realloc(b, sizeof *b + b->cap + extra);
    if (nb) {
        b = nb;
        b->cap += extra;
        strcat(b->data, "!!!");
        b->len = strlen(b->data);
        printf("after grow: \"%s\" len=%zu cap=%zu\n", b->data, b->len, b->cap);
    }

    /* Array of *pointers* to FAM structs is fine (array of structs is not) */
    struct packet *list[3];
    for (int i = 0; i < 3; i++) list[i] = packet_new((unsigned short)(i + 1), i + 1);
    for (int i = 0; i < 3; i++) {
        printf("list[%d]: count=%u last=%d\n", i, list[i]->count,
               list[i]->values[list[i]->count - 1]);
        free(list[i]);
    }

    free(p);
    free(b);
    return 0;
}
