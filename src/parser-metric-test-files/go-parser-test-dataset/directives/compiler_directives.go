// Feature : Compiler and tool directives in comments - //go:noinline,
//
//	//go:nosplit, //go:generate, //go:linkname (restricted),
//	//line, //export (cgo, described only), "Code generated" marker
//
// Version : Go 1.x; //go:linkname to std internals restricted in Go 1.23
// Spec    : cmd/compile "Compiler Directives"; cmd/go "Generate Go files by
//
//	processing source"; go.dev/s/generatedcode
//
// Parser edge cases:
//   - Directives are LINE comments with NO space after `//` and a
//     `name:` prefix: `//go:noinline`. `// go:noinline` (with a space) is an
//     ordinary comment and has no effect.
//   - `//line file.go:100` and `/*line file.go:100:5*/` (block form) change
//     the position reported for FOLLOWING tokens - parsers that report
//     positions must honour them.
//   - `//go:linkname local pkg.symbol` requires `import _ "unsafe"`.
//   - Directives must immediately precede the declaration they apply to.
package main

import (
	"fmt"
	"runtime"
	_ "unsafe" // required for //go:linkname
)

//go:generate echo "go generate would run this command"

//go:noinline
func add(a, b int) int { return a + b }

//go:nosplit
func fastPath(x int) int { return x << 1 }

// go:noinline   <- NOT a directive (space after //); just a comment
func notReallyNoinline() int { return 7 }

// Pull-style linkname to a symbol in THIS package (allowed everywhere).
//
//go:linkname localAlias main.add
func localAlias(a, b int) int

func whereAmI() string {
	_, file, line, _ := runtime.Caller(1)
	return fmt.Sprintf("%s:%d", file, line)
}

func main() {
	fmt.Println("add (noinline):", add(2, 3))
	fmt.Println("fastPath (nosplit):", fastPath(21))
	fmt.Println("not a directive:", notReallyNoinline())
	fmt.Println("linkname alias:", localAlias(40, 2))

	fmt.Println("before //line:", whereAmI())
//line virtual_file.go:1000
	fmt.Println("after //line:", whereAmI())
}
