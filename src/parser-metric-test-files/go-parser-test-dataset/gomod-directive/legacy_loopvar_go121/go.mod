// Feature : The `go` directive in go.mod selects the LANGUAGE VERSION for
//           every file in the module. `go 1.21` keeps pre-1.22 loop
//           variable semantics even when built with a newer toolchain.
// Spec    : go.dev/ref/mod "go directive"; Go 1.22 release notes
// Parser edge case: identical source has different semantics depending on
// this file - a parser that ignores go.mod cannot model loop scoping.
module example.com/legacyloopvar

go 1.21
