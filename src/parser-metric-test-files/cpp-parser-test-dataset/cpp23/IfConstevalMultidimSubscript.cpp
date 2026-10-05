/*
 * Feature : if consteval, multidimensional subscript operator, static
 *           operator() and operator[], constexpr relaxations
 * Version : C++23
 * Spec    : P1938R3 (if consteval), P2128R6 (multidimensional subscript),
 *           P1169R4 (static operator()), P2589R1 (static operator[]),
 *           P2242R3 (non-literal variables / labels / goto in constexpr),
 *           P2647R1 (static constexpr variables in constexpr functions)
 *
 * Parser edge cases:
 *  - `if consteval { ... } else { ... }` - NO parentheses, and braces are
 *    REQUIRED. `if !consteval { }` is also valid.
 *  - `m[i, j]` - comma inside [] is now an argument separator for a
 *    user-defined operator[] with multiple parameters. (In C++20 this was a
 *    deprecated comma expression; in C++17 it was the comma operator.)
 *  - `operator[]()` with ZERO parameters is allowed: `obj[]`.
 *  - `static auto operator()(...)` - static call operator, and lambdas
 *    declared `[]() static { }`.
 */
#include <iostream>
#include <vector>
#include <array>
#include <cstddef>
#include <cmath>

constexpr double fast_sqrt(double x) {
    if consteval {
        // Compile-time path: Newton iteration (no std::sqrt in constexpr pre-C++26)
        if (x <= 0) return 0;
        double g = x;
        for (int i = 0; i < 30; ++i) g = 0.5 * (g + x / g);
        return g;
    } else {
        return std::sqrt(x);                       // run-time path
    }
}

constexpr const char* where() {
    if !consteval { return "runtime"; }
    else { return "compile time"; }
}

template <typename T>
class Matrix {
    std::size_t rows_, cols_;
    std::vector<T> data_;
public:
    Matrix(std::size_t r, std::size_t c) : rows_(r), cols_(c), data_(r * c) {}
    T& operator[](std::size_t r, std::size_t c) { return data_[r * cols_ + c]; }
    const T& operator[](std::size_t r, std::size_t c) const { return data_[r * cols_ + c]; }
    std::size_t operator[]() const { return data_.size(); }   // zero-argument subscript
    std::size_t rows() const { return rows_; }
    std::size_t cols() const { return cols_; }
};

template <std::size_t X, std::size_t Y, std::size_t Z>
struct Grid3 {
    std::array<int, X * Y * Z> cells{};
    constexpr int& operator[](std::size_t x, std::size_t y, std::size_t z) {
        return cells[(x * Y + y) * Z + z];
    }
};

struct Hasher {
    static std::size_t operator()(int v) { return static_cast<std::size_t>(v) * 2654435761u; }
};

struct Table {
    static constexpr int lookup[4] = {10, 20, 30, 40};
    static constexpr int operator[](std::size_t i) { return lookup[i]; }
};

// P2242R3: a constexpr function may now CONTAIN goto/labels/static vars as
// long as they are not evaluated during constant evaluation.
constexpr int count_down(int n) {
    if (n < 0) {
        static int error_count = 0;              // never reached at compile time
        ++error_count;
        goto fail;
    }
    return n;
fail:
    return -1;
}

int main() {
    constexpr double ct = fast_sqrt(2.0);
    double rt = fast_sqrt(2.0);
    std::cout.precision(12);
    std::cout << "compile-time sqrt(2) = " << ct << '\n';
    std::cout << "runtime sqrt(2)      = " << rt << '\n';
    constexpr const char* w = where();
    std::cout << "where() in constexpr: " << w << ", at runtime: " << where() << '\n';

    Matrix<int> m(3, 4);
    for (std::size_t r = 0; r < m.rows(); ++r)
        for (std::size_t c = 0; c < m.cols(); ++c)
            m[r, c] = static_cast<int>(r * 10 + c);  // multi-arg subscript
    std::cout << "m[2, 3] = " << m[2, 3] << ", m[] (size) = " << m[] << '\n';

    Grid3<2, 3, 4> g;
    g[1, 2, 3] = 99;
    std::cout << "g[1, 2, 3] = " << g[1, 2, 3] << '\n';

    std::cout << "Hasher{}(42) = " << Hasher{}(42) << ", Hasher::operator()(42) = "
              << Hasher::operator()(42) << '\n';
    std::cout << "Table{}[2] = " << Table{}[2] << '\n';

    auto twice = [](int x) static { return x * 2; };   // static lambda
    std::cout << "static lambda: " << twice(21) << '\n';

    constexpr int cd = count_down(5);
    std::cout << "count_down(5) = " << cd << ", count_down(-1) = " << count_down(-1) << '\n';
    return 0;
}
