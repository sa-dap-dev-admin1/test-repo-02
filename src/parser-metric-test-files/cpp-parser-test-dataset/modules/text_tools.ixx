// Feature : C++20 module interface in MSVC's .ixx extension; exported
//           templates, `export using`, re-exported aliases, inline
//           namespaces inside a module
// Version : C++20
// Spec    : P1103R3; MSVC documentation "Overview of modules in C++"
//
// Same grammar as .cppm - this file exists to test that the parser maps
// the `.ixx` EXTENSION to "C++ module interface unit".
//   cl /std:c++20 /c text_tools.ixx
//   clang++ -std=c++20 -x c++-module --precompile text_tools.ixx
//
// Parser edge cases:
//  - No global module fragment: `export module` is the first declaration.
//    Standard headers cannot be #included after it without causing them to
//    attach to the module, so this file uses no #includes at all.
//  - `export using Alias = ...;` and `export using ns::name;`.
//  - `export inline namespace v1 { }`.
//  - An exported class template with an out-of-class member definition.
export module text_tools;

export namespace text {
    constexpr bool is_space(char c) { return c == ' ' || c == '\t' || c == '\n' || c == '\r'; }
    constexpr bool is_upper(char c) { return c >= 'A' && c <= 'Z'; }
    constexpr char to_lower(char c) { return is_upper(c) ? static_cast<char>(c + 32) : c; }

    // Minimal fixed-capacity string so we need no standard headers
    template <unsigned N>
    class FixedText {
    public:
        constexpr FixedText() = default;
        constexpr FixedText(const char* s) { while (*s && len_ < N) buf_[len_++] = *s++; }
        constexpr unsigned size() const { return len_; }
        constexpr char operator[](unsigned i) const { return buf_[i]; }
        constexpr FixedText lowered() const;
        constexpr unsigned count_words() const;
    private:
        char buf_[N]{};
        unsigned len_ = 0;
    };
}

// Out-of-class member template definitions (still exported via the class)
template <unsigned N>
constexpr text::FixedText<N> text::FixedText<N>::lowered() const {
    FixedText out;
    for (unsigned i = 0; i < len_; ++i) out.buf_[out.len_++] = to_lower(buf_[i]);
    return out;
}

template <unsigned N>
constexpr unsigned text::FixedText<N>::count_words() const {
    unsigned words = 0;
    bool in_word = false;
    for (unsigned i = 0; i < len_; ++i) {
        if (is_space(buf_[i])) in_word = false;
        else if (!in_word) { in_word = true; ++words; }
    }
    return words;
}

export using Text64 = text::FixedText<64>;
export using text::to_lower;

export inline namespace v1 {
    constexpr const char* tools_version() { return "text_tools v1"; }
}

static_assert(Text64("Hello Module World").count_words() == 3);
static_assert(Text64("ABC").lowered()[1] == 'b');
