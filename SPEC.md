## Synacor OSCON 2012 Challenge
In this challenge, your job is to use this architecture spec to create a
virtual machine capable of running the included binary.  Along the way,
you will find codes; submit these to the challenge website to track
your progress.  Good luck!

## Architecture
- three storage regions
  - memory with 15-bit address space storing 16-bit values
  - eight registers
  - an unbounded stack which holds individual 16-bit values
- all numbers are unsigned integers 0..32767 (15-bit)
- all math is modulo 32768; 32758 + 15 => 5

## Binary Format
- each number is stored as a 16-bit little-endian pair (low byte, high byte)
- numbers 0..32767 mean a literal value
- numbers 32768..32775 instead mean registers 0..7
- numbers 32776..65535 are invalid
- programs are loaded into memory starting at address 0
- address 0 is the first 16-bit value, address 1 is the second 16-bit value, etc

## Execution
- After an operation is executed, the next instruction to read is immediately after the last argument of the current operation.  If a jump was performed, the next operation is instead the exact destination of the jump.
- Encountering a register as an operation argument should be taken as reading from the register or setting into the register as appropriate.

## Hints
- Start with operations 0, 19, and 21.
- Here's a code for the challenge website: LDOb7UGhTi
- The program "9,32768,32769,4,19,32768" occupies six memory addresses and should:
  - Store into register 0 the sum of 4 and the value contained in register 1.
  - Output to the terminal the character with the ascii code contained in register 0.

## Opcode Listing
halt: 0
  stop execution and terminate the program

set: 1 a b
  set register &lt;a> to the value of &lt;b>

push: 2 a
  push &lt;a> onto the stack

pop: 3 a
  remove the top element from the stack and write it into &lt;a>; empty stack = error

eq: 4 a b c
  set &lt;a> to 1 if &lt;b> is equal to &lt;c>; set it to 0 otherwise

gt: 5 a b c
  set &lt;a> to 1 if &lt;b> is greater than &lt;c>; set it to 0 otherwise

jmp: 6 a
  jump to &lt;a>

jt: 7 a b
  if &lt;a> is nonzero, jump to &lt;b>

jf: 8 a b
  if &lt;a> is zero, jump to &lt;b>

add: 9 a b c
  assign into &lt;a> the sum of &lt;b> and &lt;c> (modulo 32768)

mult: 10 a b c
  store into &lt;a> the product of &lt;b> and &lt;c> (modulo 32768)

mod: 11 a b c
  store into &lt;a> the remainder of &lt;b> divided by &lt;c>

and: 12 a b c
  stores into &lt;a> the bitwise and of &lt;b> and &lt;c>

or: 13 a b c
  stores into &lt;a> the bitwise or of &lt;b> and &lt;c>

not: 14 a b
  stores 15-bit bitwise inverse of &lt;b> in &lt;a>

rmem: 15 a b
  read memory at address &lt;b> and write it to &lt;a>

wmem: 16 a b
  write the value from &lt;b> into memory at address &lt;a>

call: 17 a
  write the address of the next instruction to the stack and jump to &lt;a>

ret: 18
  remove the top element from the stack and jump to it; empty stack = halt

out: 19 a
  write the character represented by ascii code &lt;a> to the terminal

in: 20 a
  read a character from the terminal and write its ascii code to &lt;a>; it can be assumed that once input starts, it will continue until a newline is encountered; this means that you can safely read whole lines from the keyboard instead of having to figure out how to read individual characters

noop: 21
  no operation