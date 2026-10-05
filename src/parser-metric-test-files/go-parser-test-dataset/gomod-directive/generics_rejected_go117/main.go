// Feature : NEGATIVE TEST - generics in a module whose go.mod declares
//
//	`go 1.17`
//
// Version : Any toolchain >= 1.18 building a module with language go1.17
// Spec    : go.dev/ref/mod "go directive"; Go 1.21 release notes (per-file
//
//	//go:build versions)
//
// Run from this directory:  go build .
//
// EXPECTED:
//   - Grammar-only Go parser (latest grammar)        -> PASS (syntax is valid)
//   - Version-aware parser / `go vet` / `go build`  -> FAIL:
//     "type parameter requires go1.18 or later"
//     "predeclared any requires go1.18 or later"
//
// Parser edge cases:
//   - The version comes from go.mod, not from this file.
//   - A per-file `//go:build go1.17` line would NOT work here: since Go 1.21
//     (clarified in 1.22) per-file language versions below go1.21 are
//     raised to go1.21, so generics stay enabled. Only go.mod can select a
//     pre-1.21 language version.
package main

import "fmt"

func Map[T, U any](xs []T, f func(T) U) []U {
	out := make([]U, 0, len(xs))
	for _, x := range xs {
		out = append(out, f(x))
	}
	return out
}

func main() {
	fmt.Println(Map([]int{1, 2, 3}, func(n int) string { return fmt.Sprint(n * n) }))
}
