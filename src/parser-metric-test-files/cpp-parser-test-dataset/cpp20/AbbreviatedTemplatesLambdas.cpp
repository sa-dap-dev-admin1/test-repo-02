/*
 * Feature : Abbreviated function templates (constrained auto params),
 *           template-parameter lists on lambdas, lambdas in unevaluated
 *           contexts, default-constructible stateless lambdas, pack
 *           expansion in lambda init-capture
 * Version : C++20
 * Spec    : P1141R2 (abbreviated templates / constrained auto),
 *           P0428R2 (familiar template syntax for generic lambdas),
 *           P0315R4 (lambdas in unevaluated contexts), P0624R2
 *           (default constructible and assignable stateless lambdas),
 *           P0780R2 (pack expansion in lambda init-capture)
 *
 * Parser edge cases:
 *  - `void f(std::integral auto x)` - concept-name followed by `auto` in
 *    a parameter: an abbreviated template.
 *  - `std::convertible_to<int> auto` - partial concept-id before auto.
 *  - Constrained auto in variable declarations and return types:
 *    `std::floating_point auto r = ...;`, `std::integral auto f();`
 *  - `[]<typename T>(std::vector<T> v)` - `<` directly after `]`.
 *  - `[]<typename T> requires std::integral<T> (T x)` - requires-clause
 *    between template params and the parameter list.
 *  - `decltype([]{})` - lambda in an unevaluated operand.
 *  - `[...xs = std::move(args)]` - pack init-capture.
 */
#include <iostream>
#include <concepts>
#include <vector>
#include <string>
#include <map>
#include <set>
#include <utility>

void show(std::integral auto x) { std::cout << "integral: " << x << '\n'; }
void show(std::floating_point auto x) { std::cout << "floating: " << x << '\n'; }
// Same parameter form (by value) so constraint subsumption can rank them
void show(auto x) { std::cout << "other: " << x << '\n'; }

auto scaled(std::convertible_to<double> auto x, std::convertible_to<double> auto k) {
    return static_cast<double>(x) * static_cast<double>(k);
}

std::integral auto next_even(std::integral auto n) { return n % 2 == 0 ? n + 2 : n + 1; }

// Variadic abbreviated template
auto sum_all(std::integral auto... xs) { return (0 + ... + xs); }

// A set ordered by a lambda type - lambda in unevaluated context (decltype)
using DescendingSet = std::set<int, decltype([](int a, int b) { return a > b; })>;

template <typename... Args>
auto delayed_sum(Args... args) {
    return [...xs = std::move(args)] { return (xs + ...); };   // pack init-capture
}

int main() {
    show(42);
    show(2.718);
    show(std::string("text"));
    show('c');                                   // char is integral

    std::cout << "scaled(3, 1.5) = " << scaled(3, 1.5) << '\n';
    std::floating_point auto r = scaled(2.0f, 4);
    std::cout << "constrained auto variable r = " << r << '\n';
    std::cout << "next_even(7) = " << next_even(7) << ", next_even(10L) = " << next_even(10L) << '\n';
    std::cout << "sum_all(1,2,3,4) = " << sum_all(1, 2, 3, 4) << '\n';

    // Template parameter list on a lambda
    auto vec_size = []<typename T>(const std::vector<T>& v) { return v.size(); };
    std::cout << "vec_size = " << vec_size(std::vector<double>{1, 2, 3}) << '\n';

    // Lambda with template params AND a requires-clause
    auto halve = []<typename T> requires std::integral<T> (T x) { return x / 2; };
    std::cout << "halve(9) = " << halve(9) << '\n';

    // Template lambda with explicit template argument at the call site
    auto make = []<typename T>() { return T{}; };
    auto empty_str = make.template operator()<std::string>();
    std::cout << "make<string>() size = " << empty_str.size() << '\n';

    // Using the pack to forward with perfect types
    auto call_with = []<typename F, typename... Ts>(F&& f, Ts&&... ts) {
        return std::forward<F>(f)(std::forward<Ts>(ts)...);
    };
    std::cout << "call_with(max) = " << call_with([](int a, int b) { return a > b ? a : b; }, 4, 9) << '\n';

    DescendingSet ds{5, 1, 9, 3};
    std::cout << "DescendingSet:";
    for (int x : ds) std::cout << ' ' << x;
    std::cout << '\n';

    // Stateless lambdas are default-constructible in C++20
    auto cmp = [](const std::string& a, const std::string& b) { return a.size() < b.size(); };
    decltype(cmp) cmp2;                          // default-construct
    std::map<std::string, int, decltype(cmp)> by_len;
    by_len["ccc"] = 3; by_len["a"] = 1; by_len["bb"] = 2;
    std::cout << "by length:";
    for (const auto& [k, v] : by_len) std::cout << ' ' << k;
    std::cout << " (cmp2(\"x\",\"yy\") = " << std::boolalpha << cmp2("x", "yy") << ")\n";

    auto later = delayed_sum(1, 2, 3, 4, 5);
    std::cout << "delayed_sum = " << later() << '\n';
    return 0;
}
