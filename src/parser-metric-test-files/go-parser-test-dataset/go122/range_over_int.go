// Feature : `for range n` over an integer
// Version : Go 1.22 (February 2024)
// Spec    : Go 1.22 release notes; spec "For statements with range clause"
//
//	(integer range expression); proposal #61405
//
// `for i := range 10` iterates i = 0..9. `for range 10` repeats 10 times.
// The type of i is the type of n (or int for an untyped constant).
//
// Parser edge cases:
//   - `range` followed by an INTEGER expression - grammatically identical to
//     ranging over a slice; only the type checker distinguishes it.
//   - `for range n` with NO iteration variables (allowed for all range
//     forms since Go 1.4, but newly common here).
//   - Typed integers: `for i := range uint8(3)` gives i of type uint8.
//   - range over a negative or zero n runs zero iterations.
//   - Only ONE iteration variable is allowed for integer ranges.
package main

import "fmt"

type Level int

func main() {
	fmt.Print("range 5:")
	for i := range 5 {
		fmt.Print(" ", i)
	}
	fmt.Println()

	count := 0
	for range 3 {
		count++
	}
	fmt.Println("for range 3 (no variable) ran", count, "times")

	var lv Level = 3
	for l := range lv {
		fmt.Printf("level %d has type %T\n", l, l)
	}

	for i := range uint8(2) {
		fmt.Printf("uint8 range: %d (%T)\n", i, i)
	}

	n := -4
	ran := false
	for range n {
		ran = true
	}
	fmt.Println("negative n ran body:", ran)

	// Nested integer ranges building a multiplication table
	for row := range 4 {
		for col := range 4 {
			fmt.Printf("%3d", (row+1)*(col+1))
		}
		fmt.Println()
	}

	// range over a constant expression and a len() call
	words := []string{"x", "y", "z"}
	for i := range len(words) - 1 {
		fmt.Println("pair:", words[i], words[i+1])
	}
}
