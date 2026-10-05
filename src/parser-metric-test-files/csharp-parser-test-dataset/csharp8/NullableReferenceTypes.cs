/*
 * Feature : Nullable reference types, null-forgiving operator, `??=`,
 *           nullable directives and attributes
 * Version : C# 8.0 (.NET Core 3.0)
 * Spec    : csharplang proposals/csharp-8.0/nullable-reference-types.md,
 *           null-coalescing-assignment.md
 *
 * With `#nullable enable`, `string` means non-nullable and `string?` means
 * nullable. The compiler tracks null-state and warns; `x!` suppresses.
 *
 * Parser edge cases:
 *  - `string?` on a REFERENCE type (previously only `int?` was legal).
 *  - `T?` with unconstrained generics (C# 9 refinement) vs `where T : class?`.
 *  - `x!` postfix null-forgiving: `name!.Length`, `GetValue()!`,
 *    and chains like `a!.b!.c`. Must not be confused with prefix `!x`.
 *  - `x ??= y` - compound null-coalescing assignment.
 *  - `#nullable enable | disable | restore [warnings | annotations]`.
 *  - Ambiguity: `a ? b : c` vs `a?.b` vs `T? x` vs `cond ? (T?)x : y`.
 */
#nullable enable
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;

namespace NullableRefs
{
    class Person
    {
        public string Name { get; }
        public string? Nickname { get; set; }
        public Person? Manager { get; set; }
        public Person(string name) => Name = name;

        public string DisplayName => Nickname ?? Name;
    }

    class Cache<TKey, TValue> where TKey : notnull where TValue : class
    {
        private readonly Dictionary<TKey, TValue> _items = new Dictionary<TKey, TValue>();

        public bool TryGet(TKey key, [NotNullWhen(true)] out TValue? value) => _items.TryGetValue(key, out value);

        public TValue GetOrAdd(TKey key, Func<TValue> factory)
        {
            if (!_items.TryGetValue(key, out var existing))
            {
                existing = factory();
                _items[key] = existing;
            }
            return existing;
        }
    }

    class Program
    {
        static int Length(string? s) => s?.Length ?? 0;

        [return: MaybeNull]
        static string FindOrNull(string key) => key == "known" ? "value" : null!;

        static void Require([NotNull] object? value)
        {
            if (value is null) throw new ArgumentNullException(nameof(value));
        }

        static void Main()
        {
            var ada = new Person("Ada") { Nickname = null };
            var boss = new Person("Grace") { Nickname = "Amazing Grace" };
            ada.Manager = boss;

            Console.WriteLine($"display: {ada.DisplayName}, manager: {ada.Manager?.DisplayName}");
            Console.WriteLine($"Length(null) = {Length(null)}, Length(\"abc\") = {Length("abc")}");

            // Null-forgiving operator: we know Manager was set above
            Console.WriteLine($"manager name length: {ada.Manager!.Name.Length}");
            Console.WriteLine($"chained: {ada.Manager!.Nickname!.ToUpper()}");

            // ??= assigns only when null
            string? label = null;
            label ??= "default label";
            label ??= "ignored";
            Console.WriteLine($"label = {label}");

            List<int>? numbers = null;
            (numbers ??= new List<int>()).Add(5);
            Console.WriteLine($"numbers count = {numbers.Count}");

            var cache = new Cache<string, string>();
            cache.GetOrAdd("a", () => "alpha");
            if (cache.TryGet("a", out var hit)) Console.WriteLine($"cache hit: {hit.ToUpper()}");
            Console.WriteLine($"cache miss: {(cache.TryGet("b", out var miss) ? miss : "(none)")}");

            object? maybe = "present";
            Require(maybe);
            Console.WriteLine($"after Require, maybe is non-null: {maybe.GetHashCode() != 0}");

            Console.WriteLine($"FindOrNull: {FindOrNull("known")}, {FindOrNull("x") ?? "(null)"}");

#nullable disable
            string oblivious = null;          // no warning in a disabled region
            Console.WriteLine($"oblivious is null: {oblivious == null}");
#nullable restore

            // Conditional vs nullable-type ambiguity
            bool flag = true;
            object? o = flag ? (string?)null : "x";
            Console.WriteLine($"conditional with nullable cast: {o ?? "null"}");
        }
    }
}
