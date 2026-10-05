// Feature : C++20 modules - primary module interface unit
// Version : C++20
// Spec    : P1103R3 "Merging Modules", P1766R1, P1811R0, P1815R2
//
// A module interface unit begins (after an optional global module
// fragment) with `export module name;`. Declarations are exported with
// `export`, an `export { ... }` block, or `export namespace`. A
// `module :private;` fragment ends the interface and holds implementation.
//
// Self-contained: compile alone with
//   clang++ -std=c++20 --precompile math_utils.cppm -o math_utils.pcm
//   g++ -std=c++20 -fmodules-ts -c -x c++ math_utils.cppm
//
// Parser edge cases:
//  - `module;` on its own line opens the GLOBAL MODULE FRAGMENT; only
//    preprocessor directives may appear before `export module`.
//  - `module` and `import` are CONTEXT-SENSITIVE keywords: `int module = 1;`
//    is still legal elsewhere. They act as directives only at the start of
//    a logical line in specific forms (P1703R1, P1857R3).
//  - `export` was a reserved-but-unused keyword in C++11-17 (removed
//    `export template`), now meaningful again.
//  - Dotted module names `export module math.utils;` are a single name, not
//    member access.
//  - `module :private;` - colon after `module`, no name.
//  - Extensions: .cppm (Clang convention), .ixx (MSVC), .mpp, .cxxm; GCC
//    accepts any extension with -fmodules-ts.
module;

#include <cmath>
#include <cstdint>

export module math.utils;

// Exported constant and function
export constexpr double golden_ratio = 1.6180339887498948482;

export int gcd(int a, int b);

// Export block
export {
    struct Vec2 {
        double x = 0, y = 0;
        double length() const { return std::sqrt(x * x + y * y); }
        Vec2 operator+(const Vec2& o) const { return {x + o.x, y + o.y}; }
    };

    template <typename T>
    constexpr T clamp(T v, T lo, T hi) { return v < lo ? lo : (v > hi ? hi : v); }

    enum class Rounding { Down, Nearest, Up };
}

// Exported namespace: everything inside is exported
export namespace math::stats {
    double mean(const double* data, std::size_t n);

    template <std::size_t N>
    constexpr double mean(const double (&arr)[N]) { return mean(arr, N); }
}

// Not exported: module-linkage helper, invisible to importers
namespace detail {
    constexpr std::int64_t abs64(std::int64_t v) { return v < 0 ? -v : v; }
}

export double round_with(double v, Rounding mode) {
    switch (mode) {
    case Rounding::Down:    return std::floor(v);
    case Rounding::Up:      return std::ceil(v);
    case Rounding::Nearest: return std::round(v);
    }
    return v;
}

// Context-sensitive keywords used as ordinary identifiers
export inline int count_modules(int module, int import) { return module + import; }

module :private;

// Private module fragment: definitions not visible to importers' reachability
int gcd(int a, int b) {
    a = static_cast<int>(detail::abs64(a));
    b = static_cast<int>(detail::abs64(b));
    while (b != 0) { int t = a % b; a = b; b = t; }
    return a;
}

double math::stats::mean(const double* data, std::size_t n) {
    if (n == 0) return 0.0;
    double s = 0;
    for (std::size_t i = 0; i < n; ++i) s += data[i];
    return s / static_cast<double>(n);
}
