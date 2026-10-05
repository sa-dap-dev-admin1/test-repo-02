/*
 * Feature : Designated initializers, consteval, constinit, using enum,
 *           [[likely]]/[[unlikely]], [[no_unique_address]], char8_t,
 *           range-for with initializer, explicit(bool), nested inline
 *           namespaces, __VA_OPT__, std::span, std::format
 * Version : C++20
 * Spec    : P0329R4 (designated init), P1073R3 (consteval), P1143R2
 *           (constinit), P1099R5 (using enum), P0479R5 (likely/unlikely),
 *           P0840R2 (no_unique_address), P0482R6 (char8_t), P0614R1
 *           (range-for init), P0892R2 (explicit(bool)), P1094R2 (nested
 *           inline namespaces), P0306R4 (__VA_OPT__), P0645R10 (format)
 *
 * Parser edge cases:
 *  - C++20 designated initializers must be in DECLARATION ORDER and cannot
 *    be nested (`.a.b = 1`) or mixed with positional, unlike C99.
 *  - `using enum Color;` inside a function or switch scope.
 *  - `[[likely]]` placed before a statement / case label.
 *  - `for (auto v = make(); auto& x : v)` - init-statement in range-for.
 *  - `explicit(expr)` - parenthesized condition after explicit.
 *  - `namespace a::inline b { }`.
 *  - `u8"..."` now has type const char8_t[] (breaking change vs C++17).
 */
#include <iostream>
#include <string>
#include <vector>
#include <span>
#include <format>
#include <type_traits>
#include <numeric>

struct Config {
    int width = 80;
    int height = 24;
    bool color = true;
    const char* title = "untitled";
};

enum class Color { Red, Green, Blue };

consteval int square_ct(int x) { return x * x; }   // must be evaluated at compile time
constinit int g_counter = square_ct(4);            // static init guaranteed, still mutable

struct Empty {};
struct Compact {
    int id;
    [[no_unique_address]] Empty tag;               // may occupy no storage
};

template <typename T>
struct Wrapper {
    T value;
    explicit(!std::is_convertible_v<T, int>) Wrapper(T v) : value(v) {}
};

namespace lib::inline v2 {
    inline const char* version() { return "v2"; }
}

#define LOG(fmt, ...) std::cout << std::format(fmt __VA_OPT__(,) __VA_ARGS__) << '\n'

const char* color_name(Color c) {
    switch (c) {
        using enum Color;                          // brings Red/Green/Blue into scope
        case Red:   return "red";
        case Green: return "green";
        case Blue:  return "blue";
    }
    return "?";
}

int sign(int v) {
    if (v > 0) [[likely]] return 1;
    else if (v < 0) [[unlikely]] return -1;
    return 0;
}

double average(std::span<const int> values) {
    if (values.empty()) return 0.0;
    return std::accumulate(values.begin(), values.end(), 0.0) / values.size();
}

int main() {
    Config a{.width = 120, .color = false};                 // height, title default
    Config b{.height = 50, .title = "logs"};
    LOG("a: {}x{} color={} title={}", a.width, a.height, a.color, a.title);
    LOG("b: {}x{} color={} title={}", b.width, b.height, b.color, b.title);

    constexpr int sq = square_ct(12);
    g_counter += sq;
    LOG("consteval square_ct(12) = {}, constinit g_counter = {}", sq, g_counter);

    for (Color c : {Color::Red, Color::Green, Color::Blue}) LOG("color: {}", color_name(c));
    LOG("sign: {} {} {}", sign(5), sign(-3), sign(0));

    LOG("sizeof(Compact) = {} (sizeof(int) = {})", sizeof(Compact), sizeof(int));

    Wrapper<int> w1 = 5;                           // implicit: int convertible to int
    Wrapper<std::string> w2{"explicit"};           // explicit(true): must direct-init
    LOG("Wrapper values: {} {}", w1.value, w2.value);

    // Range-for with an init-statement
    for (std::vector<int> v{3, 1, 4, 1, 5}; int x : v) std::cout << x << ' ';
    std::cout << '\n';

    int data[] = {10, 20, 30, 40};
    std::span<int> whole(data);
    LOG("span size {}, first(2) avg {}, last(2) avg {}",
        whole.size(), average(whole.first(2)), average(whole.last(2)));

    const char8_t* utf8 = u8"char8_t text";
    LOG("char8_t string length = {}", std::char_traits<char8_t>::length(utf8));
    LOG("lib::version() = {} (inline namespace)", lib::version());
    LOG("no variadic args");
    LOG("{:>8}|{:<6}|{:^7}|{:08.3f}", "right", "left", "mid", 3.14159);
    return 0;
}
