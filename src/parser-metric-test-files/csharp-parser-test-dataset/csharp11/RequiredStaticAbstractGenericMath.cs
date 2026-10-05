/*
 * Feature : required members, static abstract/virtual interface members
 *           (generic math), file-local types, generic attributes,
 *           unsigned right shift >>>, checked user-defined operators,
 *           nameof on method parameters in attributes, auto-default structs
 * Version : C# 11
 * Spec    : csharplang proposals/csharp-11.0/required-members.md,
 *           static-abstracts-in-interfaces.md, file-local-types.md,
 *           generic-attributes.md, unsigned-right-shift-operator.md,
 *           checked-user-defined-operators.md, extended-nameof-scope.md,
 *           auto-default-structs.md
 *
 * Parser edge cases:
 *  - `public required string Name { get; init; }` - `required` modifier.
 *  - `static abstract T operator +(T a, T b);` inside an interface, and
 *    `where T : INumber<T>` self-referencing constraints.
 *  - `T.Zero`, `T.Parse(...)` - member access on a TYPE PARAMETER.
 *  - `file class Helper { }` - `file` accessibility modifier.
 *  - `[Validate<int>]` - generic attribute.
 *  - `x >>> 3` and `>>>=` - new tokens. Ambiguity with nested generics:
 *    `List<List<List<int>>>` must still close three type argument lists.
 *  - `public static Money operator checked +(...)` - `checked` between
 *    `operator` and the operator token.
 */
using System;
using System.Collections.Generic;
using System.Numerics;

namespace Csharp11.Members
{
    class User
    {
        public required string Name { get; init; }
        public required string Email { get; init; }
        public int Age { get; init; }
    }

    interface IShape<TSelf> where TSelf : IShape<TSelf>
    {
        static abstract string Kind { get; }
        static abstract TSelf Unit();
        static virtual string Describe() => $"a {TSelf.Kind}";
        double Area { get; }
    }

    record Square(double Side) : IShape<Square>
    {
        public static string Kind => "square";
        public static Square Unit() => new(1);
        public double Area => Side * Side;
    }

    readonly record struct Money(long Cents) : IAdditionOperators<Money, Money, Money>
    {
        public static Money operator +(Money a, Money b) => new(a.Cents + b.Cents);
        public static Money operator checked +(Money a, Money b) => new(checked(a.Cents + b.Cents));
    }

    [AttributeUsage(AttributeTargets.Method)]
    class ExampleAttribute<T> : Attribute
    {
        public T Value { get; }
        public ExampleAttribute(T value) => Value = value;
    }

    file static class Secret                        // visible only in this file
    {
        public static string Reveal() => "file-local type";
    }

    struct AutoDefault
    {
        public int A;
        public int B;
        public AutoDefault(int a) { A = a; }        // B auto-defaulted (C# 11)
    }

    class Program
    {
        static T Sum<T>(IEnumerable<T> values) where T : INumber<T>
        {
            T total = T.Zero;
            foreach (var v in values) total += v;
            return total;
        }

        static T ParseAll<T>(string csv) where T : INumber<T>, IParsable<T>
        {
            T total = T.Zero;
            foreach (var part in csv.Split(',')) total += T.Parse(part, null);
            return total;
        }

        static string Info<T>() where T : IShape<T> => $"{T.Describe()}, unit area {T.Unit().Area}";

        [Example<int>(42)]
        [Obsolete(nameof(value))]                   // nameof a parameter in an attribute
        static void Annotated(int value) { }

        static void Main()
        {
            var u = new User { Name = "Ada", Email = "ada@example.com" };   // omitting either is CS9035
            Console.WriteLine($"required members: {u.Name} <{u.Email}> age {u.Age}");

            Console.WriteLine($"Sum<int> = {Sum(new[] { 1, 2, 3 })}, Sum<double> = {Sum(new[] { 0.5, 0.25 })}, Sum<decimal> = {Sum(new[] { 1.1m, 2.2m })}");
            Console.WriteLine($"ParseAll<long> = {ParseAll<long>("10,20,30")}");
            Console.WriteLine(Info<Square>());

            var total = new Money(150) + new Money(275);
            Console.WriteLine($"Money: {total}");
            try { var overflow = checked(new Money(long.MaxValue) + new Money(1)); }
            catch (OverflowException) { Console.WriteLine("checked operator threw OverflowException"); }

            int negative = -16;
            Console.WriteLine($"-16 >> 2 = {negative >> 2}, -16 >>> 28 = {negative >>> 28}");
            uint bits = 0xF0;
            bits >>>= 4;
            Console.WriteLine($"bits >>>= 4 -> {bits}");
            List<List<List<int>>> nested = new() { new() { new() { 1 } } };   // >>> closing generics
            Console.WriteLine($"nested generics: {nested[0][0][0]}");

            Console.WriteLine(Secret.Reveal());
            var ad = new AutoDefault(5);
            Console.WriteLine($"auto-default struct: A={ad.A} B={ad.B}");
        }
    }
}
