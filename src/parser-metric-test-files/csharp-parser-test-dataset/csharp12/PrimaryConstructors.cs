/*
 * Feature : Primary constructors for classes and structs
 * Version : C# 12 (.NET 8)
 * Spec    : csharplang proposals/csharp-12.0/primary-constructors.md
 *
 * Any class or struct (not just records) may declare a parameter list after
 * its name. The parameters are in scope for the whole type body: field and
 * property initializers, methods, and the base-class argument list. Unlike
 * records, NO public properties are generated.
 *
 * Parser edge cases:
 *  - `class Service(ILogger log, int retries) : Base(log) { }` - parameter
 *    list on a CLASS and arguments in the base list (previously records only).
 *  - `struct Point(double x, double y);` - body may be replaced by `;`
 *    (C# 12 allows `class C(int x);` too).
 *  - Parameters captured and MUTATED in methods (they are not readonly).
 *  - Other constructors must chain to the primary one with `: this(...)`.
 *  - Attributes on primary-constructor parameters and `method:` target on
 *    the type to annotate the constructor.
 */
using System;
using System.Collections.Generic;

namespace Csharp12.PrimaryCtors
{
    interface ILogger { void Log(string message); }
    class ConsoleLogger(string prefix) : ILogger
    {
        public void Log(string message) => Console.WriteLine($"{prefix} {message}");
    }

    abstract class ServiceBase(ILogger logger)
    {
        protected void Info(string m) => logger.Log(m);
    }

    class OrderService(ILogger logger, int maxRetries) : ServiceBase(logger)
    {
        private readonly List<string> _orders = new();
        public int MaxRetries { get; } = maxRetries;              // property initialized from parameter

        public OrderService(ILogger logger) : this(logger, 3) { } // must chain to the primary ctor

        public void Place(string item)
        {
            _orders.Add(item);
            Info($"placed {item} (#{_orders.Count}, retries {maxRetries})");
        }

        public void LowerRetries() => maxRetries--;                // parameter is mutable state
        public int CurrentRetries => maxRetries;
    }

    struct Distance(double meters)
    {
        public readonly double Kilometers => meters / 1000;
        public readonly override string ToString() => $"{meters} m";
    }

    class Counter(int start);                                    // class with `;` body

    [method: Obsolete("demo: attribute on the primary constructor")]
    class Legacy(int value)
    {
        public int Value => value;
    }

    class Program
    {
        static void Main()
        {
            var svc = new OrderService(new ConsoleLogger("[orders]"));
            svc.Place("keyboard");
            svc.Place("mouse");
            svc.LowerRetries();
            Console.WriteLine($"MaxRetries (captured at init) = {svc.MaxRetries}, current = {svc.CurrentRetries}");

            var d = new Distance(4200);
            Console.WriteLine($"{d} = {d.Kilometers} km");

            var c = new Counter(5);
            Console.WriteLine($"Counter type exists: {c.GetType().Name}");
#pragma warning disable CS0618
            Console.WriteLine($"Legacy.Value = {new Legacy(9).Value}");
#pragma warning restore CS0618
        }
    }
}
