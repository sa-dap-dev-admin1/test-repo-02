/*
 * Feature : out variables, local functions, ref returns and ref locals,
 *           throw expressions, expression-bodied members everywhere,
 *           binary literals and digit separators, generalized async returns
 * Version : C# 7.0
 * Spec    : csharplang proposals/csharp-7.0/ (out-var.md, local-functions.md,
 *           ref-locals-returns, throw-expression.md,
 *           expression-bodied-everything, binary-literals, digit-separators,
 *           task-types.md)
 *
 * Parser edge cases:
 *  - `int.TryParse(s, out var n)` / `out int n` - declaration inside an
 *    argument list.
 *  - A local function declared AFTER its use in the same block, and
 *    declared inside a lambda.
 *  - `ref int Find(...)` return type with `ref` and `return ref arr[i];`
 *  - `ref var slot = ref Find(...);` - ref local with `ref` on both sides.
 *  - `x ?? throw new ...` and `cond ? a : throw ...` - throw as expression.
 *  - Expression-bodied constructor, finalizer, get/set accessors.
 *  - `0b1010_1010`, `1_000_000` (leading `_` after prefix is C# 7.2).
 */
using System;
using System.Threading.Tasks;

namespace Features70
{
    class Account
    {
        private string _owner;
        private decimal _balance;

        public Account(string owner) => Owner = owner;                         // ctor
        ~Account() => Console.WriteLine("finalizer (expression-bodied)");      // finalizer

        public string Owner
        {
            get => _owner;
            set => _owner = value ?? throw new ArgumentNullException(nameof(value));
        }

        public decimal Balance
        {
            get => _balance;
            private set => _balance = value >= 0 ? value : throw new InvalidOperationException("negative");
        }

        public void Deposit(decimal amount) => Balance += amount;
    }

    class Program
    {
        static int[] _scores = { 10, 20, 30, 40 };

        static ref int FindSlot(int[] arr, int target)
        {
            for (int i = 0; i < arr.Length; i++)
                if (arr[i] == target) return ref arr[i];
            throw new InvalidOperationException("not found");
        }

        static async ValueTask<int> CachedLengthAsync(string s)
        {
            if (s.Length < 5) return s.Length;    // completes synchronously, no Task alloc
            await Task.Delay(1);
            return s.Length;
        }

        static void Main()
        {
            // out var
            if (int.TryParse("2017", out var year))
                Console.WriteLine($"parsed year {year}");
            if (!double.TryParse("nope", out double d))
                Console.WriteLine($"failed parse leaves d = {d}");

            // Local function used before its declaration
            Console.WriteLine($"Fib(15) = {Fib(15)}");
            long Fib(int n) => n < 2 ? n : Fib(n - 1) + Fib(n - 2);

            // Local function capturing locals, and an iterator local function
            int multiplier = 3;
            int Scale(int v) => v * multiplier;
            Console.WriteLine($"Scale(7) = {Scale(7)}");

            foreach (var e in Evens(10)) Console.Write($"{e} ");
            Console.WriteLine();
            System.Collections.Generic.IEnumerable<int> Evens(int limit)
            {
                for (int i = 0; i <= limit; i += 2) yield return i;
            }

            // ref returns and ref locals
            ref int slot = ref FindSlot(_scores, 30);
            slot = 300;
            Console.WriteLine($"scores after ref write: {string.Join(",", _scores)}");

            // throw expressions
            var acct = new Account("ada");
            acct.Deposit(50m);
            try { acct.Owner = null; }
            catch (ArgumentNullException ex) { Console.WriteLine($"caught: {ex.ParamName}"); }
            string config = null;
            try { var c = config ?? throw new InvalidOperationException("config missing"); }
            catch (InvalidOperationException ex) { Console.WriteLine($"caught: {ex.Message}"); }

            // Literals
            int mask = 0b1111_0000;
            long big = 9_223_372_036_854_775_807;
            double sci = 6.022_140_76e23;
            Console.WriteLine($"mask={mask} big={big} sci={sci:E3}");

            // Generalized async return type
            Console.WriteLine($"ValueTask results: {CachedLengthAsync("abc").Result}, {CachedLengthAsync("longer text").Result}");
            Console.WriteLine($"balance = {acct.Balance}");
        }
    }
}
