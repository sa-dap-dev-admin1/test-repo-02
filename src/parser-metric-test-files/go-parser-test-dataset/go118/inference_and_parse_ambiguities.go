// Feature : Generic type inference edge cases and parse ambiguities
//
//	introduced by square-bracket type parameters
//
// Version : Go 1.18 (inference improved in 1.21: see go121/)
// Spec    : spec "Type inference"; Type Parameters Proposal "Notes /
//
//	Ambiguities"; go/parser issue #49482
//
// Parser edge cases:
//   - `x := T[int]{}`  - composite literal of an instantiated generic type.
//   - `a[b](c)`        - generic call vs index-then-call (needs types).
//   - `m[K, V]`        - multi-index: in Go only valid as instantiation.
//   - `type A[N int] struct{}` -> N is a type parameter constrained by int?
//     NO: `type A[N int]` is parsed as a generic type with parameter N and
//     constraint int. But `type A [N]int` (space, no constraint) is an
//     ARRAY type. The parser decides by looking for a constraint after
//     the first identifier.
//   - `func f[T interface{ ~[]E }, E any](s T)` - constraint mentions a
//     later type parameter (core type inference).
//   - Partial explicit instantiation: `Convert[float64](xs)` - remaining
//     params inferred.
package main

import "fmt"

const N = 3

type Arr [N]int // array type: N is a constant

type Gen[T any] struct{ v T } // generic type

type Matrix[T any] [N][N]T // generic type whose underlying type is an array

func First[S ~[]E, E any](s S) E { return s[0] } // E inferred from S's core type

func Convert[To, From ~int | ~float64](xs []From) []To {
	out := make([]To, len(xs))
	for i, x := range xs {
		out[i] = To(x)
	}
	return out
}

type MySlice []string

func Apply[T any](f func(T) T, v T) T { return f(v) }

func double(n int) int { return n * 2 }

func main() {
	var a Arr
	a[1] = 5
	g := Gen[int]{v: 42} // composite literal of instantiated type
	var m Matrix[string]
	m[2][2] = "corner"
	fmt.Println("array:", a, "generic literal:", g.v, "generic matrix:", m[2][2], len(m))

	fmt.Println("First with ~[]E inference:", First(MySlice{"x", "y"}), First([]float64{2.5}))

	floats := Convert[float64]([]int{1, 2, 3}) // partial instantiation: From inferred
	fmt.Printf("partial instantiation: %v (%T)\n", floats, floats)

	// Function arguments inferred from a non-generic func value
	fmt.Println("Apply(double, 21) =", Apply(double, 21))

	// Index-then-call on a slice of funcs vs generic call - same syntax
	funcs := []func(int) int{double}
	fmt.Println("index then call:", funcs[0](5), "generic call:", Apply[int](double, 5))

	// Map with generic value types
	cache := map[string]Gen[[]int]{"k": {v: []int{1, 2}}}
	fmt.Println("nested instantiation in map type:", cache["k"].v)
}
