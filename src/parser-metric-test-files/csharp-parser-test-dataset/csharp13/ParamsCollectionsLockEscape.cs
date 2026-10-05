/*
 * Feature : params collections, System.Threading.Lock, \e escape, implicit
 *           index access (^) in object initializers, ref locals and unsafe
 *           contexts in async methods and iterators, method group natural
 *           type improvements, OverloadResolutionPriority
 * Version : C# 13 (.NET 9)
 * Spec    : csharplang proposals/csharp-13.0/params-collections.md,
 *           lock-object.md, esc-escape-sequence.md,
 *           implicit-index-access-in-object-initializers (docs "What's new
 *           in C# 13"), ref-unsafe-in-iterators-async.md,
 *           overload-resolution-priority.md
 *
 * VALIDATION: written against the C# 13 spec; NOT compiled in the dataset
 * build environment (Roslyn 4.8 / .NET 8 supports up to C# 12). Verify with
 * the .NET 9 SDK: `dotnet run` with <LangVersion>13</LangVersion>.
 *
 * Parser edge cases:
 *  - `params ReadOnlySpan<int> xs` / `params List<string> xs` - params on a
 *    non-array collection type.
 *  - `"\e[1m"` - new escape sequence for U+001B (ESC). Before C# 13 `\e` is
 *    error CS1009 "Unrecognized escape sequence".
 *  - `new Buffer { Items = { [^1] = 9 } }` - `^` index inside an object
 *    initializer's indexer target.
 *  - `lock (lockObj)` where lockObj is System.Threading.Lock - same syntax,
 *    different lowering (EnterScope()).
 *  - `ref int r = ref ...;` inside an `async` method (allowed when not
 *    crossing an await).
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Threading;
using System.Threading.Tasks;

namespace Csharp13.Params
{
    class Countdown
    {
        public int[] Slots { get; } = new int[5];
    }

    static class Printer
    {
        [OverloadResolutionPriority(1)]
        public static string Show(params ReadOnlySpan<object> items) => "span: " + string.Join(",", items.ToArray());
        public static string Show(params object[] items) => "array: " + string.Join(",", items);
    }

    class Program
    {
        static int Sum(params ReadOnlySpan<int> values)        // params span: no array allocation
        {
            int total = 0;
            foreach (var v in values) total += v;
            return total;
        }

        static string Join(params List<string> parts) => string.Join("-", parts);

        static int CountAll(params IEnumerable<int> seq) => seq.Count();

        static readonly Lock Gate = new();                      // System.Threading.Lock
        static int _shared;

        static async Task<int> RefInAsync(int[] data)
        {
            {
                ref int first = ref data[0];                    // ref local in async (C# 13)
                first += 100;
            }                                                   // ref local ends before the await
            await Task.Yield();
            return data[0];
        }

        static IEnumerable<int> RefInIterator(int[] data)
        {
            for (int i = 0; i < data.Length; i++)
            {
                {
                    ref int cell = ref data[i];                  // ref local in iterator (C# 13)
                    cell *= 2;
                }
                yield return data[i];
            }
        }

        static async Task Main()
        {
            Console.WriteLine($"Sum() = {Sum()}, Sum(1,2,3) = {Sum(1, 2, 3)}");
            Console.WriteLine($"Join = {Join("a", "b", "c")}");
            Console.WriteLine($"CountAll = {CountAll(4, 5, 6, 7)}");
            Console.WriteLine(Printer.Show(1, "two", 3.0));      // priority picks the span overload

            Parallel.For(0, 1000, _ => { lock (Gate) { _shared++; } });
            Console.WriteLine($"Lock-protected counter = {_shared}");
            using (Gate.EnterScope()) { Console.WriteLine("explicit Lock.EnterScope()"); }

            const string Bold = "\e[1m", Reset = "\e[0m";
            Console.WriteLine($"{Bold}ESC escape sequence{Reset} (\\e == U+001B: {(int)'\e' == 0x1B})");

            var cd = new Countdown { Slots = { [^1] = 1, [^2] = 2, [^3] = 3 } };   // ^ in object initializer
            Console.WriteLine($"countdown slots: [{string.Join(",", cd.Slots)}]");

            Console.WriteLine($"ref in async: {await RefInAsync(new[] { 1, 2 })}");
            Console.WriteLine($"ref in iterator: [{string.Join(",", RefInIterator(new[] { 1, 2, 3 }))}]");
        }
    }
}
