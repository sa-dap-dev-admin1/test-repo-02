// Feature : Generic type aliases (`type A[T any] = ...`), weak pointers,
//
//	runtime.AddCleanup, encoding omitzero (library)
//
// Version : Go 1.24 (February 2025)
// Spec    : Go 1.24 release notes "Changes to the language"; spec "Alias
//
//	declarations"; proposal #46477
//
// Parser edge cases:
//   - `type Set[K comparable] = map[K]struct{}` - type parameters on an
//     ALIAS declaration (the `=` distinguishes alias from definition). This
//     was a syntax error before Go 1.24 (1.23 accepted it only with
//     GOEXPERIMENT=aliastypeparams).
//   - Aliases of generic types with partially fixed arguments:
//     `type StringMap[V any] = map[string]V`.
//   - Methods cannot be declared on an alias of a generic type with type
//     parameters (`func (s Set[K]) ...` is an error) - but the alias is
//     fully interchangeable with its target type.
package main

import (
	"fmt"
	"slices"
	"sort"
	"weak"
)

type Set[K comparable] = map[K]struct{}

type StringMap[V any] = map[string]V

type Pair[A, B any] struct {
	First  A
	Second B
}

type IntPair[B any] = Pair[int, B] // alias fixing one type argument

type Predicate[T any] = func(T) bool

func Keys[K comparable](s Set[K]) []K {
	out := make([]K, 0, len(s))
	for k := range s {
		out = append(out, k)
	}
	return out
}

func CountIf[T any](xs []T, p Predicate[T]) int {
	n := 0
	for _, x := range xs {
		if p(x) {
			n++
		}
	}
	return n
}

func main() {
	s := Set[string]{"go": {}, "rust": {}, "zig": {}}
	var plain map[string]struct{} = s // alias is identical to its target
	keys := Keys(plain)
	sort.Strings(keys)
	fmt.Println("Set alias keys:", keys)

	ages := StringMap[int]{"ada": 36, "alan": 41}
	fmt.Println("StringMap[int]:", ages["alan"])

	p := IntPair[string]{First: 7, Second: "seven"}
	var full Pair[int, string] = p
	fmt.Println("IntPair alias:", full.First, full.Second)

	nums := []int{1, 5, 8, 12, 3}
	var big Predicate[int] = func(n int) bool { return n > 4 }
	fmt.Println("CountIf with alias Predicate:", CountIf(nums, big))

	value := new(int)
	*value = 2025
	wp := weak.Make(value) // weak pointer (Go 1.24)
	if strong := wp.Value(); strong != nil {
		fmt.Println("weak pointer still alive:", *strong)
	}
	fmt.Println("slices.Contains alias-typed slice:", slices.Contains(keys, "zig"))
}
