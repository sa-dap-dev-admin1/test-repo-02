/*
 * Feature : Relaxed constexpr functions and variable templates
 * Version : C++14
 * Spec    : N3652 (relaxing constraints on constexpr functions),
 *           N3651 (variable templates)
 *
 * C++11 constexpr functions had to be a single return statement. C++14
 * allows local variables, loops, if/switch and mutation of locals.
 * Variable templates: `template<class T> constexpr T pi = T(3.14159...);`
 *
 * Parser edge cases:
 *  - A `template<...>` header followed by a VARIABLE declaration (not a
 *    class or function) - C++11 parsers reject this.
 *  - Explicit and partial specialization of variable templates.
 *  - `pi<float>` in an expression: `<` is a template-argument-list opener
 *    because `pi` names a variable template - needs name lookup.
 *  - constexpr member functions are no longer implicitly const (C++14).
 */
#include <iostream>
#include <cstddef>
#include <type_traits>

template <typename T>
constexpr T pi = T(3.1415926535897932385L);

template <typename T>
constexpr bool is_small = sizeof(T) <= 2;

// Explicit specialization of a variable template
template <>
constexpr bool is_small<bool> = false;

// Partial specialization for pointers
template <typename T>
constexpr bool is_small<T*> = false;

template <std::size_t N>
constexpr std::size_t factorial_v = N * factorial_v<N - 1>;
template <>
constexpr std::size_t factorial_v<0> = 1;

// Relaxed constexpr: loops, locals, branches, mutation
constexpr int count_primes(int limit) {
    int count = 0;
    for (int n = 2; n <= limit; ++n) {
        bool prime = true;
        for (int d = 2; d * d <= n; ++d) {
            if (n % d == 0) { prime = false; break; }
        }
        if (prime) ++count;
    }
    return count;
}

constexpr int digit_sum(long long v) {
    if (v < 0) v = -v;
    int s = 0;
    while (v > 0) { s += static_cast<int>(v % 10); v /= 10; }
    return s;
}

struct Counter {
    int value = 0;
    constexpr void inc() { ++value; }   // non-const constexpr member (C++14)
};

constexpr int counted(int n) {
    Counter c;
    for (int i = 0; i < n; ++i) c.inc();
    return c.value;
}

static_assert(count_primes(30) == 10, "10 primes below 30");
static_assert(digit_sum(-9875) == 29, "digit sum");
static_assert(factorial_v<5> == 120, "5!");
static_assert(counted(7) == 7, "counter");

int main() {
    std::cout.precision(10);
    std::cout << "pi<float>  = " << pi<float> << '\n';
    std::cout << "pi<double> = " << pi<double> << '\n';
    std::cout << "pi<int>    = " << pi<int> << '\n';
    std::cout << std::boolalpha;
    std::cout << "is_small<char>=" << is_small<char> << " is_small<int>=" << is_small<int>
              << " is_small<bool>=" << is_small<bool> << " is_small<char*>=" << is_small<char*> << '\n';
    std::cout << "factorial_v<10> = " << factorial_v<10> << '\n';

    constexpr int primes = count_primes(100);
    int arr[primes];                       // usable as array bound
    std::cout << "primes <= 100: " << primes << " (array size " << sizeof arr / sizeof arr[0] << ")\n";

    // Expression where '<' after pi must parse as template args
    double area = pi<double> * 2 * 2;
    bool cmp = pi<double> < 4.0;
    std::cout << "area r=2: " << area << ", pi<4: " << cmp << '\n';
    std::cout << "digit_sum(123456789) = " << digit_sum(123456789) << '\n';
    return 0;
}
