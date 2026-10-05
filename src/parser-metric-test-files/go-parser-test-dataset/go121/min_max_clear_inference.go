// Feature : Built-in min, max and clear; improved generic type inference;
//
//	package-initialization order rules; slices/maps/cmp packages
//
// Version : Go 1.21 (August 2023)
// Spec    : Go 1.21 release notes "Changes to the language"; spec
//
//	"Min and max", "Clear", "Type inference" (rewritten)
//
// Parser edge cases:
//   - min/max/clear are PREDECLARED FUNCTIONS (not keywords) - they can be
//     shadowed: `min := 3` is legal, and user funcs named min still work.
//   - min/max accept any number (>= 1) of ordered arguments of the same
//     type, including untyped constants: `max(1, 2.5)` is a constant.
//   - Inference of type arguments from ASSIGNMENT context and from generic
//     function arguments passed to other generic functions (1.21).
package main

import (
	"cmp"
	"fmt"
	"maps"
	"slices"
	"strings"
)

type Version struct{ Major, Minor int }

func compareVersions(a, b Version) int {
	return cmp.Or(cmp.Compare(a.Major, b.Major), cmp.Compare(a.Minor, b.Minor))
}

// Generic function used as an argument to another generic function
func Identity[T any](v T) T { return v }
func Apply[T, U any](xs []T, f func(T) U) []U {
	out := make([]U, len(xs))
	for i, x := range xs {
		out[i] = f(x)
	}
	return out
}

func main() {
	fmt.Println("min/max ints:", min(3, 1, 2), max(3, 1, 2))
	fmt.Println("min/max strings:", min("pear", "apple"), max("pear", "apple"))
	const c = max(1, 2.5, 2) // constant expression with untyped constants
	fmt.Println("constant max:", c)
	fmt.Println("min with NaN-free floats:", min(2.5, -1.25))

	m := map[string]int{"a": 1, "b": 2}
	s := []int{4, 5, 6}
	clear(m) // deletes all entries
	clear(s) // zeroes elements, keeps length
	fmt.Println("after clear: map len", len(m), "slice", s)

	vs := []Version{{1, 21}, {1, 9}, {2, 0}, {1, 10}}
	slices.SortFunc(vs, compareVersions)
	fmt.Println("slices.SortFunc:", vs)
	fmt.Println("slices.Contains / Index:", slices.Contains(vs, Version{2, 0}), slices.Index(vs, Version{1, 10}))
	fmt.Println("slices.Max:", slices.MaxFunc(vs, compareVersions))

	src := map[string]int{"x": 1, "y": 2}
	dst := maps.Clone(src)
	dst["z"] = 3
	fmt.Println("maps.Clone independent:", len(src), len(dst), "equal:", maps.Equal(src, dst))

	// 1.21 inference: Identity's T inferred from Apply's parameter
	words := Apply([]string{"go", "1.21"}, Identity)
	upper := Apply(words, strings.ToUpper)
	fmt.Println("generic func as argument:", words, upper)

	// min/max/clear are not keywords
	{
		min := "shadowed min"
		fmt.Println(min, max(10, 20))
	}
}
