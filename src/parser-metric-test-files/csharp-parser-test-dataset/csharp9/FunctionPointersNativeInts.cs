/*
 * Feature : Function pointers (delegate*), native-sized integers
 *           (nint/nuint), SkipLocalsInit, partial method extensions,
 *           `static` anonymous functions in unsafe code
 * Version : C# 9.0
 * Spec    : csharplang proposals/csharp-9.0/function-pointers.md,
 *           native-integers.md, skip-localsinit.md,
 *           extending-partial-methods.md
 *
 * Requires: -unsafe (AllowUnsafeBlocks)
 *
 * Parser edge cases:
 *  - `delegate*<int, int, int>` - `delegate` followed by `*` and a generic
 *    argument list; last type argument is the return type.
 *  - Calling conventions: `delegate* managed<...>`,
 *    `delegate* unmanaged[Cdecl]<...>`.
 *  - `&MethodName` - address-of a METHOD group.
 *  - `nint` / `nuint` are contextual: `nint` is a keyword-like type name
 *    but a user type named `nint` would shadow it.
 *  - `partial` methods with return values, out params and access modifiers
 *    (must then have an implementation).
 */
using System;
using System.Runtime.CompilerServices;

namespace FnPtr
{
    partial class Calculator
    {
        public partial int Combine(int a, int b);           // C# 9: accessible partial with return
        private partial bool TryParse(string s, out int value);
    }

    partial class Calculator
    {
        public partial int Combine(int a, int b) => a * 10 + b;
        private partial bool TryParse(string s, out int value) => int.TryParse(s, out value);
        public int ParseOr(string s, int fallback) => TryParse(s, out var v) ? v : fallback;
    }

    unsafe class Program
    {
        static int Add(int a, int b) => a + b;
        static int Mul(int a, int b) => a * b;
        static void Report(string name, int value) => Console.WriteLine($"{name} = {value}");

        static int Apply(delegate*<int, int, int> op, int a, int b) => op(a, b);

        [SkipLocalsInit]
        static int SumStack(int n)
        {
            int* buffer = stackalloc int[n];                 // not zeroed because of SkipLocalsInit
            for (int i = 0; i < n; i++) buffer[i] = i;
            int total = 0;
            for (int i = 0; i < n; i++) total += buffer[i];
            return total;
        }

        static void Main()
        {
            delegate*<int, int, int> add = &Add;
            delegate* managed<int, int, int> mul = &Mul;
            delegate*<string, int, void> report = &Report;

            report("Apply(add, 2, 3)", Apply(add, 2, 3));
            report("Apply(mul, 4, 5)", Apply(mul, 4, 5));

            // Array of function pointers
            var ops = stackalloc delegate*<int, int, int>[] { &Add, &Mul };
            for (int i = 0; i < 2; i++) report($"ops[{i}](6, 7)", ops[i](6, 7));

            // Native-sized integers
            nint ptrSized = 42;
            nuint unsignedPtr = 7;
            nint offset = ptrSized + (nint)unsignedPtr;
            Console.WriteLine($"nint size = {sizeof(nint)}, offset = {offset}, nint.MaxValue > int.MaxValue: {nint.MaxValue > int.MaxValue}");

            report("SumStack(10)", SumStack(10));

            var calc = new Calculator();
            report("Combine(4, 2)", calc.Combine(4, 2));
            report("ParseOr(\"x\", -1)", calc.ParseOr("x", -1));
        }
    }
}
