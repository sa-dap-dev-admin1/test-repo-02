/*
 * Feature : Classic library header (.h) - include guards, extern "C"
 *           guard, opaque types, prototypes, static inline helpers
 * Version : C99 (static inline, // is avoided for C89-tolerant tooling)
 * Spec    : C17 6.7.4 (inline), 6.9 (external definitions), 6.2.2 (linkage)
 *
 * Header files contain only declarations plus static inline definitions.
 * They are not runnable on their own; a compiler accepts them with
 * `cc -fsyntax-only -x c geometry.h`.
 *
 * Parser edge cases:
 *  - Extension `.h` is ambiguous: C or C++? This header is valid in BOTH,
 *    using the `#ifdef __cplusplus extern "C" {` idiom. A C parser must
 *    skip the `extern "C"` group, which is not valid C.
 *  - Opaque (incomplete) struct type used only through pointers.
 *  - Function-pointer typedefs and prototypes with unnamed parameters.
 *  - `extern` object declarations without definitions.
 *  - Empty translation unit after preprocessing if the guard is defined.
 */
#ifndef GEOMETRY_H_INCLUDED
#define GEOMETRY_H_INCLUDED

#include <stddef.h>
#include <math.h>

#ifdef __cplusplus
extern "C" {
#endif

#define GEOM_VERSION_MAJOR 1
#define GEOM_VERSION_MINOR 4
#define GEOM_EPSILON 1e-9

typedef struct geom_point { double x, y; } geom_point;
typedef struct geom_rect  { geom_point min, max; } geom_rect;

/* Opaque type: definition lives in a .c file the parser never sees */
typedef struct geom_polygon geom_polygon;

typedef enum geom_status {
    GEOM_OK = 0,
    GEOM_ERR_ALLOC,
    GEOM_ERR_DEGENERATE,
    GEOM_ERR_RANGE
} geom_status;

/* Function-pointer typedefs */
typedef double (*geom_metric_fn)(geom_point, geom_point);
typedef void (*geom_visit_fn)(const geom_point *pt, size_t index, void *user);

/* Extern object declarations */
extern const geom_point GEOM_ORIGIN;
extern int geom_debug_level;

/* Prototypes: named, unnamed and pointer-to-array parameters */
geom_polygon *geom_polygon_create(size_t capacity);
void          geom_polygon_destroy(geom_polygon *);
geom_status   geom_polygon_add(geom_polygon *poly, geom_point pt);
size_t        geom_polygon_size(const geom_polygon *);
double        geom_polygon_area(const geom_polygon *poly);
void          geom_polygon_visit(const geom_polygon *poly,
                                 geom_visit_fn fn, void *user);
geom_status   geom_transform(size_t n, geom_point pts[], const double (*m)[3]);
const char   *geom_status_str(geom_status status);

/* static inline helpers are fine in headers (internal linkage per TU) */
static inline geom_point geom_make(double x, double y)
{
    geom_point p;
    p.x = x;
    p.y = y;
    return p;
}

static inline double geom_dist(geom_point a, geom_point b)
{
    double dx = a.x - b.x, dy = a.y - b.y;
    return sqrt(dx * dx + dy * dy);
}

static inline int geom_rect_contains(const geom_rect *r, geom_point p)
{
    return p.x >= r->min.x && p.x <= r->max.x &&
           p.y >= r->min.y && p.y <= r->max.y;
}

static inline geom_rect geom_rect_union(geom_rect a, geom_rect b)
{
    geom_rect u;
    u.min.x = a.min.x < b.min.x ? a.min.x : b.min.x;
    u.min.y = a.min.y < b.min.y ? a.min.y : b.min.y;
    u.max.x = a.max.x > b.max.x ? a.max.x : b.max.x;
    u.max.y = a.max.y > b.max.y ? a.max.y : b.max.y;
    return u;
}

#define GEOM_NEARLY_EQUAL(a, b) (fabs((a) - (b)) < GEOM_EPSILON)
#define GEOM_RECT_WIDTH(r)  ((r).max.x - (r).min.x)
#define GEOM_RECT_HEIGHT(r) ((r).max.y - (r).min.y)

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* GEOMETRY_H_INCLUDED */
