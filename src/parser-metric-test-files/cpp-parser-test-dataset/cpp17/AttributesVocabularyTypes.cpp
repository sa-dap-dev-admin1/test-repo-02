/*
 * Feature : Standard attributes [[nodiscard]], [[maybe_unused]],
 *           [[fallthrough]]; attributes on namespaces/enumerators;
 *           `using` in attribute lists; __has_include; u8 char literals;
 *           std::optional / std::variant / std::any / std::string_view
 * Version : C++17
 * Spec    : P0189R1 (nodiscard), P0212R1 (maybe_unused), P0188R1
 *           (fallthrough), N4266 (attributes for namespaces and
 *           enumerators), P0028R4 (using attribute namespaces),
 *           P0061R1 (__has_include), N4267 (u8 character literals),
 *           P0088R3 (variant), P0220R1 (optional, any, string_view)
 *
 * Parser edge cases:
 *  - `[[using gnu: always_inline, hot]]` - attribute namespace prefix.
 *  - Attribute on an enumerator: `Old [[deprecated]] = 1,`.
 *  - Attribute on a namespace: `namespace [[deprecated]] legacy {}`.
 *  - `[[fallthrough]];` is an attribute applied to a NULL statement and
 *    must immediately precede a case label.
 *  - `u8'a'` (C++17) - in C++20 its type changes to char8_t.
 *  - Hex float literals `0x1.8p1` become standard C++ in C++17.
 *  - `std::visit` with a generic lambda over a variant.
 */
#include <iostream>
#include <optional>
#include <variant>
#include <any>
#include <string>
#include <string_view>
#include <vector>

#if __has_include(<filesystem>)
#  include <filesystem>
#  define HAVE_FS 1
#else
#  define HAVE_FS 0
#endif

namespace [[deprecated("use modern::")]] legacy { inline int version() { return 1; } }
namespace modern { inline int version() { return 2; } }

enum class Mode { Fast, Safe, Old [[deprecated]] = 9 };

[[nodiscard]] std::optional<int> parse_int(std::string_view s) {
    if (s.empty()) return std::nullopt;
    int v = 0;
    for (char c : s) {
        if (c < '0' || c > '9') return {};
        v = v * 10 + (c - '0');
    }
    return v;
}

[[using gnu: cold, noinline]] void slow_path() { std::cout << "slow path\n"; }

int classify(int n) {
    int score = 0;
    switch (n) {
    case 3: score += 100; [[fallthrough]];
    case 2: score += 10;  [[fallthrough]];
    case 1: score += 1;   break;
    default: score = -1;
    }
    return score;
}

using Value = std::variant<int, double, std::string>;

std::string to_text(const Value& v) {
    return std::visit([](const auto& x) -> std::string {
        using T = std::decay_t<decltype(x)>;
        if constexpr (std::is_same_v<T, std::string>) return "\"" + x + "\"";
        else return std::to_string(x);
    }, v);
}

int main([[maybe_unused]] int argc, [[maybe_unused]] char** argv) {
    for (std::string_view s : {"123", "", "4x"}) {
        auto r = parse_int(s);
        std::cout << "parse_int(\"" << s << "\") = "
                  << (r ? std::to_string(*r) : "nullopt") << '\n';
    }
    std::cout << "value_or: " << parse_int("bad").value_or(-1) << '\n';

    std::vector<Value> values{42, 3.5, std::string("text")};
    for (const auto& v : values) std::cout << "variant index " << v.index() << ": " << to_text(v) << '\n';
    if (auto* d = std::get_if<double>(&values[1])) std::cout << "get_if<double>: " << *d << '\n';

    std::any box = 10;
    box = std::string("now a string");
    std::cout << "any holds string: " << std::any_cast<std::string>(box) << '\n';

    for (int n = 0; n <= 3; ++n) std::cout << "classify(" << n << ") = " << classify(n) << '\n';

    [[maybe_unused]] auto unused = modern::version();
    char ch = u8'A';
    double hexf = 0x1.8p1;          // 3.0
    std::cout << "u8'A' = " << int(ch) << ", 0x1.8p1 = " << hexf << ", HAVE_FS = " << HAVE_FS << '\n';

    std::string_view sv = "  trimmed  ";
    sv.remove_prefix(sv.find_first_not_of(' '));
    sv.remove_suffix(sv.size() - sv.find_last_not_of(' ') - 1);
    std::cout << "string_view trim: [" << sv << "]\n";
    if (argc > 99) slow_path();
    return 0;
}
