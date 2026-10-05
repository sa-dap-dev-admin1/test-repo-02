// Feature : Direct slice-to-array conversion, `comparable` satisfied by
//
//	interfaces and other non-strictly-comparable types,
//	unsafe.SliceData / String / StringData, errors.Join / %w twice
//
// Version : Go 1.20 (February 2023)
// Spec    : Go 1.20 release notes "Changes to the language"; proposal #46505
//
//	(slice to array), #56548 (comparable)
//
// Parser edge cases:
//   - `[4]int(s)` - conversion to an ARRAY type (1.17 only allowed array
//     POINTER). Composite-literal ambiguity: `[4]int{...}` is a literal,
//     `[4]int(s)` is a conversion.
//   - `[...]T(s)` is NOT allowed (length must be explicit).
//   - Generic `[K comparable]` instantiated with `any` - type-checker rule.
//   - fmt.Errorf with multiple %w verbs.
package main

import (
	"errors"
	"fmt"
	"unsafe"
)

func Index[K comparable](xs []K, target K) int {
	for i, x := range xs {
		if x == target {
			return i
		}
	}
	return -1
}

func main() {
	s := []byte("hello, go 1.20")
	arr := [5]byte(s) // copies the first five elements
	arr[0] = 'J'
	fmt.Printf("array conversion: %s (slice unchanged: %s)\n", arr[:], s[:5])

	lit := [3]int{1, 2, 3}             // composite literal
	conv := [3]int([]int{7, 8, 9, 10}) // conversion
	fmt.Println("literal vs conversion:", lit, conv)

	func() {
		defer func() { fmt.Println("recovered:", recover()) }()
		_ = [10]byte(s[:3])
	}()

	// comparable now accepts interface types such as any
	things := []any{1, "two", 3.0}
	fmt.Println("Index[any]:", Index(things, any("two")))

	str := "unsafe string"
	data := unsafe.StringData(str)
	back := unsafe.String(data, len(str))
	bs := []byte{'a', 'b'}
	fmt.Println("unsafe.String roundtrip:", back, "SliceData:", *unsafe.SliceData(bs) == 'a')

	errA := errors.New("disk full")
	errB := errors.New("retry exhausted")
	joined := errors.Join(errA, errB)
	wrapped := fmt.Errorf("save failed: %w; %w", errA, errB)
	fmt.Println("errors.Is joined:", errors.Is(joined, errB), "double %w:", errors.Is(wrapped, errA), errors.Is(wrapped, errB))
}
