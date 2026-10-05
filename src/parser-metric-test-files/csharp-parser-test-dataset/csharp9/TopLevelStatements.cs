/*
 * Feature : Top-level statements (no Main method, no class)
 * Version : C# 9.0
 * Spec    : csharplang proposals/csharp-9.0/top-level-statements.md
 *
 * A single file per program may contain statements directly at the top
 * level. The compiler synthesizes the entry point. `args` is implicitly
 * available, `await` makes it async, and `return n;` sets the exit code.
 * Type and namespace declarations may FOLLOW the statements.
 *
 * Parser edge cases:
 *  - A compilation unit that starts with statements, not `namespace`/`class`.
 *  - `using` directives must come FIRST; statements next; then type
 *    declarations. A statement after a type declaration is an error.
 *  - Local functions at top level look like method declarations without a
 *    containing type: `int Square(int x) => x * x;`
 *  - `args` is an implicit parameter, not a declared variable.
 *  - `await` at top level (implicit async Main).
 *  - Ambiguity: `Foo(x);` could be a call or (in top-level scope) still a
 *    call - but `Foo x;` is a local declaration.
 *  - Only ONE file in a project may have top-level statements - a good
 *    multi-file negative test.
 */
using System;
using System.Linq;
using System.Threading.Tasks;

Console.WriteLine("Hello from top-level statements");
Console.WriteLine($"args.Length = {args.Length}");

var numbers = Enumerable.Range(1, 10).ToArray();
Console.WriteLine($"sum of squares = {numbers.Sum(Square)}");

int Square(int x) => x * x;                       // top-level local function

static string Shout(string s) => s.ToUpperInvariant() + "!";   // static local function
Console.WriteLine(Shout("top level"));

await Task.Delay(1);                              // makes the synthesized Main async
Console.WriteLine("after await");

var inventory = new Inventory();
inventory.Add("bolts", 10);
inventory.Add("nuts", 4);
Console.WriteLine(inventory.Report());

var p = new Point(3, 4);
Console.WriteLine($"{p} distance {p.Distance():F1}");

if (args.Contains("--fail")) return 1;           // exit code from top-level code
return 0;

// Type declarations must come after all top-level statements
class Inventory
{
    private readonly System.Collections.Generic.Dictionary<string, int> _items = new();
    public void Add(string name, int qty) => _items[name] = qty;
    public string Report() => string.Join(", ", _items.Select(kv => $"{kv.Key}={kv.Value}"));
}

record Point(double X, double Y)
{
    public double Distance() => Math.Sqrt(X * X + Y * Y);
}
