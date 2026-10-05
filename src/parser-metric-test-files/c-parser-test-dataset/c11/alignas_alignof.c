/*
 * Feature : _Alignas, _Alignof, <stdalign.h>, max_align_t, aligned_alloc
 * Version : C11
 * Spec    : N1570 6.7.5 (alignment specifier), 6.5.3.4 (_Alignof),
 *           7.15 (stdalign.h), 7.19 (max_align_t), 7.22.3.1 (aligned_alloc)
 *
 * _Alignas(N) or _Alignas(type) raises an object's alignment.
 * _Alignof(type) yields the alignment requirement of a type.
 *
 * Parser edge cases:
 *  - _Alignas takes EITHER a type-name OR a constant expression:
 *    `_Alignas(double)` vs `_Alignas(16)` - parser must disambiguate.
 *  - _Alignof takes only a parenthesized TYPE-NAME in ISO C (applying it to
 *    an expression is a GNU extension).
 *  - _Alignas cannot appear in a typedef, on a bit-field, on a function or
 *    on a `register` object (constraints).
 *  - C23 makes `alignas`/`alignof` keywords; here they are macros.
 */
#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <stdalign.h>
#include <stdint.h>

struct plain { char c; int i; double d; };

struct cache_line {
    _Alignas(64) unsigned char bytes[64];
};

struct mixed {
    char tag;
    alignas(16) float simd[4];     /* stdalign.h macro spelling */
    _Alignas(double) char raw[8];  /* type-name operand */
};

static _Alignas(32) int file_scope_buf[8];

#define SHOW_ALIGN(T) printf("%-22s size=%3zu align=%2zu\n", #T, sizeof(T), _Alignof(T))

int main(void)
{
    SHOW_ALIGN(char);
    SHOW_ALIGN(short);
    SHOW_ALIGN(int);
    SHOW_ALIGN(long long);
    SHOW_ALIGN(double);
    SHOW_ALIGN(long double);
    SHOW_ALIGN(void *);
    SHOW_ALIGN(max_align_t);
    SHOW_ALIGN(struct plain);
    SHOW_ALIGN(struct cache_line);
    SHOW_ALIGN(struct mixed);
    SHOW_ALIGN(int[10]);
    SHOW_ALIGN(int (*)(void));

    printf("offsetof(struct mixed, simd) = %zu\n", offsetof(struct mixed, simd));
    printf("offsetof(struct mixed, raw)  = %zu\n", offsetof(struct mixed, raw));

    _Alignas(16) char local[16];
    alignas(alignof(max_align_t)) unsigned char arena[128];
    printf("local aligned to 16: %s\n",
           ((uintptr_t)local % 16 == 0) ? "yes" : "no");
    printf("arena aligned to max_align_t: %s\n",
           ((uintptr_t)arena % alignof(max_align_t) == 0) ? "yes" : "no");
    printf("file_scope_buf aligned to 32: %s\n",
           ((uintptr_t)file_scope_buf % 32 == 0) ? "yes" : "no");

    /* aligned_alloc: size must be a multiple of alignment */
    double *v = aligned_alloc(64, 64 * sizeof(double));
    if (v) {
        printf("aligned_alloc(64): %s\n", ((uintptr_t)v % 64 == 0) ? "ok" : "bad");
        free(v);
    }
    printf("__alignas_is_defined=%d __alignof_is_defined=%d\n",
           __alignas_is_defined, __alignof_is_defined);
    return 0;
}
