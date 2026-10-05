/*
 * Feature : Pack indexing, `_` placeholder variables, structured binding
 *           as a condition, attributes on structured bindings,
 *           variadic friends
 * Version : C++26 (working draft; ISO publication expected 2026)
 * Spec    : P2662R3 (pack indexing), P2169R4 (nice placeholder with no
 *           name), P0963R3 (structured binding declaration as a
 *           condition), P0609R3 (attributes for structured bindings),
 *           P2893R3 (variadic friends)
 *
 * Compiler support: Clang 19+ (-std=c++2c), GCC 15+ for most features.
 *
 * Parser edge cases:
 *  - `Ts...[0]` and `args...[I]` - an ellipsis FOLLOWED by a subscript.
 *    Previously `T...[N]` was a pack expansion of an array declarator in a
 *    parameter list (deprecated/repurposed: P2662 notes the ambiguity).
 *  - `auto _ = f();` repeated in the same scope - `_` may be redeclared
 *    (name-independent declaration) but then cannot be referenced.
 *  - `if (auto [a, b] = f())` - structured binding in a condition, tested
 *    via the whole object's contextual bool conversion.
 *  - `auto [x [[maybe_unused]], y] = p;` - attribute after a binding name.
 *  - `friend Ts...;` - pack expansion in a friend declaration.
 */
#include <iostream>
#include <string>
#include <tuple>
#include <utility>
#include <mutex>

template <typename... Ts>
using First = Ts...[0];

template <typename... Ts>
using Last = Ts...[sizeof...(Ts) - 1];

template <std::size_t I, typename... Ts>
constexpr auto pick(Ts... args) { return args...[I]; }

template <typename... Ts>
constexpr auto first_and_last(Ts... args) {
    return std::pair{args...[0], args...[sizeof...(args) - 1]};
}

// A type that is contextually convertible to bool AND tuple-like
struct ParseResult {
    int value;
    std::string rest;
    bool ok;
    explicit operator bool() const { return ok; }
    template <std::size_t I> auto get() const {
        if constexpr (I == 0) return value; else return rest;
    }
};
template <> struct std::tuple_size<ParseResult> : std::integral_constant<std::size_t, 2> {};
template <> struct std::tuple_element<0, ParseResult> { using type = int; };
template <> struct std::tuple_element<1, ParseResult> { using type = std::string; };

ParseResult parse_leading_int(const std::string& s) {
    std::size_t i = 0;
    int v = 0;
    while (i < s.size() && s[i] >= '0' && s[i] <= '9') v = v * 10 + (s[i++] - '0');
    return {v, s.substr(i), i > 0};
}

// Variadic friends: grant access to every type in the pack
template <typename... Friends>
class Vault {
    friend Friends...;
    int secret_ = 1234;
};
struct Auditor;
struct Admin { static int peek(const Vault<Admin, Auditor>& v) { return v.secret_; } };
struct Auditor { static int peek(const Vault<Admin, Auditor>& v) { return v.secret_ * 2; } };

int main() {
    static_assert(std::is_same_v<First<int, char, double>, int>);
    static_assert(std::is_same_v<Last<int, char, double>, double>);
    std::cout << "pick<2>(10, 'x', 3.5, \"s\") = " << pick<2>(10, 'x', 3.5, "s") << '\n';
    auto [f, l] = first_and_last(1, 2, 3, 4, 5);
    std::cout << "first_and_last = " << f << ", " << l << '\n';

    // `_` placeholders: multiple in one scope, never referenced
    std::mutex m1, m2;
    auto _ = std::lock_guard{m1};
    auto _ = std::lock_guard{m2};                 // redeclaration is OK for `_`
    auto [_, second, _] = std::tuple{1, "kept", 3.0};
    std::cout << "middle element kept: " << second << '\n';

    // Structured binding as an if-condition (tests ParseResult::operator bool)
    for (std::string in : {"123abc", "xyz"}) {
        if (auto [value, rest] = parse_leading_int(in))
            std::cout << in << " -> value " << value << ", rest \"" << rest << "\"\n";
        else
            std::cout << in << " -> no leading integer\n";
    }

    // Attribute on an individual structured binding
    auto [x [[maybe_unused]], y] = std::pair{10, 20};
    std::cout << "y = " << y << '\n';

    Vault<Admin, Auditor> vault;
    std::cout << "Admin::peek = " << Admin::peek(vault) << ", Auditor::peek = " << Auditor::peek(vault) << '\n';
    return 0;
}
