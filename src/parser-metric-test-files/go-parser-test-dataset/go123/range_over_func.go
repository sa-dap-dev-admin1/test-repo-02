// Feature : Range-over-func iterators - ranging over functions of type
//
//	func(yield func() bool), func(yield func(K) bool),
//	func(yield func(K, V) bool)
//
// Version : Go 1.23 (August 2024)
// Spec    : Go 1.23 release notes "Changes to the language"; spec "For
//
//	statements with range clause" (function iterators); proposal
//	#61405; go.dev/blog/range-functions
//
// Parser edge cases:
//   - `for x := range seq` where seq is a FUNCTION value - same syntax as
//     ranging over a slice.
//   - `for k, v := range f` with 2 variables, `for range f` with 0.
//   - `break`, `continue`, `return` and `goto` out of the loop body must be
//     translated into the yield function returning false.
//   - `defer` inside the loop body runs when the ENCLOSING function returns.
//   - Generic iterator constructors returning func(yield func(T) bool).
package main

import "fmt"

func Count(n int) func(yield func(int) bool) {
	return func(yield func(int) bool) {
		for i := 0; i < n; i++ {
			if !yield(i) {
				return
			}
		}
	}
}

func Enumerate[T any](xs []T) func(yield func(int, T) bool) {
	return func(yield func(int, T) bool) {
		for i, x := range xs {
			if !yield(i, x) {
				return
			}
		}
	}
}

func Fibonacci() func(func(int) bool) { // infinite iterator
	return func(yield func(int) bool) {
		a, b := 0, 1
		for {
			if !yield(a) {
				return
			}
			a, b = b, a+b
		}
	}
}

func Times(n int) func(func() bool) { // zero-argument yield
	return func(yield func() bool) {
		for range n {
			if !yield() {
				return
			}
		}
	}
}

type Tree struct {
	Left, Right *Tree
	Val         int
}

func (t *Tree) InOrder(yield func(int) bool) bool {
	if t == nil {
		return true
	}
	return t.Left.InOrder(yield) && yield(t.Val) && t.Right.InOrder(yield)
}

func (t *Tree) All() func(func(int) bool) {
	return func(yield func(int) bool) { t.InOrder(yield) }
}

func firstOver(limit int) int {
	for f := range Fibonacci() {
		if f > limit {
			return f // return from inside a range-over-func loop
		}
	}
	return -1
}

func main() {
	fmt.Print("Count(4):")
	for i := range Count(4) {
		fmt.Print(" ", i)
	}
	fmt.Println()

	for i, s := range Enumerate([]string{"a", "b", "c", "d"}) {
		if i == 1 {
			continue
		}
		if s == "d" {
			break
		}
		fmt.Println("enumerate:", i, s)
	}

	hellos := 0
	for range Times(3) {
		hellos++
	}
	fmt.Println("Times(3) ran", hellos, "times")
	fmt.Println("first Fibonacci over 1000:", firstOver(1000))

	tree := &Tree{Val: 5, Left: &Tree{Val: 2, Right: &Tree{Val: 3}}, Right: &Tree{Val: 8}}
	fmt.Print("tree in-order:")
	for v := range tree.All() {
		fmt.Print(" ", v)
	}
	fmt.Println()

outer:
	for i := range Count(3) {
		for j := range Count(3) {
			if j == 2 {
				continue outer // labeled continue across nested iterators
			}
			fmt.Print("(", i, ",", j, ")")
		}
	}
	fmt.Println()
}
