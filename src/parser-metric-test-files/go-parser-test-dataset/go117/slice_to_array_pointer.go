// Feature : Conversion from slice to array pointer, unsafe.Add and
//
//	unsafe.Slice
//
// Version : Go 1.17 (August 2021)
// Spec    : Go 1.17 release notes "Changes to the language"; spec
//
//	"Conversions from slice to array or array pointer"; proposal
//	#395
//
// `(*[4]int)(s)` converts a slice to a pointer to its underlying array
// (panics if len(s) < 4). Direct slice-to-ARRAY conversion `[4]int(s)` is
// Go 1.20 (see go120/).
//
// Parser edge cases:
//   - `(*[N]T)(expr)` - the type MUST be parenthesized because `*[N]T(expr)`
//     parses as `*([N]T(expr))` (dereference of a conversion).
//   - unsafe.Add(ptr, len) and unsafe.Slice(ptr, len) are builtins-like
//     functions in package unsafe.
//   - Also in 1.17: module graph pruning and `//go:build` lines (see
//     build-tags/) became the preferred constraint syntax.
package main

import (
	"fmt"
	"unsafe"
)

func firstFour(s []int) *[4]int {
	return (*[4]int)(s) // panics if len(s) < 4
}

func main() {
	s := []int{10, 20, 30, 40, 50}
	p := firstFour(s)
	p[0] = 99 // writes through to the slice's backing array
	fmt.Println("array pointer:", *p, "slice now:", s)

	// Zero-length conversion of a nil slice yields a nil pointer
	var empty []byte
	z := (*[0]byte)(empty)
	fmt.Println("nil slice -> nil *[0]byte:", z == nil)

	// Panic when the slice is too short
	func() {
		defer func() { fmt.Println("recovered:", recover()) }()
		_ = (*[8]int)(s)
	}()

	// unsafe.Add and unsafe.Slice
	arr := [5]int32{1, 2, 3, 4, 5}
	base := unsafe.Pointer(&arr[0])
	third := (*int32)(unsafe.Add(base, 2*unsafe.Sizeof(arr[0])))
	fmt.Println("unsafe.Add -> arr[2] =", *third)

	view := unsafe.Slice(&arr[1], 3)
	fmt.Println("unsafe.Slice(&arr[1], 3) =", view, "len", len(view))

	// The parenthesization matters: *[2]int(x) would be a different parse
	pair := (*[2]int)(s[1:3])
	fmt.Println("subslice to array pointer:", *pair)
}
