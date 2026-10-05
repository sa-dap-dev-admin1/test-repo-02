/*
 * Feature : Raw string literals, raw interpolated strings, newlines in
 *           interpolation holes, UTF-8 string literals (u8)
 * Version : C# 11 (.NET 7)
 * Spec    : csharplang proposals/csharp-11.0/raw-string-literal.md,
 *           new-line-in-interpolation.md, utf8-string-literals.md
 *
 * Parser edge cases:
 *  - `"""..."""` - three OR MORE quotes; the content may contain runs of
 *    quotes shorter than the delimiter. No escape sequences are processed.
 *  - Multi-line raw strings: opening quotes must end the line, closing
 *    quotes start their own line, and the closing line's indentation is
 *    stripped from every content line.
 *  - `$"""...{x}..."""` - interpolation; `$$"""...{{x}}..."""` - the number
 *    of `$` sets how many braces open a hole, so single `{` is literal
 *    (handy for JSON).
 *  - Interpolation holes may now contain newlines: `{ cond\n ? a\n : b }`.
 *  - `"abc"u8` - suffix on a string literal producing ReadOnlySpan<byte>.
 */
using System;
using System.Text;

namespace Csharp11.Strings
{
    class Program
    {
        static void Main()
        {
            string single = """She said "hi" and left""";
            Console.WriteLine(single);

            string quad = """"Contains """triple""" quotes"""";
            Console.WriteLine(quad);

            string path = """C:\no\escapes\here\n""";
            Console.WriteLine(path);

            string multi = """
                {
                  "kind": "raw",
                  "indent": "stripped to the closing quotes"
                }
                """;
            Console.WriteLine(multi);

            string name = "parser";
            int version = 11;
            string interp = $"""Name: "{name}", version {version}""";
            Console.WriteLine(interp);

            // $$ : two braces open a hole, so single braces are literal JSON
            string json = $$"""
                {
                  "name": "{{name}}",
                  "version": {{version}},
                  "tags": ["{{name.ToUpper()}}", "raw"]
                }
                """;
            Console.WriteLine(json);

            // Newlines inside an interpolation hole
            int score = 87;
            string grade = $"Grade: {score switch
            {
                >= 90 => "A",
                >= 80 => "B",
                _ => "C",
            }}";
            Console.WriteLine(grade);

            ReadOnlySpan<byte> utf8 = "héllo"u8;
            Console.WriteLine($"u8 literal: {utf8.Length} bytes, first = 0x{utf8[0]:X2}, roundtrip = {Encoding.UTF8.GetString(utf8)}");
            byte[] arr = "AB"u8.ToArray();
            Console.WriteLine($"u8 ToArray: {string.Join(",", arr)}");
        }
    }
}
