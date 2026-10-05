/*
 * Feature : Null-conditional assignment, nameof with unbound generic types,
 *           implicit Span conversions, simple lambda parameters with
 *           modifiers, partial constructors and events, user-defined
 *           compound assignment operators
 * Version : C# 14 (.NET 10)
 * Spec    : csharplang proposals/csharp-14.0/null-conditional-assignment.md,
 *           unbound-generic-types-in-nameof.md, first-class-span-types.md,
 *           simple-lambda-parameters-with-modifiers.md, partial-events-and-
 *           constructors.md, user-defined-compound-assignment.md
 *
 * VALIDATION: NOT compiled in the dataset build environment (C# 12 max).
 * Verify with the .NET 10 SDK.
 *
 * Parser edge cases:
 *  - `customer?.Order = value;` and `a?.b?.c += 1;` - a null-conditional
 *    access on the LEFT side of an assignment (error CS0131 before C# 14).
 *    `x?.y++` / `x?.y--` remain invalid.
 *  - `nameof(List<>)`, `nameof(Dictionary<,>)` - empty type argument list.
 *  - `(ref x, out y) => ...` - modifiers WITHOUT parameter types.
 *  - `public partial Widget(int id);` + implementing `public partial
 *    Widget(int id) { ... }`; `partial event EventHandler Changed;`.
 *  - `public void operator +=(Money m)` - instance compound operator
 *    declaration with `void` return and a single parameter.
 */
using System;
using System.Collections.Generic;

namespace Csharp14.Misc
{
    class Order { public decimal Total { get; set; } public int Items { get; set; } }
    class Customer { public string Name { get; set; } = ""; public Order? Order { get; set; } }

    delegate bool TryParser<T>(string text, out T value);
    delegate void Scaler(ref int value);

    partial class Widget
    {
        public partial Widget(int id);                           // defining declaration
        public partial event EventHandler? Changed;
    }

    partial class Widget
    {
        private EventHandler? _changed;
        public int Id { get; }
        public partial Widget(int id) { Id = id; }               // implementing declaration
        public partial event EventHandler? Changed
        {
            add => _changed += value;
            remove => _changed -= value;
        }
        public void Touch() => _changed?.Invoke(this, EventArgs.Empty);
    }

    class Accumulator
    {
        public long Total { get; private set; }
        public void operator +=(long amount) => Total += amount;   // user-defined compound assignment
        public void operator ++() => Total++;
    }

    class Program
    {
        static int Sum(ReadOnlySpan<int> values) { int s = 0; foreach (var v in values) s += v; return s; }

        static void Main()
        {
            Customer? missing = null;
            var present = new Customer { Name = "Ada", Order = new Order() };

            missing?.Order = new Order();                        // no-op: receiver is null
            present?.Order = new Order { Total = 10m, Items = 1 };
            present?.Order?.Total += 5m;                         // compound null-conditional assignment
            Console.WriteLine($"null-conditional assignment: total {present?.Order?.Total}, missing still null: {missing is null}");

            Console.WriteLine($"nameof unbound generics: {nameof(List<>)}, {nameof(Dictionary<,>)}");

            int[] arr = { 1, 2, 3 };
            Console.WriteLine($"implicit array -> ReadOnlySpan: Sum = {Sum(arr)}");
            ReadOnlySpan<char> chars = "span conversion".AsSpan();
            Console.WriteLine($"span length {chars.Length}");

            TryParser<int> parse = (text, out result) => int.TryParse(text, out result);   // modifiers, no types
            Scaler triple = (ref v) => v *= 3;
            int n = 7;
            triple(ref n);
            Console.WriteLine($"lambda modifiers: parse ok = {parse("42", out var parsed)} ({parsed}), tripled = {n}");

            var w = new Widget(5);
            w.Changed += (_, _) => Console.WriteLine($"partial event fired for widget {w.Id}");
            w.Touch();

            var acc = new Accumulator();
            acc += 40;
            acc++;
            acc++;
            Console.WriteLine($"compound operator: total = {acc.Total}");
        }
    }
}
