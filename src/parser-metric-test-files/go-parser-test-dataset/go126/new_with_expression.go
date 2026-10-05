// Feature : `new(expr)` - the built-in new accepts an EXPRESSION operand
//
//	and returns a pointer to a variable initialized to its value;
//	generic types may refer to themselves in their own type
//	parameter list
//
// Version : Go 1.26 (February 2026)
// Spec    : Go 1.26 release notes "Changes to the language"; proposal
//
//	#45624 (new(expr)); proposal #68162 (self-referential generic
//	type constraints)
//
// VALIDATION: NOT compiled in the dataset build environment (Go 1.24).
// Verify with Go 1.26+:  go run new_with_expression.go
//
// Parser edge cases:
//   - `new(42)`, `new(time.Now())`, `new(cfg.Timeout * 2)` - previously
//     new's operand had to be a TYPE. The parser must accept either a type
//     or an expression, and some operands are ambiguous (`new(T)` where T
//     could be a type or a value) until name resolution.
//   - `type Adder[A Adder[A]] interface { Add(A) A }` - the type being
//     declared appears in its own constraint (an error before 1.26:
//     "invalid recursive type").
package main

import (
	"encoding/json"
	"fmt"
	"time"
)

type Config struct {
	Name    string     `json:"name"`
	Retries *int       `json:"retries,omitempty"`
	Ratio   *float64   `json:"ratio,omitempty"`
	Created *time.Time `json:"created,omitempty"`
}

type Adder[A Adder[A]] interface { // self-referential constraint (Go 1.26)
	Add(A) A
}

type Vec struct{ X, Y int }

func (v Vec) Add(o Vec) Vec { return Vec{v.X + o.X, v.Y + o.Y} }

func SumAll[A Adder[A]](zero A, xs ...A) A {
	acc := zero
	for _, x := range xs {
		acc = acc.Add(x)
	}
	return acc
}

func main() {
	base := 3
	cfg := Config{
		Name:    "parser",
		Retries: new(base * 2), // pointer to an int initialized to 6
		Ratio:   new(0.75),     // untyped constant -> *float64
		Created: new(time.Unix(0, 0).UTC()),
	}
	out, _ := json.Marshal(cfg)
	fmt.Println(string(out))
	fmt.Println("retries:", *cfg.Retries, "ratio:", *cfg.Ratio)

	p := new(int) // the classic type form still works
	*p = 10
	fmt.Println("new(int):", *p)

	fmt.Println("self-referential constraint:", SumAll(Vec{}, Vec{1, 2}, Vec{3, 4}))
}
