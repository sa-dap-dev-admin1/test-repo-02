/*
 * Feature : Variable length arrays (VLAs) and variably modified types
 * Version : C99 (6.7.5.2 "Array declarators"); optional since C11
 *           (__STDC_NO_VLA__). C23 makes VM *types* mandatory again but
 *           automatic VLA *objects* remain optional.
 * Spec    : N1256 6.7.5.2p4, N2778 (C23 variably-modified types)
 *
 * The array size can be a run-time expression. Parameters can use earlier
 * parameters in their size, and `[*]` appears in prototypes.
 *
 * Parser edge cases:
 *  - `int a[n]` where n is not a constant: legal at block scope only.
 *  - `void f(int n, int m, double a[n][m])` - size refers to earlier params.
 *  - `void f(int n, double a[*][*]);` - `[*]` only in prototypes.
 *  - `void f(double a[static 10])` - `static` inside [] (C99) means
 *    "at least 10 elements". Also `a[const n]`, `a[restrict]`.
 *  - `sizeof` on a VLA type is evaluated at run time.
 */
#include <stdio.h>

/* Prototypes using [*] (unspecified VLA size in prototype scope) */
void fill(int rows, int cols, double m[*][*]);
double trace(int n, double m[n][n]);

/* static and type-qualifiers inside array parameter brackets */
static double sum_at_least_4(const double v[static 4])
{
    return v[0] + v[1] + v[2] + v[3];
}

static void scale(int n, double v[const n], double k)
{
    for (int i = 0; i < n; i++) v[i] *= k;
}

void fill(int rows, int cols, double m[rows][cols])
{
    for (int i = 0; i < rows; i++)
        for (int j = 0; j < cols; j++)
            m[i][j] = i * 10 + j;
}

double trace(int n, double m[n][n])
{
    double t = 0;
    for (int i = 0; i < n; i++) t += m[i][i];
    return t;
}

static void print_matrix(int rows, int cols, double (*m)[cols])
{
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) printf("%5.0f", m[i][j]);
        printf("\n");
    }
}

int main(void)
{
    for (int n = 2; n <= 4; n++) {
        double sq[n][n];                 /* VLA object */
        fill(n, n, sq);
        printf("n=%d sizeof(sq)=%zu trace=%.0f\n", n, sizeof sq, trace(n, sq));
    }

    int rows = 3, cols = 5;
    double grid[rows][cols];
    fill(rows, cols, grid);
    print_matrix(rows, cols, grid);

    /* typedef of a variably modified type: size frozen at typedef time */
    int k = 4;
    typedef int row_t[k];
    k = 100;                              /* does not affect row_t */
    row_t r;
    printf("sizeof(row_t) = %zu (k is now %d)\n", sizeof r, k);

    /* Pointer to VLA */
    double (*pg)[cols] = grid;
    printf("pg[2][4] = %.0f\n", pg[2][4]);

    double v[] = { 1.5, 2.5, 3.5, 4.5, 5.5 };
    printf("sum_at_least_4 = %.1f\n", sum_at_least_4(v));
    scale(5, v, 2.0);
    printf("scaled v[4] = %.1f\n", v[4]);

    /* sizeof of a VLA type with side effects IS evaluated */
    int counter = 0;
    size_t sz = sizeof(int[++counter + 2]);
    printf("sz=%zu counter=%d\n", sz, counter);
    return 0;
}
