/*
 * Feature : Ranges library - views, pipes, range algorithms, projections
 * Version : C++20
 * Spec    : P0896R4 "The One Ranges Proposal"; P1035R7 (input range
 *           adaptors); P2210R2
 *
 * Mostly a LIBRARY feature, but it stresses the parser with heavy template
 * argument deduction, overloaded `|`, niebloids, and nested lambdas.
 *
 * Parser edge cases:
 *  - `v | std::views::filter(pred) | std::views::transform(f)` - operator|
 *    chains on objects, not integers.
 *  - Projections as pointers-to-member: `std::ranges::sort(v, {}, &T::m);`
 *    - `{}` as an argument (value-initialized comparator).
 *  - Range-for over a temporary view expression.
 *  - Deeply nested template types with `>>` closing: `std::vector<std::
 *    vector<int>>` (fine since C++11 but still a classic trap).
 *  - Unbounded `std::views::iota(1)` with `| std::views::take(n)`.
 */
#include <iostream>
#include <ranges>
#include <vector>
#include <string>
#include <algorithm>
#include <map>

struct Employee {
    std::string name;
    std::string dept;
    int salary;
};

int main() {
    std::vector<int> nums{5, 12, 7, 3, 18, 9, 20, 1};

    auto even_squares = nums
        | std::views::filter([](int n) { return n % 2 == 0; })
        | std::views::transform([](int n) { return n * n; });
    std::cout << "even squares:";
    for (int x : even_squares) std::cout << ' ' << x;
    std::cout << '\n';

    std::cout << "first 5 odd numbers from iota:";
    for (int x : std::views::iota(1)
                 | std::views::filter([](int n) { return n % 2; })
                 | std::views::take(5))
        std::cout << ' ' << x;
    std::cout << '\n';

    std::cout << "reverse + drop(2):";
    for (int x : nums | std::views::reverse | std::views::drop(2)) std::cout << ' ' << x;
    std::cout << '\n';

    std::cout << "take_while < 10:";
    for (int x : nums | std::views::take_while([](int n) { return n < 10; })) std::cout << ' ' << x;
    std::cout << '\n';

    // Range algorithms with projections
    std::vector<Employee> staff{
        {"ada", "eng", 180}, {"bob", "ops", 95}, {"cy", "eng", 140}, {"dee", "ops", 120}};
    std::ranges::sort(staff, {}, &Employee::salary);
    std::cout << "by salary:";
    for (const auto& e : staff) std::cout << ' ' << e.name << '(' << e.salary << ')';
    std::cout << '\n';

    std::ranges::sort(staff, std::ranges::greater{}, &Employee::name);
    std::cout << "by name desc:";
    for (const auto& e : staff | std::views::transform(&Employee::name)) std::cout << ' ' << e;
    std::cout << '\n';

    auto it = std::ranges::find(staff, "cy", &Employee::name);
    if (it != staff.end()) std::cout << "found cy in " << it->dept << '\n';

    auto rich = std::ranges::count_if(staff, [](int s) { return s > 100; }, &Employee::salary);
    std::cout << "earning > 100: " << rich << '\n';
    auto [mn, mx] = std::ranges::minmax(staff, {}, &Employee::salary);
    std::cout << "min " << mn.name << ", max " << mx.name << '\n';

    // keys / values / elements views over a map
    std::map<std::string, int> inventory{{"bolts", 40}, {"nuts", 25}, {"washers", 60}};
    std::cout << "keys:";
    for (const auto& k : std::views::keys(inventory)) std::cout << ' ' << k;
    std::cout << "\nvalues total: ";
    int total = 0;
    for (int v : inventory | std::views::values) total += v;
    std::cout << total << '\n';

    // Nested containers and join
    std::vector<std::vector<int>> grid{{1, 2}, {3}, {}, {4, 5, 6}};
    std::cout << "joined:";
    for (int x : grid | std::views::join) std::cout << ' ' << x;
    std::cout << '\n';

    // split a string view into words
    std::string text = "ranges make pipelines readable";
    std::cout << "words:";
    for (auto word : text | std::views::split(' ')) {
        std::cout << " [" << std::string_view(word.begin(), word.end()) << ']';
    }
    std::cout << '\n';

    // Range concepts
    static_assert(std::ranges::random_access_range<std::vector<int>>);
    static_assert(std::ranges::view<decltype(std::views::iota(0, 5))>);
    std::cout << "iota(0,5) size = " << std::ranges::size(std::views::iota(0, 5)) << '\n';
    return 0;
}
