# C++ parser test dataset: expected results

Strict mode = `-std=c++XX -pedantic-errors` for the Version column.

Validation: Pass rows were compiled with GCC 14.2 AND Clang 21.1 and the GCC binary was run, unless notes say otherwise. C++26 rows are compile-only with Clang 21 (`-std=c++2c`).

Fill in the "Parser result" column when you run your parser. "Pass" means the parser accepts the file in the mode for its version. "Fail" means a correct parser must REJECT it with a useful error, not crash or silently mis-parse.

| File | Version | Expected (strict) | Parser result | Validated with / notes |
|---|---|---|---|---|
| `cpp14/GenericLambdas.cpp` | C++14 | Pass | | GCC+Clang, ran |
| `cpp14/RelaxedConstexprVariableTemplates.cpp` | C++14 | Pass | | GCC+Clang, ran |
| `cpp14/ReturnTypeDeductionLiterals.cpp` | C++14 | Pass | | GCC+Clang, ran |
| `cpp17/AttributesVocabularyTypes.cpp` | C++17 | Pass | | GCC+Clang, ran |
| `cpp17/ClassTemplateArgumentDeduction.cpp` | C++17 | Pass | | GCC+Clang, ran |
| `cpp17/IfConstexprFoldExpressions.cpp` | C++17 | Pass | | GCC+Clang, ran |
| `cpp17/InitStatementsInlineVariables.cpp` | C++17 | Pass | | GCC+Clang, ran |
| `cpp17/StructuredBindings.cpp` | C++17 | Pass | | GCC+Clang, ran |
| `cpp20/AbbreviatedTemplatesLambdas.cpp` | C++20 | Pass | | GCC+Clang, ran |
| `cpp20/ClassTypeTemplateParameters.cpp` | C++20 | Pass | | GCC+Clang, ran |
| `cpp20/ConceptsRequires.cpp` | C++20 | Pass | | GCC+Clang, ran |
| `cpp20/Coroutines.cpp` | C++20 | Pass | | GCC+Clang, ran |
| `cpp20/DesignatedInitConstevalMisc.cpp` | C++20 | Pass | | GCC+Clang, ran |
| `cpp20/RangesViews.cpp` | C++20 | Pass | | GCC+Clang, ran |
| `cpp20/ThreeWayComparison.cpp` | C++20 | Pass | | GCC+Clang, ran |
| `cpp23/DeducingThis.cpp` | C++23 | Pass | | GCC+Clang, ran |
| `cpp23/ExpectedPrintLibrary.cpp` | C++23 | Pass | | GCC+Clang, ran. Library-heavy; trimmed to features both libstdc++ 14 and libc++ 21 ship. |
| `cpp23/IfConstevalMultidimSubscript.cpp` | C++23 | Pass | | GCC+Clang, ran |
| `cpp23/MiscSyntaxChanges.cpp` | C++23 | Pass | | GCC+Clang, ran |
| `cpp26/DeletedWithReasonUserStaticAssert.cpp` | C++26 | Pass (C++26) / Fail (C++23) | | Clang 21 compile-only, both directions checked |
| `cpp26/PackIndexingPlaceholders.cpp` | C++26 | Pass (C++26) / Fail (C++23) | | Clang 21 compile-only, both directions checked. GCC 15+ for most features. |
| `headers/ObservableValue.hpp` | C++17 | Pass | | GCC syntax-only + instantiated via test TU on GCC+Clang |
| `modules/import_std_consumer.cpp` | C++20 modules | Pass (C++23) | | Parsed by GCC 14; NOT built. `import std;` needs a prebuilt std module (GCC 15+, Clang+libc++, MSVC 17.5+). |
| `modules/inventory-items.cppm` | C++20 modules | Pass | | GCC 14 `-fmodules-ts`. Partition interface unit (`module inventory:items`). |
| `modules/math_utils.cppm` | C++20 modules | Pass | | GCC 14 `-fmodules-ts`. Module interface unit. Extension `.cppm`. |
| `modules/text_tools.ixx` | C++20 modules | Pass | | GCC 14 `-fmodules-ts` (`-x c++`). Tests the MSVC `.ixx` extension mapping. |
| `parser-traps/AngleBracketsVexingParse.cpp` | C++11+ | Pass (C++11..23) | | GCC+Clang at C++11 and C++23, ran |
| `parser-traps/TemplateMetaprogrammingSfinae.cpp` | C++11+ | Pass (C++17+) | | GCC+Clang, ran |
| `removed-features/RemovedInCpp17.cpp` | C++14 only | Pass (C++14) / Fail (C++17+) | | GCC+Clang, both directions checked. GCC needs `-trigraphs` (or strict `-std=c++14`) to replace trigraphs. |
