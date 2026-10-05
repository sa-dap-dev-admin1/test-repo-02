/*
 * Feature : Relational patterns (<, <=, >, >=), logical patterns
 *           (and, or, not), parenthesized patterns, type patterns
 *           without a designation
 * Version : C# 9.0
 * Spec    : csharplang proposals/csharp-9.0/patterns3.md
 *
 * Parser edge cases:
 *  - `is not null` - `not` is a CONTEXTUAL keyword only inside patterns.
 *  - `x is > 0 and < 10` - relational operators with no left operand.
 *  - `c is (>= 'a' and <= 'z') or (>= 'A' and <= 'Z')` - parenthesized
 *    patterns vs a parenthesized expression vs a tuple/positional pattern.
 *  - `case int or long:` - type pattern with NO variable (C# 9 allows bare
 *    `Type` as a pattern where C# 8 required `Type _`).
 *  - Precedence: `not` > `and` > `or`.
 *  - `and`, `or`, `not` remain valid identifiers outside patterns:
 *    `var and = 1;` still compiles.
 */
using System;

namespace Combinators
{
    class Program
    {
        static string Bmi(double value) => value switch
        {
            < 18.5 => "underweight",
            >= 18.5 and < 25 => "normal",
            >= 25 and < 30 => "overweight",
            >= 30 => "obese",
            double.NaN => "invalid",
        };

        static bool IsLetter(char c) => c is (>= 'a' and <= 'z') or (>= 'A' and <= 'Z');

        static bool IsSeparator(char c) => c is ' ' or ',' or ';' or '\t';

        static string Kind(object o) => o switch
        {
            int or long => "integer type",
            float or double => "floating type",
            string { Length: > 10 } => "long string",
            string => "short string",
            not null => "something else",
            null => "null",
        };

        static string HttpClass(int status) => status switch
        {
            >= 100 and <= 199 => "informational",
            >= 200 and <= 299 => "success",
            301 or 302 or 307 or 308 => "redirect",
            >= 400 and < 500 and not 418 => "client error",
            418 => "teapot",
            >= 500 => "server error",
            _ => "unknown",
        };

        static void Main()
        {
            foreach (var v in new[] { 17.0, 22.4, 27.0, 33.3, double.NaN })
                Console.WriteLine($"BMI {v} -> {Bmi(v)}");

            Console.WriteLine($"IsLetter: {IsLetter('q')} {IsLetter('Q')} {IsLetter('7')}");
            Console.WriteLine($"IsSeparator: {IsSeparator(',')} {IsSeparator('x')}");

            foreach (var o in new object[] { 5, 5L, 2.5f, 1.0, "hello", "a much longer string", 'c', null })
                Console.WriteLine($"{o ?? "null"} -> {Kind(o)}");

            foreach (var s in new[] { 101, 204, 302, 404, 418, 503, 42 })
                Console.WriteLine($"{s} -> {HttpClass(s)}");

            string maybe = DateTime.Now.Year > 2000 ? "present" : null;
            if (maybe is not null) Console.WriteLine($"not-null pattern: {maybe}");

            object boxed = 3;
            if (boxed is not (int and > 5)) Console.WriteLine("boxed is not an int greater than 5");

            // `and`, `or`, `not` are still ordinary identifiers outside patterns
            int and = 1, or = 2, not = 3;
            Console.WriteLine($"identifiers: {and + or + not}");
        }
    }
}
