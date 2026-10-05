/*
 * Feature : Compound literals
 * Version : C99 (ISO/IEC 9899:1999, 6.5.2.5 "Compound literals")
 * Spec    : N1256 6.5.2.5
 *
 * `(type-name){ initializer-list }` creates an unnamed object of the given
 * type. At block scope it has automatic storage; at file scope, static.
 * Compound literals are lvalues, so you can take their address.
 *
 * Parser edge cases:
 *  - `(struct point){1, 2}` starts like a cast expression; the parser must
 *    see the `{` after the parenthesized type-name to know it's a literal.
 *  - `(int[]){1,2,3}` - array compound literal with deduced size.
 *  - Postfix operators apply directly: `(int[]){10,20}[1]`, `(struct s){0}.m`.
 *  - Designators are allowed inside: `(struct point){ .y = 1 }`.
 *  - C++ has no compound literals (GNU allows them as an extension).
 */
#include <stdio.h>
#include <string.h>

struct point { int x, y; };
struct line  { struct point a, b; };

/* File-scope compound literal: static storage duration */
static int *global_table = (int[]){ 100, 200, 300 };

static int manhattan(struct point p, struct point q)
{
    int dx = p.x - q.x, dy = p.y - q.y;
    return (dx < 0 ? -dx : dx) + (dy < 0 ? -dy : dy);
}

static int sum(const int *v, int n)
{
    int s = 0;
    for (int i = 0; i < n; i++) s += v[i];
    return s;
}

static void print_line(const struct line *l)
{
    printf("line (%d,%d)->(%d,%d)\n", l->a.x, l->a.y, l->b.x, l->b.y);
}

int main(void)
{
    /* Basic: pass struct by value without a named temporary */
    printf("manhattan = %d\n",
           manhattan((struct point){ 1, 2 }, (struct point){ 4, 6 }));

    /* Array literal passed as pointer, size deduced */
    printf("sum = %d\n", sum((int[]){ 1, 2, 3, 4, 5 }, 5));

    /* Designators inside a compound literal */
    struct point p = (struct point){ .y = 9 };
    printf("p = (%d,%d)\n", p.x, p.y);

    /* Address of a compound literal (it is an lvalue) */
    print_line(&(struct line){ .a = { 0, 0 }, .b = { 3, 4 } });

    /* Nested compound literals */
    struct line l = (struct line){ (struct point){ 1, 1 }, (struct point){ 2, 2 } };
    print_line(&l);

    /* Postfix operators applied directly to the literal */
    int second = (int[]){ 10, 20, 30 }[1];
    int ycoord = (struct point){ 5, 6 }.y;
    printf("second=%d ycoord=%d\n", second, ycoord);

    /* Modifiable: assigning through a pointer to the literal */
    int *arr = (int[4]){ 0 };
    for (int i = 0; i < 4; i++) arr[i] = i * i;
    printf("arr = %d %d %d %d\n", arr[0], arr[1], arr[2], arr[3]);

    /* const-qualified compound literal from a string literal */
    const char *msg = (const char[]){ "hello" };
    printf("msg = %s (len %zu)\n", msg, strlen(msg));

    /* Reassigning a struct from a compound literal */
    p = (struct point){ p.y, p.x };       /* swap via literal */
    printf("swapped p = (%d,%d)\n", p.x, p.y);

    /* sizeof applied to a compound literal */
    printf("sizeof literal = %zu\n", sizeof (double[3]){ 0 });

    printf("global_table[2] = %d\n", global_table[2]);

    /* Compound literal inside a loop body */
    for (int i = 0; i < 3; i++) {
        struct point *q = &(struct point){ i, i * 10 };
        printf("q[%d] = (%d,%d)\n", i, q->x, q->y);
    }
    return 0;
}
