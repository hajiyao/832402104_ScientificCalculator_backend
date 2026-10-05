# 后端代码规范（codestyle.md）

**标准来源：[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)，
并参考 Oracle《Code Conventions for the Java TM Programming Language》。**
本项目在上述标准基础上结合作业实际情况做了少量约定，具体如下。

## 1. 命名

- 类名使用大驼峰（`UpperCamelCase`），例如 `CalculatorService`、`ApiHandler`。
- 方法名、变量名使用小驼峰（`lowerCamelCase`），例如 `listHistory`、`dbPath`。
- 常量使用全大写加下划线，例如 `CREATE_TABLE_SQL`、`DRIVER`。
- 包名全部小写，不使用下划线，例如 `com.course.calculator.core`。
- 命名要能表达含义，不使用 `a1`、`tmp2` 这类无意义名字（循环变量 `i` 除外）。

## 2. 格式

- 缩进使用 4 个空格，不使用 Tab。
- 每行代码不超过 100 个字符，超长时在运算符前换行并增加缩进。
- 左大括号不单独成行，右大括号单独成行。
- 方法之间、逻辑块之间用一个空行分隔，不连续出现多个空行。
- import 按包名字母顺序排列，不使用通配符 import（`import xxx.*`）。

## 3. 注释

- 类和复杂方法使用 Javadoc 注释，说明"做什么"，而不是逐行翻译代码。
- 简单的 getter、一目了然的逻辑不加注释。
- 注释随代码一起修改，删除代码时同步删除对应注释。

## 4. 语言特性

- 统一使用 Java 8 语法，不使用更高版本的特性，保证兼容性。
- 字符串拼接大量内容时使用 `StringBuilder`。
- 数据库操作全部使用 `PreparedStatement` 预编译语句，禁止拼接 SQL。
- 禁止使用 `eval`、反射调用等方式执行用户输入。
- 捕获异常后要么处理、要么向上抛出并说明，禁止空 catch（确需忽略时写明原因）。

## 5. 类设计

- 一个文件只写一个顶层类，类的职责单一。
- 工具类构造方法声明为 private，不允许实例化。
- 不可变数据（如 `CalculationRecord`）字段声明为 final。
- 控制器只处理请求与响应，业务逻辑放在 service，数据库操作放在 db 层，
  不允许跨层写逻辑。

## 6. 其他

- 源文件统一使用 UTF-8 编码。
- 提交前保证可以通过编译，并运行过基本测试。
- 构建产物、数据库文件等不提交到仓库（见 `.gitignore`）。
