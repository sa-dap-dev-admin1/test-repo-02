// Feature : Implicit build constraints from the FILE NAME suffix
//
//	(_GOOS.go, _GOARCH.go, _GOOS_GOARCH.go, _test.go)
//
// Version : Go 1.0+ (file-name constraints predate //go:build)
// Spec    : cmd/go "Build constraints" - "If a file's name, after
//
//	stripping the extension and a possible _test suffix, matches
//	*_GOOS, *_GOARCH, or *_GOOS_GOARCH ..."
//
// This file has NO //go:build line, yet it only builds when GOOS=linux.
//
// EXPECTED:
//   - Pure syntax parser            -> PASS (it is valid Go on any OS)
//   - Build-aware parser on linux   -> PASS / included in the package
//   - Build-aware parser elsewhere  -> file is EXCLUDED (not an error)
//
// Parser edge cases:
//   - The suffix rule applies to the name only: `linux.go` alone is NOT
//     constrained; `foo_linux.go` and `foo_linux_amd64.go` are.
//   - Known GOOS values include "unix"? NO - `unix` is a //go:build tag
//     (Go 1.19) but is NOT recognized as a file-name suffix.
package main

import (
	"fmt"
	"os"
	"runtime"
	"strings"
)

func main() {
	fmt.Println("this file only builds on linux; GOOS =", runtime.GOOS)
	data, err := os.ReadFile("/proc/self/status")
	if err != nil {
		fmt.Println("procfs unavailable:", err)
		return
	}
	for _, line := range strings.Split(string(data), "\n") {
		if strings.HasPrefix(line, "Name:") || strings.HasPrefix(line, "Threads:") {
			fmt.Println(" ", strings.Join(strings.Fields(line), " "))
		}
	}
}
