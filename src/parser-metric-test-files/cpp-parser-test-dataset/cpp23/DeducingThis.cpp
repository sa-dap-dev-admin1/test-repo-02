/*
 * Feature : Explicit object parameters ("deducing this")
 * Version : C++23 (ISO/IEC 14882:2024)
 * Spec    : P0847R7 "Deducing this"
 *
 * A member function may declare its object parameter explicitly as the
 * first parameter, prefixed with `this`:
 *     void f(this Self&& self, int x);
 * This removes const/non-const/&/&& overload duplication, enables
 * recursive lambdas, and CRTP without templates on the class.
 *
 * Parser edge cases:
 *  - `this` appears as a DECL-SPECIFIER at the start of a parameter:
 *    `(this auto&& self)`, `(this const Widget& w)`, `(this Widget w)`.
 *  - Only the FIRST parameter may have `this`; such functions cannot be
 *    static, virtual, or have cv/ref qualifiers after the parameter list.
 *  - Inside the body there is NO implicit `this` - members must be
 *    accessed through the named parameter (`self.member`).
 *  - Recursive lambda: `[](this auto self, int n) { ... self(n-1) ... }`.
 *  - Calling via pointer-to-member yields an ordinary function pointer.
 */
#include <iostream>
#include <string>
#include <vector>
#include <utility>
#include <optional>

class TextBuffer {
    std::string text_;
public:
    explicit TextBuffer(std::string t) : text_(std::move(t)) {}

    // One function replaces four overloads (&, const&, &&, const&&)
    template <typename Self>
    auto&& value(this Self&& self) { return std::forward<Self>(self).text_; }

    // Explicit object by value: operates on a copy
    TextBuffer upper(this TextBuffer copy) {
        for (char& c : copy.text_) if (c >= 'a' && c <= 'z') c -= 32;
        return copy;
    }

    // Non-template explicit object parameter
    std::size_t length(this const TextBuffer& self) { return self.text_.size(); }

    // Fluent builder chaining that preserves value category
    template <typename Self>
    Self&& append(this Self&& self, const std::string& more) {
        self.text_ += more;
        return std::forward<Self>(self);
    }
};

// CRTP replacement: base deduces the derived type through `this auto&`
struct Printable {
    void print(this const auto& self) {
        std::cout << "[" << self.name() << "] " << self.describe() << '\n';
    }
};
struct Circle : Printable {
    double r;
    explicit Circle(double radius) : r(radius) {}
    std::string name() const { return "circle"; }
    std::string describe() const { return "r=" + std::to_string(r); }
};
struct Square : Printable {
    int side;
    explicit Square(int s) : side(s) {}
    std::string name() const { return "square"; }
    std::string describe() const { return "side=" + std::to_string(side); }
};

// Binary tree visitor using a recursive explicit-object lambda
struct Node {
    int value;
    Node* left = nullptr;
    Node* right = nullptr;
};

int main() {
    TextBuffer buf("hello");
    buf.value() += " world";                         // lvalue -> std::string&
    const TextBuffer& cref = buf;
    std::cout << "const value: " << cref.value() << '\n';
    std::string stolen = TextBuffer("temporary").value();  // rvalue -> moved
    std::cout << "rvalue value: " << stolen << '\n';

    std::cout << "upper: " << buf.upper().value() << " (original: " << buf.value() << ")\n";
    std::cout << "length: " << buf.length() << '\n';

    TextBuffer built = TextBuffer("a").append("b").append("c");
    std::cout << "chained: " << built.value() << '\n';

    Circle c{1.5};
    Square s{4};
    c.print();
    s.print();

    // Recursive lambda without std::function or Y-combinator tricks
    auto fib = [](this auto self, int n) -> long { return n < 2 ? n : self(n - 1) + self(n - 2); };
    std::cout << "fib(25) = " << fib(25) << '\n';

    Node n4{4}, n5{5}, n2{2, &n4, &n5}, n3{3}, root{1, &n2, &n3};
    auto sum_tree = [](this auto&& self, const Node* n) -> int {
        return n ? n->value + self(n->left) + self(n->right) : 0;
    };
    std::cout << "tree sum = " << sum_tree(&root) << '\n';

    // Pointer to an explicit-object member function is a plain function pointer
    std::size_t (*len_fn)(const TextBuffer&) = &TextBuffer::length;
    std::cout << "via function pointer: " << len_fn(buf) << '\n';
    return 0;
}
