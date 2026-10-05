// Feature : Doc comment syntax (links, lists, headings) and typed atomics
// Version : Go 1.19 (August 2022)
// Spec    : Go 1.19 release notes "Doc Comments", "sync/atomic";
//
//	go.dev/doc/comment; proposal #51082
//
// Go 1.19 made no grammar changes; it formalized DOC COMMENT syntax (which
// gofmt now reformats) and added generic-free typed atomics. A parser that
// extracts documentation must recognize these comment forms.
//
// Parser edge cases (doc-comment level):
//   - `# Heading` lines inside a doc comment.
//   - `[Name]` and `[pkg.Name]` doc links, `[text]: URL` link definitions.
//   - Lists: lines starting with "-", "*", "+", or "1." after indentation.
//   - Code blocks: indented lines inside the comment.
//   - Directive comments `//go:...` at the END of a doc comment are not part
//     of the documentation text.
package main

import (
	"fmt"
	"go/doc/comment"
	"sync"
	"sync/atomic"
)

// Counter is a concurrency-safe counter.
//
// # Usage
//
// Create one with [NewCounter] and call [Counter.Inc] from any goroutine.
// The value is stored in an [atomic.Int64]; see the [memory model].
//
// Guarantees:
//   - increments are never lost
//   - Load observes all prior increments
//     from the same goroutine
//
// Example:
//
//	c := NewCounter()
//	c.Inc()
//
// [memory model]: https://go.dev/ref/mem
type Counter struct {
	n    atomic.Int64
	flag atomic.Bool
	last atomic.Pointer[string]
}

// NewCounter returns a zeroed [Counter].
func NewCounter() *Counter { return &Counter{} }

// Inc adds one and returns the new value. The directive below ends the
// doc comment and is NOT part of the rendered documentation.
//
//go:noinline
func (c *Counter) Inc() int64 { return c.n.Add(1) }

func main() {
	c := NewCounter()
	var wg sync.WaitGroup
	for i := 0; i < 50; i++ {
		wg.Add(1)
		go func() { defer wg.Done(); c.Inc() }()
	}
	wg.Wait()
	label := "done"
	c.last.Store(&label)
	c.flag.Store(true)
	fmt.Println("atomic.Int64:", c.n.Load(), "atomic.Bool:", c.flag.Load(), "atomic.Pointer:", *c.last.Load())

	// Parse a doc comment with the Go 1.19 go/doc/comment package
	var p comment.Parser
	doc := p.Parse("# Title\n\nSee [Counter].\n\n  - one\n  - two\n")
	var pr comment.Printer
	fmt.Print(string(pr.Markdown(doc)))
}
