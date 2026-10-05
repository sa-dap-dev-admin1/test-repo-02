/*
 * Feature : if constexpr, fold expressions, auto non-type template params
 * Version : C++17
 * Spec    : P0292R2 (constexpr if), N4295 / P0036R0 (fold expressions),
 *           P0127R2 (template <auto>)
 *
 * `if constexpr (cond)` discards the untaken branch at template
 * instantiation. Fold expressions reduce a parameter pack with a binary
 * operator: (... op pack), (pack op ...), (init op ... op pack),
 * (pack op ... op init). `template <auto N>` deduces the NTTP's type.
 *
 * Parser edge cases:
 *  - Fold expressions MUST be parenthesized: `(args + ...)`; `args + ...`
 *    alone is ill-formed.
 *  - Unary right fold `(pack op ...)` vs unary left `(... op pack)`.
 *  - Binary fold with the comma operator: `((std::cout << args), ...)`.
 *  - Empty-pack folds: only &&, || and , have identity values.
 *  - `if constexpr` with an else-if chain: `else if constexpr`.
 *  - The discarded branch may contain code invalid for the given T (but
 *    must still be syntactically valid).
 */
#include <iostream>
#include <string>
#include <type_traits>
#include <vector>

template <typename... Ts> auto sum(Ts... xs) { return (xs + ...); }           // unary right
template <typename... Ts> auto sum_left(Ts... xs) { return (... + xs); }      // unary left
template <typename... Ts> auto sum_init(Ts... xs) { return (0 + ... + xs); }  // binary left
template <typename... Ts> bool all_true(Ts... xs) { return (xs && ...); }     // empty -> true
template <typename... Ts> bool any_true(Ts... xs) { return (xs || ...); }     // empty -> false

template <typename... Ts>
void print_all(const Ts&... xs) {
    ((std::cout << xs << ' '), ...);                                          // comma fold
    std::cout << '\n';
}

template <typename T, typename... Ts>
void push_all(std::vector<T>& v, Ts&&... xs) {
    (v.push_back(std::forward<Ts>(xs)), ...);
}

// Subtraction is not associative: left vs right fold differ
template <typename... Ts> int minus_right(Ts... xs) { return (xs - ...); }
template <typename... Ts> int minus_left(Ts... xs) { return (... - xs); }

template <typename T>
std::string describe(const T& value) {
    if constexpr (std::is_same_v<T, bool>) {
        return value ? "bool:true" : "bool:false";
    } else if constexpr (std::is_integral_v<T>) {
        return "int:" + std::to_string(value);
    } else if constexpr (std::is_floating_point_v<T>) {
        return "float:" + std::to_string(value);
    } else if constexpr (std::is_convertible_v<T, std::string>) {
        return "string:" + std::string(value);
    } else {
        return "unknown (size " + std::to_string(sizeof(T)) + ")";
    }
}

template <typename T>
auto unwrap(T* ptr) {
    if constexpr (std::is_pointer_v<T>) return unwrap(*ptr);  // recursion only for T**
    else return *ptr;
}

// template <auto>: the NTTP type is deduced
template <auto Value>
struct Constant {
    static constexpr auto value = Value;
    using type = decltype(Value);
};

template <auto... Vs>
constexpr auto sum_constants = (Vs + ... + 0);

int main() {
    std::cout << "sum(1,2,3,4)      = " << sum(1, 2, 3, 4) << '\n';
    std::cout << "sum_left(1.5,2.5) = " << sum_left(1.5, 2.5) << '\n';
    std::cout << "sum_init()        = " << sum_init() << " (empty pack)\n";
    std::cout << std::boolalpha;
    std::cout << "all_true()        = " << all_true() << ", any_true() = " << any_true() << '\n';
    std::cout << "all_true(1,true,'x') = " << all_true(1, true, 'x') << '\n';
    std::cout << "minus_right(10,3,2) = " << minus_right(10, 3, 2) << " (10-(3-2))\n";
    std::cout << "minus_left(10,3,2)  = " << minus_left(10, 3, 2) << " ((10-3)-2)\n";
    print_all("mixed", 1, 2.5, 'c', std::string("str"));

    std::vector<std::string> names;
    push_all(names, "ada", std::string("grace"), "linus");
    std::cout << "names.size() = " << names.size() << '\n';

    std::cout << describe(true) << ' ' << describe(42) << ' ' << describe(2.5) << ' '
              << describe("hi") << ' ' << describe(names) << '\n';

    int v = 9; int* p = &v; int** pp = &p;
    std::cout << "unwrap(pp) = " << unwrap(pp) << '\n';

    std::cout << "Constant<'A'>::value = " << Constant<'A'>::value
              << ", is char: " << std::is_same_v<Constant<'A'>::type, char> << '\n';
    std::cout << "Constant<100u> is unsigned: "
              << std::is_same_v<Constant<100u>::type, unsigned> << '\n';
    std::cout << "sum_constants<1, 2L, 3> = " << sum_constants<1, 2L, 3> << '\n';
    return 0;
}
