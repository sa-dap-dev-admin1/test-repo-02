/*
 * Feature : Extended property patterns, improved definite assignment,
 *           interpolated string handlers, static abstract members
 *           (preview in 10, GA in 11 - not used here)
 * Version : C# 10
 * Spec    : csharplang proposals/csharp-10.0/extended-property-patterns.md,
 *           improved-interpolated-strings.md
 *
 * Parser edge cases:
 *  - `{ Address.City: "Oslo" }` - DOTTED member path inside a property
 *    pattern (C# 8/9 required `{ Address: { City: "Oslo" } }`).
 *  - Dotted paths combined with relational patterns:
 *    `{ Order.Total: > 100, Order.Items.Count: >= 3 }`.
 *  - `[InterpolatedStringHandler]` struct with an `AppendLiteral` /
 *    `AppendFormatted` shape; a method parameter of that type receives
 *    `$"..."` arguments, and `[InterpolatedStringHandlerArgument("x")]`
 *    references another parameter by name.
 */
using System;
using System.Runtime.CompilerServices;
using System.Text;

namespace Csharp10.Patterns
{
    record Address(string City, string Country);
    record Order(decimal Total, int ItemCount);
    record Customer(string Name, Address Address, Order? LastOrder);

    [InterpolatedStringHandler]
    ref struct LogHandler
    {
        private StringBuilder? _sb;
        public bool Enabled { get; }

        public LogHandler(int literalLength, int formattedCount, Logger logger, out bool isEnabled)
        {
            Enabled = isEnabled = logger.Level >= 2;
            _sb = isEnabled ? new StringBuilder(literalLength) : null;
        }
        public void AppendLiteral(string s) => _sb!.Append(s);
        public void AppendFormatted<T>(T value) => _sb!.Append('<').Append(value).Append('>');
        public override string ToString() => _sb?.ToString() ?? "";
    }

    class Logger
    {
        public int Level { get; set; }
        public int Evaluations;
        public void Debug([InterpolatedStringHandlerArgument("")] ref LogHandler handler)
        {
            if (handler.Enabled) Console.WriteLine($"[debug] {handler.ToString()}");
        }
        public int Expensive() { Evaluations++; return 42; }
    }

    class Program
    {
        static string Classify(Customer c) => c switch
        {
            { Address.Country: "NO", LastOrder.Total: > 1000 } => "big Norwegian spender",
            { Address.City: "Oslo" } => "Oslo customer",
            { LastOrder.ItemCount: >= 5 } => "bulk buyer",
            { LastOrder: null } => "no orders yet",
            _ => "regular",
        };

        static void Main()
        {
            var customers = new[]
            {
                new Customer("A", new Address("Bergen", "NO"), new Order(1500m, 2)),
                new Customer("B", new Address("Oslo", "NO"), new Order(50m, 1)),
                new Customer("C", new Address("Lyon", "FR"), new Order(80m, 7)),
                new Customer("D", new Address("Rome", "IT"), null),
                new Customer("E", new Address("Graz", "AT"), new Order(10m, 1)),
            };
            foreach (var c in customers) Console.WriteLine($"{c.Name}: {Classify(c)}");

            if (customers[1] is { Address.City: var city, Name.Length: 1 })
                Console.WriteLine($"extended pattern in `is`: city={city}");

            var log = new Logger { Level = 1 };
            log.Debug($"value is {log.Expensive()}");              // handler disabled: Expensive() not called
            Console.WriteLine($"evaluations while disabled: {log.Evaluations}");
            log.Level = 3;
            log.Debug($"value is {log.Expensive()} at level {log.Level}");
            Console.WriteLine($"evaluations while enabled: {log.Evaluations}");
        }
    }
}
