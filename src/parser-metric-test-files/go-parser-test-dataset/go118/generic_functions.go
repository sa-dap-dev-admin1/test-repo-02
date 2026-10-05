// Feature : Type parameters on functions, `any`, `comparable`, explicit
//
//	and inferred instantiation
//
// Version : Go 1.18 (March 2022)
// Spec    : Go 1.18 release notes "Generics"; Type Parameters Proposal
//
//	(golang/proposal design/43651-type-parameters.md); spec "Type
//	parameter declarations", "Instantiations"
//
// Parser edge cases:
//   - `func Map[T, U any](...)` - square-bracket type parameter list after
//     the function NAME (not after `func` for methods - methods can't have
//     their own type params).
//   - Explicit instantiation `Map[int, string](xs, f)` looks like an index
//     expression followed by a call; `Sum[float64]` alone is a function
//     VALUE (instantiation without call).
//   - `a, b = w < x, y > (z)` - in Go 1.18 this remains a pair of
//     comparisons: the grammar resolves `<` as an operator since Go has no
//     angle-bracket generics.
//   - `any` is a predeclared alias for interface{}; it can be shadowed.
//   - Ambiguity `f(a[b](c))` - index then call, or instantiation then call:
//     only resolvable with type information.
package main

import (
	"fmt"
	"strconv"
	"strings"
)

func Map[T, U any](xs []T, f func(T) U) []U {
	out := make([]U, 0, len(xs))
	for _, x := range xs {
		out = append(out, f(x))
	}
	return out
}

func Filter[T any](xs []T, keep func(T) bool) []T {
	var out []T
	for _, x := range xs {
		if keep(x) {
			out = append(out, x)
		}
	}
	return out
}

func Reduce[T, A any](xs []T, init A, f func(A, T) A) A {
	acc := init
	for _, x := range xs {
		acc = f(acc, x)
	}
	return acc
}

func Keys[K comparable, V any](m map[K]V) []K {
	out := make([]K, 0, len(m))
	for k := range m {
		out = append(out, k)
	}
	return out
}

func Contains[T comparable](xs []T, v T) bool {
	for _, x := range xs {
		if x == v {
			return true
		}
	}
	return false
}

func Zero[T any]() T {
	var z T
	return z
}

func Pair[A, B any](a A, b B) struct {
	First  A
	Second B
} {
	return struct {
		First  A
		Second B
	}{a, b}
}

func main() {
	nums := []int{1, 2, 3, 4, 5, 6}
	strs := Map(nums, strconv.Itoa)                                  // inferred T=int, U=string
	squares := Map[int, int](nums, func(n int) int { return n * n }) // explicit
	evens := Filter(nums, func(n int) bool { return n%2 == 0 })
	sum := Reduce(nums, 0, func(a, n int) int { return a + n })
	joined := Reduce(strs, "", func(a string, s string) string { return a + s })
	fmt.Println("Map:", strings.Join(strs, ","), "squares:", squares, "evens:", evens, "sum:", sum, "joined:", joined)

	toUpper := Map[string, string] // instantiation without a call
	fmt.Println("instantiated func value:", toUpper([]string{"a", "b"}, strings.ToUpper))

	m := map[string]int{"x": 1}
	fmt.Println("Keys:", Keys(m), "Contains:", Contains(strs, "3"), Contains([]float64{1.5}, 2))
	fmt.Printf("Zero[int]=%d Zero[string]=%q Zero[*int]=%v\n", Zero[int](), Zero[string](), Zero[*int]())
	p := Pair("answer", 42)
	fmt.Println("Pair:", p.First, p.Second)

	// `<` and `>` are always comparison operators in Go
	w, x, y, z := 1, 2, 3, 4
	a, b := w < x, y > (z)
	fmt.Println("comparisons, not generics:", a, b)

	// `any` can be shadowed like any predeclared identifier
	{
		any := "shadowed"
		fmt.Println("any =", any)
	}
}
