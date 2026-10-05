/*
 * Feature : Pattern matching - type patterns in `is`, `switch` on any type,
 *           `case` guards with `when`, constant patterns, var patterns
 * Version : C# 7.0
 * Spec    : csharplang proposals/csharp-7.0/pattern-matching.md
 *
 * C# 7.0 introduced the first patterns: `expr is Type name`, constant
 * patterns (`is null`, `case 0:`), and `var` patterns. `switch` statements
 * accept any type and case labels can declare variables with `when` guards.
 * (Recursive/property patterns and switch EXPRESSIONS are C# 8.)
 *
 * Parser edge cases:
 *  - `if (o is int n && n > 0)` - declaration pattern introduces `n` whose
 *    scope leaks into the enclosing statement ("definite assignment when
 *    true").
 *  - `case int n when n > 100:` - `when` is a contextual keyword.
 *  - `case null:` and `is null`.
 *  - Order matters: a case subsumed by an earlier case is an error.
 *  - `is var x` always matches (including null).
 *  - Ambiguity: `e is A < B > C` - generic type pattern vs relational.
 */
using System;
using System.Collections.Generic;

namespace Patterns7
{
    abstract class Shape { }
    class Circle : Shape { public double Radius; public Circle(double r) { Radius = r; } }
    class Rect : Shape { public double W, H; public Rect(double w, double h) { W = w; H = h; } }
    class Triangle : Shape { public double Base, Height; public Triangle(double b, double h) { Base = b; Height = h; } }

    class Program
    {
        static double Area(Shape s)
        {
            switch (s)
            {
                case Circle c:
                    return Math.PI * c.Radius * c.Radius;
                case Rect r when r.W == r.H:
                    Console.Write("(square) ");
                    return r.W * r.W;
                case Rect r:
                    return r.W * r.H;
                case null:
                    throw new ArgumentNullException(nameof(s));
                default:
                    return 0;
            }
        }

        static string Describe(object o)
        {
            switch (o)
            {
                case 0:                         return "zero";
                case int n when n < 0:          return $"negative int {n}";
                case int n:                     return $"positive int {n}";
                case long l:                    return $"long {l}";
                case string str when str.Length == 0: return "empty string";
                case string str:                return $"string \"{str}\"";
                case IEnumerable<int> seq:      return $"int sequence";
                case null:                      return "null";
                default:                        return $"other: {o.GetType().Name}";
            }
        }

        static void Main()
        {
            var shapes = new List<Shape> { new Circle(1), new Rect(2, 3), new Rect(4, 4), new Triangle(3, 2) };
            foreach (var s in shapes)
                Console.WriteLine($"{s.GetType().Name}: {Area(s):F2}");

            object[] things = { 0, -5, 42, 7L, "", "hi", new[] { 1, 2 }, null, 3.5 };
            foreach (var t in things) Console.WriteLine(Describe(t));

            // `is` with a declaration pattern; variable usable afterwards
            object boxed = 123;
            if (boxed is int value && value > 100)
                Console.WriteLine($"boxed is a large int: {value}");

            // Pattern variable scope leaks to the enclosing block when the
            // condition is negated and the method returns early
            object maybe = "text";
            if (!(maybe is string text)) return;
            Console.WriteLine($"text is in scope after the if: {text.ToUpper()}");

            // Constant patterns
            object nothing = null;
            Console.WriteLine($"nothing is null: {nothing is null}");
            Console.WriteLine($"boxed is 123: {boxed is 123}");

            // var pattern always matches
            if (nothing is var anything)
                Console.WriteLine($"var pattern matched null: {anything == null}");

            // Generic type in a pattern
            object list = new List<string> { "a" };
            if (list is List<string> strings) Console.WriteLine($"List<string> count {strings.Count}");
        }
    }
}
