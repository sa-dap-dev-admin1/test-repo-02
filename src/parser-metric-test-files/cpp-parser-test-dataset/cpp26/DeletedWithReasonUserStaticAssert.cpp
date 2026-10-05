/*
 * Feature : `= delete("reason")`, user-generated static_assert messages,
 *           constexpr cast from void*, constexpr placement new, `@` `$` and
 *           backtick in the basic character set, [[indeterminate]]
 * Version : C++26 (working draft)
 * Spec    : P2573R2 (= delete("should have a reason")), P2741R3
 *           (user-generated static_assert messages), P2738R1 (constexpr
 *           cast from void*), P2747R2 (constexpr placement new), P2558R2
 *           (add @, $, and ` to the basic character set), P2795R5 (erroneous behaviour / [[indeterminate]])
 *
 * Compiler support: Clang 19-21 (-std=c++2c); GCC 15+ for most.
 *
 * Parser edge cases:
 *  - `void f(int) = delete("use f(long)");` - a string-literal argument
 *    after `delete` in a function definition.
 *  - `static_assert(cond, expr)` where expr is NOT a string literal but a
 *    constant expression with .size() and .data() members.
 *  - `#embed` is NOT used here: Clang 21 still diagnoses it in C++ as an
 *    extension, so it is covered by the C23 dataset instead.
 *  - `[[indeterminate]]` on a local variable declaration.
 */
#include <iostream>
#include <string_view>
#include <new>
#include <cstddef>

struct Handle {
    explicit Handle(int fd) : fd_(fd) {}
    Handle(const Handle&) = delete("handles are unique; move instead");
    Handle& operator=(const Handle&) = delete("handles are unique; move instead");
    Handle(Handle&& o) noexcept : fd_(o.fd_) { o.fd_ = -1; }
    int fd() const { return fd_; }
private:
    int fd_;
};

void set_timeout(long ms) { std::cout << "timeout " << ms << "ms\n"; }
void set_timeout(double) = delete("timeouts are whole milliseconds; pass a long");

// A message type for static_assert: anything with constexpr size() and data()
struct Message {
    const char* text;
    constexpr std::size_t size() const { return std::string_view(text).size(); }
    constexpr const char* data() const { return text; }
};

template <typename T>
constexpr Message size_message() {
    if constexpr (sizeof(T) > 8) return {"type is larger than 8 bytes"};
    else return {"type fits in a register"};
}

template <typename T>
void require_small() {
    static_assert(sizeof(T) <= 8, size_message<T>());   // non-literal message
}

constexpr int roundtrip(int v) {
    void* p = &v;
    int* back = static_cast<int*>(p);                 // constexpr cast from void*
    return *back + 1;
}

constexpr int placement_demo() {
    struct Slot { int x; };
    Slot storage{0};
    Slot* s = ::new (&storage) Slot{41};              // constexpr placement new
    return s->x + 1;
}


int main() {
    Handle h(3);
    Handle moved(static_cast<Handle&&>(h));
    std::cout << "moved fd = " << moved.fd() << ", source fd = " << h.fd() << '\n';

    set_timeout(250L);
    require_small<double>();

    static_assert(roundtrip(9) == 10);
    static_assert(placement_demo() == 42);
    std::cout << "constexpr void* roundtrip = " << roundtrip(9)
              << ", placement new = " << placement_demo() << '\n';

    int value [[indeterminate]];                       // opt out of erroneous-value init
    value = 5;
    std::cout << "indeterminate-then-assigned value = " << value << '\n';
    return 0;
}
