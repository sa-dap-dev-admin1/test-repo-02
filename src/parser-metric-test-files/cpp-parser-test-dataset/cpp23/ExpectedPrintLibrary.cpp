/*
 * Feature : std::expected, std::print/println, std::optional monadic ops,
 *           std::ranges::to, views::zip,
 *           std::to_underlying, std::unreachable
 * Version : C++23
 * Spec    : P0323R12 (expected), P2093R14 (print), P0798R8 (optional
 *           monadic), P1206R7 (ranges::to), P2321R2 (zip), P2164R9
 *           (enumerate), P2442R1 (chunk/slide), P2374R4 (cartesian_product),
 *           P1682R3 (to_underlying), P0627R6 (unreachable)
 *
 * Library-heavy file: the syntax is ordinary, but the parser must cope with
 * long chains of member calls on prvalues, lambdas returning
 * std::unexpected, nested template-ids and structured bindings over zip.
 *
 * Parser edge cases:
 *  - `std::unexpected{...}` used as a return value (CTAD).
 *  - `.and_then(...).transform(...).or_else(...)` chains.
 *  - `| std::ranges::to<std::vector>()` - template template argument
 *    without its parameters.
 *  - `for (auto [name, score] : std::views::zip(a, b))`.
 *
 * Library note: views::enumerate/chunk/slide and range formatting ({} on a
 * vector) are C++23 too but are left out because GCC 14's libstdc++ / Clang's
 * libc++ do not all ship them yet - this file targets the GRAMMAR.
 */
#include <print>
#include <expected>
#include <optional>
#include <string>
#include <string_view>
#include <vector>
#include <map>
#include <ranges>
#include <utility>
#include <charconv>

enum class ParseError { Empty, NotANumber, OutOfRange };
enum class Level : unsigned char { Low = 1, Mid = 5, High = 9 };

std::string_view to_string(ParseError e) {
    switch (e) {
    case ParseError::Empty:      return "empty";
    case ParseError::NotANumber: return "not a number";
    case ParseError::OutOfRange: return "out of range";
    }
    std::unreachable();
}

std::expected<int, ParseError> parse(std::string_view s) {
    if (s.empty()) return std::unexpected{ParseError::Empty};
    int value = 0;
    auto [ptr, ec] = std::from_chars(s.data(), s.data() + s.size(), value);
    if (ec == std::errc::result_out_of_range) return std::unexpected{ParseError::OutOfRange};
    if (ec != std::errc{} || ptr != s.data() + s.size()) return std::unexpected{ParseError::NotANumber};
    return value;
}

std::expected<int, ParseError> must_be_positive(int v) {
    if (v <= 0) return std::unexpected{ParseError::OutOfRange};
    return v;
}

std::optional<std::string> lookup(const std::map<int, std::string>& m, int k) {
    if (auto it = m.find(k); it != m.end()) return it->second;
    return std::nullopt;
}

int main() {
    for (std::string_view in : {"42", "", "4x2", "-7", "99999999999"}) {
        auto result = parse(in)
            .and_then(must_be_positive)
            .transform([](int v) { return v * 2; });
        if (result) std::println("parse(\"{}\") -> doubled {}", in, *result);
        else        std::println("parse(\"{}\") -> error: {}", in, to_string(result.error()));
    }
    std::println("value_or: {}", parse("oops").value_or(-1));

    std::map<int, std::string> users{{1, "ada"}, {2, "grace"}};
    for (int id : {1, 3}) {
        auto name = lookup(users, id)
            .transform([](const std::string& s) { return s + "!"; })
            .or_else([] { return std::optional<std::string>{"<unknown>"}; });
        std::println("user {} -> {}", id, *name);
    }

    std::vector<std::string> names{"x", "y", "z"};
    std::vector<int> scores{90, 75, 60};
    for (auto [name, score] : std::views::zip(names, scores)) std::println("zip: {}={}", name, score);

    auto evens = std::views::iota(1, 11)
               | std::views::filter([](int n) { return n % 2 == 0; })
               | std::ranges::to<std::vector>();
    std::print("ranges::to<vector>:");
    for (int e : evens) std::print(" {}", e);
    std::println("");

    auto as_strings = std::views::iota(1, 4)
                    | std::views::transform([](int n) { return std::to_string(n * n); })
                    | std::ranges::to<std::vector<std::string>>();
    std::println("ranges::to<vector<string>> size = {}, last = {}", as_strings.size(), as_strings.back());

    std::println("to_underlying(Level::High) = {}", std::to_underlying(Level::High));
    std::print("{:*^20}\n", " done ");
    return 0;
}
