// Feature : Loop-variable semantics controlled by go.mod (`go 1.21`)
// Version : Built with Go 1.22+ toolchain, language version 1.21
// Spec    : Go 1.22 release notes "Changes to the language"; go.dev/ref/mod
//
//	"go directive"; go.dev/wiki/LoopvarExperiment
//
// Run from this directory:  go run .
//
// EXPECTED OUTPUT (because go.mod says go 1.21):
//
//	closures: 3 3 3
//	range-for-int: compile error if uncommented (needs go1.22)
//
// Parser edge cases:
//   - Same text as go122/per_iteration_loop_variables.go but different
//     meaning. A semantic-aware parser must read the module's go directive.
//   - `for i := range 3` would FAIL to compile here: range-over-int
//     requires language version go1.22.
package main

import "fmt"

func main() {
	var funcs []func() int
	for i := 0; i < 3; i++ {
		funcs = append(funcs, func() int { return i })
	}
	fmt.Print("closures:")
	for _, f := range funcs {
		fmt.Print(" ", f())
	}
	fmt.Println(" (shared loop variable under go 1.21)")

	// for i := range 3 { fmt.Println(i) }   // error: requires go1.22 or later
}
