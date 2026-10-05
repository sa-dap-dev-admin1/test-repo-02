/*
 * Feature : Template metaprogramming stress test - SFINAE, variadic
 *           templates, partial specialization, template template
 *           parameters, dependent default arguments, recursive templates
 * Version : C++17 (void_t, if constexpr); techniques date from C++11
 * Spec    : [temp.deduct] (SFINAE), [temp.variadic], [temp.class.spec],
 *           N3911 (std::void_t)
 *
 * Pre-concepts template code is the hardest everyday C++ to parse: long
 * nested template-ids, decltype/declval in default template arguments,
 * `typename ... ::template rebind<U>::other`, and pack expansions in odd
 * positions.
 *
 * Parser edge cases:
 *  - `template <typename T, typename = void>` - unnamed defaulted param.
 *  - `std::enable_if_t<cond, int> = 0` as a non-type template parameter.
 *  - `decltype(std::declval<T&>().size())` in a template argument.
 *  - `template <template <typename...> class C>` (template template param);
 *    `typename` instead of `class` there is C++17.
 *  - Pack expansion patterns: `f(g(args)...)`, `Ts::value...`,
 *    `std::tuple<std::vector<Ts>...>`.
 *  - `sizeof...(Ts)` vs `sizeof(Ts)...`.
 */
#include <iostream>
#include <type_traits>
#include <utility>
#include <vector>
#include <list>
#include <string>
#include <tuple>

// Detection idiom
template <typename T, typename = void>
struct has_size : std::false_type {};
template <typename T>
struct has_size<T, std::void_t<decltype(std::declval<const T&>().size())>> : std::true_type {};

// enable_if as a defaulted non-type template parameter
template <typename T, std::enable_if_t<std::is_integral_v<T>, int> = 0>
std::string classify(T) { return "integral"; }
template <typename T, std::enable_if_t<std::is_floating_point_v<T>, int> = 0>
std::string classify(T) { return "floating"; }
template <typename T, std::enable_if_t<has_size<T>::value, int> = 0>
std::string classify(const T& t) { return "sized(" + std::to_string(t.size()) + ")"; }

// Trailing-return SFINAE
template <typename T>
auto describe_size(const T& t, int) -> decltype(t.size(), std::string()) { return "size " + std::to_string(t.size()); }
template <typename T>
std::string describe_size(const T&, long) { return "no size()"; }

// Template template parameter with a variadic parameter list
template <template <typename...> typename Container, typename T>
Container<T> make_filled(std::size_t n, const T& v) { return Container<T>(n, v); }

// Recursive variadic template: compile-time type list operations
template <typename... Ts> struct TypeList { static constexpr std::size_t size = sizeof...(Ts); };

template <typename List> struct Front;
template <typename H, typename... Ts> struct Front<TypeList<H, Ts...>> { using type = H; };

template <typename List, typename T> struct PushBack;
template <typename... Ts, typename T> struct PushBack<TypeList<Ts...>, T> { using type = TypeList<Ts..., T>; };

template <typename T, typename List> struct Contains;
template <typename T, typename... Ts>
struct Contains<T, TypeList<Ts...>> : std::bool_constant<(std::is_same_v<T, Ts> || ...)> {};

// Pack expansion patterns
template <typename... Ts>
using VectorsOf = std::tuple<std::vector<Ts>...>;

template <typename... Ts>
constexpr std::size_t total_size() { return (sizeof(Ts) + ... + 0); }

template <typename F, typename... Args>
auto apply_each(F f, Args... args) { return std::vector{f(args)...}; }

template <std::size_t... Is>
constexpr int sum_indices(std::index_sequence<Is...>) { return (static_cast<int>(Is) + ... + 0); }

// Partial specialization on pointer, array and member-pointer types
template <typename T> struct Kind { static constexpr const char* name = "value"; };
template <typename T> struct Kind<T*> { static constexpr const char* name = "pointer"; };
template <typename T, std::size_t N> struct Kind<T[N]> { static constexpr const char* name = "array"; };
template <typename C, typename M> struct Kind<M C::*> { static constexpr const char* name = "member pointer"; };

struct Widget { int id; };

int main() {
    std::cout << std::boolalpha;
    std::cout << "has_size<vector<int>> = " << has_size<std::vector<int>>::value
              << ", has_size<int> = " << has_size<int>::value << '\n';
    std::cout << "classify: " << classify(5) << ", " << classify(2.5) << ", "
              << classify(std::string("abc")) << '\n';
    std::cout << "describe_size: " << describe_size(std::list<int>{1, 2}, 0) << ", "
              << describe_size(3.0, 0) << '\n';

    auto v = make_filled<std::vector>(3, std::string("x"));
    auto l = make_filled<std::list>(2, 9);
    std::cout << "make_filled: vector size " << v.size() << ", list front " << l.front() << '\n';

    using L = TypeList<int, char>;
    using L2 = PushBack<L, double>::type;
    std::cout << "TypeList size " << L2::size << ", front is int: "
              << std::is_same_v<Front<L2>::type, int> << ", contains double: "
              << Contains<double, L2>::value << ", contains float: " << Contains<float, L2>::value << '\n';

    VectorsOf<int, std::string> vs;
    std::get<1>(vs).push_back("tuple of vectors");
    std::cout << std::get<1>(vs)[0] << '\n';
    std::cout << "total_size<char, int, double> = " << total_size<char, int, double>() << '\n';

    auto squares = apply_each([](int x) { return x * x; }, 1, 2, 3, 4);
    std::cout << "apply_each squares:";
    for (int s : squares) std::cout << ' ' << s;
    std::cout << '\n';
    std::cout << "sum_indices<0..9> = " << sum_indices(std::make_index_sequence<10>{}) << '\n';

    std::cout << "Kind: " << Kind<int>::name << ", " << Kind<int*>::name << ", "
              << Kind<int[4]>::name << ", " << Kind<int Widget::*>::name << '\n';
    return 0;
}
