/*
 * Feature : Collection expressions and the spread element
 * Version : C# 12
 * Spec    : csharplang proposals/csharp-12.0/collection-expressions.md
 *
 * `[a, b, c]` creates a collection whose type comes from the TARGET:
 * arrays, List<T>, Span<T>, ReadOnlySpan<T>, IEnumerable<T>, immutable
 * collections, and types with [CollectionBuilder]. `..expr` spreads.
 *
 * Parser edge cases:
 *  - `[` starting an EXPRESSION (previously only attributes, indexers and,
 *    since C# 11, list patterns started with `[`). Statement-level
 *    `[Attr] void Local()` vs `int[] a = [1, 2];` must be disambiguated.
 *  - `[]` - empty collection.
 *  - `[..first, ..second, 99]` - spread element `..` inside brackets vs the
 *    range operator `a..b` and the slice pattern `..`.
 *  - Collection expression as a method argument, return value, and inside
 *    a ternary: `flag ? [1] : []`.
 *  - Nested: `int[][] jagged = [[1, 2], [3]];`.
 *  - `x is [1, ..]` (pattern) and `x = [1, ..y]` (expression) look alike.
 */
using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;

namespace Csharp12.Collections
{
    class Program
    {
        static int Sum(ReadOnlySpan<int> values)
        {
            int s = 0;
            foreach (var v in values) s += v;
            return s;
        }

        static IEnumerable<string> Defaults() => ["alpha", "beta"];

        static List<int> Merge(List<int> a, int[] b) => [.. a, .. b, -1];

        static void Main()
        {
            int[] arr = [1, 2, 3];
            List<string> names = ["ada", "grace"];
            Span<char> letters = ['x', 'y', 'z'];
            ReadOnlySpan<int> ro = [10, 20];
            HashSet<int> set = [3, 1, 3, 2];
            ImmutableArray<int> imm = [7, 8, 9];
            IReadOnlyList<double> readOnly = [0.5, 1.5];
            int[] empty = [];

            Console.WriteLine($"array {arr.Length}, list {names.Count}, span {letters.Length}, ro {ro.Length}, set {set.Count}, immutable {imm.Length}, IReadOnlyList {readOnly.Count}, empty {empty.Length}");

            // Spread
            int[] more = [0, .. arr, 4, .. Enumerable.Range(5, 3)];
            Console.WriteLine($"spread: [{string.Join(",", more)}]");
            Console.WriteLine($"Merge: [{string.Join(",", Merge([1, 2], [3]))}]");

            // As arguments and return values
            Console.WriteLine($"Sum([1,2,3,4]) = {Sum([1, 2, 3, 4])}");
            Console.WriteLine($"Defaults: {string.Join("/", Defaults())}");

            bool flag = arr.Length > 2;
            int[] chosen = flag ? [100] : [];
            Console.WriteLine($"ternary collection: [{string.Join(",", chosen)}]");

            // Nested
            int[][] jagged = [[1, 2], [3], []];
            List<List<string>> table = [["a", "b"], ["c"]];
            Console.WriteLine($"jagged lengths: {string.Join(",", jagged.Select(r => r.Length))}, table[0][1] = {table[0][1]}");

            // Pattern vs expression with the same shape
            if (more is [0, .., var last]) Console.WriteLine($"list pattern after collection expression, last = {last}");
            more = [.. more[1..3]];
            Console.WriteLine($"spread of a range slice: [{string.Join(",", more)}]");

            // Dictionary-like via collection of KeyValuePairs
            List<KeyValuePair<string, int>> pairs = [new("one", 1), new("two", 2)];
            Console.WriteLine($"pairs: {string.Join(", ", pairs.Select(p => $"{p.Key}={p.Value}"))}");
        }
    }
}
