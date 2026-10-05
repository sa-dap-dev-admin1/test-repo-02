// Feature : Generic types (structs, slices, maps, interfaces), methods on
//
//	generic types, generic linked structures, instantiated
//	embedded fields
//
// Version : Go 1.18
// Spec    : spec "Type definitions" (with type parameters), "Method
//
//	declarations" (receiver type parameters)
//
// Parser edge cases:
//   - `type Stack[T any] struct { ... }` - type parameters on a TYPE.
//   - Method receivers re-declare the parameters by NAME only:
//     `func (s *Stack[T]) Push(v T)`; names may differ from the type
//     declaration (`func (s *Stack[E]) Len()` is fine); `_` allowed.
//   - Methods cannot declare additional type parameters.
//   - Recursive generic types: `type Node[T any] struct{ next *Node[T] }`.
//   - Embedded instantiated type: `struct { List[int]; ... }`.
//   - Generic type alias was NOT allowed until Go 1.24 (see go124/).
package main

import (
	"cmp"
	"fmt"
	"sort"
	"strings"
)

type Stack[T any] struct{ items []T }

func (s *Stack[T]) Push(v T) { s.items = append(s.items, v) }
func (s *Stack[T]) Pop() (T, bool) {
	var zero T
	if len(s.items) == 0 {
		return zero, false
	}
	v := s.items[len(s.items)-1]
	s.items = s.items[:len(s.items)-1]
	return v, true
}
func (s *Stack[E]) Len() int   { return len(s.items) } // different parameter name
func (s Stack[_]) Empty() bool { return len(s.items) == 0 }

type Node[T any] struct {
	Value T
	Next  *Node[T]
}

type List[T any] struct {
	head *Node[T]
	size int
}

func (l *List[T]) Prepend(v T) { l.head = &Node[T]{v, l.head}; l.size++ }
func (l *List[T]) Each(f func(T)) {
	for n := l.head; n != nil; n = n.Next {
		f(n.Value)
	}
}

type Pair[K cmp.Ordered, V any] struct {
	Key K
	Val V
}

type OrderedMap[K cmp.Ordered, V any] map[K]V

func (m OrderedMap[K, V]) Sorted() []Pair[K, V] {
	out := make([]Pair[K, V], 0, len(m))
	for k, v := range m {
		out = append(out, Pair[K, V]{k, v})
	}
	sort.Slice(out, func(i, j int) bool { return out[i].Key < out[j].Key })
	return out
}

type Container[T any] interface {
	Len() int
	Push(T)
}

type IntHistory struct {
	List[int] // embedded instantiated generic type
	label     string
}

func Fill[T any](c Container[T], vals ...T) {
	for _, v := range vals {
		c.Push(v)
	}
}

func main() {
	var s Stack[string]
	Fill[string](&s, "a", "b", "c")
	top, _ := s.Pop()
	fmt.Println("Stack: popped", top, "len", s.Len(), "empty", s.Empty())

	var l List[float64]
	for _, v := range []float64{1.5, 2.5, 3.5} {
		l.Prepend(v)
	}
	var parts []string
	l.Each(func(v float64) { parts = append(parts, fmt.Sprint(v)) })
	fmt.Println("List:", strings.Join(parts, " -> "), "size", l.size)

	om := OrderedMap[string, int]{"pear": 3, "apple": 7, "fig": 1}
	for _, p := range om.Sorted() {
		fmt.Printf("  %s=%d\n", p.Key, p.Val)
	}

	h := IntHistory{label: "history"}
	h.Prepend(1)
	h.Prepend(2) // promoted method from embedded List[int]
	fmt.Println(h.label, "size via embedded generic:", h.size)
}
