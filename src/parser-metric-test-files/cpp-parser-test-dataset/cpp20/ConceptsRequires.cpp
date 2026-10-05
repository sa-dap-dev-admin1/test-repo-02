/*
 * Feature : Concepts, requires-clauses and requires-expressions
 * Version : C++20 (ISO/IEC 14882:2020)
 * Spec    : P0734R0 "Wording Paper, C++ extensions for Concepts",
 *           P0857R0, P1084R2 (compound requirements), P1452R2
 *
 * `template <typename T> concept Name = constraint-expression;`
 * `requires` appears in two roles:
 *   - requires-CLAUSE: `template <class T> requires C<T> void f(T);`
 *   - requires-EXPRESSION: `requires (T a) { a + a; typename T::x; ... }`
 *
 * Parser edge cases:
 *  - `requires requires (T x) { ... }` - clause followed by an expression.
 *  - Four requirement kinds inside { }: simple `a + b;`, type
 *    `typename T::value_type;`, compound `{ a.size() } -> std::integral;`,
 *    nested `requires sizeof(T) > 1;`.
 *  - Trailing requires-clause after the declarator: `void f() requires X;`
 *  - Constrained template parameter: `template <std::integral T>`.
 *  - In a requires-clause, `&&`/`||` bind primary expressions; a function
 *    call like `requires is_ok<T>()` needs parentheses in some positions.
 *  - Overload resolution by constraint subsumption.
 */
#include <iostream>
#include <concepts>
#include <string>
#include <vector>
#include <list>
#include <type_traits>
#include <iterator>

template <typename T>
concept Numeric = std::integral<T> || std::floating_point<T>;

template <typename T>
concept Container = requires(T c) {
    typename T::value_type;                          // type requirement
    { c.size() } -> std::convertible_to<std::size_t>; // compound requirement
    c.begin();                                       // simple requirement
    c.end();
};

template <typename T>
concept RandomAccessContainer = Container<T> && requires(T c, std::size_t i) {
    { c[i] } -> std::same_as<typename T::value_type&>;
};

template <typename T>
concept Hashable = requires(T a) {
    { std::hash<T>{}(a) } noexcept -> std::convertible_to<std::size_t>;
};

template <typename T>
concept SmallTrivial = std::is_trivially_copyable_v<T> && requires {
    requires sizeof(T) <= 8;                         // nested requirement
};

// Constrained template parameter
template <Numeric T>
T clamp_abs(T v, T limit) { T a = v < 0 ? -v : v; return a > limit ? limit : a; }

// requires-clause after the template head
template <typename T>
    requires Container<T>
std::size_t count_items(const T& c) { return c.size(); }

// Trailing requires-clause
template <typename T>
auto middle(const T& c) requires RandomAccessContainer<T> { return c[c.size() / 2]; }

// Ad-hoc constraint: requires requires
template <typename T>
    requires requires(T x) { x.reserve(1u); }
void prepare(T& c, std::size_t n) { c.reserve(n); }

// Overloads selected by subsumption: the more constrained wins
template <Container T> std::string kind(const T&) { return "container"; }
template <RandomAccessContainer T> std::string kind(const T&) { return "random-access container"; }
template <typename T> std::string kind(const T&) { return "not a container"; }

// Constraints on member functions of a class template
template <typename T>
struct Stats {
    std::vector<T> data;
    T sum() const requires Numeric<T> {
        T s{};
        for (const auto& x : data) s += x;
        return s;
    }
    std::size_t total_length() const requires std::same_as<T, std::string> {
        std::size_t n = 0;
        for (const auto& x : data) n += x.size();
        return n;
    }
};

int main() {
    std::cout << "clamp_abs(-15, 10) = " << clamp_abs(-15, 10) << '\n';
    std::cout << "clamp_abs(2.5, 9.0) = " << clamp_abs(2.5, 9.0) << '\n';

    std::vector<int> v{1, 2, 3, 4, 5};
    std::list<char> l{'a', 'b'};
    std::cout << "count_items(v) = " << count_items(v) << ", count_items(l) = " << count_items(l) << '\n';
    std::cout << "middle(v) = " << middle(v) << '\n';
    prepare(v, 100);
    std::cout << "capacity >= 100: " << std::boolalpha << (v.capacity() >= 100) << '\n';

    std::cout << "kind(vector) = " << kind(v) << '\n';
    std::cout << "kind(list)   = " << kind(l) << '\n';
    std::cout << "kind(42)     = " << kind(42) << '\n';

    std::cout << "Hashable<std::string>: " << Hashable<std::string> << '\n';
    std::cout << "Hashable<std::vector<int>>: " << Hashable<std::vector<int>> << '\n';
    std::cout << "SmallTrivial<double>: " << SmallTrivial<double>
              << ", SmallTrivial<std::string>: " << SmallTrivial<std::string> << '\n';

    Stats<int> si{{1, 2, 3}};
    Stats<std::string> ss{{"ab", "cde"}};
    std::cout << "Stats<int>::sum = " << si.sum() << ", Stats<string>::total_length = "
              << ss.total_length() << '\n';

    // requires-expression used directly as a bool
    constexpr bool can_add_strings = requires(std::string a) { a + a; };
    std::cout << "can_add_strings: " << can_add_strings << '\n';
    return 0;
}
