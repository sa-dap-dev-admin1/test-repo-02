/*
 * Feature : Grab-bag of C++23 core-language syntax changes
 * Version : C++23
 * Spec    : P0849R8 (auto(x) decay-copy), P0330R8 (uz/z literal suffixes),
 *           P1102R2 (optional () on lambdas with specifiers), P2173R1
 *           (attributes on lambdas), P2334R1 (#elifdef/#elifndef),
 *           P2437R1 (#warning), P2324R2 (labels at end of compound stmt),
 *           P1774R8 ([[assume]]), P2360R0 (alias-declaration in
 *           init-statement), P2290R3 (\x{...} \o{...} delimited escapes),
 *           P2071R2 (\N{...} named universal characters), P2266R3 (simpler
 *           implicit move), P2186R2 (removed garbage-collection support)
 *
 * Parser edge cases:
 *  - `auto(x)` / `auto{x}` - `auto` used as a functional-cast type.
 *  - `42uz`, `10z` - new integer suffixes for size_t / signed size_t.
 *  - `[] mutable { }` and `[] noexcept { }` with no parameter list.
 *  - `[][[nodiscard]] () { }` - attribute between capture and params.
 *  - `for (using T = int; T x : v)` - alias-declaration as init-statement.
 *  - `"\x{41}\o{102}\N{LATIN CAPITAL LETTER C}"` - new escape forms.
 *  - `label: }` - a label may now end a block.
 */
#include <iostream>
#include <vector>
#include <string>
#include <cstddef>
#include <type_traits>
#include <algorithm>

#define FEATURE_FAST
#ifdef FEATURE_SLOW
#  define MODE "slow"
#elifdef FEATURE_FAST
#  define MODE "fast"
#elifndef FEATURE_NONE
#  define MODE "default"
#endif

#if 0
#  warning "this #warning is inside a skipped group"
#endif

template <typename C>
void remove_all_copies_of_front(C& c) {
    // auto(x) makes a prvalue copy, so erase() doesn't see a dangling ref
    auto value = auto(c.front());
    std::erase(c, value);
}

int divide_assume_positive(int a, int b) {
    [[assume(b > 0)]];
    return a / b;
}

void process(const std::vector<int>& v) {
    for (int x : v) {
        if (x < 0) goto next;
        std::cout << x << ' ';
    next:                                        // label at end of block
    }
    std::cout << '\n';
}

int main() {
    std::vector<int> v{3, 1, 3, 4, 3, 5};
    remove_all_copies_of_front(v);
    std::cout << "after removing copies of 3:";
    for (int x : v) std::cout << ' ' << x;
    std::cout << '\n';

    auto sz = 42uz;
    auto ssz = -5z;
    static_assert(std::is_same_v<decltype(sz), std::size_t>);
    static_assert(std::is_signed_v<decltype(ssz)>);
    std::cout << "42uz = " << sz << ", -5z = " << ssz << '\n';

    for (auto i = 0uz; i < v.size(); ++i) std::cout << "v[" << i << "]=" << v[i] << ' ';
    std::cout << '\n';

    int counter = 0;
    auto bump = [counter] mutable { return ++counter; };   // no () needed
    bump(); bump();
    std::cout << "mutable lambda without (): " << bump() << '\n';

    auto safe = [] noexcept { return 7; };
    auto marked = [] [[nodiscard]] (int x) { return x + 1; };
    std::cout << "noexcept lambda: " << safe() << ", attributed lambda: " << marked(1) << '\n';

    for (using Pair = std::pair<int, char>; Pair p : {Pair{1, 'a'}, Pair{2, 'b'}})
        std::cout << p.first << p.second << ' ';
    std::cout << '\n';

    std::string esc = "\x{41}\o{102}\N{LATIN CAPITAL LETTER C}";
    std::cout << "delimited/named escapes: " << esc << '\n';

    std::cout << "MODE = " << MODE << '\n';
    std::cout << "divide_assume_positive(9, 3) = " << divide_assume_positive(9, 3) << '\n';
    process({1, -2, 3, -4, 5});
    return 0;
}
