// Feature : Number literal syntax - binary (0b), octal (0o), hex floats
//
//	(0x1p-2), imaginary suffix on any literal, digit separators (_)
//
// Version : Go 1.13 (September 2019)
// Spec    : Go 1.13 release notes "Changes to the language"; proposal
//
//	#19308 (binary/octal literals), #28493 (digit separators);
//	spec "Integer literals", "Floating-point literals"
//
// Parser edge cases:
//   - 0b1010, 0B1010, 0o17, 0O17 prefixes; legacy 0755 (leading zero) is
//     still octal.
//   - Hex floating point: 0x1.8p1, 0x.8p0, 0x1p-2 - the `p` exponent is
//     REQUIRED for hex floats (0x1.8 alone is invalid).
//   - Underscore separators: only BETWEEN digits or after a prefix:
//     1_000, 0x_FF, 0b_1010. Invalid: 1__0, 1_, _1 (that's an identifier!).
//   - Imaginary suffix `i` on any literal form: 0b101i, 0o17i, 0x10i, 1_0i.
//   - Signed shift counts (`x << n` with signed n) also arrived in 1.13.
package main

import (
	"fmt"
	"math"
)

const (
	KB = 1 << 10
	MB = 1_024 * KB
)

func main() {
	bin := 0b1010_1010
	oct := 0o755
	legacyOct := 0755
	hex := 0xDEAD_BEEF
	million := 1_000_000
	fmt.Println("binary:", bin, "octal:", oct, legacyOct, "hex:", hex, "million:", million)

	h1 := 0x1.8p1   // 1.5 * 2^1 = 3
	h2 := 0x1p-2    // 0.25
	h3 := 0x.8p0    // 0.5
	h4 := 0x_1F.Cp0 // 31.75
	fmt.Println("hex floats:", h1, h2, h3, h4)

	c1 := 0b101i
	c2 := 0o17i
	c3 := 0x10i
	c4 := 1_000i
	fmt.Println("imaginary:", c1, c2, c3, c4, real(3+c1), imag(c4))

	pi := 3.141_592_653
	avogadro := 6.022_140_76e23
	fmt.Printf("pi=%.9f avogadro=%.4e MB=%d\n", pi, avogadro, MB)

	// Signed shift count (Go 1.13): n is an int, not uint
	n := 3
	fmt.Println("1 << n (signed n):", 1<<n, "uint64 max bits:", uint64(math.MaxUint64)>>n)

	// `_1` is an identifier, not a number
	_1 := "identifier"
	fmt.Println("_1 is", _1)

	masks := []uint8{0b0000_0001, 0b0000_0010, 0b1000_0000}
	var combined uint8
	for _, m := range masks {
		combined |= m
	}
	fmt.Printf("combined mask = %08b (%#o, %#x)\n", combined, combined, combined)
}
