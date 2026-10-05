/*
 * Feature : Header-only "template" via macros; #pragma once
 * Version : C11 (uses _Static_assert, _Alignof); header-only
 * Spec    : C17 6.10.3 (macro replacement), 6.10.6 (#pragma),
 *           6.7.10 (static assertions)
 *
 * A macro generates a complete typed ring buffer (struct + functions) for
 * any element type. This is how many C libraries emulate templates
 * (e.g. klib, stb, BSD queue.h). Not runnable alone; accept with
 * `cc -std=c11 -fsyntax-only -x c typed_ring_buffer.h`.
 *
 * Parser edge cases:
 *  - `#pragma once` is NOT standard C but universally supported - a strict
 *    parser must still accept it (unknown pragmas are ignored, 6.10.6).
 *  - Function definitions generated entirely by macro expansion: a parser
 *    that does not expand macros sees `RING_DEFINE(int, ring_int)` at file
 *    scope - which looks like a call without a semicolon.
 *  - Very long macro bodies with backslash continuation.
 *  - Token pasting to build function names: name##_push.
 */
#pragma once

#include <stdbool.h>
#include <stddef.h>
#include <string.h>

#define RING_DEFINE(T, name)                                                  \
    typedef struct name {                                                     \
        T *data;                                                              \
        size_t head, tail, count, cap;                                        \
    } name;                                                                   \
                                                                              \
    _Static_assert(_Alignof(T) > 0, #T " must be a complete type");           \
                                                                              \
    static inline void name##_init(name *rb, T *storage, size_t cap)          \
    {                                                                         \
        rb->data = storage;                                                   \
        rb->head = rb->tail = rb->count = 0;                                  \
        rb->cap = cap;                                                        \
    }                                                                         \
                                                                              \
    static inline bool name##_push(name *rb, T value)                         \
    {                                                                         \
        if (rb->count == rb->cap) return false;                               \
        rb->data[rb->tail] = value;                                           \
        rb->tail = (rb->tail + 1) % rb->cap;                                  \
        rb->count++;                                                          \
        return true;                                                          \
    }                                                                         \
                                                                              \
    static inline bool name##_pop(name *rb, T *out)                           \
    {                                                                         \
        if (rb->count == 0) return false;                                     \
        *out = rb->data[rb->head];                                            \
        rb->head = (rb->head + 1) % rb->cap;                                  \
        rb->count--;                                                          \
        return true;                                                          \
    }                                                                         \
                                                                              \
    static inline bool name##_peek(const name *rb, T *out)                    \
    {                                                                         \
        if (rb->count == 0) return false;                                     \
        memcpy(out, &rb->data[rb->head], sizeof(T));                          \
        return true;                                                          \
    }                                                                         \
                                                                              \
    static inline size_t name##_size(const name *rb) { return rb->count; }    \
    static inline bool name##_empty(const name *rb) { return !rb->count; }    \
    static inline bool name##_full(const name *rb)                            \
    {                                                                         \
        return rb->count == rb->cap;                                          \
    }

/* Pre-instantiated buffers for common types */
RING_DEFINE(int, ring_int)
RING_DEFINE(double, ring_double)

typedef struct ring_event { int code; const char *msg; } ring_event;
RING_DEFINE(ring_event, ring_events)

/* Iteration helper macro: declares a loop over the live elements */
#define RING_FOR_EACH(rb, idx_var)                                            \
    for (size_t idx_var##_n = 0, idx_var = (rb)->head;                        \
         idx_var##_n < (rb)->count;                                           \
         idx_var##_n++, idx_var = (idx_var + 1) % (rb)->cap)
