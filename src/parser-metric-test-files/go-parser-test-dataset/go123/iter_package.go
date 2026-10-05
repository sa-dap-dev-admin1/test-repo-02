// Feature : iter.Seq / iter.Seq2 types, iter.Pull, slices.All / Values /
//
//	Collect / Sorted, maps.Keys / All iterator functions
//
// Version : Go 1.23 (August 2024)
// Spec    : package iter docs; Go 1.23 release notes "Iterators",
//
//	"New iter package", "slices", "maps"
//
// Parser edge cases:
//   - Ranging over an expression that is a CALL returning an iterator:
//     `for k := range maps.Keys(m)`.
//   - iter.Seq[V] is a generic named func type: `type Seq[V any] func(yield
//     func(V) bool)` - ranging over a value of a NAMED function type.
//   - iter.Pull returns (next, stop) - converts push to pull iteration.
//   - Method value used as an iterator: `for x := range obj.Items`.
package main

import (
	"fmt"
	"iter"
	"maps"
	"slices"
	"strings"
)

type Inventory struct{ items map[string]int }

func (inv *Inventory) InStock(yield func(string, int) bool) { // method value as iter.Seq2
	for _, name := range slices.Sorted(maps.Keys(inv.items)) {
		if inv.items[name] > 0 && !yield(name, inv.items[name]) {
			return
		}
	}
}

func Filter[V any](seq iter.Seq[V], keep func(V) bool) iter.Seq[V] {
	return func(yield func(V) bool) {
		for v := range seq {
			if keep(v) && !yield(v) {
				return
			}
		}
	}
}

func Map[V, W any](seq iter.Seq[V], f func(V) W) iter.Seq[W] {
	return func(yield func(W) bool) {
		for v := range seq {
			if !yield(f(v)) {
				return
			}
		}
	}
}

func main() {
	words := []string{"iter", "seq", "pull", "yield", "range"}

	for i, w := range slices.All(words) {
		if i < 2 {
			fmt.Println("slices.All:", i, w)
		}
	}

	long := Filter(slices.Values(words), func(s string) bool { return len(s) > 4 })
	upper := slices.Collect(Map(long, strings.ToUpper))
	fmt.Println("Filter+Map+Collect:", upper)

	m := map[string]int{"b": 2, "a": 1, "c": 3}
	fmt.Println("slices.Sorted(maps.Keys):", slices.Sorted(maps.Keys(m)))

	inv := &Inventory{items: map[string]int{"bolts": 4, "nuts": 0, "gears": 9}}
	for name, qty := range inv.InStock {
		fmt.Println("in stock:", name, qty)
	}

	next, stop := iter.Pull(slices.Values(words))
	defer stop()
	first, _ := next()
	second, ok := next()
	fmt.Println("iter.Pull:", first, second, ok)
}
