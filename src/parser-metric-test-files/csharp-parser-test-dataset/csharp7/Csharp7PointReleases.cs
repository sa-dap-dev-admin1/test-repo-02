/*
 * Feature : C# 7.1, 7.2 and 7.3 point-release features
 * Version : C# 7.1 / 7.2 / 7.3 (compile with -langversion:7.3)
 * Spec    : csharplang proposals/csharp-7.1, csharp-7.2, csharp-7.3
 *   7.1: async Main, `default` literal, inferred tuple element names,
 *        pattern matching on generic type parameters
 *   7.2: `in` parameters, `ref readonly` returns, `readonly struct`,
 *        `ref struct`, `private protected`, non-trailing named arguments,
 *        leading digit separator `0x_FF`, conditional ref expression
 *   7.3: tuple == and !=, `stackalloc` initializers, attributes on
 *        auto-property backing fields (`[field: ...]`), ref local
 *        reassignment, generic constraints `unmanaged`, `Enum`, `Delegate`,
 *        expression variables in initializers
 *
 * Parser edge cases:
 *  - `static async Task Main()` - async entry point.
 *  - `default` used as a literal with no type: `int x = default;`.
 *  - `private protected` - TWO accessibility keywords together.
 *  - `ref readonly int Get()` and `in` parameter modifier.
 *  - `ref struct` and `readonly struct` declarations.
 *  - `Call(name: x, 5)` - named argument followed by positional.
 *  - `ref var r = ref (cond ? ref a : ref b);` - conditional ref.
 *  - `[field: NonSerialized] public int P { get; set; }`.
 *  - `where T : unmanaged`, `where T : System.Enum`.
 */
using System;
using System.Threading.Tasks;

namespace PointReleases
{
    readonly struct Money
    {
        public readonly decimal Amount;
        public readonly string Currency;
        public Money(decimal amount, string currency) { Amount = amount; Currency = currency; }
        public override string ToString() => $"{Amount} {Currency}";
    }

    ref struct SpanCounter
    {
        private Span<int> _buffer;
        public SpanCounter(Span<int> buffer) { _buffer = buffer; }
        public int Sum() { int s = 0; foreach (var v in _buffer) s += v; return s; }
    }

    class Base
    {
        private protected int SharedWithDerivedInAssembly = 7;   // C# 7.2
    }
    class Derived : Base
    {
        public int Read() => SharedWithDerivedInAssembly;
    }

    class Settings
    {
        [field: NonSerialized]                                    // C# 7.3 field-targeted attribute
        public string Cache { get; set; } = "warm";
    }

    class Program
    {
        static readonly int[] Table = { 5, 10, 15 };

        static ref readonly int Largest() => ref Table[Table.Length - 1];

        static decimal Total(in Money a, in Money b) => a.Amount + b.Amount;   // in params

        static string Format(string name, int width, char fill = ' ') => name.PadLeft(width, fill);

        static int SizeOf<T>() where T : unmanaged { unsafe { return sizeof(T); } }

        static string[] EnumNames<T>() where T : struct, Enum => Enum.GetNames(typeof(T));

        static string Kind<T>(T value)
        {
            switch (value)                                         // 7.1: pattern on generic T
            {
                case int i: return $"int {i}";
                case string s: return $"string {s}";
                default: return "other";
            }
        }

        enum Weekday { Mon, Tue, Wed }

        static void RefAndSpanDemos()
        {
            ref readonly int big = ref Largest();
            Console.WriteLine($"ref readonly largest = {big}");

            var m1 = new Money(10.5m, "EUR");
            var m2 = new Money(4.5m, "EUR");
            Console.WriteLine($"Total(in) = {Total(m1, m2)}");

            Console.WriteLine($"[{Format(name: "id", 6, '.')}]");   // 7.2 non-trailing named arg

            Span<int> numbers = stackalloc int[] { 1, 2, 3, 4 };    // 7.3 stackalloc initializer
            var counter = new SpanCounter(numbers);
            Console.WriteLine($"ref struct sum = {counter.Sum()}");

            int a = 1, b = 2;
            bool pickA = false;
            ref int chosen = ref (pickA ? ref a : ref b);          // 7.2 conditional ref
            chosen = 20;
            Console.WriteLine($"conditional ref wrote b = {b}");
            chosen = ref a;                                         // 7.3 ref reassignment
            chosen = 10;
            Console.WriteLine($"ref reassigned wrote a = {a}");
        }

        static async Task Main()                                   // 7.1 async Main
        {
            await Task.Yield();

            int zero = default;                                    // 7.1 default literal
            string none = default;
            Console.WriteLine($"default int={zero}, default string null={none == null}");

            int count = 3; string name = "items";
            var t = (count, name);                                 // 7.1 inferred names
            Console.WriteLine($"inferred tuple names: {t.count} {t.name}");
            Console.WriteLine($"tuple equality: {(1, "a") == (1, "a")}, {(1, 2) != (1, 3)}");  // 7.3

            Console.WriteLine($"Kind: {Kind(5)}, {Kind("x")}, {Kind(2.0)}");

            RefAndSpanDemos();      // ref locals / Span are not allowed in async methods

            int hex = 0x_FF_FF;                                     // 7.2 leading separator
            Console.WriteLine($"0x_FF_FF = {hex}");

            Console.WriteLine($"sizeof(long) via unmanaged constraint = {SizeOf<long>()}");
            Console.WriteLine($"Enum constraint: {string.Join(",", EnumNames<Weekday>())}");
            Console.WriteLine($"private protected read = {new Derived().Read()}");
            Console.WriteLine($"field-targeted attribute property = {new Settings().Cache}");
        }
    }
}
