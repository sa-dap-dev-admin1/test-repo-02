/*
 * Feature : Language features REMOVED in C++17 (negative version test)
 * Version : Valid C++11/C++14. INVALID in C++17 and later.
 * Spec    : P0001R1 (remove `register`), P0002R1 (remove operator++ on
 *           bool), N4086 (remove trigraphs), P0003R5 (remove dynamic
 *           exception specifications throw(T)); `throw()` itself survived
 *           until C++20 (P1152R4).
 *
 * EXPECTED: parser in C++14 mode -> PASS
 *           parser in C++17/20/23 mode -> FAIL (each marked line)
 *
 * This tests that a parser gates syntax by version in BOTH directions:
 * not just "new syntax is rejected in old modes" but "removed syntax is
 * rejected in new modes".
 *
 * Parser edge cases:
 *  - Trigraphs are replaced in translation phase 1, BEFORE tokenization:
 *    `??=` -> `#`, `??/` -> `\`, `??(` -> `[`, `??)` -> `]`, `??<` -> `{`,
 *    `??>` -> `}`, `??!` -> `|`, `??'` -> `^`, `??-` -> `~`.
 *    In C++17 they are just `?` characters, so the meaning CHANGES - a
 *    trigraph inside a string literal is still valid C++17 but different.
 *  - `register` is still a reserved KEYWORD in C++17 (unused), so
 *    `register int x;` is ill-formed rather than an unknown identifier.
 *  - `void f() throw(std::runtime_error);` - dynamic exception spec.
 *
 * Build: g++ -std=c++14 -trigraphs RemovedInCpp17.cpp   (GCC needs -trigraphs
 *        or -std=c++14 strict mode; gnu++14 ignores trigraphs by default)
 */
#include <iostream>
#include <stdexcept>
#include <string>

// [removed in C++17] dynamic exception specification
void risky(int v) throw(std::runtime_error) {
    if (v < 0) throw std::runtime_error("negative");
}

int sum_registers(int n) {
    register int total = 0;                // [removed in C++17] register
    for (register int i = 1; i <= n; ++i) total += i;
    return total;
}

int main() {
    bool flag = false;
    flag++;                                // [removed in C++17] ++ on bool
    std::cout << "flag after ++: " << flag << '\n';

    std::cout << "sum_registers(10) = " << sum_registers(10) << '\n';

    try {
        risky(-1);
    } catch (const std::runtime_error& e) {
        std::cout << "caught: " << e.what() << '\n';
    }

    // [removed in C++17] trigraphs: ??( ??) become [ ] in C++14
    int arr??(3??) = ??< 1, 2, 3 ??>;
    std::cout << "trigraph array arr[2] = " << arr??(2??) << '\n';

    // Inside a string: "??!" is "|" in C++14 but literally "??!" in C++17
    std::string s = "what??!";
    std::cout << "string with trigraph: " << s << " (length " << s.size() << ")\n";
    return 0;
}
