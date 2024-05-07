# Changelog

## [2.0.0](https://github.com/voidpointer0x00/group-whitelist/compare/1.1.1...2.0.0) (2024-05-07)


### ⚠ BREAKING CHANGES

* change configuration extension to `.conf`
* switch to hocon configuration with comment support
* rename `shouldDropPing`->`hideStatus`

### ref

* change configuration extension to `.conf` ([d6b2f70](https://github.com/voidpointer0x00/group-whitelist/commit/d6b2f70c2200e5d2155cf8adfe9882d48e500bef))
* rename `shouldDropPing`-&gt;`hideStatus` ([6fa02cb](https://github.com/voidpointer0x00/group-whitelist/commit/6fa02cb470269b903a1090b9636e25600c9424a9))


### Features

* authentication using secret keys ([5b5ab55](https://github.com/voidpointer0x00/group-whitelist/commit/5b5ab55594d26c62c094534b2a313118824aeea3))
* switch to hocon configuration with comment support ([2d7cd2d](https://github.com/voidpointer0x00/group-whitelist/commit/2d7cd2d5cff280f743fcb347e125371dcfd32166))


### Bug Fixes

* no longer call `setWhitelistConfig()` on locale reload ([6c86304](https://github.com/voidpointer0x00/group-whitelist/commit/6c86304232ecdbaf13d0b6054b1a5664bd722942))

## [1.1.1](https://github.com/voidpointer0x00/group-whitelist/compare/1.1.0...1.1.1) (2024-05-05)


### Bug Fixes

* **build:** not a git repository ([b5c8415](https://github.com/voidpointer0x00/group-whitelist/commit/b5c8415943aead8c9f7eabced2f931fa4754c393))


### Reverts

* release 1.1.0 ([85047df](https://github.com/voidpointer0x00/group-whitelist/commit/85047df132517b8d2a34fb1299e619abbcef7315))
* the different approach didn't go exactly as planned ([b590b23](https://github.com/voidpointer0x00/group-whitelist/commit/b590b23c32b89d85e29aeee94f3386a057c09adc))

## [1.1.0](https://github.com/voidpointer0x00/group-whitelist/compare/1.0.0...1.1.0) (2024-05-05)


### Features

* optionally, don't respond to ping packets ([a5723e5](https://github.com/voidpointer0x00/group-whitelist/commit/a5723e55b04a5043da1579988da6ccc80fac174b))
