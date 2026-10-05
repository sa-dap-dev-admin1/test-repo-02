// Feature : //go:embed directive and the embed package
// Version : Go 1.16 (February 2021)
// Spec    : Go 1.16 release notes "Embedded Files"; package embed docs;
//
//	proposal #41191
//
// A `//go:embed pattern` comment directly above a package-level var of
// type string, []byte, or embed.FS makes the compiler embed files.
// This file embeds ITSELF so the dataset stays self-contained.
//
// Parser edge cases:
//   - `//go:embed` is a COMMENT with semantic meaning: no space after //,
//     and it must sit in the comment block directly above the var
//     declaration (only blank lines and line comments may come between).
//   - Requires `import "embed"` (blank import `_ "embed"` for string/[]byte).
//   - Patterns: quoted strings with spaces, multiple patterns per line,
//     directories, `all:` prefix to include dotfiles.
//   - Not allowed on local variables or inside functions.
package main

import (
	"bytes"
	"embed"
	"fmt"
	"io/fs"
	"strings"
)

//go:embed embed_directive.go
var selfSource string

//go:embed embed_directive.go
var selfBytes []byte

//go:embed "embed_directive.go"
var sourceFS embed.FS

func main() {
	firstLine := strings.SplitN(selfSource, "\n", 2)[0]
	fmt.Println("first line of embedded source:", firstLine)
	fmt.Println("string and []byte agree:", bytes.Equal([]byte(selfSource), selfBytes))
	fmt.Println("lines embedded:", strings.Count(selfSource, "\n"))

	entries, err := fs.ReadDir(sourceFS, ".")
	if err != nil {
		panic(err)
	}
	for _, e := range entries {
		info, _ := e.Info()
		fmt.Printf("embed.FS entry: %s (%d bytes, dir=%v)\n", e.Name(), info.Size(), e.IsDir())
	}
	data, _ := sourceFS.ReadFile("embed_directive.go")
	fmt.Println("embed.FS ReadFile contains directive:", bytes.Contains(data, []byte("//go:"+"embed")))
}
