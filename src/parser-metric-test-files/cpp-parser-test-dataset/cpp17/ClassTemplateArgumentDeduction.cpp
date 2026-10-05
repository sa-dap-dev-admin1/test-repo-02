/*
 * Feature : Class template argument deduction (CTAD) and deduction guides
 * Version : C++17
 * Spec    : P0091R3 "Template argument deduction for class templates",
 *           P0512R0, P0620R0 (deduction guide refinements)
 *
 * `std::pair p{1, 2.0};` deduces std::pair<int, double>. User-defined
 * deduction guides `Name(Args...) -> Name<Deduced...>;` steer deduction.
 *
 * Parser edge cases:
 *  - Deduction guide syntax looks like a function declaration with a
 *    trailing return type but NO return type and NO body:
 *      template<class It> Range(It, It) -> Range<typename It::value_type>;
 *  - `explicit` deduction guides.
 *  - Template name used without <> as a type in a declaration: `Box b{1};`
 *    and in a functional cast: `Box{3.5}`, `std::vector{1, 2, 3}`.
 *  - `new Box{1}` and CTAD in `auto x = std::optional{5};`.
 *  - CTAD for aggregates and alias templates is C++20, not C++17.
 */
#include <iostream>
#include <vector>
#include <string>
#include <map>
#include <tuple>
#include <optional>
#include <array>
#include <memory>
#include <mutex>

template <typename T>
struct Box {
    T value;
    Box(T v) : value(v) {}
};

// Range over an iterator pair, deducing the element type
template <typename T>
struct Range {
    std::vector<T> items;
    template <typename It>
    Range(It first, It last) : items(first, last) {}
};
template <typename It>
Range(It, It) -> Range<typename std::iterator_traits<It>::value_type>;

// String literals deduce as std::string rather than const char*
template <typename T>
struct Named {
    T name;
    int id;
    Named(T n, int i) : name(n), id(i) {}
};
Named(const char*, int) -> Named<std::string>;

// explicit deduction guide: only usable with direct-initialization
template <typename T>
struct Wrapper {
    T inner;
    template <typename U> Wrapper(U u) : inner(u) {}
};
template <typename U> explicit Wrapper(U) -> Wrapper<U>;

template <typename A, typename B>
struct Pairish {
    A a; B b;
    Pairish(A x, B y) : a(x), b(y) {}
};

int main() {
    Box b1{42};                     // Box<int>
    Box b2{2.5};                    // Box<double>
    auto b3 = Box{std::string("s")}; // functional cast form
    std::cout << "Box values: " << b1.value << ' ' << b2.value << ' ' << b3.value << '\n';

    std::pair p{1, 2.0};            // pair<int, double>
    std::tuple t{1, 'x', "three"};  // tuple<int, char, const char*>
    std::vector v{1, 2, 3, 4};      // vector<int>
    std::array arr{1.0, 2.0, 3.0};  // array<double, 3>
    std::optional opt{7};           // optional<int>
    std::map m{std::pair{1, std::string("one")}, std::pair{2, std::string("two")}};
    std::cout << "pair: " << p.first << ',' << p.second
              << " tuple<2>: " << std::get<2>(t)
              << " vector size: " << v.size()
              << " array size: " << arr.size()
              << " optional: " << *opt
              << " map[2]: " << m[2] << '\n';

    // Copy deduction: vector{v} is vector<int>, not vector<vector<int>>
    std::vector copy{v};
    std::cout << "copy deduces vector<int>: size " << copy.size() << '\n';

    // User deduction guide with iterator traits
    int raw[] = {5, 6, 7};
    Range r(std::begin(raw), std::end(raw));
    std::cout << "Range from array: " << r.items.size() << " items, last " << r.items.back() << '\n';
    Range rs(v.begin() + 1, v.end());
    std::cout << "Range from vector slice: " << rs.items.front() << '\n';

    Named n{"literal", 3};          // Named<std::string> via guide
    n.name += " extended";
    std::cout << "Named: " << n.name << " #" << n.id << '\n';

    Wrapper w(10);                  // direct-init: explicit guide OK
    std::cout << "Wrapper: " << w.inner << '\n';

    Pairish pr{"key", 3.5};         // Pairish<const char*, double>
    std::cout << "Pairish: " << pr.a << " = " << pr.b << '\n';

    auto heap = new Box{'z'};       // CTAD in new-expression
    std::cout << "heap Box<char>: " << heap->value << '\n';
    delete heap;

    std::mutex mtx;
    std::lock_guard guard{mtx};     // lock_guard<std::mutex>
    std::cout << "lock_guard deduced\n";
    return 0;
}
