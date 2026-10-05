/*
 * Feature : Async streams (IAsyncEnumerable, await foreach, async
 *           iterators), async disposable (await using), using
 *           declarations, static local functions, readonly members,
 *           stackalloc in nested expressions
 * Version : C# 8.0
 * Spec    : csharplang proposals/csharp-8.0/async-streams.md,
 *           using.md, static-local-functions.md, readonly-instance-members.md
 *
 * Parser edge cases:
 *  - `await foreach (var x in stream)` - `await` BEFORE `foreach`.
 *  - `await using var res = ...;` and `await using (var r = ...) { }`.
 *  - `using var file = ...;` - using DECLARATION with no parentheses or
 *    block; disposal at end of enclosing scope.
 *  - `async IAsyncEnumerable<T> F()` with both `await` and `yield return`.
 *  - `[EnumeratorCancellation] CancellationToken ct` parameter attribute.
 *  - `static int Helper(...)` LOCAL function - `static` inside a method.
 *  - `public readonly double Length()` - readonly on a struct member.
 *  - `ConfigureAwait(false)` on await foreach.
 */
using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Threading;
using System.Threading.Tasks;

namespace AsyncStreams
{
    class Resource : IDisposable
    {
        private readonly string _name;
        public Resource(string name) { _name = name; Console.WriteLine($"  open {_name}"); }
        public void Dispose() => Console.WriteLine($"  dispose {_name}");
    }

    class AsyncResource : IAsyncDisposable
    {
        private readonly string _name;
        public AsyncResource(string name) { _name = name; Console.WriteLine($"  open async {_name}"); }
        public async ValueTask DisposeAsync()
        {
            await Task.Delay(1);
            Console.WriteLine($"  dispose async {_name}");
        }
    }

    struct Vector
    {
        public double X, Y;
        public Vector(double x, double y) { X = x; Y = y; }
        public readonly double Length() => Math.Sqrt(X * X + Y * Y);   // readonly member
        public readonly override string ToString() => $"({X}, {Y})";
    }

    class Program
    {
        static async IAsyncEnumerable<int> CountAsync(int to, [EnumeratorCancellation] CancellationToken ct = default)
        {
            for (int i = 1; i <= to; i++)
            {
                await Task.Delay(1, ct);
                yield return i;
            }
        }

        static async IAsyncEnumerable<string> ReadLinesAsync()
        {
            var lines = new[] { "header", "row 1", "", "row 2" };
            foreach (var line in lines)
            {
                await Task.Yield();
                if (line.Length == 0) continue;
                yield return line;
            }
        }

        static async Task Main()
        {
            Console.WriteLine("await foreach:");
            await foreach (var n in CountAsync(5)) Console.Write($"{n} ");
            Console.WriteLine();

            await foreach (var line in ReadLinesAsync().ConfigureAwait(false))
                Console.WriteLine($"  line: {line}");

            // Cancellation via WithCancellation
            using var cts = new CancellationTokenSource();
            int seen = 0;
            try
            {
                await foreach (var n in CountAsync(100).WithCancellation(cts.Token))
                {
                    if (++seen == 3) cts.Cancel();
                }
            }
            catch (OperationCanceledException) { Console.WriteLine($"cancelled after {seen}"); }

            Console.WriteLine("using declarations:");
            UsingDemo();

            Console.WriteLine("await using:");
            await using (var a = new AsyncResource("A"))
            {
                Console.WriteLine("  inside block");
            }
            await using var b = new AsyncResource("B");
            Console.WriteLine("  B lives until end of Main");

            var v = new Vector(3, 4);
            Console.WriteLine($"readonly member: {v} length {v.Length()}");
            Console.WriteLine($"static local function: {Sum(stackalloc int[] { 1, 2, 3 })}");

            static int Sum(Span<int> values)       // static local function: no captures
            {
                int s = 0;
                foreach (var x in values) s += x;
                return s;
            }
        }

        static void UsingDemo()
        {
            using var first = new Resource("first");
            using var second = new Resource("second");
            Console.WriteLine("  body runs; disposal happens in reverse order at end of scope");
        }
    }
}
