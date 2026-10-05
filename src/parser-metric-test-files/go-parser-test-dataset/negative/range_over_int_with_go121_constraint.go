//go:build go1.21

// Feature : NEGATIVE TEST - Go 1.22/1.23 syntax uses in a file pinned to
//
//	language version 1.21 by its build constraint
//
// Version : Requires toolchain Go 1.21+ for per-file language versions
// Spec    : Go 1.21 release notes (per-file versions); Go 1.22 (range over
//
//	int); Go 1.23 (range over func)
//
// EXPECTED:
//   - Grammar-only Go parser            -> PASS
//   - Version-aware parser / go build   -> FAIL:
//     "cannot range over 5 (untyped int constant): requires go1.22 or later"
//     "cannot range over seq ...: requires go1.23 or later"
//
// Parser edge cases: `range` over an int or a func is syntactically the
// same as any range clause; only the file's language version rejects it.
package main

import "fmt"

func seq(yield func(int) bool) {
	for i := 0; i < 3; i++ {
		if !yield(i) {
			return
		}
	}
}

func main() {
	for i := range 5 {
		fmt.Print(i)
	}
	for v := range seq {
		fmt.Print(v)
	}
	fmt.Println()
}
