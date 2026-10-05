// Feature : Embedding interfaces with overlapping method sets
// Version : Go 1.14 (February 2020)
// Spec    : Go 1.14 release notes "Changes to the language"; proposal
//
//	#6977 "permit embedding of interfaces with overlapping method
//	sets"; spec "Interface types - Embedded interfaces"
//
// Before 1.14, embedding two interfaces that both declare the same method
// (e.g. Close() error) was a compile error "duplicate method Close".
//
// Parser edge cases:
//   - Purely a TYPE-CHECKING change; syntax is unchanged. A parser that
//     performs semantic checks on interface method sets must not reject
//     duplicates that have IDENTICAL signatures.
//   - Duplicates with DIFFERENT signatures are still an error.
//   - Explicitly declared method + embedded interface with the same method.
package main

import (
	"fmt"
	"io"
	"strings"
)

type ReadCloser interface {
	io.Reader
	io.Closer
}

type WriteCloser interface {
	io.Writer
	io.Closer
}

// Both embedded interfaces declare Close() error - legal since Go 1.14
type ReadWriteCloser interface {
	ReadCloser
	WriteCloser
	Close() error // explicitly repeated as well
}

type memFile struct {
	buf    strings.Builder
	data   *strings.Reader
	closed bool
}

func (m *memFile) Read(p []byte) (int, error)  { return m.data.Read(p) }
func (m *memFile) Write(p []byte) (int, error) { return m.buf.Write(p) }
func (m *memFile) Close() error {
	if m.closed {
		return fmt.Errorf("already closed")
	}
	m.closed = true
	return nil
}

type Named interface{ Name() string }
type Labeled interface {
	Name() string
	Label() string
}

type Entity interface {
	Named
	Labeled // overlapping Name()
}

type user struct{ first, role string }

func (u user) Name() string  { return u.first }
func (u user) Label() string { return u.first + " (" + u.role + ")" }

func main() {
	f := &memFile{data: strings.NewReader("overlapping interfaces")}
	var rwc ReadWriteCloser = f

	p := make([]byte, 11)
	n, _ := rwc.Read(p)
	fmt.Printf("read %d bytes: %q\n", n, p[:n])
	fmt.Fprintf(rwc, "written via %s", "Fprintf")
	fmt.Println("buffer:", f.buf.String())
	fmt.Println("first close:", rwc.Close(), "second close:", rwc.Close())

	var e Entity = user{"ada", "admin"}
	fmt.Println(e.Name(), "/", e.Label())
}
