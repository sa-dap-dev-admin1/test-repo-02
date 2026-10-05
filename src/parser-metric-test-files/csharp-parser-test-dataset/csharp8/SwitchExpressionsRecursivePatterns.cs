/*
 * Feature : Switch expressions, property patterns, positional patterns,
 *           tuple patterns, discard pattern
 * Version : C# 8.0
 * Spec    : csharplang proposals/csharp-8.0/patterns.md
 *
 * `value switch { pattern => result, ... }` is an EXPRESSION. Patterns can
 * now recurse: property patterns `{ Prop: pattern }`, positional patterns
 * `(p1, p2)` via Deconstruct, and tuple patterns on `(a, b) switch`.
 *
 * Parser edge cases:
 *  - `x switch { ... }` - `switch` in INFIX position after an expression.
 *  - Arms are separated by commas; a trailing comma is allowed.
 *  - `_ => ...` discard arm.
 *  - `{ Length: 0 }`, `{ Address: { City: "Oslo" } }` - nested braces that
 *    are patterns, not blocks or object initializers.
 *  - `Point(0, 0)` positional pattern looks like a constructor call.
 *  - `(a, b) switch { (0, _) => ..., }` - tuple expression then switch.
 *  - `when` guards in switch-expression arms.
 *  - `{ }` matches any non-null value.
 *  - Relational/logical patterns (`> 5`, `and`, `or`, `not`) are C# 9.
 */
using System;
using System.Collections.Generic;

namespace SwitchExpr
{
    public class Address { public string City { get; set; } = ""; public string Country { get; set; } = ""; }
    public class Customer
    {
        public string Name { get; set; } = "";
        public int Orders { get; set; }
        public Address Address { get; set; } = new Address();
        public bool IsVip { get; set; }
    }

    public readonly struct Point
    {
        public int X { get; }
        public int Y { get; }
        public Point(int x, int y) { X = x; Y = y; }
        public void Deconstruct(out int x, out int y) { x = X; y = Y; }
    }

    enum Light { Red, Amber, Green }

    class Program
    {
        static string Quadrant(Point p) => p switch
        {
            (0, 0) => "origin",
            (var x, 0) when x > 0 => "positive x-axis",
            (_, 0) => "negative x-axis",
            (0, _) => "y-axis",
            var (x, y) when x > 0 && y > 0 => "Q1",
            (_, _) => "other quadrant",
        };

        static decimal Discount(Customer c) => c switch
        {
            { IsVip: true, Orders: var n } when n >= 10 => 0.25m,
            { IsVip: true } => 0.15m,
            { Address: { Country: "NO" } } => 0.10m,
            { Orders: 0 } => 0.05m,
            { } => 0m,
            null => throw new ArgumentNullException(nameof(c)),
        };

        static Light Next(Light current, bool emergency) => (current, emergency) switch
        {
            (_, true) => Light.Red,
            (Light.Red, false) => Light.Green,
            (Light.Green, false) => Light.Amber,
            (Light.Amber, false) => Light.Red,
            _ => throw new ArgumentOutOfRangeException(nameof(current)),
        };

        static string Classify(object o) => o switch
        {
            int i => $"int {i}",
            string { Length: 0 } => "empty string",
            string s => $"string of {s.Length}",
            int[] { Length: 0 } => "empty int[]",
            int[] arr => $"int[{arr.Length}]",
            Point(1, 1) => "the point (1,1)",
            Point _ => "some point",
            null => "null",
            _ => o.GetType().Name,
        };

        static void Main()
        {
            foreach (var p in new[] { new Point(0, 0), new Point(3, 0), new Point(-2, 0), new Point(0, 5), new Point(2, 2), new Point(-1, -1) })
                Console.WriteLine($"({p.X},{p.Y}) -> {Quadrant(p)}");

            var customers = new List<Customer>
            {
                new Customer { Name = "A", IsVip = true, Orders = 12 },
                new Customer { Name = "B", IsVip = true, Orders = 2 },
                new Customer { Name = "C", Address = new Address { Country = "NO" } },
                new Customer { Name = "D", Orders = 0 },
                new Customer { Name = "E", Orders = 3 },
            };
            foreach (var c in customers) Console.WriteLine($"{c.Name}: discount {Discount(c):P0}");

            var light = Light.Red;
            for (int i = 0; i < 4; i++)
            {
                light = Next(light, emergency: i == 3);
                Console.Write($"{light} ");
            }
            Console.WriteLine();

            foreach (var o in new object[] { 4, "", "abc", new int[0], new[] { 1, 2 }, new Point(1, 1), new Point(2, 3), null, 1.5 })
                Console.WriteLine(Classify(o));

            // Property pattern in an `is` expression
            var cust = customers[2];
            if (cust is { Address: { Country: "NO" }, Name: var name })
                Console.WriteLine($"{name} lives in Norway");
        }
    }
}
