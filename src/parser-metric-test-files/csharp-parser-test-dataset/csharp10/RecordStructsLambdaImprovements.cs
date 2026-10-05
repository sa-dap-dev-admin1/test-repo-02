/*
 * Feature : record struct / readonly record struct / record class,
 *           `with` on structs and anonymous types, lambda natural types,
 *           explicit lambda return types, attributes on lambdas,
 *           parameterless struct constructors and field initializers,
 *           mixed declaration+assignment deconstruction
 * Version : C# 10
 * Spec    : csharplang proposals/csharp-10.0/record-structs.md,
 *           lambda-improvements.md, parameterless-struct-constructors.md
 *
 * Parser edge cases:
 *  - `record struct P(int X);` and `readonly record struct` and the
 *    explicit `record class`.
 *  - `var f = (int x) => x * 2;` - lambda with a NATURAL type (Func<int,int>).
 *  - `var g = int? (string s) => ...;` - explicit return type BEFORE the
 *    parameter list. `ref int (ref int x) => ref x` also valid.
 *  - `[Obsolete] (x) => x` - attribute list in front of a lambda.
 *  - `(x, var y) = point;` - existing variable and new declaration mixed
 *    inside one deconstruction.
 *  - `public S() { ... }` - parameterless constructor in a struct.
 */
using System;
using System.Collections.Generic;

namespace Csharp10.Structs
{
    public record struct Point(int X, int Y);                     // mutable record struct
    public readonly record struct Money(decimal Amount, string Currency);
    public record class Tag(string Name);                         // explicit `record class`

    public struct Settings
    {
        public int Retries = 3;                                   // field initializer in a struct
        public string Mode;
        public Settings() { Mode = "default"; }                   // parameterless ctor
    }

    class Program
    {
        static void Main()
        {
            var p = new Point(1, 2);
            p.X = 10;                                             // record struct is mutable
            var q = p with { Y = 20 };
            Console.WriteLine($"{p} -> {q}, equal? {p == q}");

            var m = new Money(9.99m, "USD");
            var eur = m with { Currency = "EUR" };
            Console.WriteLine($"{m} / {eur}");

            var anon = new { Name = "anon", Count = 1 };
            var anon2 = anon with { Count = 2 };                  // `with` on anonymous type
            Console.WriteLine($"anonymous with: {anon2}");

            Console.WriteLine($"record class: {new Tag("x")}");
            var s = new Settings();
            Console.WriteLine($"Settings: retries={s.Retries} mode={s.Mode}");

            // Lambda natural types
            var square = (int x) => x * x;
            var greet = () => "hello";
            var parse = int? (string text) => int.TryParse(text, out var v) ? v : null;   // explicit return type
            Func<int, int> asFunc = square;
            Delegate d = greet;
            Console.WriteLine($"square(7)={square(7)}, greet()={greet()}, parse(\"x\")={(parse("x")?.ToString() ?? "null")}");
            Console.WriteLine($"natural types: {square.GetType().Name}, delegate invoke: {d.DynamicInvoke()}, asFunc(3)={asFunc(3)}");

            // Attributes on lambdas and lambda parameters
            var checkedLen = [Obsolete("demo")] ([System.Diagnostics.CodeAnalysis.NotNull] string str) => str.Length;
            Console.WriteLine($"attributed lambda: {checkedLen("four")}");

            // Method group natural type
            Action<string> line = Console.WriteLine;
            line("method group converted to Action<string>");

            // Mixed deconstruction
            int x;
            (x, var y) = p;
            Console.WriteLine($"mixed deconstruction: x={x} y={y}");

            var lookup = new Dictionary<string, Func<double, double>>
            {
                ["half"] = v => v / 2,
                ["neg"] = static v => -v,
            };
            foreach (var (name, fn) in lookup) Console.WriteLine($"{name}(8) = {fn(8)}");
        }
    }
}
