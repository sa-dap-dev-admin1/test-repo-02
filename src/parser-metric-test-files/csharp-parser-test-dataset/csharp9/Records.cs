/*
 * Feature : Records (record classes), positional records, `with`
 *           expressions, value equality, inheritance, init-only properties
 * Version : C# 9.0 (.NET 5)
 * Spec    : csharplang proposals/csharp-9.0/records.md, init.md
 *
 * `record Person(string First, string Last);` declares an immutable
 * reference type with value-based equality, a deconstructor, ToString,
 * and non-destructive mutation via `p with { Last = "X" }`.
 *
 * Parser edge cases:
 *  - `record` is a CONTEXTUAL keyword: `record Name(...)` at type level;
 *    a type named `record` is now a warning, and `record` as a local is OK.
 *  - Positional parameter list directly after the type name, optionally
 *    followed by `;` (no body) or a `{ ... }` body.
 *  - `: Base(arg1, arg2)` - base record with ARGUMENTS in the base list.
 *  - `expr with { P = v, Q = w }` - `with` as a binary operator followed
 *    by an object-initializer-like block.
 *  - `init` accessor instead of `set`.
 *  - Attributes targeting the generated property: `[property: JsonX]`.
 *  - `record struct` / `record class` explicit forms are C# 10.
 */
using System;
using System.Collections.Generic;

namespace Records9
{
    public record Person(string First, string Last)
    {
        public string FullName => $"{First} {Last}";
        public int Age { get; init; }
    }

    // Inheritance with arguments forwarded to the base record
    public record Employee(string First, string Last, string Team) : Person(First, Last);

    public abstract record Shape
    {
        public abstract double Area { get; }
    }
    public record Circle(double Radius) : Shape
    {
        public override double Area => Math.PI * Radius * Radius;
    }
    public sealed record Square(double Side) : Shape
    {
        public override double Area => Side * Side;
        // Custom ToString member printing
        protected override bool PrintMembers(System.Text.StringBuilder sb)
        {
            sb.Append($"Side = {Side}, Area = {Area}");
            return true;
        }
    }

    // Nominal record (no positional parameters) with init-only properties
    public record Config
    {
        public string Host { get; init; } = "localhost";
        public int Port { get; init; } = 80;
        public IReadOnlyList<string> Tags { get; init; } = Array.Empty<string>();
    }

    // Positional record with an attribute on the generated property
    public record Tagged([property: Obsolete("use Label2")] string Label);

    class Program
    {
        static void Main()
        {
            var ada = new Person("Ada", "Lovelace") { Age = 36 };
            var ada2 = new Person("Ada", "Lovelace") { Age = 36 };
            Console.WriteLine(ada);
            Console.WriteLine($"value equality: {ada == ada2}, reference equality: {ReferenceEquals(ada, ada2)}");
            Console.WriteLine($"hash codes equal: {ada.GetHashCode() == ada2.GetHashCode()}");

            // Non-destructive mutation
            var married = ada with { Last = "King" };
            var older = married with { Age = married.Age + 1 };
            Console.WriteLine($"with: {married.FullName}, age {older.Age}; original still {ada.Last}");

            // Deconstruction from positional parameters
            var (first, last) = ada;
            Console.WriteLine($"deconstructed: {first} / {last}");

            // Inheritance and equality respecting runtime type
            Person emp = new Employee("Grace", "Hopper", "Navy");
            Person plain = new Person("Grace", "Hopper");
            Console.WriteLine(emp);
            Console.WriteLine($"Employee equals Person with same names? {emp == plain}");
            var moved = (Employee)emp with { Team = "Compilers" };
            Console.WriteLine($"with on derived keeps type: {moved.GetType().Name}, team {moved.Team}");

            var shapes = new List<Shape> { new Circle(1), new Square(2) };
            foreach (var s in shapes) Console.WriteLine($"{s} -> area {s.Area:F2}");

            var cfg = new Config { Port = 8080, Tags = new[] { "prod" } };
            var dev = cfg with { Host = "dev.local" };
            Console.WriteLine(cfg);
            Console.WriteLine($"dev: {dev.Host}:{dev.Port}");

            // `record` used as an ordinary identifier
            var record = new Tagged("x");
            Console.WriteLine($"contextual keyword as local: {record.GetType().Name}");
        }
    }
}
