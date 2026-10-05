// Feature : Constraint interfaces with type sets - union elements (|),
//
//	approximation elements (~T), methods + type sets combined
//
// Version : Go 1.18
// Spec    : spec "Interface types - General interfaces", "Type sets";
//
//	Type Parameters Proposal "Type sets of constraints"
//
// Parser edge cases:
//   - `~int | ~int64 | float64` - union of terms inside an interface body;
//     `~` is a NEW token meaning "underlying type".
//   - Inline constraint literal in a type parameter list:
//     `func F[T interface{ ~int | ~string }]`, and the shorthand
//     `func F[T ~int | ~string]` (interface{} omitted).
//   - `[P *C]` ambiguity in a GENERIC TYPE declaration: `type T[P *C] ...`
//     could be an array type `[P * C]`; write `[P interface{ *C }]` or
//     `[P *C,]` (trailing comma) to disambiguate.
//   - General interfaces (with type terms) can only be used as constraints,
//     not as ordinary variable types.
package main

import (
	"fmt"
	"strings"
)

type Integer interface {
	~int | ~int8 | ~int16 | ~int32 | ~int64
}

type Float interface{ ~float32 | ~float64 }

type Number interface {
	Integer | Float
}

type Stringish interface {
	~string
	Len() int // type set AND a method requirement
}

type Celsius float64
type Meters int
type Name string

func (n Name) Len() int { return len(n) }

func Sum[T Number](xs ...T) T {
	var s T
	for _, x := range xs {
		s += x
	}
	return s
}

func Max[T ~int | ~float64 | ~string](a, b T) T { // shorthand inline constraint
	if a > b {
		return a
	}
	return b
}

func Clamp[T interface{ ~int | ~float64 }](v, lo, hi T) T { // explicit interface literal
	return Max(lo, min2(v, hi))
}

func min2[T ~int | ~float64](a, b T) T {
	if a < b {
		return a
	}
	return b
}

func Describe[S Stringish](s S) string {
	return fmt.Sprintf("%q has %d bytes (Len()=%d)", strings.ToUpper(string(s)), len(s), s.Len())
}

// Pointer-method constraint pattern; the trailing comma in [T any, PT ...]
// is not needed here, but `[P *C,]` would be for a single *C param.
type Setter[T any] interface {
	*T
	Set(string)
}

type Config struct{ value string }

func (c *Config) Set(v string) { c.value = v }

func FromStrings[T any, PT Setter[T]](vals []string) []T {
	out := make([]T, len(vals))
	for i, v := range vals {
		PT(&out[i]).Set(v)
	}
	return out
}

// Generic type whose single parameter needs the trailing-comma disambiguation
type PtrBox[P *Config,] struct{ p P }

func main() {
	fmt.Println("Sum ints:", Sum(1, 2, 3), "Sum floats:", Sum(0.5, 0.25))
	fmt.Println("Sum Celsius (~float64):", Sum(Celsius(20.5), Celsius(1.5)))
	fmt.Println("Sum Meters (~int):", Sum[Meters](100, 250))
	fmt.Println("Max:", Max(3, 9), Max("apple", "pear"), Max(Celsius(5), Celsius(-5)))
	fmt.Println("Clamp:", Clamp(15, 0, 10), Clamp(-2.5, 0, 1))
	fmt.Println("Describe:", Describe(Name("gopher")))
	cfgs := FromStrings[Config]([]string{"a", "b"})
	fmt.Println("pointer-method constraint:", cfgs[0].value, cfgs[1].value)
	box := PtrBox[*Config]{p: &cfgs[0]}
	fmt.Println("PtrBox holds:", box.p.value)
}
