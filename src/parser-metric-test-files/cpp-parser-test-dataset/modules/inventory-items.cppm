// Feature : C++20 modules - module PARTITION interface unit
// Version : C++20
// Spec    : P1103R3 section "Module partitions"
//
// `export module inventory:items;` declares the `items` partition of
// module `inventory`. The primary interface would `export import :items;`.
// A partition interface unit is compilable on its own:
//   clang++ -std=c++20 --precompile inventory-items.cppm -o inventory-items.pcm
// (Clang requires the file name to match for implicit lookup; that is why
// this file is named inventory-items.cppm.)
//
// Parser edge cases:
//  - `module-name : partition-name` - the colon is part of the module
//    declaration, not a label or bit-field.
//  - `import :other;` (partition import) has no module name before ':'.
//  - `export import <header>;` / `import "header.h";` header units -
//    the angle-bracket form looks like an #include without the '#'.
//  - Template definitions, inline variables and concepts exported from a
//    partition.
//  - Unexported struct with an exported function returning it (reachable
//    but not visible by name).
module;

#include <string>
#include <vector>
#include <cstddef>

export module inventory:items;

export enum class Category : unsigned char { Tool, Part, Consumable };

export struct Item {
    std::string sku;
    std::string name;
    Category category = Category::Part;
    int quantity = 0;
    double unit_price = 0.0;

    double value() const { return quantity * unit_price; }
};

export template <typename T>
concept Priced = requires(const T& t) {
    { t.value() } -> std::convertible_to<double>;
};

export template <Priced T>
double total_value(const std::vector<T>& items) {
    double sum = 0;
    for (const auto& i : items) sum += i.value();
    return sum;
}

export inline constexpr int low_stock_threshold = 5;

// Not exported by name, but reachable through the exported function below
struct StockReport {
    std::size_t total_items = 0;
    std::size_t low_stock = 0;
};

export StockReport summarize(const std::vector<Item>& items) {
    StockReport r;
    r.total_items = items.size();
    for (const auto& i : items)
        if (i.quantity < low_stock_threshold) ++r.low_stock;
    return r;
}

export const char* to_string(Category c) {
    switch (c) {
    case Category::Tool:       return "tool";
    case Category::Part:       return "part";
    case Category::Consumable: return "consumable";
    }
    return "?";
}
