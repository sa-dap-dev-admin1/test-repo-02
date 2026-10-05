// Feature : `import std;` - the standard library module
// Version : C++23
// Spec    : P2465R3 "Standard Library Modules std and std.compat"
//
// An ordinary translation unit (NOT a module unit) that imports the whole
// standard library with a single import-declaration instead of #include.
//
// Build (toolchain must ship the std module):
//   clang++ -std=c++23 -stdlib=libc++ -fmodule-file=std=std.pcm import_std_consumer.cpp
//   cl /std:c++latest /EHsc import_std_consumer.cpp   (MSVC 17.5+)
//   g++ -std=c++23 -fmodules import_std_consumer.cpp  (GCC 15+)
//
// Parser edge cases:
//  - `import std;` at file scope of a non-module TU: the parser must treat
//    `import` as a keyword here (start of line, followed by a module name
//    and `;`), but as an identifier in `int import = 0;`.
//  - `import std.compat;` - dotted name; also provides ::printf etc.
//  - No #include at all, yet std:: names are available.
//  - Macros are NOT exported by `import std;` (e.g. assert, EOF are
//    missing) - a parser must not assume them.
import std;

struct Reading {
    std::string sensor;
    double value;
};

int main() {
    std::vector<Reading> readings{{"t1", 21.5}, {"t2", 19.0}, {"t1", 22.5}, {"t3", 30.25}};

    std::map<std::string, std::vector<double>> by_sensor;
    for (const auto& [sensor, value] : readings) by_sensor[sensor].push_back(value);

    for (const auto& [sensor, values] : by_sensor) {
        double avg = std::accumulate(values.begin(), values.end(), 0.0) / values.size();
        std::println("{}: {} reading(s), avg {:.2f}", sensor, values.size(), avg);
    }

    auto hot = readings | std::views::filter([](const Reading& r) { return r.value > 21.0; })
                        | std::views::transform(&Reading::sensor);
    std::print("above 21.0:");
    for (const auto& s : hot) std::print(" {}", s);
    std::println("");

    int import = 3;                       // `import` as an ordinary identifier
    int module = 4;                       // likewise `module`
    std::println("import + module = {}", import + module);
    return 0;
}
