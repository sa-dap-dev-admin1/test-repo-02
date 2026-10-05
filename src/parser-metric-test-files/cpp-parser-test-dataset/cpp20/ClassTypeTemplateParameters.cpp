/*
 * Feature : Class types and floating-point as non-type template
 *           parameters, string-literal template arguments, template
 *           argument deduction for aggregates and alias templates,
 *           `typename` made optional in more contexts
 * Version : C++20
 * Spec    : P1907R1 (inconsistencies with NTTPs: class types, floats),
 *           P0732R2 (class types in NTTPs), P1816R0 / P2082R1 (CTAD for
 *           aggregates), P1814R0 (CTAD for alias templates), P0634R3
 *           (down with typename!)
 *
 * Parser edge cases:
 *  - `template <FixedString S>` - a CLASS type as an NTTP; the argument can
 *    be a string literal: `Tag<"hello">`.
 *  - `template <double D>` - floating-point NTTP (ill-formed in C++17).
 *  - `template <auto V>` with a structural class type argument.
 *  - `typename` omitted in return types / member declarations where only a
 *    type can appear: `T::value_type get();` inside a template.
 *  - Aggregate CTAD: `Pair p{1, 2.0};` with no constructor or guide.
 */
#include <iostream>
#include <string_view>
#include <algorithm>
#include <cstddef>
#include <vector>

template <std::size_t N>
struct FixedString {
    char data[N]{};
    constexpr FixedString(const char (&s)[N]) { std::copy_n(s, N, data); }
    constexpr std::string_view view() const { return {data, N - 1}; }
    constexpr std::size_t size() const { return N - 1; }
};

template <FixedString Name>
struct Tag {
    static constexpr std::string_view name() { return Name.view(); }
};

template <FixedString Fmt>
constexpr std::size_t count_placeholders() {
    std::size_t n = 0;
    for (std::size_t i = 0; i + 1 < Fmt.size() + 1; ++i)
        if (Fmt.data[i] == '{' && Fmt.data[i + 1] == '}') ++n;
    return n;
}

template <double Factor>
double scale(double x) { return x * Factor; }

struct Rgb { int r, g, b; };                         // structural type

template <Rgb C>
constexpr int brightness() { return (C.r * 299 + C.g * 587 + C.b * 114) / 1000; }

// Aggregate CTAD (no constructor, no deduction guide)
template <typename A, typename B>
struct Pair { A first; B second; };

// Alias template CTAD
template <typename T>
using IntPairWith = Pair<int, T>;

// `typename` no longer required in these type-only contexts
template <typename C>
struct Inspector {
    using value_type = C::value_type;                // P0634
    C::size_type count(const C& c) const { return c.size(); }
    static C::value_type first(const C& c) { return c.front(); }
};

int main() {
    std::cout << "Tag<\"widget\">::name() = " << Tag<"widget">::name() << '\n';
    std::cout << "Tag<\"a b c\">::name().size() = " << Tag<"a b c">::name().size() << '\n';
    constexpr auto n = count_placeholders<"x={}, y={}, z={}">();
    static_assert(n == 3);
    std::cout << "placeholders = " << n << '\n';

    std::cout << "scale<2.5>(4) = " << scale<2.5>(4) << '\n';
    std::cout << "scale<-0.5>(8) = " << scale<-0.5>(8) << '\n';

    std::cout << "brightness<Rgb{255,255,255}> = " << brightness<Rgb{255, 255, 255}>() << '\n';
    std::cout << "brightness<{0,0,255}> = " << brightness<{0, 0, 255}>() << '\n';

    Pair p{1, 2.5};                                   // Pair<int, double>
    Pair q{"text", 'c'};                              // Pair<const char*, char>
    std::cout << "Pair p: " << p.first << ", " << p.second << " | q: " << q.first << ", " << q.second << '\n';

    IntPairWith ip{7, std::string_view("alias CTAD")};
    std::cout << "IntPairWith: " << ip.first << ", " << ip.second << '\n';

    std::vector<int> v{9, 8, 7};
    Inspector<std::vector<int>> insp;
    std::cout << "Inspector count = " << insp.count(v) << ", first = " << insp.first(v) << '\n';
    return 0;
}
