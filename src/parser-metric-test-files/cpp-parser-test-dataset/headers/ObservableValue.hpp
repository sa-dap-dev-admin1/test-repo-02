/*
 * Feature : Header-only C++ library (.hpp) - #pragma once, inline
 *           variables, class templates with out-of-class member
 *           definitions, nested templates, ODR-safe inline functions,
 *           [[nodiscard]] API, noexcept specifications
 * Version : C++17
 * Spec    : P0386R2 (inline variables), [basic.def.odr], [temp.mem]
 *
 * Header files are not runnable. Accept with:
 *   g++ -std=c++17 -fsyntax-only -x c++ ObservableValue.hpp
 *
 * Parser edge cases:
 *  - `.hpp` must be mapped to C++ (unlike `.h`, which is ambiguous).
 *  - Out-of-class definition of a member TEMPLATE of a class TEMPLATE:
 *      template <typename T> template <typename F>
 *      auto Observable<T>::subscribe(F&& f) -> SubscriptionId { ... }
 *    (two template headers in a row).
 *  - `typename Observable<T>::SubscriptionId` in a trailing return type.
 *  - Conversion operator template, explicit operator bool.
 *  - noexcept(noexcept(expr)) - the operator inside the specifier.
 */
#pragma once

#include <cstddef>
#include <functional>
#include <map>
#include <utility>
#include <string>

namespace obs {

inline constexpr const char* library_version = "1.2.0";
inline std::size_t global_subscription_count = 0;   // one definition across TUs

template <typename T>
class Observable {
public:
    using value_type = T;
    using SubscriptionId = std::size_t;
    using Callback = std::function<void(const T& old_value, const T& new_value)>;

    Observable() = default;
    explicit Observable(T initial) noexcept(std::is_nothrow_move_constructible_v<T>)
        : value_(std::move(initial)) {}

    [[nodiscard]] const T& get() const noexcept { return value_; }

    template <typename U>
    void set(U&& v) noexcept(noexcept(std::declval<T&>() = std::forward<U>(v)));

    template <typename F>
    [[nodiscard]] auto subscribe(F&& f) -> SubscriptionId;

    bool unsubscribe(SubscriptionId id) noexcept;

    [[nodiscard]] std::size_t subscriber_count() const noexcept { return callbacks_.size(); }

    explicit operator bool() const noexcept { return !callbacks_.empty(); }

    template <typename U, typename = std::enable_if_t<std::is_convertible_v<T, U>>>
    operator U() const { return static_cast<U>(value_); }

    // Nested class template
    template <typename Projection>
    class Derived {
    public:
        Derived(const Observable& src, Projection p) : src_(&src), proj_(std::move(p)) {}
        auto get() const { return proj_(src_->get()); }
    private:
        const Observable* src_;
        Projection proj_;
    };

    template <typename Projection>
    Derived<Projection> map(Projection p) const { return Derived<Projection>(*this, std::move(p)); }

private:
    T value_{};
    SubscriptionId next_id_ = 1;
    std::map<SubscriptionId, Callback> callbacks_;
};

// Out-of-class member template definitions: two template headers
template <typename T>
template <typename U>
void Observable<T>::set(U&& v) noexcept(noexcept(std::declval<T&>() = std::forward<U>(v))) {
    T old = value_;
    value_ = std::forward<U>(v);
    for (const auto& [id, cb] : callbacks_) cb(old, value_);
}

template <typename T>
template <typename F>
auto Observable<T>::subscribe(F&& f) -> SubscriptionId {
    const SubscriptionId id = next_id_++;
    callbacks_.emplace(id, Callback(std::forward<F>(f)));
    ++global_subscription_count;
    return id;
}

template <typename T>
bool Observable<T>::unsubscribe(SubscriptionId id) noexcept {
    return callbacks_.erase(id) > 0;
}

// Free function template and an explicit specialization (inline for ODR)
template <typename T>
std::string describe(const Observable<T>& o) {
    return "Observable with " + std::to_string(o.subscriber_count()) + " subscriber(s)";
}

template <>
inline std::string describe(const Observable<std::string>& o) {
    return "Observable<string> = \"" + o.get() + "\"";
}

} // namespace obs
