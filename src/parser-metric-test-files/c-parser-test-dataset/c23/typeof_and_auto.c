/*
 * Feature : typeof, typeof_unqual, and `auto` type inference
 * Version : C23
 * Spec    : N2927 (typeof), N2930 (typeof_unqual), N3007 (auto inference)
 *
 * `typeof(expr)` / `typeof(type)` yields a type. `typeof_unqual` also strips
 * qualifiers (const, volatile, restrict, _Atomic). `auto x = expr;` deduces
 * the type of a single object from its initializer.
 *
 * Parser edge cases:
 *  - `auto` was a storage-class specifier since K&R. In C23, `auto x = 1;`
 *    (no type specifier) means inference. `auto int x;` is still storage
 *    class (and still legal).
 *  - `auto` inference: one declarator only, no arrays (`auto a[] = ...` is
 *    invalid), the initializer is not a braced list.
 *  - typeof operand: type-name OR expression - ambiguous `typeof(T)` when T
 *    could be either (resolved by symbol table).
 *  - typeof(expr) does not evaluate expr unless it has VM type.
 *  - `__typeof__` (GNU) is NOT the same token; it's an extension.
 */
#include <stdio.h>

#define SWAP(a, b) do { typeof(a) tmp_ = (a); (a) = (b); (b) = tmp_; } while (0)
#define MAX(a, b) ((a) > (b) ? (a) : (b))
#define TYPE_NAME(x) _Generic((x), int: "int", long: "long", double: "double", \
    float: "float", unsigned: "unsigned", char *: "char *", \
    const char *: "const char *", default: "other")

struct point { int x, y; };

static double area(double w, double h) { return w * h; }

int main(void)
{
    /* typeof with an expression */
    int a = 10, b = 20;
    SWAP(a, b);
    printf("after SWAP: a=%d b=%d\n", a, b);

    double da = 1.5, db = 2.5;
    SWAP(da, db);
    printf("after SWAP: da=%.1f db=%.1f\n", da, db);

    /* typeof with a type name, including complex declarators */
    typeof(int[4]) arr = { 1, 2, 3, 4 };
    typeof(double (*)(double, double)) fn = area;
    typeof(fn) fn2 = fn;
    printf("sizeof arr = %zu, fn2(3,4) = %.0f\n", sizeof arr, fn2(3, 4));

    /* typeof does not evaluate its operand */
    int counter = 0;
    typeof(counter++) copy = 7;
    printf("counter still %d, copy=%d\n", counter, copy);

    /* typeof keeps qualifiers, typeof_unqual removes them */
    const volatile int cv = 42;
    typeof_unqual(cv) mutable_copy = cv;
    mutable_copy += 1;
    printf("typeof_unqual copy = %d\n", mutable_copy);
    static_assert(_Generic(&mutable_copy, int *: 1, default: 0));

    /* auto inference */
    auto i = 5;            /* int */
    auto l = 5L;           /* long */
    auto d = 0.5 * 3;      /* double */
    auto u = 3u;           /* unsigned */
    auto s = "literal";    /* char * (array decays) */
    auto p = (struct point){ 1, 2 };
    auto pp = &p;
    printf("i:%s l:%s d:%s u:%s s:%s\n", TYPE_NAME(i), TYPE_NAME(l),
           TYPE_NAME(d), TYPE_NAME(u), TYPE_NAME(s));
    printf("p=(%d,%d) via auto pointer: %d\n", p.x, p.y, pp->y);

    /* auto in a for-init declaration */
    for (auto k = 0; k < 3; k++) printf("k=%d ", k);
    printf("\n");

    /* Classic storage-class auto still valid */
    auto int classic = MAX(3, 9);
    printf("classic auto int = %d\n", classic);
    return 0;
}
