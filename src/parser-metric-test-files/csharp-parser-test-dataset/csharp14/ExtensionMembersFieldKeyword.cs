/*
 * Feature : Extension members (extension blocks: properties, static members,
 *           operators), the `field` keyword in property accessors
 * Version : C# 14 (.NET 10, November 2025)
 * Spec    : csharplang proposals/csharp-14.0/extensions.md,
 *           field-keyword.md
 *
 * VALIDATION: NOT compiled in the dataset build environment (C# 12 max).
 * Verify with the .NET 10 SDK.
 *
 * Parser edge cases:
 *  - `extension(string s) { ... }` - a new member-like BLOCK inside a
 *    static class, with a receiver parameter list. It can contain instance
 *    members (using the receiver), and `extension(string)` with no receiver
 *    name for STATIC extension members.
 *  - Generic extension blocks: `extension<T>(IEnumerable<T> source)`.
 *  - Extension PROPERTIES: `public bool IsBlank => ...;` inside the block,
 *    called as `"x".IsBlank` (no parentheses).
 *  - `field` inside an accessor refers to the compiler-synthesized backing
 *    field: `set => field = value.Trim();`. A member actually NAMED `field`
 *    must now be written `@field` or `this.field` (breaking change).
 */
using System;
using System.Collections.Generic;
using System.Linq;

namespace Csharp14.Extensions
{
    public static class StringExtensions
    {
        extension(string s)                                     // instance extension members
        {
            public bool IsBlank => string.IsNullOrWhiteSpace(s);
            public string Shout() => s.ToUpperInvariant() + "!";
            public int WordCount => s.Split(' ', StringSplitOptions.RemoveEmptyEntries).Length;
        }

        extension(string)                                       // static extension members
        {
            public static string Repeat(string text, int n) => string.Concat(Enumerable.Repeat(text, n));
        }
    }

    public static class SequenceExtensions
    {
        extension<T>(IEnumerable<T> source) where T : IComparable<T>
        {
            public bool IsEmpty => !source.Any();
            public T Largest => source.Max()!;
            public IEnumerable<T> Sorted() => source.OrderBy(x => x);
        }

        // Classic `this` extension methods still coexist
        public static string Describe<T>(this IEnumerable<T> source) => $"[{string.Join(", ", source)}]";
    }

    class Temperature
    {
        public double Celsius
        {
            get;
            set => field = value < -273.15 ? throw new ArgumentOutOfRangeException(nameof(value)) : value;
        }

        public string Label
        {
            get => field ?? "(unnamed)";
            set => field = value.Trim();
        }

        private int @field = 3;                                  // a real member named `field` needs @
        public int RealFieldValue => @field;
    }

    class Program
    {
        static void Main()
        {
            Console.WriteLine($"\"   \".IsBlank = {"   ".IsBlank}, \"hi\".Shout() = {"hi".Shout()}");
            Console.WriteLine($"WordCount = {"extension members are here".WordCount}");
            Console.WriteLine($"string.Repeat = {string.Repeat("ab", 3)}");

            int[] nums = { 5, 3, 9, 1 };
            Console.WriteLine($"IsEmpty={nums.IsEmpty}, Largest={nums.Largest}, Sorted={nums.Sorted().Describe()}");

            var t = new Temperature { Celsius = 21.5, Label = "  lab  " };
            Console.WriteLine($"field keyword: {t.Celsius} C, label [{t.Label}], @field = {t.RealFieldValue}");
            try { t.Celsius = -300; }
            catch (ArgumentOutOfRangeException) { Console.WriteLine("setter using `field` validated input"); }
        }
    }
}
