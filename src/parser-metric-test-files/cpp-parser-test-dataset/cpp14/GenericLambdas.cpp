/*
 * Feature : Generic lambdas and init-captures
 * Version : C++14 (ISO/IEC 14882:2014)
 * Spec    : N3649 (generic lambdas), N3648 (init-capture / generalized
 *           lambda capture)
 *
 * A lambda parameter declared `auto` makes operator() a template.
 * Init-captures `[x = expr]` / `[&r = expr]` / `[p = std::move(p)]`
 * introduce new closure members initialized from arbitrary expressions.
 *
 * Parser edge cases:
 *  - `auto` in a lambda parameter list is C++14; in C++11 it's an error.
 *  - Variadic generic lambdas: `[](auto&&... args)`.
 *  - Init-capture with move: `[u = std::move(ptr)]` - `=` inside [].
 *  - `[&r = x]` reference init-capture vs `[&x]` simple capture.
 *  - Nested lambdas returning lambdas (currying).
 *  - Explicit template parameter lists `[]<typename T>(T)` are C++20,
 *    not C++14.
 */
#include <iostream>
#include <memory>
#include <string>
#include <vector>
#include <algorithm>
#include <utility>

int main() {
    // Basic generic lambda: works for any type with operator+
    auto add = [](auto a, auto b) { return a + b; };
    std::cout << "add ints:    " << add(2, 3) << '\n';
    std::cout << "add doubles: " << add(1.5, 2.25) << '\n';
    std::cout << "add strings: " << add(std::string("con"), "cat") << '\n';

    // Generic lambda with forwarding reference and decltype(auto) return
    auto first = [](auto&& container) -> decltype(auto) {
        return std::forward<decltype(container)>(container).front();
    };
    std::vector<int> v{5, 3, 9, 1};
    first(v) = 50;
    std::cout << "first(v) after assign: " << v.front() << '\n';

    // Variadic generic lambda
    auto count_args = [](auto&&... args) { return sizeof...(args); };
    std::cout << "count_args: " << count_args(1, 'a', "x", 2.0) << '\n';

    // Generic comparator passed to an algorithm
    std::sort(v.begin(), v.end(), [](const auto& l, const auto& r) { return l > r; });
    std::cout << "sorted desc:";
    for (const auto& x : v) std::cout << ' ' << x;
    std::cout << '\n';

    // Init-capture: new variable computed at capture time
    int base = 10;
    auto offset = [delta = base * 2](int x) { return x + delta; };
    base = 1000;  // does not affect delta
    std::cout << "offset(5): " << offset(5) << '\n';

    // Init-capture with move of a move-only type
    auto ptr = std::make_unique<std::string>("moved into lambda");
    auto owner = [p = std::move(ptr)] { return *p; };
    std::cout << "owner(): " << owner() << " (ptr is "
              << (ptr ? "set" : "null") << ")\n";

    // Reference init-capture
    int counter = 0;
    auto bump = [&c = counter](int n) { c += n; };
    bump(3); bump(4);
    std::cout << "counter: " << counter << '\n';

    // Mutable lambda with init-capture as internal state
    auto next_id = [id = 0]() mutable { return ++id; };
    std::cout << "ids:";
    for (int i = 0; i < 3; ++i) std::cout << ' ' << next_id();
    std::cout << '\n';

    // Currying: generic lambda returning a generic lambda
    auto curry_mul = [](auto a) { return [a](auto b) { return a * b; }; };
    auto triple = curry_mul(3);
    std::cout << "triple(7): " << triple(7) << ", triple(1.5): " << triple(1.5) << '\n';

    // Generic lambda calling itself via an auto& parameter (Y-combinator style)
    auto fact = [](auto& self, int n) -> long { return n <= 1 ? 1 : n * self(self, n - 1); };
    std::cout << "fact(10): " << fact(fact, 10) << '\n';
    return 0;
}
