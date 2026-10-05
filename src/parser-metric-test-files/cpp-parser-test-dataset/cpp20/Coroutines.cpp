/*
 * Feature : Coroutines (co_await, co_yield, co_return)
 * Version : C++20
 * Spec    : P0912R5 "Merge Coroutines TS into C++20 working draft"
 *           (N4775 Coroutines TS)
 *
 * A function is a coroutine if its body contains co_await, co_yield or
 * co_return. Its return type must provide a promise_type. This file builds
 * a minimal Generator<T> (co_yield) and a lazy Task<T> (co_await /
 * co_return) using only <coroutine> - no third-party library.
 * (C++23 adds std::generator, but this stays within C++20.)
 *
 * Parser edge cases:
 *  - co_await / co_yield / co_return are KEYWORDS in C++20; identifiers in
 *    C++17 (a C++17 parser sees `co_yield x;` as two identifiers).
 *  - `co_await expr` is a unary expression with the same precedence as
 *    other unary operators: `co_await a + b` is `(co_await a) + b`.
 *  - `co_yield expr` is NOT a unary expression; it has assignment-
 *    expression precedence: `co_yield a + b` yields (a + b).
 *  - `co_return;` (void) vs `co_return expr;`.
 *  - A lambda can be a coroutine.
 *  - A coroutine cannot use plain `return`, `auto` return type, or be
 *    constexpr / a constructor / main.
 */
#include <iostream>
#include <coroutine>
#include <exception>
#include <optional>
#include <string>
#include <utility>

template <typename T>
class Generator {
public:
    struct promise_type {
        std::optional<T> current;
        Generator get_return_object() { return Generator{Handle::from_promise(*this)}; }
        std::suspend_always initial_suspend() noexcept { return {}; }
        std::suspend_always final_suspend() noexcept { return {}; }
        std::suspend_always yield_value(T v) { current = std::move(v); return {}; }
        void return_void() {}
        void unhandled_exception() { std::terminate(); }
    };
    using Handle = std::coroutine_handle<promise_type>;

    explicit Generator(Handle h) : h_(h) {}
    Generator(Generator&& o) noexcept : h_(std::exchange(o.h_, {})) {}
    Generator(const Generator&) = delete;
    ~Generator() { if (h_) h_.destroy(); }

    bool next() { h_.resume(); return !h_.done(); }
    const T& value() const { return *h_.promise().current; }

private:
    Handle h_;
};

template <typename T>
class Task {
public:
    struct promise_type {
        T result{};
        std::coroutine_handle<> continuation;
        Task get_return_object() { return Task{std::coroutine_handle<promise_type>::from_promise(*this)}; }
        std::suspend_always initial_suspend() noexcept { return {}; }
        auto final_suspend() noexcept {
            struct Final {
                bool await_ready() noexcept { return false; }
                std::coroutine_handle<> await_suspend(std::coroutine_handle<promise_type> h) noexcept {
                    auto c = h.promise().continuation;
                    return c ? c : std::noop_coroutine();
                }
                void await_resume() noexcept {}
            };
            return Final{};
        }
        void return_value(T v) { result = std::move(v); }
        void unhandled_exception() { std::terminate(); }
    };

    explicit Task(std::coroutine_handle<promise_type> h) : h_(h) {}
    Task(Task&& o) noexcept : h_(std::exchange(o.h_, {})) {}
    ~Task() { if (h_) h_.destroy(); }

    // Awaitable interface so one Task can co_await another
    bool await_ready() const noexcept { return false; }
    std::coroutine_handle<> await_suspend(std::coroutine_handle<> caller) noexcept {
        h_.promise().continuation = caller;
        return h_;
    }
    T await_resume() { return std::move(h_.promise().result); }

    T run() { h_.resume(); return std::move(h_.promise().result); }

private:
    std::coroutine_handle<promise_type> h_;
};

Generator<int> range(int from, int to, int step = 1) {
    for (int i = from; i < to; i += step) co_yield i;
}

Generator<long> fibonacci(int count) {
    long a = 0, b = 1;
    while (count-- > 0) {
        co_yield a;
        a = std::exchange(b, a + b);
    }
    co_return;
}

Generator<std::string> words(std::string text) {
    std::string cur;
    for (char c : text) {
        if (c == ' ') { if (!cur.empty()) co_yield std::exchange(cur, {}); }
        else cur += c;
    }
    if (!cur.empty()) co_yield cur + "";          // co_yield takes a full expression
}

Task<int> compute_part(int x) { co_return x * x; }

Task<int> compute_total() {
    int a = co_await compute_part(3);
    int b = co_await compute_part(4) + 1;          // (co_await ...) + 1
    co_return a + b;
}

int main() {
    std::cout << "range(0, 10, 3):";
    for (auto g = range(0, 10, 3); g.next();) std::cout << ' ' << g.value();
    std::cout << '\n';

    std::cout << "fibonacci(10):";
    for (auto g = fibonacci(10); g.next();) std::cout << ' ' << g.value();
    std::cout << '\n';

    std::cout << "words:";
    for (auto g = words("  coroutines  are lazy "); g.next();) std::cout << " [" << g.value() << ']';
    std::cout << '\n';

    std::cout << "compute_total() = " << compute_total().run() << '\n';

    // A coroutine lambda
    auto countdown = [](int n) -> Generator<int> { while (n > 0) co_yield n--; };
    std::cout << "countdown:";
    for (auto g = countdown(3); g.next();) std::cout << ' ' << g.value();
    std::cout << '\n';
    return 0;
}
