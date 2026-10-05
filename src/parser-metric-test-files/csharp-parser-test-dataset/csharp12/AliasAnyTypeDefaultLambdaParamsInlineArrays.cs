/*
 * Feature : `using` alias for any type (tuples, arrays, pointers, nullable),
 *           default values for lambda parameters, params arrays in lambdas,
 *           inline arrays, ref readonly parameters, Experimental attribute
 * Version : C# 12
 * Spec    : csharplang proposals/csharp-12.0/using-alias-types.md,
 *           lambda-method-group-defaults.md, inline-arrays.md,
 *           ref-readonly-parameters.md, experimental-attribute.md
 *
 * Parser edge cases:
 *  - `using Point = (int X, int Y);` - TUPLE type in a using alias;
 *    also `using Grid = int[,];`, `using unsafe Ptr = int*;`,
 *    `using MaybeInt = int?;`.
 *  - `var f = (int x = 10) => ...;` - default value in a lambda parameter.
 *  - `(params int[] xs) => xs.Length`.
 *  - `[InlineArray(8)] struct Buffer { private int _e; }` - attribute with
 *    a size, the struct has exactly ONE field; indexing `buf[3]`.
 *  - `void M(ref readonly int x)` - `ref readonly` as a PARAMETER modifier.
 */
using System;
using System.Diagnostics.CodeAnalysis;
using System.Runtime.CompilerServices;

using Point = (int X, int Y);
using Grid = int[,];
using MaybeInt = int?;
using unsafe IntPtrAlias = int*;
using Names = string[];

namespace Csharp12.Misc
{
    [InlineArray(8)]
    struct Buffer8
    {
        private int _element0;                       // exactly one field
    }

    [Experimental("PARSER001")]
    class PreviewApi { public static string Hello() => "experimental API"; }

    class Program
    {
        static Point Midpoint(Point a, Point b) => ((a.X + b.X) / 2, (a.Y + b.Y) / 2);

        static int ReadOnlyRef(ref readonly int value) => value * 2;

        static unsafe int Deref(IntPtrAlias p) => *p;

        static void Main()
        {
            Point m = Midpoint((0, 0), (10, 20));
            Console.WriteLine($"alias tuple: ({m.X}, {m.Y})");

            Grid g = new int[2, 3];
            g[1, 2] = 5;
            MaybeInt none = null;
            Names names = ["x", "y"];
            Console.WriteLine($"alias array: {g.GetLength(1)} cols, g[1,2]={g[1, 2]}; alias nullable: {none.HasValue}; alias string[]: {names.Length}");

            unsafe
            {
                int local = 77;
                Console.WriteLine($"alias pointer deref: {Deref(&local)}");
            }

            var greet = (string name = "world", string punct = "!") => $"hello {name}{punct}";
            Console.WriteLine(greet());
            Console.WriteLine(greet("parser"));
            var count = (params int[] xs) => xs.Length;
            Console.WriteLine($"params lambda: {count(1, 2, 3)}");

            var buf = new Buffer8();
            for (int i = 0; i < 8; i++) buf[i] = i * i;
            Span<int> span = buf;
            Console.WriteLine($"inline array: buf[7]={buf[7]}, span length {span.Length}");

            int x = 21;
            Console.WriteLine($"ref readonly param: {ReadOnlyRef(in x)} / {ReadOnlyRef(ref x)}");

#pragma warning disable PARSER001
            Console.WriteLine(PreviewApi.Hello());
#pragma warning restore PARSER001
        }
    }
}
