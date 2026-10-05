/*
 * Feature : Structured bindings
 * Version : C++17 (ISO/IEC 14882:2017)
 * Spec    : P0217R3 "Proposed wording for structured bindings";
 *           P0961R1 / P0969R0 (tuple-like protocol relaxations)
 *
 * `auto [a, b, c] = expr;` decomposes arrays, tuple-like types (std::tuple,
 * std::pair, or anything with tuple_size/tuple_element/get) and structs
 * with all-public non-static members.
 *
 * Parser edge cases:
 *  - `auto [` - the `[` after auto starts an identifier list, not a lambda
 *    or attribute. `auto [[x]] ...` would be an attribute -> different.
 *  - cv/ref qualifiers: `const auto& [k, v]`, `auto&& [x, y]`.
 *  - In range-for: `for (auto& [key, value] : map)`.
 *  - In if/switch init-statements: `if (auto [it, ok] = m.insert(..); ok)`.
 *  - C++20 allows `static`/`thread_local` and lambda capture of bindings;
 *    C++26 allows `[[maybe_unused]]` on individual names and a pack
 *    `auto [first, ...rest]`. Neither is valid C++17.
 */
#include <iostream>
#include <map>
#include <string>
#include <tuple>
#include <utility>
#include <array>

struct Point3 { double x, y, z; };

// Custom tuple-like type via tuple_size / tuple_element / get
class Rgb {
    unsigned char r_, g_, b_;
public:
    constexpr Rgb(unsigned char r, unsigned char g, unsigned char b) : r_(r), g_(g), b_(b) {}
    template <std::size_t I> constexpr unsigned char get() const {
        if constexpr (I == 0) return r_;
        else if constexpr (I == 1) return g_;
        else return b_;
    }
};
namespace std {
template <> struct tuple_size<Rgb> : std::integral_constant<std::size_t, 3> {};
template <std::size_t I> struct tuple_element<I, Rgb> { using type = unsigned char; };
}

std::tuple<int, std::string, double> load_record() { return {42, "answer", 0.5}; }

std::pair<int, int> divmod(int a, int b) { return {a / b, a % b}; }

int main() {
    // Struct decomposition
    Point3 p{1.0, 2.0, 3.0};
    auto [x, y, z] = p;
    std::cout << "point: " << x << ", " << y << ", " << z << '\n';

    // By reference: modifies the original
    auto& [rx, ry, rz] = p;
    rx *= 10; ry *= 10; rz *= 10;
    std::cout << "scaled point: " << p.x << ", " << p.y << ", " << p.z << '\n';

    // Array decomposition
    int arr[3] = {7, 8, 9};
    auto [a0, a1, a2] = arr;
    std::array<char, 2> chars{'o', 'k'};
    const auto& [c0, c1] = chars;
    std::cout << "array: " << a0 << a1 << a2 << ", std::array: " << c0 << c1 << '\n';

    // Tuple and pair
    auto [id, name, weight] = load_record();
    std::cout << "record: " << id << ' ' << name << ' ' << weight << '\n';
    auto [q, r] = divmod(17, 5);
    std::cout << "17 divmod 5 = " << q << " r " << r << '\n';

    // Custom tuple-like
    constexpr Rgb teal{0, 128, 128};
    auto [red, green, blue] = teal;
    std::cout << "rgb: " << +red << ' ' << +green << ' ' << +blue << '\n';

    // In range-for over a map
    std::map<std::string, int> stock{{"apples", 3}, {"pears", 0}, {"plums", 12}};
    for (const auto& [fruit, count] : stock)
        std::cout << "  " << fruit << ": " << count << '\n';

    // In an if-init statement with insert's pair<iterator,bool>
    if (auto [it, inserted] = stock.insert({"kiwis", 5}); inserted)
        std::cout << "inserted " << it->first << '\n';
    if (auto [it, inserted] = stock.insert({"apples", 99}); !inserted)
        std::cout << "apples already present with " << it->second << '\n';

    // Forwarding reference binding to a temporary
    auto&& [tid, tname, tw] = load_record();
    tname += "!";
    std::cout << "temporary extended: " << tname << '\n';

    // Nested: decompose a pair whose second is a struct
    std::pair<std::string, Point3> labeled{"origin", {0, 0, 0}};
    auto& [label, pt] = labeled;
    auto [ox, oy, oz] = pt;
    std::cout << label << " = (" << ox << ',' << oy << ',' << oz << ")\n";
    return 0;
}
