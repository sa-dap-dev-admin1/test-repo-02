/*
 * Feature : File-scoped namespaces, global using directives, constant
 *           interpolated strings, CallerArgumentExpression, sealed
 *           ToString in records, AsyncMethodBuilder on methods
 * Version : C# 10 (.NET 6)
 * Spec    : csharplang proposals/csharp-10.0/file-scoped-namespaces.md,
 *           GlobalUsingDirective.md, constant_interpolated_strings.md,
 *           caller-argument-expression.md, record-structs.md (sealed ToString)
 *
 * Parser edge cases:
 *  - `namespace Foo.Bar;` - namespace declaration terminated by `;` that
 *    covers the REST OF THE FILE. Only one per file, and it cannot be mixed
 *    with block-scoped namespaces.
 *  - `global using System.Text;` and `global using static System.Math;`
 *    and `global using Alias = ...;` - must precede all non-global usings
 *    and all member declarations.
 *  - `const string X = $"{A} and {B}";` - interpolation in a constant when
 *    every hole is itself a constant string.
 *  - `[CallerArgumentExpression("value")]` - attribute with a string naming
 *    a sibling parameter.
 */
global using System;
global using System.Collections.Generic;
global using static System.Math;
global using StringMap = System.Collections.Generic.Dictionary<string, string>;

using System.Runtime.CompilerServices;

namespace Csharp10.Namespaces;

static class Guard
{
    public static T NotNull<T>(T? value, [CallerArgumentExpression("value")] string expr = "") where T : class
        => value ?? throw new ArgumentNullException(expr, $"'{expr}' was null");

    public static int Positive(int value, [CallerArgumentExpression("value")] string expr = "")
        => value > 0 ? value : throw new ArgumentOutOfRangeException(expr, $"'{expr}' must be positive, got {value}");
}

record Version(int Major, int Minor)
{
    public sealed override string ToString() => $"v{Major}.{Minor}";   // sealed ToString (C# 10)
}

record Release(int Major, int Minor, string Codename) : Version(Major, Minor);

class Program
{
    const string Product = "Parser";
    const string Edition = "Pro";
    const string Title = $"{Product} {Edition}";            // constant interpolated string

    [Obsolete($"Use {nameof(NewApi)} in {Title}")]           // constant interpolation in an attribute
    static void OldApi() { }
    static void NewApi() { }

    static void Main()
    {
        Console.WriteLine($"Title = {Title}");
        Console.WriteLine($"global using static Math: Sqrt(81) = {Sqrt(81)}, Max = {Max(3, 9)}");

        var map = new StringMap { ["k"] = "v" };
        Console.WriteLine($"global alias StringMap: {map["k"]}");

        var list = new List<int> { 1, 2, 3 };
        Console.WriteLine($"Guard.NotNull ok: {Guard.NotNull(list).Count}");
        try { Guard.Positive(list.Count - 10); }
        catch (ArgumentOutOfRangeException ex) { Console.WriteLine($"caught: {ex.ParamName}"); }

        string? missing = null;
        try { Guard.NotNull(missing); }
        catch (ArgumentNullException ex) { Console.WriteLine($"caught: {ex.ParamName}"); }

        Console.WriteLine($"sealed ToString inherited by derived record: {new Release(2, 1, "Kite")}");
    }
}
