/*
 * Feature : List patterns and slice patterns
 * Version : C# 11
 * Spec    : csharplang proposals/csharp-11.0/list-patterns.md
 *
 * `[p1, p2, ..]` matches a countable, indexable sequence element by
 * element. `..` (slice) matches zero or more elements and may capture:
 * `[first, .. var middle, last]`.
 *
 * Parser edge cases:
 *  - `[` at the START of a pattern - previously `[` could only appear in
 *    attribute or indexer context. `x is [1, 2]`.
 *  - `..` inside a list pattern (slice) vs `..` range operator in an
 *    expression; `.. var rest` and `.. [_, _]` (nested pattern on slice).
 *  - Empty list pattern `[]`.
 *  - Nested list patterns in property and positional patterns:
 *    `{ Items: [_, ..] }`, `([1, ..], _)`.
 *  - Only ONE slice allowed per list pattern.
 */
using System;
using System.Collections.Generic;

namespace Csharp11.ListPatterns
{
    record Command(string Name, string[] Args);

    class Program
    {
        static string Describe(int[] values) => values switch
        {
            [] => "empty",
            [var only] => $"single {only}",
            [0, ..] => "starts with zero",
            [_, _] => "exactly two",
            [var first, .., var last] when first == last => $"bookends {first}",
            [var first, .. var middle, var last] => $"first {first}, {middle.Length} in the middle, last {last}",
        };

        static string Run(Command c) => c switch
        {
            { Name: "help", Args: [] } => "showing help",
            { Name: "add", Args: [var a, var b] } => $"sum = {int.Parse(a) + int.Parse(b)}",
            { Name: "echo", Args: [.. var words] } => string.Join(' ', words),
            { Name: "git", Args: ["commit", "-m", var msg, ..] } => $"commit '{msg}'",
            { Args: [.., "--verbose"] } => $"{c.Name} (verbose)",
            _ => $"unknown command {c.Name}",
        };

        static bool IsPalindrome(ReadOnlySpan<char> s) => s switch
        {
            [] or [_] => true,
            [var a, .. var mid, var b] => a == b && IsPalindrome(mid),
        };

        static void Main()
        {
            foreach (var arr in new[] { new int[0], new[] { 5 }, new[] { 0, 9, 9 }, new[] { 3, 4 }, new[] { 7, 1, 2, 7 }, new[] { 1, 2, 3, 4, 5 } })
                Console.WriteLine($"[{string.Join(",", arr)}] -> {Describe(arr)}");

            var cmds = new[]
            {
                new Command("help", Array.Empty<string>()),
                new Command("add", new[] { "2", "40" }),
                new Command("echo", new[] { "list", "patterns", "rock" }),
                new Command("git", new[] { "commit", "-m", "init", "--quiet" }),
                new Command("build", new[] { "--release", "--verbose" }),
                new Command("rm", new[] { "x" }),
            };
            foreach (var c in cmds) Console.WriteLine($"{c.Name}: {Run(c)}");

            foreach (var w in new[] { "level", "parser", "a", "" })
                Console.WriteLine($"\"{w}\" palindrome: {IsPalindrome(w)}");

            var grid = new List<int[]> { new[] { 1, 2 }, new[] { 3 } };
            if (grid is [[1, ..], [var lone]]) Console.WriteLine($"nested list pattern, lone = {lone}");

            int[] data = { 1, 2, 3, 4 };
            if (data is [_, .. [var x, var y], _]) Console.WriteLine($"slice with nested pattern: {x}, {y}");
        }
    }
}
