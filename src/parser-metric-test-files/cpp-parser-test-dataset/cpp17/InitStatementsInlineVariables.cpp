/*
 * Feature : if/switch with initializer, inline variables, nested namespace
 *           definitions, guaranteed copy elision, constexpr lambdas
 * Version : C++17
 * Spec    : P0305R1 (selection statements with initializer), P0386R2
 *           (inline variables), N4230 (nested namespace definition),
 *           P0135R1 (guaranteed copy elision), P0170R1 (constexpr lambda),
 *           P0018R3 (lambda capture of *this)
 *
 * Parser edge cases:
 *  - `if (init; condition)` and `switch (init; value)` - the `;` inside
 *    the parentheses. `if (int x = f(); x > 0)`.
 *  - `inline` on a VARIABLE (namespace scope or static data member).
 *  - `namespace a::b::c { }` - qualified name in a namespace definition.
 *    (`namespace a::inline b` is C++20.)
 *  - `[*this]` capture-by-copy of the enclosing object.
 *  - `constexpr` on a lambda: `[](int x) constexpr { ... }`.
 *  - Returning a non-copyable, non-movable type by value (prvalue).
 */
#include <iostream>
#include <map>
#include <string>
#include <mutex>
#include <atomic>

namespace app::config::defaults {
    inline constexpr int max_retries = 3;
    inline const std::string service_name = "parser-service";
}

struct Registry {
    static inline int instances = 0;            // no out-of-class definition needed
    static inline const char* const prefix = "reg";
    Registry() { ++instances; }
};

// Non-copyable, non-movable - returned by value thanks to guaranteed elision
struct Pinned {
    int id;
    explicit Pinned(int i) : id(i) {}
    Pinned(const Pinned&) = delete;
    Pinned(Pinned&&) = delete;
};
Pinned make_pinned(int id) { return Pinned{id}; }

struct Sensor {
    std::string name;
    int reading;
    auto snapshot_reader() const {
        return [*this] { return name + "=" + std::to_string(reading); };  // copies *this
    }
};

enum class Status { Ok, Warn, Fail };
Status check(int v) { return v < 50 ? Status::Ok : v < 80 ? Status::Warn : Status::Fail; }

int main() {
    std::map<std::string, int> scores{{"ada", 91}, {"bob", 47}};

    if (auto it = scores.find("ada"); it != scores.end())
        std::cout << "found ada: " << it->second << '\n';
    else
        std::cout << "ada missing\n";

    if (auto it = scores.find("zed"); it == scores.end())
        std::cout << "zed missing (it still in scope in else too)\n";

    // Lock scoped to the if statement
    std::mutex m;
    if (std::lock_guard<std::mutex> lk(m); scores.size() > 1)
        std::cout << "locked check: " << scores.size() << " entries\n";

    // switch with initializer
    for (int v : {10, 65, 99}) {
        switch (Status s = check(v); s) {
        case Status::Ok:   std::cout << v << " -> ok\n"; break;
        case Status::Warn: std::cout << v << " -> warn\n"; break;
        case Status::Fail: std::cout << v << " -> fail\n"; break;
        }
    }

    std::cout << app::config::defaults::service_name << " retries="
              << app::config::defaults::max_retries << '\n';

    Registry r1, r2, r3;
    (void)r1; (void)r2; (void)r3;
    std::cout << Registry::prefix << " instances: " << Registry::instances << '\n';

    Pinned p = make_pinned(7);                  // no copy/move needed
    std::cout << "pinned id: " << p.id << '\n';

    Sensor s{"temp", 21};
    auto reader = s.snapshot_reader();
    s.reading = 99;                             // lambda holds its own copy
    std::cout << "snapshot: " << reader() << " (live " << s.reading << ")\n";

    constexpr auto cube = [](int x) constexpr { return x * x * x; };
    static_assert(cube(3) == 27);               // static_assert without message (C++17)
    int buf[cube(2)];
    std::cout << "constexpr lambda array size: " << sizeof buf / sizeof buf[0] << '\n';
    return 0;
}
