/*
 * Feature : nullptr and nullptr_t
 * Version : C23
 * Spec    : N3042 "Introduce the nullptr constant"; C23 6.4.4.6, 7.21.2
 *
 * `nullptr` is a predefined constant of type `nullptr_t` (from <stddef.h>,
 * where it is `typeof(nullptr)`). It converts to any pointer type and to
 * bool, but NOT to integers - unlike NULL, which may be plain 0.
 *
 * Parser edge cases:
 *  - `nullptr` is a KEYWORD in C23; an identifier in C17.
 *  - nullptr_t is a typedef in <stddef.h>, not a keyword.
 *  - `int i = nullptr;` is a constraint violation (good negative test).
 *  - `_Generic(nullptr, nullptr_t: ...)` selects its own association;
 *    `_Generic(NULL, ...)` may match `int` or `void *`.
 *  - Variadic calls: passing nullptr to a `...` function is safe for
 *    pointer parameters, unlike a bare 0.
 */
#include <stdio.h>
#include <stddef.h>
#include <stdarg.h>
#include <string.h>

#define KIND(x) _Generic((x),            \
    nullptr_t: "nullptr_t",              \
    void *: "void *",                    \
    int: "int",                          \
    long: "long",                        \
    default: "other")

struct node { int value; struct node *next; };

static int list_len(const struct node *n)
{
    int len = 0;
    for (; n != nullptr; n = n->next) len++;
    return len;
}

/* Variadic function terminated by a null pointer sentinel */
static size_t total_len(const char *first, ...)
{
    size_t sum = 0;
    va_list ap;
    va_start(ap, first);
    for (const char *s = first; s != nullptr; s = va_arg(ap, const char *))
        sum += strlen(s);
    va_end(ap);
    return sum;
}

static const char *find_char(const char *s, char c)
{
    if (s == nullptr) return nullptr;
    const char *p = strchr(s, c);
    return p ? p : nullptr;
}

int main(void)
{
    printf("KIND(nullptr) = %s\n", KIND(nullptr));
    printf("KIND(NULL)    = %s (implementation-defined)\n", KIND(NULL));
    printf("sizeof(nullptr_t) = %zu, sizeof(void*) = %zu\n",
           sizeof(nullptr_t), sizeof(void *));

    struct node c = { 3, nullptr }, b = { 2, &c }, a = { 1, &b };
    printf("list length = %d\n", list_len(&a));

    int *ip = nullptr;
    void (*fp)(void) = nullptr;
    nullptr_t np = nullptr;
    bool is_null = !ip;          /* nullptr converts to false */
    printf("ip null? %d  fp null? %d  np == nullptr? %d\n",
           is_null, fp == nullptr, np == nullptr);

    printf("total_len = %zu\n", total_len("ab", "cde", "f", nullptr));

    const char *hit = find_char("parser", 's');
    const char *miss = find_char(nullptr, 'x');
    printf("hit = %s, miss is %s\n", hit ? hit : "(none)",
           miss == nullptr ? "nullptr" : "set");

    /* Conditional operator: nullptr with a pointer yields that pointer type */
    int x = 5;
    int *maybe = (x > 3) ? &x : nullptr;
    printf("*maybe = %d\n", maybe ? *maybe : -1);
    return 0;
}
