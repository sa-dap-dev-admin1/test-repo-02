/*
 * Feature : Value tuples, tuple element names, deconstruction, discards
 * Version : C# 7.0 (Visual Studio 2017, .NET Framework 4.7 / .NET Core 2.0)
 * Spec    : csharplang proposals/csharp-7.0/tuples.md, deconstruction,
 *           discards (docs: "Tuple types", "Deconstructing tuples")
 *
 * `(int, string)` is System.ValueTuple<int, string>. Elements can be named:
 * `(int Count, string Name)`. Deconstruction `var (a, b) = t;` works on
 * tuples and any type with a Deconstruct(out ...) method (instance or
 * extension). `_` is a discard.
 *
 * Parser edge cases:
 *  - `(int, string) Pair()` - a parenthesized TYPE as a return type.
 *  - `var (x, y) = ...` vs `(var x, var y) = ...` vs `(x, y) = ...`
 *    (assignment to existing variables) - three deconstruction forms.
 *  - `(a, b) = (b, a)` - swap via tuple literal on both sides.
 *  - `_` as a discard vs `_` as a legal identifier when a variable named
 *    `_` is in scope.
 *  - Nested: `var (id, (first, last)) = ...`.
 *  - Inferred element names (`(p.Name, p.Age)`) arrive in C# 7.1.
 */
using System;
using System.Collections.Generic;
using System.Linq;

namespace Tuples
{
    class Point
    {
        public int X { get; }
        public int Y { get; }
        public Point(int x, int y) { X = x; Y = y; }
        public void Deconstruct(out int x, out int y) { x = X; y = Y; }
    }

    static class DateTimeExtensions
    {
        // Extension Deconstruct method
        public static void Deconstruct(this DateTime d, out int year, out int month, out int day)
        {
            year = d.Year; month = d.Month; day = d.Day;
        }
    }

    class Program
    {
        static (int Min, int Max, double Average) Stats(IEnumerable<int> values)
        {
            var list = values.ToList();
            return (list.Min(), list.Max(), list.Average());
        }

        static (string, int) Unnamed() => ("unnamed", 2);

        static (int Id, (string First, string Last) Name) Person() => (7, ("Ada", "Lovelace"));

        static void Main()
        {
            var stats = Stats(new[] { 4, 8, 15, 16, 23, 42 });
            Console.WriteLine($"min={stats.Min} max={stats.Max} avg={stats.Average:F2}");

            // Deconstruct into new variables (var form)
            var (min, max, avg) = Stats(new[] { 1, 2, 3 });
            Console.WriteLine($"deconstructed: {min} {max} {avg}");

            // Explicitly typed deconstruction
            (string label, int count) = Unnamed();
            Console.WriteLine($"explicit: {label} {count}");

            // Unnamed tuple uses Item1/Item2
            var u = Unnamed();
            Console.WriteLine($"Item1={u.Item1} Item2={u.Item2}");

            // Assignment into existing variables and swap
            int a = 1, b = 2;
            (a, b) = (b, a);
            Console.WriteLine($"swapped: a={a} b={b}");

            // Nested deconstruction
            var (id, (first, last)) = Person();
            Console.WriteLine($"person #{id}: {first} {last}");

            // Discards
            var (_, highest, _) = Stats(new[] { 10, 20, 30 });
            Console.WriteLine($"only max: {highest}");

            // User-defined Deconstruct
            var (px, py) = new Point(3, 4);
            Console.WriteLine($"point: ({px}, {py})");

            // Extension Deconstruct
            var (year, month, _) = new DateTime(2017, 3, 7);
            Console.WriteLine($"year={year} month={month}");

            // Tuples as dictionary keys / values, with equality
            var lookup = new Dictionary<(int, int), string> { [(0, 0)] = "origin", [(1, 0)] = "east" };
            Console.WriteLine($"lookup[(1,0)] = {lookup[(1, 0)]}");

            // Tuple in foreach with deconstruction
            var pairs = new List<(string Key, int Value)> { ("x", 1), ("y", 2) };
            foreach (var (key, value) in pairs) Console.WriteLine($"  {key} -> {value}");

            // Tuple names are not part of the type identity
            (int Width, int Height) size = (5, 6);
            Console.WriteLine($"size: {size.Width}x{size.Height}");

            // Discard as a standalone expression
            _ = int.TryParse("123", out var parsed);
            Console.WriteLine($"parsed via discard: {parsed}");
        }
    }
}
