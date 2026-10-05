# Back-end Code Style (codestyle.md)

**Base standard: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html),
with references to Oracle's Code Conventions for the Java Programming Language.**
A few project-specific conventions are added below.

## 1. Naming

- Class names use `UpperCamelCase`, e.g. `CalculatorService`, `ApiHandler`.
- Method and variable names use `lowerCamelCase`, e.g. `listHistory`, `dbPath`.
- Constants are `UPPER_SNAKE_CASE`, e.g. `CREATE_TABLE_SQL`, `DRIVER`.
- Package names are all lowercase, no underscores, e.g. `com.course.calculator.core`.
- Names should be descriptive. No meaningless names like `a1`, `tmp2` (loop counters such as `i` are fine).

## 2. Formatting

- Indent with 4 spaces; never tabs.
- Keep lines under 100 characters. Wrap before an operator when a line gets too long.
- Opening brace stays on the same line; closing brace goes on its own line.
- Separate methods and logical blocks with one blank line; never stack multiple blank lines.
- Order imports alphabetically by package. No wildcard imports (`import xxx.*`).

## 3. Comments

- Use Javadoc on classes and non-trivial methods, explaining *what* and *why* rather than restating the code line by line.
- Simple getters and obvious logic stay uncommented.
- Comments are updated together with the code; when code is removed, its comments go too.

## 4. Language usage

- Stick to Java 8 syntax for compatibility.
- Use `StringBuilder` when building long strings.
- All database access goes through `PreparedStatement`. Never concatenate SQL strings.
- Never evaluate user input with `eval`, reflection, or similar mechanisms.
- Catch exceptions and either handle them or rethrow with context. No empty catch blocks; if an exception is deliberately ignored, write why.

## 5. Class design

- One top-level class per file, with a single responsibility.
- Utility classes have a private constructor and cannot be instantiated.
- Fields of immutable data objects (e.g. `CalculationRecord`) are `final`.
- Controllers only deal with requests and responses; business logic lives in the service layer, database access in the db layer. No cross-layer logic.

## 6. Miscellaneous

- All source files are UTF-8.
- Before committing, the project must compile and the basic tests must pass.
- Build artifacts and database files are never committed (see `.gitignore`).
