/*
 * Feature : Three-way comparison operator <=> and defaulted comparisons
 * Version : C++20
 * Spec    : P0515R3 "Consistent comparison", P0905R1, P1185R2
 *           (<=> != ==), P1186R3 (when do you actually use <=>?)
 *
 * `a <=> b` returns std::strong_ordering / weak_ordering / partial_ordering.
 * `auto operator<=>(const T&) const = default;` generates memberwise
 * comparison; a defaulted <=> also implicitly declares defaulted ==.
 * The compiler rewrites `a < b` as `(a <=> b) < 0` and `a != b` as
 * `!(a == b)`, including reversed candidates `(b <=> a)`.
 *
 * Parser edge cases:
 *  - `<=>` is a single new token. In C++17, `a<=>b` lexes as `a <= > b`.
 *  - Template trap: `X<&Y::operator<=>>` - the lexer must handle `<=>>`.
 *  - `operator<=>` declared as `= default` with a deduced `auto` return.
 *  - `bool operator==(const T&) const = default;` - defaulted equality.
 *  - Comparing the result with literal 0: `(a <=> b) < 0` (only 0 allowed).
 *  - Precedence: <=> binds tighter than < > <= >= but looser than << >>.
 */
#include <iostream>
#include <compare>
#include <string>
#include <vector>
#include <algorithm>
#include <set>
#include <cmath>
#include <tuple>

struct Version {
    int major = 0, minor = 0, patch = 0;
    auto operator<=>(const Version&) const = default;   // memberwise; also gives ==
};

struct CaseInsensitive {
    std::string s;
    std::weak_ordering operator<=>(const CaseInsensitive& o) const {
        auto lower = [](char c) { return (c >= 'A' && c <= 'Z') ? char(c + 32) : c; };
        auto n = std::min(s.size(), o.s.size());
        for (std::size_t i = 0; i < n; ++i) {
            if (auto c = lower(s[i]) <=> lower(o.s[i]); c != 0)
                return c == std::strong_ordering::less ? std::weak_ordering::less
                                                       : std::weak_ordering::greater;
        }
        return s.size() <=> o.s.size();
    }
    bool operator==(const CaseInsensitive& o) const { return (*this <=> o) == 0; }
};

struct Measurement {
    double value;
    std::partial_ordering operator<=>(const Measurement& o) const { return value <=> o.value; }
    bool operator==(const Measurement&) const = default;
};

// Heterogeneous comparison: reversed candidates make `5 < Meters{..}` work
struct Meters {
    int v;
    auto operator<=>(int other) const { return v <=> other; }
    bool operator==(int other) const { return v == other; }
};

const char* name(std::strong_ordering o) {
    return o < 0 ? "less" : o > 0 ? "greater" : "equal";
}
const char* name(std::partial_ordering o) {
    if (o == std::partial_ordering::unordered) return "unordered";
    return o < 0 ? "less" : o > 0 ? "greater" : "equivalent";
}

int main() {
    std::cout << "3 <=> 5 : " << name(3 <=> 5) << '\n';
    std::cout << "5 <=> 5 : " << name(5 <=> 5) << '\n';
    std::cout << "1.0 <=> NaN : " << name(1.0 <=> std::nan("")) << '\n';

    Version a{1, 4, 2}, b{1, 10, 0}, c{1, 4, 2};
    std::cout << std::boolalpha;
    std::cout << "a < b: " << (a < b) << ", a == c: " << (a == c)
              << ", a != b: " << (a != b) << ", b >= a: " << (b >= a) << '\n';

    std::vector<Version> versions{{2, 0, 0}, {1, 10, 1}, {1, 2, 3}, {1, 10, 0}};
    std::sort(versions.begin(), versions.end());
    std::cout << "sorted:";
    for (const auto& v : versions) std::cout << ' ' << v.major << '.' << v.minor << '.' << v.patch;
    std::cout << '\n';

    std::set<CaseInsensitive> names{{"bob"}, {"Alice"}, {"BOB"}, {"carol"}};
    std::cout << "case-insensitive set (" << names.size() << "):";
    for (const auto& n : names) std::cout << ' ' << n.s;
    std::cout << '\n';
    std::cout << "\"Hello\" == \"hELLO\": " << (CaseInsensitive{"Hello"} == CaseInsensitive{"hELLO"}) << '\n';

    Measurement m1{2.5}, m2{std::nan("")};
    std::cout << "m1 < m2: " << (m1 < m2) << ", m1 > m2: " << (m1 > m2)
              << ", m1 == m1: " << (m1 == m1) << '\n';

    Meters d{12};
    std::cout << "d > 10: " << (d > 10) << ", 5 < d (reversed): " << (5 < d)
              << ", 12 == d (reversed ==): " << (12 == d) << '\n';

    // Precedence: <=> binds tighter than <, looser than <<
    bool p = (1 << 2 <=> 3) > 0;              // (1<<2) <=> 3 -> greater
    std::cout << "(1 << 2 <=> 3) > 0: " << p << '\n';

    // std::tuple and std::string already provide <=>
    auto t = std::tuple{1, std::string("b")} <=> std::tuple{1, std::string("a")};
    std::cout << "tuple <=> : " << (t > 0 ? "greater" : "not greater") << '\n';
    return 0;
}
