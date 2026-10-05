// Feature : Per-iteration loop variables in 3-clause `for` loops and
//
//	`for range` loops
//
// Version : Go 1.22 (February 2024) - applies to modules declaring go 1.22+
// Spec    : Go 1.22 release notes "Changes to the language"; proposal
//
//	#60078; go.dev/blog/loopvar-preview
//
// Each iteration now has its OWN copy of the loop variable, so closures
// and goroutines capturing it see that iteration's value.
//
// Parser edge cases:
//   - Syntax is unchanged; SEMANTICS depend on the language version, which
//     comes from the go.mod `go` directive or a per-file `//go:build go1.N`
//     constraint. See gomod-directive/ and negative/ for the old behaviour.
//   - Modifying the loop variable inside the body of a 3-clause loop still
//     affects the NEXT iteration (the value is copied forward).
package main

import (
	"fmt"
	"sort"
	"sync"
)

func main() {
	// Closures capture a distinct i per iteration
	var funcs []func() int
	for i := 0; i < 3; i++ {
		funcs = append(funcs, func() int { return i })
	}
	fmt.Print("3-clause closures:")
	for _, f := range funcs {
		fmt.Print(" ", f())
	}
	fmt.Println(" (Go 1.21 and earlier would print 3 3 3)")

	// Range loops: taking the address yields a different pointer each time
	items := []string{"a", "b", "c"}
	var ptrs []*string
	for _, it := range items {
		ptrs = append(ptrs, &it)
	}
	fmt.Print("range addresses:")
	for _, p := range ptrs {
		fmt.Print(" ", *p)
	}
	fmt.Println()

	// Goroutines capturing the loop variable
	var mu sync.Mutex
	var wg sync.WaitGroup
	var seen []int
	for i := range 5 {
		wg.Add(1)
		go func() {
			defer wg.Done()
			mu.Lock()
			seen = append(seen, i)
			mu.Unlock()
		}()
	}
	wg.Wait()
	sort.Ints(seen)
	fmt.Println("goroutines saw:", seen)

	// Mutation inside the body carries into the next iteration's copy
	for i := 0; i < 10; i++ {
		if i == 2 {
			i = 7
		}
		fmt.Print(i, " ")
	}
	fmt.Println("(mutation copied forward)")
}
