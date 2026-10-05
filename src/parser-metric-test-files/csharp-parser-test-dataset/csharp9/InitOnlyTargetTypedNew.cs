/*
 * Feature : init-only setters, target-typed `new()`, target-typed
 *           conditional, covariant return types, static anonymous
 *           functions, lambda discard parameters, attributes on local
 *           functions, extension GetEnumerator, module initializers
 * Version : C# 9.0
 * Spec    : csharplang proposals/csharp-9.0/init.md,
 *           target-typed-new.md, target-typed-conditional-expression.md,
 *           covariant-returns.md, static-anonymous-functions.md,
 *           lambda-discard-parameters.md, local-function-attributes.md,
 *           extension-getenumerator.md, module-initializers.md
 *
 * Parser edge cases:
 *  - `new()` / `new(args)` with NO type name; `new() { X = 1 }`.
 *  - `{ get; init; }` - `init` accessor keyword.
 *  - `static x => x * 2` and `static () => ...` - static lambdas.
 *  - `(_, _) => 0` - two discard parameters (an identifier named `_` can
 *    appear only once in C# 8).
 *  - `[ModuleInitializer]` on an internal static method.
 *  - `public override Circle Clone()` overriding `Shape Clone()`.
 *  - `[Conditional("DEBUG")] void Log() { }` - attribute on a LOCAL function.
 *  - `foreach` over a type that only has an EXTENSION GetEnumerator.
 */
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.CompilerServices;

namespace Csharp9Misc
{
    class Shape
    {
        public string Name { get; init; } = "shape";
        public virtual Shape Clone() => new Shape { Name = Name };
    }

    class Circle : Shape
    {
        public double Radius { get; init; }
        public override Circle Clone() => new() { Name = Name, Radius = Radius };   // covariant return
    }

    readonly struct Range3
    {
        public int Start { get; init; }
        public int Count { get; init; }
    }

    static class RangeExtensions
    {
        public static IEnumerator<int> GetEnumerator(this Range3 r)      // extension GetEnumerator
        {
            for (int i = 0; i < r.Count; i++) yield return r.Start + i;
        }
    }

    static class Startup
    {
        public static string InitializedBy = "nobody";

        [ModuleInitializer]
        internal static void Init() => InitializedBy = "module initializer";
    }

    class Program
    {
        static readonly Dictionary<string, List<int>> Index = new();     // target-typed new

        static Shape Pick(bool round) => round ? new Circle { Radius = 1 } : new Shape();

        static void Main()
        {
            Console.WriteLine($"Startup.InitializedBy = {Startup.InitializedBy}");

            var c = new Circle { Name = "unit", Radius = 1.0 };
            // c.Radius = 2;   // error CS8852: init-only property
            Circle copy = c.Clone();                                       // no cast needed
            Console.WriteLine($"covariant clone: {copy.Name} r={copy.Radius}");

            Index["evens"] = new() { 2, 4, 6 };
            List<string> names = new() { "a", "b" };
            Point p = new(3, 4);
            Console.WriteLine($"target-typed new: {Index["evens"].Count} evens, {names.Count} names, point {p.X},{p.Y}");

            // Target-typed conditional: both branches convert to int?
            bool flag = false;
            int? maybe = flag ? 1 : null;
            Console.WriteLine($"target-typed conditional: {(maybe.HasValue ? maybe.ToString() : "null")}");

            Console.WriteLine($"Pick(true) is {Pick(true).GetType().Name}");

            // Static lambdas cannot capture
            Func<int, int> twice = static x => x * 2;
            Func<int> fortyTwo = static () => 42;
            Console.WriteLine($"static lambdas: {twice(21)}, {fortyTwo()}");

            // Discard parameters
            Func<int, int, int> always = (_, _) => 7;
            EventHandler handler = (_, _) => Console.WriteLine("event raised with discarded args");
            handler(null, EventArgs.Empty);
            Console.WriteLine($"discard lambda = {always(1, 2)}");

            // Attribute on a local function
            [Conditional("NEVER_DEFINED")]
            static void TraceLocal(string m) => Console.WriteLine($"trace: {m}");
            TraceLocal("this call is removed by [Conditional]");

            // foreach using extension GetEnumerator
            var range = new Range3 { Start = 5, Count = 3 };
            Console.Write("extension GetEnumerator:");
            foreach (var i in range) Console.Write($" {i}");
            Console.WriteLine();
        }
    }

    record Point(int X, int Y);
}
