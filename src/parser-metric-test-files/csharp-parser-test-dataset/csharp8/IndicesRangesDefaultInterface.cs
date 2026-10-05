/*
 * Feature : Indices and ranges (^ and ..), default interface methods,
 *           interface static members, unmanaged constructed types,
 *           interpolated verbatim strings with $@ in either order
 * Version : C# 8.0 (.NET Core 3.0+; default interface methods need runtime
 *           support - not available on .NET Framework)
 * Spec    : csharplang proposals/csharp-8.0/ranges.md,
 *           default-interface-methods.md, unmanaged-constructed-types.md
 *
 * Parser edge cases:
 *  - `^1` - prefix hat operator meaning "index from end" (NOT XOR: binary
 *    `a ^ b` still means XOR).
 *  - `..` range operator with optional operands: `a..b`, `..b`, `a..`, `..`.
 *    Lexer must not read `1..2` as a malformed floating literal `1.`.
 *  - `arr[^2..]`, `arr[..^1]`, `s[1..^1]`.
 *  - Interface members WITH bodies, `virtual`/`abstract`/`sealed`/`private`
 *    /`static` modifiers inside an interface.
 *  - `@$"..."` accepted in addition to `$@"..."` (C# 8).
 */
using System;
using System.Linq;

namespace RangesDim
{
    interface ILogger
    {
        void Write(string message);                                 // abstract

        void Info(string message) => Write($"[info] {message}");    // default implementation
        void Error(string message) => Write($"[error] {Decorate(message)}");

        private string Decorate(string m) => m.ToUpperInvariant();  // private interface member
        static string Version => "logger-api 2";                    // static interface member
        static int Created;                                         // static field in interface
    }

    class ConsoleLogger : ILogger
    {
        public ConsoleLogger() => ILogger.Created++;
        public void Write(string message) => Console.WriteLine(message);
    }

    class PrefixLogger : ILogger
    {
        public void Write(string message) => Console.WriteLine($">> {message}");
        public void Info(string message) => Write($"(overridden info) {message}");   // override default
    }

    struct Pair<T> { public T First; public T Second; }

    class Program
    {
        static unsafe int SizeOfPair() => sizeof(Pair<int>);         // unmanaged constructed type

        static void Main()
        {
            int[] nums = { 0, 10, 20, 30, 40, 50 };
            Console.WriteLine($"nums[^1] = {nums[^1]}, nums[^2] = {nums[^2]}");
            Console.WriteLine($"nums[1..3]  = [{string.Join(",", nums[1..3])}]");
            Console.WriteLine($"nums[..2]   = [{string.Join(",", nums[..2])}]");
            Console.WriteLine($"nums[^2..]  = [{string.Join(",", nums[^2..])}]");
            Console.WriteLine($"nums[..]    = [{string.Join(",", nums[..])}]");
            Console.WriteLine($"nums[1..^1] = [{string.Join(",", nums[1..^1])}]");

            Index last = ^1;
            Range middle = 2..^2;
            Console.WriteLine($"Index/Range variables: {nums[last]}, [{string.Join(",", nums[middle])}]");

            string word = "parsers";
            Console.WriteLine($"word[1..^1] = {word[1..^1]}, word[^3..] = {word[^3..]}");

            Span<int> span = nums;
            Console.WriteLine($"span[^3..].Length = {span[^3..].Length}");

            int xor = 6 ^ 3;                                         // binary ^ is still XOR
            Console.WriteLine($"6 ^ 3 = {xor}");

            var r = Enumerable.Range(0, 10).ToArray();
            int start = 3, end = 2;
            Console.WriteLine($"computed range [{string.Join(",", r[start..^end])}]");

            ILogger logger = new ConsoleLogger();
            logger.Info("default interface method");
            logger.Error("shouted");
            ILogger pref = new PrefixLogger();
            pref.Info("custom");
            Console.WriteLine($"{ILogger.Version}, created={ILogger.Created}");

            Console.WriteLine($"sizeof(Pair<int>) = {SizeOfPair()}");

            string path = "C:\\temp";
            Console.WriteLine(@$"verbatim-interpolated: {path}\file.txt");
            Console.WriteLine($@"also fine: {path}\other.txt");
        }
    }
}
