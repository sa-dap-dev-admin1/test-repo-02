/*
 * Feature : Function return type deduction, decltype(auto), binary
 *           literals, digit separators, [[deprecated]], std::make_unique,
 *           standard user-defined literals
 * Version : C++14
 * Spec    : N3638 (return type deduction), N3472 (binary literals),
 *           N3781 (digit separators), N3760 ([[deprecated]]),
 *           N3656 (make_unique), N3642 (s, ms, h literals; "..."s)
 *
 * Parser edge cases:
 *  - `auto f() { ... }` with NO trailing return type (C++11 required ->).
 *  - `decltype(auto)` as a return type and variable type.
 *  - Digit separator `'` inside numbers: 1'000'000, 0b1010'1010, 0x'FF is
 *    invalid. A C++11 lexer starts a character literal at the quote.
 *  - `"text"s` and `250ms` - user-defined literal suffixes with no space;
 *    require `using namespace std::literals`.
 *  - `[[deprecated("msg")]]` on functions, classes, variables, enumerators
 *    (enumerator attributes came in C++17).
 */
#include <iostream>
#include <memory>
#include <string>
#include <chrono>
#include <vector>

using namespace std::literals;

auto square(int x) { return x * x; }          // deduced int

auto fib(int n) {                              // recursion after first return
    if (n < 2) return n;
    return fib(n - 1) + fib(n - 2);
}

std::vector<int> g_data{1, 2, 3};
auto get_copy() { return g_data[0]; }          // returns int (copy)
decltype(auto) get_ref() { return (g_data[0]); } // returns int& (parenthesized)

template <typename Container>
decltype(auto) element(Container& c, std::size_t i) { return c[i]; }

[[deprecated("use square() instead")]]
int old_square(int x) { return x * x; }

struct [[deprecated]] LegacyWidget { int id = 0; };

struct Node {
    std::string name;
    std::unique_ptr<Node> next;
    explicit Node(std::string n) : name(std::move(n)) {}
};

int main() {
    std::cout << "square(12) = " << square(12) << '\n';
    std::cout << "fib(20)    = " << fib(20) << '\n';

    get_ref() = 100;                             // writes through reference
    std::cout << "g_data[0] after get_ref()=100: " << get_copy() << '\n';

    std::vector<int> nums{4, 5, 6};
    element(nums, 1) = 50;                       // decltype(auto) keeps int&
    std::cout << "nums[1] = " << nums[1] << '\n';

    // Binary literals and digit separators
    int mask = 0b1111'0000;
    long long big = 9'223'372'036'854'775'807LL;
    double precise = 3.141'592'653'589;
    unsigned hex = 0xDEAD'BEEFu;
    std::cout << "mask=" << mask << " big=" << big << " hex=" << hex << '\n';
    std::cout.precision(13);
    std::cout << "precise=" << precise << '\n';

    // Standard library literals
    auto s = "std::string literal"s;
    auto timeout = 1500ms;
    auto hours = 2h;
    std::cout << s << " (size " << s.size() << ")\n";
    std::cout << "timeout = " << timeout.count() << "ms, "
              << std::chrono::duration_cast<std::chrono::minutes>(hours).count() << " minutes\n";

    // make_unique and a linked chain
    auto head = std::make_unique<Node>("first");
    head->next = std::make_unique<Node>("second");
    head->next->next = std::make_unique<Node>("third");
    for (const Node* n = head.get(); n; n = n->next.get()) std::cout << n->name << ' ';
    std::cout << '\n';

    auto arr = std::make_unique<int[]>(5);
    for (int i = 0; i < 5; ++i) arr[i] = i * 10;
    std::cout << "arr[4] = " << arr[4] << '\n';

    // decltype(auto) variable
    int value = 7;
    int& ref = value;
    decltype(auto) same_ref = ref;               // int&
    same_ref = 70;
    std::cout << "value via decltype(auto) ref = " << value << '\n';
    return 0;
}
