//go:build (linux || darwin || windows) && (amd64 || arm64 || !cgo) && go1.21
// +build linux darwin windows
// +build amd64 arm64 !cgo
// +build go1.21

// Feature : Build constraints - `//go:build` boolean expressions and the
//
//	legacy `// +build` lines
//
// Version : `//go:build` introduced in Go 1.17 (August 2021); `// +build`
//
//	deprecated by gofmt synchronization, still accepted
//
// Spec    : cmd/go "Build constraints"; proposal #41184; go/build/constraint
//
// Parser edge cases:
//   - The constraint must appear BEFORE the package clause, preceded only by
//     blank lines and other line comments, and be followed by a BLANK LINE
//     (otherwise it is treated as the package doc comment, not a
//     constraint).
//   - `//go:build` uses Go-like syntax: &&, ||, !, parentheses.
//   - Legacy `// +build a b` means a OR b; separate lines are ANDed;
//     `a,b` means a AND b. gofmt keeps both forms in sync.
//   - `go1.21` tag also sets the FILE's language version (see negative/).
//   - Only ONE `//go:build` line is allowed per file.
package main

import (
	"fmt"
	"go/build/constraint"
	"runtime"
)

func main() {
	fmt.Printf("built because %s/%s satisfies the constraint\n", runtime.GOOS, runtime.GOARCH)

	exprs := []string{
		"//go:build (linux || darwin) && !cgo",
		"//go:build go1.21 && amd64",
		"// +build linux,amd64 darwin",
	}
	tags := map[string]bool{"linux": true, "amd64": true, "go1.21": true}
	for _, line := range exprs {
		e, err := constraint.Parse(line)
		if err != nil {
			fmt.Println("parse error:", err)
			continue
		}
		fmt.Printf("%-40s -> %-30s eval(linux,amd64) = %v\n", line, e.String(), e.Eval(func(t string) bool { return tags[t] }))
	}
	plus, _ := constraint.PlusBuildLines(constraint.Expr(&constraint.AndExpr{X: &constraint.TagExpr{Tag: "linux"}, Y: &constraint.NotExpr{X: &constraint.TagExpr{Tag: "cgo"}}}))
	fmt.Println("generated +build lines:", plus)
}
