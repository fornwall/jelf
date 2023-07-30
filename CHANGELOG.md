# Change Log

## v0.9.0 (2023-07-30)
- Make BackingData public and add ElfFile.from(BackingData). [#20](https://github.com/fornwall/jelf/pull/20)

## v0.8.0 (2023-07-21)
- Add a `getData` method to `ElfSection` for getting the bytes of an ELF section. [#17](https://github.com/fornwall/jelf/issues/17)
- Make the `getSymbol` and `getSymbolIndex` methods in `ElfRelocation` respect `EI_CLASS`. [#18](https://github.com/fornwall/jelf/issues/18)
- Fix `getELFSymbol(symbolName)` not working in certain cases. [#19](https://github.com/fornwall/jelf/issues/19)

