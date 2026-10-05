/*
 * Feature : Classic C++ parsing ambiguities
 * Version : C++11 baseline (valid C++11 through C++23)
 * Spec    : C++11 [temp.names]p3 (">>" in template args, N1757),
 *           [dcl.ambig.res] (declaration vs expression), [temp.res]
 *           (`typename` / `template` disambiguators)
 *
 * Every construct here is valid, but a parser that guesses instead of
 * applying the standard's disambiguation rules will mis-parse it.
 *
 * Parser edge cases:
 *  1. `>>` closing two template argument lists (C++11; error in C++03).
 *  2. `>>` that IS a shift inside template args must be parenthesized:
 *     `Fixed<(16 >> 2)>`.
 *  3. Most vexing parse: `Timer t(Clock());` declares a FUNCTION.
 *  4. `T::type * x;` - multiplication or pointer declaration? Requires
 *     `typename` in a dependent context.
 *  5. `obj.template get<0>()` - `template` disambiguator after `.`/`->`/`::`.
 *  6. `<` / `>` as comparison vs template brackets next to template-ids.
 *  7. `(T)(x)` - C-style cast vs function-style call of parenthesized name.
 *  8. `int(x);` as a declaration of x vs a functional cast expression.
 *  9. Digraphs/alternative tokens: `<:` `:>` `<%` `%>` `and` `or` `not`.
 * 10. `<::` - since C++11 lexes as `<` `::` in `std::vector<::Foo>`.
 */
#include <iostream>
#include <vector>
#include <map>
#include <tuple>
#include <string>
#include <type_traits>

struct Foo { int v = 7; };

template <int N> struct Fixed { static constexpr int value = N; };

template <typename T>
struct Holder {
    using type = T;
    template <int I> T get() const { return T(I); }
};

struct Clock { int now() const { return 42; } };
struct Timer {
    explicit Timer(Clock c) : start(c.now()) {}
    int start;
};

template <typename T>
int dependent_names(const T& holder) {
    typename T::type value = holder.template get<5>();   // both disambiguators
    typename T::type* ptr = &value;                      // pointer, not multiply
    return static_cast<int>(*ptr);
}

namespace alt {
    bool both(bool a, bool b) <% return a and b; %>      // digraph braces + `and`
    bool either(bool a, bool b) { return a or not b; }
}

int main() {
    // 1. >> closes two template lists
    std::vector<std::vector<int>> grid{{1, 2}, {3}};
    std::map<std::string, std::vector<std::pair<int, int>>> deep;
    deep["k"].push_back({1, 2});
    std::cout << "grid[0][1] = " << grid[0][1] << ", deep size = " << deep.size() << '\n';

    // 2. Shift inside template args needs parentheses
    std::cout << "Fixed<(16 >> 2)>::value = " << Fixed<(16 >> 2)>::value << '\n';
    std::cout << "Fixed<(1 > 0)>::value = " << Fixed<(1 > 0)>::value << '\n';

    // 3. Most vexing parse avoided with braces / extra parens
    Timer t1{Clock{}};
    Timer t2((Clock()));
    std::cout << "timers: " << t1.start << ' ' << t2.start << '\n';
    Timer vexing(Clock());                 // declares function `vexing` - never called
    (void)sizeof(&vexing);

    // 4 & 5. Dependent names
    Holder<double> h;
    std::cout << "dependent_names = " << dependent_names(h) << '\n';

    // 6. `<` as less-than vs template opener
    int a = 1, b = 2, c = 3;
    // Unparenthesized `a < b > c` is valid grammar but Clang 21 now rejects
    // chained comparisons by default, so the parenthesized form is used.
    bool cmp = (a < b) > c;                // 1 > 3 -> false
    bool tmpl = Fixed<1>::value < b;       // `<` after a template-id's `>`
    std::cout << std::boolalpha << "(a < b) > c = " << cmp << ", Fixed<1>::value < b = " << tmpl << '\n';

    // 7. Cast vs call
    double d = 3.9;
    int as_cast = (int)(d);
    auto twice = [](double x) { return 2 * x; };
    double as_call = (twice)(d);
    std::cout << "(int)(d) = " << as_cast << ", (twice)(d) = " << as_call << '\n';

    // 8. Declaration statement that looks like an expression
    int(x) = 10;                           // declares int x = 10
    int(*fp)(int) = nullptr;               // pointer to function
    std::cout << "int(x) declared x = " << x << ", fp null = " << (fp == nullptr) << '\n';

    // 9. Alternative tokens and digraphs
    int arr<:3:> = <%1, 2, 3%>;            // int arr[3] = {1, 2, 3};
    std::cout << "digraph array arr<:2:> = " << arr<:2:> << '\n';
    std::cout << "alt::both = " << alt::both(true, false) << ", alt::either = " << alt::either(false, false) << '\n';
    int bits = 12 bitand 10;               // 0b1100 & 0b1010 = 8 (binary literals are C++14)
    std::cout << "bitand = " << bits << ", compl 0 = " << (compl 0) << '\n';

    // 10. `<::` lexes as `<` `::` since C++11
    std::vector<::Foo> foos(2);
    std::cout << "vector<::Foo>[1].v = " << foos[1].v << '\n';

    // Bonus: `> >` with a space and `>>=` after a template-id
    std::vector<std::vector<int> > spaced(1);
    int shift = 256;
    shift >>= Fixed<2>::value;
    std::cout << "spaced size = " << spaced.size() << ", shift = " << shift << '\n';
    return 0;
}
