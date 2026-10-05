// Feature : Go 1.25 - removal of "core types" from the language spec;
//
//	containers-aware GOMAXPROCS; testing/synctest; WaitGroup.Go
//
// Version : Go 1.25 (August 2025)
// Spec    : Go 1.25 release notes "Changes to the language"; go.dev/blog/
//
//	coretypes
//
// VALIDATION: written against Go 1.25; the dataset build environment has Go
// 1.24, which accepts everything here EXCEPT sync.WaitGroup.Go (marked).
//
// Go 1.25 made NO syntax changes. The spec dropped the notion of "core
// type" and restated rules (for range, slicing, composite literals, etc.)
// in terms of type sets. Behaviour is unchanged for valid programs, so this
// file exercises exactly the generic operations whose spec wording changed,
// guarding a parser/type-checker against regressions.
//
// Parser edge cases:
//   - Ranging over a value whose type is a type parameter constrained to
//     slices of the same element type.
//   - Slicing and indexing a type-parameter-typed operand.
//   - Composite literal of a type parameter's underlying struct type is
//     still NOT allowed (`T{...}` with T a type param is an error).
package main

import (
	"fmt"
	"sync"
)

func SumAll[S ~[]E, E ~int | ~float64](s S) E {
	var total E
	for _, v := range s { // range over a type-parameter-typed value
		total += v
	}
	return total
}

func Middle[S ~[]E | ~[4]E, E any](s S) E { // union of slice and array kinds
	return s[1]
}

func Halves[S ~[]E, E any](s S) (S, S) {
	return s[:len(s)/2], s[len(s)/2:] // slicing a type-param value
}

type Scores []float64

func main() {
	fmt.Println("SumAll:", SumAll([]int{1, 2, 3}), SumAll(Scores{1.5, 2.5}))
	// E cannot be inferred through a slice/array union, so instantiate explicitly
	fmt.Println("Middle slice/array:", Middle[[]string, string]([]string{"a", "b", "c"}), Middle[[4]int, int]([4]int{9, 8, 7, 6}))
	l, r := Halves([]rune("parser"))
	fmt.Println("Halves:", string(l), string(r))

	var wg sync.WaitGroup
	results := make([]int, 3)
	for i := range 3 {
		wg.Add(1)
		go func() { defer wg.Done(); results[i] = i * i }()
		// Go 1.25: wg.Go(func() { results[i] = i * i })   <- requires Go 1.25
	}
	wg.Wait()
	fmt.Println("results:", results)
}
