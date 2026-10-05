/*
 * Feature : partial properties and indexers, ref struct types implementing
 *           interfaces, `allows ref struct` anti-constraint, ref struct
 *           type arguments
 * Version : C# 13 (.NET 9)
 * Spec    : csharplang proposals/csharp-13.0/partial-properties.md,
 *           ref-struct-interfaces.md
 *
 * VALIDATION: NOT compiled in the dataset build environment (C# 12 max).
 * Verify with the .NET 9 SDK.
 *
 * Parser edge cases:
 *  - `public partial string Name { get; set; }` - a partial property
 *    DECLARATION (looks exactly like an auto-property) and a separate
 *    IMPLEMENTATION with accessor bodies in the other part.
 *  - `public partial int this[int i] { get; }` - partial indexer.
 *  - `ref struct Cursor : IEnumerator<char>` - interface base list on a
 *    ref struct (an error before C# 13).
 *  - `where T : allows ref struct` - `allows` is a contextual keyword that
 *    RELAXES rather than restricts; it must come last in the constraint list.
 */
using System;
using System.Collections;
using System.Collections.Generic;

namespace Csharp13.Partial
{
    partial class Person
    {
        public partial string Name { get; set; }                 // declaring declaration
        public partial int this[int index] { get; }              // partial indexer
    }

    partial class Person
    {
        private string _name = "";
        private readonly int[] _scores = { 90, 85, 77 };

        public partial string Name                               // implementing declaration
        {
            get => _name;
            set => _name = value?.Trim() ?? throw new ArgumentNullException(nameof(value));
        }

        public partial int this[int index] => _scores[index];
    }

    interface IMeasurable { int Measure(); }

    ref struct SpanWindow : IMeasurable                          // ref struct implementing an interface
    {
        private readonly Span<int> _span;
        public SpanWindow(Span<int> span) => _span = span;
        public int Measure() => _span.Length;
        public int Sum() { int s = 0; foreach (var v in _span) s += v; return s; }
    }

    class Program
    {
        // Generic method that accepts ref struct type arguments
        static int MeasureIt<T>(T item) where T : IMeasurable, allows ref struct => item.Measure();

        static void Consume<T>(scoped T value, Action<string> report) where T : allows ref struct
            => report($"consumed a {typeof(T).Name}");

        static void Main()
        {
            var p = new Person { Name = "   Ada   " };
            Console.WriteLine($"partial property trimmed: [{p.Name}], partial indexer p[1] = {p[1]}");

            Span<int> data = stackalloc int[] { 3, 4, 5 };
            var window = new SpanWindow(data);
            Console.WriteLine($"ref struct via interface constraint: Measure={MeasureIt(window)}, Sum={window.Sum()}");

            ReadOnlySpan<char> text = "allows ref struct";
            Consume(text, Console.WriteLine);
        }
    }
}
