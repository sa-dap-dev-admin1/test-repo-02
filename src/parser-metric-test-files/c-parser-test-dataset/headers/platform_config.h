/*
 * Feature : Portability header - feature detection across C versions,
 *           compilers and platforms
 * Version : C99 baseline with C11/C23 feature detection
 * Spec    : C23 6.10.1 (__has_include, __has_c_attribute), C17 6.10.8
 *           (predefined macros: __STDC_VERSION__, __STDC_NO_VLA__,
 *           __STDC_NO_ATOMICS__, __STDC_NO_THREADS__)
 *
 * Every real codebase has one of these. Valid C99 through C23.
 * Accept with: `cc -std=c99 -fsyntax-only -x c platform_config.h`
 * (and again with -std=c11, -std=c17, -std=c23).
 *
 * Parser edge cases:
 *  - Deeply nested #if/#elif/#else with defined() and numeric comparisons.
 *  - `__has_include` / `__has_c_attribute` used ONLY after checking
 *    `defined(__has_include)` - otherwise older preprocessors error.
 *  - Macros that expand to attributes in three different syntaxes:
 *    [[nodiscard]] (C23), __attribute__((warn_unused_result)) (GNU),
 *    _Check_return_ (MSVC SAL) - only one is live per configuration.
 *  - Version-dependent keyword spellings (_Noreturn vs [[noreturn]],
 *    _Static_assert vs static_assert, _Bool vs bool).
 */
#ifndef PLATFORM_CONFIG_H
#define PLATFORM_CONFIG_H

/* ---- Language standard ---- */
#if defined(__STDC_VERSION__) && __STDC_VERSION__ >= 202311L
#  define PC_C23 1
#  define PC_C11 1
#elif defined(__STDC_VERSION__) && __STDC_VERSION__ >= 201112L
#  define PC_C23 0
#  define PC_C11 1
#else
#  define PC_C23 0
#  define PC_C11 0
#endif

/* ---- Compiler ---- */
#if defined(__clang__)
#  define PC_COMPILER "clang"
#elif defined(__GNUC__)
#  define PC_COMPILER "gcc"
#elif defined(_MSC_VER)
#  define PC_COMPILER "msvc"
#else
#  define PC_COMPILER "unknown"
#endif

/* ---- Platform ---- */
#if defined(_WIN32) || defined(_WIN64)
#  define PC_OS_WINDOWS 1
#  define PC_PATH_SEP '\\'
#elif defined(__APPLE__) && defined(__MACH__)
#  define PC_OS_MACOS 1
#  define PC_PATH_SEP '/'
#elif defined(__linux__)
#  define PC_OS_LINUX 1
#  define PC_PATH_SEP '/'
#else
#  define PC_PATH_SEP '/'
#endif

/* ---- bool ---- */
#if !PC_C23
#  include <stdbool.h>
#endif

/* ---- Static assertions ---- */
#if PC_C23
#  define PC_STATIC_ASSERT(cond, msg) static_assert(cond, msg)
#elif PC_C11
#  define PC_STATIC_ASSERT(cond, msg) _Static_assert(cond, msg)
#else
#  define PC_STATIC_ASSERT(cond, msg) \
       typedef char pc_static_assert_##__LINE__[(cond) ? 1 : -1]
#endif

/* ---- noreturn ---- */
#if PC_C23
#  define PC_NORETURN [[noreturn]]
#elif PC_C11
#  define PC_NORETURN _Noreturn
#elif defined(__GNUC__)
#  define PC_NORETURN __attribute__((noreturn))
#else
#  define PC_NORETURN
#endif

/* ---- nodiscard ---- */
/* Check PC_C23 too: GCC reports __has_c_attribute(nodiscard) even in C17
 * mode, where the [[...]] syntax is still rejected by -pedantic-errors. */
#if PC_C23 && defined(__has_c_attribute)
#  if __has_c_attribute(nodiscard)
#    define PC_NODISCARD [[nodiscard]]
#  endif
#endif
#ifndef PC_NODISCARD
#  if defined(__GNUC__)
#    define PC_NODISCARD __attribute__((warn_unused_result))
#  elif defined(_MSC_VER)
#    define PC_NODISCARD _Check_return_
#  else
#    define PC_NODISCARD
#  endif
#endif

/* ---- Optional headers ---- */
#if defined(__has_include)
#  if __has_include(<threads.h>) && !defined(__STDC_NO_THREADS__)
#    define PC_HAVE_THREADS 1
#  endif
#  if __has_include(<stdckdint.h>)
#    define PC_HAVE_CKDINT 1
#  endif
#endif
#ifndef PC_HAVE_THREADS
#  define PC_HAVE_THREADS 0
#endif
#ifndef PC_HAVE_CKDINT
#  define PC_HAVE_CKDINT 0
#endif

/* ---- VLA support (optional since C11) ---- */
#if defined(__STDC_NO_VLA__)
#  define PC_HAVE_VLA 0
#else
#  define PC_HAVE_VLA 1
#endif

PC_STATIC_ASSERT(sizeof(void *) >= 4, "need at least 32-bit pointers");

PC_NODISCARD int pc_parse_int(const char *text, int *out);
PC_NORETURN void pc_panic(const char *msg);
bool pc_is_absolute_path(const char *path);

#endif /* PLATFORM_CONFIG_H */
